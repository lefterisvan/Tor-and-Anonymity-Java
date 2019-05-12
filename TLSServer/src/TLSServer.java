import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.*;
import javax.net.ServerSocketFactory;
import javax.net.ssl.*;


public class TLSServer
{
    private static final int PORT = 4443;

    public static void main(String[] args)
    {
        Socket socket;
        //setting system properties
        System.setProperty("javax.net.ssl.trustStore", "serverTrustore.ts");  //defines the trustore file
        System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "false");
        System.setProperty("sun.security.ssl.allowLegacyHelloMessages", "true");
        System.setProperty("https.protocols", "SSLv2");
        if (true)
        {
            System.setProperty("javax.net.debug", "ssl");
        }

        try
        {
            ServerSocketFactory serverSocketFactory = TLSServer.getServerSocketFactory("TLS"); //asks for a TLS serverSocketFactory to be generated
            ServerSocket sslServerSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(PORT);

            InetAddress inetAddress = null;

            while (true)
            {
                socket = sslServerSocket.accept();
                SocketAddress socketAddress = socket.getRemoteSocketAddress();

                if (socketAddress instanceof InetSocketAddress)
                {
                    inetAddress = ((InetSocketAddress)socketAddress).getAddress(); //gets the ip address of the connected client
                    System.out.println("Connected to client: "+inetAddress);
                }
                else
                {
                    System.err.println("Not an internet protocol socket.");
                }

                new ThreadClass(socket,inetAddress.toString()).run(); //creates a ThreadClass object and calls the run function
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    private static ServerSocketFactory getServerSocketFactory(String type) //generates a ServerSocketFactory
    {
        if (type.equals("TLS"))
        {
            SSLServerSocketFactory sslServerSocketFactory;

            try {
                // set up key manager to do server authentication
                SSLContext sslContext = SSLContext.getInstance("TLS");
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
                KeyStore keyStore = KeyStore.getInstance("JKS"); //keystore type
                char[] passphrase = "server2018".toCharArray(); //keystore password
                keyStore.load(new FileInputStream("D:/MSC/1ο Εξάμηνο/Ασφάλεια Δικτύων/Εργασία Β1/Φάση 3/TLSServer/ServerKeystore.jks"), passphrase); //keystpre file
                keyManagerFactory.init(keyStore, passphrase); //initializes the keyManagerFactory
                sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom()); //initializes the sslContext

                sslServerSocketFactory = sslContext.getServerSocketFactory();
                return sslServerSocketFactory;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else
        {
            return ServerSocketFactory.getDefault();
        }
        return null;
    }
}

