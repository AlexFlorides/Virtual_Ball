# Virtual_Ball
Socket-based client-server system to play a virtual ball. Each client application is a player. Once a player re-ceives a ball, it prompts the user whom to pass the ball to. The clients can connect and disconnect from the server at any time.
Implemented in two versions of each of the server and the client applications: in C# and in Java. All these applications have to work with each other seamlessly.

<ins>The rules of the game:</ins>

At any point in time, exactly one player has the virtual ball. This player needs to decide who to pass the ball to. (They are allowed to pass the ball to themselves.) Once the decision is made, they pass the ball to the corresponding player.
New players can join the game at any time. The number of players in the game is unlimited. Every player joining the game has to be as-signed a unique ID that will not change until they leave the game and will not be reused after they leave the game. All the players including the one with the ball will immediately learn about new players and, hence, the current ball owner can decide to pass the ball to the player who has just joint the game.
Any player can leave the game at any time (the client application can be closed/killed). If the player with the ball leaves the game, the server passes the ball to one of the remaining players.
If there are no players in the game, the server waits until someone joins the game in which case the first player to connect receives the ball.

<ins>Implemented Functionality Table:</ins>

|                               Function                              |                                                    C#                                                    | Java |
|:-------------------------------------------------------------------:|:--------------------------------------------------------------------------------------------------------:|:----:|
|          Client establishes a   connection with the server          |                                                    YES                                                   |  YES |
|        Client is assigned a   unique ID when joining the game       |                                                    YES                                                   |  YES |
|    Client displays up-to-date   information about the game state    | YES, but console always wait for a user input, so after the input entered you can see the queued updates |  YES |
|          Client allows passing the   ball to another player         |                                                    YES                                                   |  YES |
|             Server manages multiple   client connections            |                                                    YES                                                   |  YES |
|             Server accepts connections   during the game            |                                                    YES                                                   |  YES |
|         Server correctly handles clients   leaving the game         |                                                    YES                                                   |  YES |
|     Client is compatible with   the server in the other language    |                                                    YES                                                   |  YES |
|                              Client GUI                             |                                                    NO                                                    |  YES |

<ins>Client Threads:</ins>

In Java language, client creates a new Socket object which is used for the communication between the client and the server. The equivalent for C# is TcpClient. They both have as their parameters the IP address and a port number. In Java, the socket is used to create the InputStream and OutputStream, where the client sends and receives data to and from the server. In C#, using the socket of the client, we create a NetworkStream. This stream is used to create the StreamReader and StreamWriter, which are equivalent to the InputStream and OutputStream of Java. The creation and assign of the socket happen at the start of the run() method, on both languages, and terminates when the user sends the “logout” command, meaning the termination of the client process.

<ins>Server Threads:</ins>

Each client connecting to the server is assigned a new thread, so that the server can interact with each client individually. In both languages, threads are created in the main method, before anything else. Then, each thread is being passed to the Handler class of the server, where the threads are being used to create the reader and writer of each client. The thread of each client is terminated at the end of the program, when the client disconnects from the server. So, that thread will not being used again by the server.
In Java language, the server is creating a server socket that binds it to a specific port number. That server socket listens for any connections and in an infinity loop, it passes each one to the Handler class. The Handler class implements the Runnable interface, which is an interface that is to be implemented by a class whose instances are intended to be executed by a thread. Runnable interface defines a single method run(), which is meant to contain the code that is executed by the thread. 
In C# language, when the server starts, it uses TcpListener to listen for any connections from TCP network clients. So, in an infinite loop it waits for connections to happen and accepts them by using AcceptTcpClient() method. Each connection is creating a System.Threading Thread object that calls the Handler class and pass to it an instance of a TcpClient.

<ins>Protocol:</ins>

Each client initiates a connection, using a socket with parameters the IP address and the port number, with the server and receives some data, such as what commands they can type in, the online players and who has the ball at that time. Clients can receive and send data to the server using the socket streams. Specifically, InputStream for Java and StreamReader for C# to receive data and OutputStream for Java and StreamWriter for C# to send data. When the input stream stops receiving data from the server, user can enter certain commands, which are send to the server using the output stream and receive back the appropriate results. 
*	“whoisin” command: client is expected to receive in a text all the ids of the players who are connected on the server at that moment.
*	“ball” command: client is expected to receive in a text the id of the player who is holding the ball at that moment.
*	“pass (playerId)” command: client send a request to the serve, where they try to pass the ball to the chosen player. If the client is not holding the ball when they make that request, the server responds with the appropriate message. The same happens when client tries to send the ball to a player who is not online. When the pass of the ball is successful, all clients gets notified about the pass, so they know who is holding the ball at any time.
*	“commands” command: client is expected to receive a text of all the available commands.
*	“logout” command: client send a request to the server to close client’s socket and stop sending and receiving any more data.
When a client disconnects from the server, the server notifies the remaining connected players. In case that client was holding the ball the time it disconnected, a new player is chosen randomly and receives the ball, notifying again the remaining connected players.
