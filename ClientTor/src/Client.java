import java.io.*;
import javax.net.ssl.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.security.cert.CertificateException;

public class Client
{
    private static String tlsServerAddress = "192.168.1.83"; //this is incorrect on purpose
    private static final int port = 4443;

    private static String request;
    private static DataOutputStream dataOutputStream;
    private static DataInputStream dataInputStream;

    public static void main(String[] args)
    {
        //setting system properties
        System.setProperty("javax.net.ssl.trustStore", "clientTrustore.ts");//defines the trustore file
        System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "false");
        System.setProperty("sun.security.ssl.allowLegacyHelloMessages", "true");
        System.setProperty("https.protocols", "SSLv2");
        if (true)
        {
            System.setProperty("javax.net.debug", "ssl");
        }

        try
        {
            SSLSocketFactory sslSocketFactory = getFactory(); //SSLSocketFactory creation
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(tlsServerAddress, port); //creates and initializes server socket

            new FileSharing(sslSocket).run(); //Creates a new FileSharing object with the ssl socket that was created and calls the run function
        }
        catch (IOException e1)
        {
            System.out.println("Exception: "+e1.getMessage());

            if(e1.getMessage().equals("Connection timed out: connect"))
            {
//                String coordinationServer = "192.168.1.81";
//                int portCoord = 5555;

                System.out.println("Trying to connect to coordination server..Please Wait");

                try
                {
                    InetSocketAddress hiddenProxyAddress = new InetSocketAddress("127.0.0.1", 9150);
                    Proxy hiddenProxy = new Proxy(Proxy.Type.SOCKS, hiddenProxyAddress);
                    Socket socketCoord = new Socket(hiddenProxy);
                    InetSocketAddress hiddenServiceAddress = InetSocketAddress.createUnresolved("qre4kj56wu72m76u.onion", 80); //the onion address that corresponds ONLY to this client!
                    socketCoord.connect(hiddenServiceAddress);
                    System.out.println("Successfully connected to Coordination Server's Hidden Service!");

//                    Socket socketCoord = new Socket(coordinationServer, portCoord); //this should not work now
//                    System.out.println("Connection OK");
//
                    dataOutputStream = new DataOutputStream(socketCoord.getOutputStream()); //creates data output and input streams
                    dataInputStream = new DataInputStream(socketCoord.getInputStream());

                    dataOutputStream.writeBytes("no tls server found\n");
                    dataOutputStream.flush();

                    request = dataInputStream.readLine(); //reads the new ip

                    tlsServerAddress = request;

                    System.out.println("New tls ip is: "+request);
                    System.out.println("**********************************************************\n");

                    dataOutputStream.writeBytes("received ip ok\n");
                    dataOutputStream.flush();

                    dataOutputStream.close();
                    dataInputStream.close();
                    socketCoord.close();

                    //edw xreiazetai mia nea prospatheia gia sindesi me ton tls server stin kainouria tou ip
                    SSLSocketFactory sslSocketFactory = getFactory(); //SSLSocketFactory creation
                    SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(tlsServerAddress, port); //creates and initializes server socket

                    new FileSharing(sslSocket).run(); //Creates a new FileSharing object with the ssl socket that was created and calls the run function

                } catch (UnknownHostException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private static SSLSocketFactory getFactory()
    {
        SSLContext sslContext = null;

        try {
            sslContext = SSLContext.getInstance("TLS");

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            KeyStore keyStore = KeyStore.getInstance("PKCS12");

            char [] pKeyPassword= "evg2018".toCharArray();
            InputStream keyInput = new FileInputStream("C:/Users/user/IdeaProjects/Client/evgKeystore.jks"); //keystore file
            keyStore.load(keyInput, pKeyPassword);
            keyInput.close();

            keyManagerFactory.init(keyStore, pKeyPassword);

            sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return sslContext.getSocketFactory();
    }
}