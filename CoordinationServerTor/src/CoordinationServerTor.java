import java.net.*;

public class CoordinationServerTor
{
    public static void main(String[] args)
    {
        ServerSocket serverSocket = null;
        SocketAddress socketAddress;
        Socket socket = null;

        try
        {
            socketAddress = new InetSocketAddress("127.0.0.1", 80); //initializing the SocketAddress with localhost as hostname and port 80 (the same info with our Savant web server)
            serverSocket = new ServerSocket(); //creates a ServerSocket
            serverSocket.bind(socketAddress); // binds the address so that the server must now accept TCP connections only from localhost/127.0.0.1 and port 80
            System.out.println("The server is up and only accepts localhost connections.");
            System.out.println("Waiting for clients to connect to our hidden service :) "); //waiting for TOR clients
            System.out.println("************************************\n");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        while(true)
        {
            try
            {
                socket  = serverSocket.accept(); //the server now accepts an incoming connection from a client (through the hidden service)
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            new IPSender(socket).run(); //creates a new IPSender object and calls the run method
        }
    }

}