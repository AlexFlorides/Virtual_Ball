/* Alexandros Florides */

using Assignment;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Sockets;

namespace Assignment
{

    public class ChatClient
    {

        string serverAddress;

        private StreamReader reader;
        private StreamWriter writer;

        public ChatClient(String serverAddress)
        {
            this.serverAddress = serverAddress;
        }

        private void Run()
        {
            try
            {
                //Create new socket to communicate with the server
                TcpClient tcpClient = new TcpClient(serverAddress, 8888);
                NetworkStream stream = tcpClient.GetStream();
                reader = new StreamReader(stream);
                writer = new StreamWriter(stream);

                //Infinite loop to get user's input and receive data
                while (true)
                {

                    String line = reader.ReadLine();
                    if (line.StartsWith("NAMEACCEPTED"))
                    {
                        Console.Title = "Player - " + line.Substring(13);
                    }
                    else if (line.StartsWith("MESSAGE"))
                    {
                        Console.WriteLine(line.Substring(8));
                    }
                    else if (line.StartsWith("PASS"))
                    {
                        Console.WriteLine((line.Substring(5)));
                    }
                    else if (line.StartsWith("WHOISIN"))
                    {
                        Console.WriteLine("Players online:");
                        Console.WriteLine("    " + line.Substring(8));
                    }
                    else if (line.StartsWith("COMMANDS"))
                    {
                        Console.WriteLine(line.Substring(9));
                    }
                    else if (line.StartsWith("LOGOUT"))
                    {
                        reader.Close();
                        writer.Close();
                        break;
                    }
                    else if (line.Equals("null"))
                    {
                        Console.WriteLine("Write command: ");
                        String command = Console.ReadLine();
                        while (command.Count() == 0)
                        {
                            Console.WriteLine("Write command: ");
                            command = Console.ReadLine();
                        }

                        writer.WriteLine(command);
                        writer.Flush();
                    }
                }
            }

            finally
            {

            }
        }

        public static void Main(string[] args)
        {
            if (args.Length != 1)
            {
                ChatClient client = new ChatClient("localhost");
                client.Run();
            }
        }
    }
}

/* Alexandros Florides */
