import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class IPSender extends Thread //multithreaded
{
    private Socket socket;
    private String tlsServerAddress = "192.168.1.81"; //the new file upload server's ip address
    private String request;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    public IPSender(){} //default constructor

    public IPSender(Socket socket)
    {
        this.socket = socket;
    }

    public void run()
    {
        try
        {
            dataOutputStream = new DataOutputStream(socket.getOutputStream()); //creates data output and input streams
            dataInputStream = new DataInputStream(socket.getInputStream());

            request = dataInputStream.readLine(); //reads what the client says

            if(request.equals("no tls server found")) //if the client sents "no tls server found"
            {
                System.out.println("Sending new IP address now"); //the server sends him the new ip address
                dataOutputStream.writeBytes(tlsServerAddress+"\n");
                dataOutputStream.flush();

                System.out.println("Sent IP to client OK");

                request = dataInputStream.readLine(); //reads what the client says

                if(request.equals("received ip ok")) //if the client received the ip
                {
                    System.out.println("Client received the ip. Terminating connection........"); //the connection is terminated
                    dataInputStream.close();
                    dataOutputStream.close();
                    socket.close();
                }
                else
                {
                    System.out.println("Error!! Client didn't receive the ip address"); //if an error occured 
                    dataInputStream.close(); //the connection is terminated
                    dataOutputStream.close();
                    socket.close();
                }
            }
            else
            {
                System.out.println("Unknown request"); //if there is an unknown request 
                dataInputStream.close(); //the connection is terminated
                dataOutputStream.close();
                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}