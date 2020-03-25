/* Alexandros Florides */

import org.junit.Assert;
import org.junit.Test;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Testing {

    PrintWriter writer;
    Scanner reader;

    // Ask the server for the list of online players and print them
    // The server must run before run the test
    @Test
    public void getPlayers() throws IOException {
        Socket socket = new Socket("localhost", 8888);
        reader = new Scanner(socket.getInputStream());
        writer = new PrintWriter(socket.getOutputStream(), true);
        while (reader.hasNextLine()){
            String s = reader.nextLine();
            if (s.equals("null")) break;
        }
        writer.println("whoisin");
        String st = reader.nextLine();
        System.out.println("Players online:");
        System.out.println("    " + st.substring(8));
        Assert.assertNotNull(reader.nextLine());
    }

}

/* Alexandros Florides */
