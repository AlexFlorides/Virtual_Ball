/* Alexandros Florides */

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    // All client names, so we can check for duplicates upon registration.
    private static Set<String> names = new HashSet<>();

    // The set of all the print writers for all the clients, used for broadcast.
    private static Set<PrintWriter> writers = new HashSet<>();

    private static String ball;

    private static List<String> numbers = new ArrayList<>();

    private static List<String> allplayers = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        System.out.println("Waiting for incoming connections...");
        ExecutorService pool = Executors.newFixedThreadPool(Integer.MAX_VALUE);
        try (ServerSocket listener = new ServerSocket(8888)) {
            while (true) {
                pool.execute(new Handler(listener.accept()));
            }
        }
    }

    //Client Handler class
    private static class Handler implements Runnable {
        private String name;
        private Socket socket;
        private Scanner in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);

                for (int k=0; k<1000; k++) {
                    numbers.add(String.valueOf(k));
                }

                int i=0;
                // Assign unique player id number
                while (true) {
                    if (allplayers.contains(numbers.get(i))){
                        i++;
                    }

                    name = numbers.get(i);

                    synchronized (names) {
                        if (!allplayers.contains(numbers.get(i))) {
                            names.add(numbers.get(i));
                            allplayers.add(numbers.get(i));
                            break;
                        }
                    }
                }

                // Now that a successful id has been chosen, add the socket's print writer
                // to the set of all writers so this client can receive broadcast messages.
                // But BEFORE THAT, let everyone else know that the new player has joined!
                out.println("NAMEACCEPTED " + name);

                out.println("COMMANDS " + "*** WELCOME " + name + ",  WRITE:");
                out.println("COMMANDS " + "     1) 'whoisin' TO VIEW ONLINE PLAYERS");
                out.println("COMMANDS " + "     2) 'ball' TO VIEW WHO IS CURRENTLY HOLDING THE BALL");
                out.println("COMMANDS " + "     3) 'pass (playerId)' TO PASS BALL TO PREFERRED playerId");
                out.println("COMMANDS " + "     4) 'commands' TO VIEW AVAILABLE COMMANDS");
                out.println("COMMANDS " + "     5) 'logout' TO DISCONNECT FROM THE SERVER ***");
                out.println("COMMANDS \n");

                for (PrintWriter writer : writers) {
                    writer.println("MESSAGE " + "Player " + name + " has joined");
                }
                writers.add(out);
                System.out.println("");
                System.out.println("Player " + name + " joined the game");
                System.out.println("Players online");
                for (String name : names) {
                    System.out.println("    " + "Player: "  + name);
                }

                for (PrintWriter writer : writers) {
                    writer.println("WHOISIN " + names.toString().replace("[", "").replace("]", ""));
                }

                for (PrintWriter writer : writers) {
                    if (writers.size() == 1) {
                        ball = name;
                        System.out.println("Player " + ball + " has the ball");
                    }
                    writer.println("MESSAGE " + "Player " + ball + " has the ball");
                }

                for (PrintWriter writer : writers) {
                    writer.println("null");
                }

                // Accept messages from this client and broadcast them.
                while (true) {
                    String input = in.nextLine();

                    if (input.toLowerCase().startsWith("logout")) {
                        out.println("LOGOUT");
                        out.println("null");
                        return;
                    }

                    if (input.toLowerCase().startsWith("pass")) {
                        if (names.contains(input.substring(5))){
                            if (ball.equals(name)){
                                for (PrintWriter writer : writers) {
                                    writer.println("PASS " + "Player " + name + " passed the ball to player " + input.substring(5) + "\n");
                                    writer.println("null");
                                }
                                ball = input.substring(5);
                                System.out.println("Player " + name + " passed the ball to player " + input.substring(5));
                            }
                            else {
                                out.println("MESSAGE " + "Can't pass the ball, you are not holding the ball");
                                out.println("null");
                            }
                        }
                        else {
                            out.println("MESSAGE " + "There is no player with id " + input.substring(5) + " currently online");
                            out.println("null");
                        }
                        continue;
                    }

                    if (input.toLowerCase().startsWith("ball")) {
                        out.println("MESSAGE " + "Player " + ball + " currently holds the ball");
                        out.println("null");
                        continue;
                    }

                    if (input.toLowerCase().startsWith("whoisin")) {
                        out.println("WHOISIN " + names.toString().replace("[", "").replace("]", "")+ "\n");
                        out.println("null");
                    }

                    if (input.toLowerCase().startsWith("commands")) {
                        out.println("COMMANDS " + "*** WELCOME " + name + ",  WRITE:");
                        out.println("COMMANDS " + "     1) 'whoisin' TO VIEW ONLINE PLAYERS");
                        out.println("COMMANDS " + "     2) 'ball' TO VIEW WHO IS CURRENTLY HOLDING THE BALL");
                        out.println("COMMANDS " + "     3) 'pass (playerId)' TO PASS BALL TO PREFERRED playerId");
                        out.println("COMMANDS " + "     4) 'commands' TO VIEW AVAILABLE COMMANDS");
                        out.println("COMMANDS " + "     5) 'logout' TO DISCONNECT FROM THE SERVER ***");
                        out.println("COMMANDS \n");
                        out.println("null");
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                if (out != null) {
                    writers.remove(out);
                }
                if (name != null) {
                    System.out.println("Player " + name + " is leaving");
                    names.remove(name);
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + "Player " + name + " has left");
                    }

                    for (PrintWriter writer : writers) {
                        writer.println("WHOISIN " + names.toString().replace("[", "").replace("]", "")+ "\n");
                    }

                    for (String name : names) {
                        System.out.println("    " + "Player: "  + name);
                    }

                    if (!names.isEmpty()) {
                        //pass the ball to a random player
                        Random rand = new Random();
                        String x;
                        String[] myArray = new String[names.size()];
                        names.toArray(myArray);
                        do {
                            x = (myArray[(rand.nextInt(myArray.length))]);
                        } while (x.equals(name));
                        ball = x;

                        for (PrintWriter writer : writers) {
                            writer.println("MESSAGE " + "Player " + x + " has the ball");
                        }
                        System.out.println("Player " + ball + " has the ball");
                    }

                }
                try { socket.close(); } catch (IOException e) {}
            }
        }
    }
}

/* Alexandros Florides */