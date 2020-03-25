/* Alexandros Florides */

using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Net;

namespace Server
{

    public class ChatServer
    {

        // All client names, so we can check for duplicates upon registration.
        private static HashSet<string> names = new HashSet<string>();

        // The set of all the print writers for all the clients, used for broadcast.
        private static HashSet<StreamWriter> writers = new HashSet<StreamWriter>();

        private static String ball = null;

        private static List<String> numbers = new List<string>();

        private static List<String> allplayers = new List<string>();

        public static void Main(string[] args)
        {
            Console.WriteLine("Waiting for incoming connections...");

            TcpListener listener = new TcpListener(IPAddress.Any, 8888);
            TcpClient client;
            listener.Start();

            while (true)
            {
                client = listener.AcceptTcpClient();
                new Thread(new Handler(client).Run).Start();
            }


        }


        /**
         * The client handler task.
         */
        private class Handler
        {
            private String name;

            private StreamReader reader;
            private StreamWriter writer;

            private TcpClient socket; 


        public Handler(TcpClient socket)
        {
            this.socket = socket;
        }

        public  void Run()
        {
            try
            {
                var client = socket;
                NetworkStream stream = client.GetStream();
                reader = new StreamReader(stream);
                writer = new StreamWriter(stream);

                writer.AutoFlush = true;

                for (int k = 0; k < 1000; k++)
                {
                    numbers.Add(k.ToString());
                }

                int i = 0;
                // Assign unique player id number
                while (true)
                {
                    if (allplayers.Contains(numbers[i]))
                    {
                        i++;
                    }

                    name = numbers[i];

                    lock (names)
                    {
                        if (!allplayers.Contains(numbers[i]))
                        {
                            names.Add(numbers[i]);
                            allplayers.Add(numbers[i]);
                            break;
                        }
                    }
                }

                // Now that a successful id has been chosen, add the socket's print writer
                // to the set of all writers so this client can receive broadcast messages.
                // But BEFORE THAT, let everyone else know that the new player has joined!
                writer.WriteLine("NAMEACCEPTED " + name);           

                writer.WriteLine("COMMANDS " + "*** WELCOME " + name + ",  WRITE:");
                writer.WriteLine("COMMANDS " + "     1) 'whoisin' TO VIEW ONLINE PLAYERS");
                writer.WriteLine("COMMANDS " + "     2) 'ball' TO VIEW WHO IS CURRENTLY HOLDING THE BALL");
                writer.WriteLine("COMMANDS " + "     3) 'pass (playerId)' TO PASS BALL TO PREFERRED playerId");
                writer.WriteLine("COMMANDS " + "     4) 'commands' TO VIEW AVAILABLE COMMANDS");
                writer.WriteLine("COMMANDS " + "     5) 'logout' TO DISCONNECT FROM THE SERVER ***");
                writer.WriteLine("COMMANDS \n");





                foreach (StreamWriter writer in writers)
                {
                    writer.WriteLine("MESSAGE " + "Player " + name + " has joined");
                }
                writers.Add(writer);
                Console.WriteLine("");
                Console.WriteLine("Player " + name + " joined the game");
                Console.WriteLine("Players online");
                foreach (String name in names)
                {
                    Console.WriteLine("    " + "Player: " + name);
                }

                foreach (StreamWriter writer in writers)
                {
                    writer.WriteLine("WHOISIN " + string.Join(", ", names));
                }

                foreach (StreamWriter writer in writers)
                {
                    if (writers.Count() == 1)
                    {
                        ball = name;
                        Console.WriteLine("Player " + ball + " has the ball");
                    }
                    writer.WriteLine("MESSAGE " + "Player " + ball + " has the ball");
                }

                foreach (StreamWriter writer in writers)
                {
                    writer.WriteLine("null");
                }

                // Accept messages from this client and broadcast them.
                while (true)
                {
                    String input = reader.ReadLine();

                    if (input.ToLower().StartsWith("logout"))
                    {
                        writer.WriteLine("LOGOUT");
                        writer.WriteLine("null");
                        return;
                    }

                    if (input.ToLower().StartsWith("pass"))
                    {
                        if (names.Contains(input.Substring(5)))
                        {
                            if (ball.Equals(name))
                            {
                                foreach (StreamWriter writer in writers)
                                {
                                    writer.WriteLine("PASS " + "Player " + name + " passed the ball to player " + input.Substring(5) + "\n");
                                    writer.WriteLine("null");
                                }
                                ball = input.Substring(5);
                                Console.WriteLine("Player " + name + " passed the ball to player " + input.Substring(5));
                            }
                            else
                            {
                                writer.WriteLine("MESSAGE " + "Can't pass the ball, you are not holding the ball");
                                writer.WriteLine("null");
                            }
                        }
                        else
                        {
                            writer.WriteLine("MESSAGE " + "There is no player with id " + input.Substring(5) + " currently online");
                            writer.WriteLine("null");
                        }
                        continue;
                    }

                    if (input.ToLower().StartsWith("ball"))
                    {
                        writer.WriteLine("MESSAGE " + "Player " + ball + " currently holds the ball");
                        writer.WriteLine("null");
                        continue;
                    }

                    if (input.ToLower().StartsWith("whoisin"))
                    {
                        writer.WriteLine("WHOISIN " + string.Join(", ", names));
                        writer.WriteLine("null");
                    }

                    if (input.ToLower().StartsWith("commands"))
                    {
                        writer.WriteLine("COMMANDS " + "*** WELCOME " + name + ",  WRITE:");
                        writer.WriteLine("COMMANDS " + "     1) 'whoisin' TO VIEW ONLINE PLAYERS");
                        writer.WriteLine("COMMANDS " + "     2) 'ball' TO VIEW WHO IS CURRENTLY HOLDING THE BALL");
                        writer.WriteLine("COMMANDS " + "     3) 'pass (playerId)' TO PASS BALL TO PREFERRED playerId");
                        writer.WriteLine("COMMANDS " + "     4) 'commands' TO VIEW AVAILABLE COMMANDS");
                        writer.WriteLine("COMMANDS " + "     5) 'logout' TO DISCONNECT FROM THE SERVER ***");
                        writer.WriteLine("COMMANDS \n");
                        writer.WriteLine("null");
                    }

                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
            }
            finally
            {
                if (writer != null)
                {
                    writers.Remove(writer);
                }
                if (name != null)
                {
                    Console.WriteLine("Player " + name + " is leaving");
                    names.Remove(name);
                    foreach (StreamWriter writer in writers)
                    {
                        writer.WriteLine("MESSAGE " + "Player " + name + " has left");
                    }

                    foreach (StreamWriter writer in writers)
                    {
                        writer.WriteLine("WHOISIN " + string.Join(", ", names));
                    }

                    foreach (String name in names)
                    {
                        Console.WriteLine("    " + "Player: " + name);
                    }

                    if (names.Count > 0)
                    {
                        //pass the ball to a random player
                        Random rand = new Random();
                        String x;
                        String[] myArray = new String[names.Count()];
                        names.CopyTo(myArray);
                        do
                        {
                            x = (myArray[(rand.Next(myArray.Count()))]);
                        } while (x.Equals(name));
                        ball = x;

                        Console.WriteLine(names.Count);
                        Console.WriteLine(myArray.Count());

                        foreach (StreamWriter writer in writers)
                        {
                            writer.WriteLine("MESSAGE " + "Player " + x + " has the ball");
                        }
                        Console.WriteLine("Player " + ball + " has the ball");
                    }

                }
                try { socket.Dispose(); } catch (IOException e) { }
            }
        }
        }
    }
}

/* Alexandros Florides */
