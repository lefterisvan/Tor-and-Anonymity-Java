import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class ThreadClass extends Thread
{
    private Socket socket;
    private FileOutputStream fileOutputStream;
    private BufferedOutputStream bufferedOutputStream;
    private BufferedInputStream bufferedInputStream;
    private InputStream inputStream;
    private OutputStream outputStream;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private String str;
    private String request;
    private String clientAddress;
    private String directoryName;
    private String ivString;
    private Encryption encr;

    public ThreadClass(ServerSocket socket){ }

    public ThreadClass(Socket socket, String clientAddress)
    {
        this.socket = socket;
        this.clientAddress = clientAddress;
    }

    public void run()
    {
        // directoryName = "C:/Documents/"+clientAddress;
        directoryName = "D:/MSC/1ο Εξάμηνο/Ασφάλεια/Εργασία Β1/"+clientAddress;
        new File(directoryName).mkdirs(); //a directory with a unique name based on the clients IP will be created

        try
        {
            dataOutputStream = new DataOutputStream(socket.getOutputStream()); //creates data output and input streams
            dataInputStream = new DataInputStream(socket.getInputStream());

            encr = new Encryption(clientAddress,16,"AES"); //creates an Encryption object and defines the parameter that will be used for the file encryption

            request = dataInputStream.readLine(); //reads what the client sent

            if(request.equals("file")) //if the client sent file the server replies that everything is ok
            {
                dataOutputStream.writeBytes("Everything is ok! We communicate over TLS\n");
                dataOutputStream.flush();

                do
                {
                    str = dataInputStream.readLine(); //reads what the client sent

                    if(str.equals("send")) //the client wants to send a file to the server
                    {
                        //server receives a file from the client and saves it
                        byte[] mybytearray = new byte[1024];
                        inputStream = socket.getInputStream();
                        fileOutputStream = new FileOutputStream(directoryName+"/clients_file.txt");
                        bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                        int bytesRead = inputStream.read(mybytearray, 0 , mybytearray.length);
                        bufferedOutputStream.write(mybytearray,0,bytesRead);
                        System.out.println("File received");
                        bufferedOutputStream.flush();
                        fileOutputStream.flush();

                        //encrypts the file and then saves it in a unique per user directory
                        encr.encryptFile(new File(directoryName+"/clients_file.txt"));

                        //writes the iv in a file and then saves it in a unique per user directory
                        ivString = encr.getInitvector();
                        try (PrintWriter printWriter = new PrintWriter(directoryName+"/iv.txt"))
                        {
                            printWriter.println(ivString);
                        }
                    }
                    else if(str.equals("receive")) //the client wants to receive a file from the server
                    {
                        //the server first sends the iv file to the client (so he will use it for the decryption)
                        File myFileIV = new File(directoryName+"/iv.txt");
                        byte[] mybytearrayIV = new byte[(int) myFileIV.length()];
                        bufferedInputStream = new BufferedInputStream(new FileInputStream(myFileIV));
                        bufferedInputStream.read(mybytearrayIV, 0, mybytearrayIV.length);
                        outputStream = socket.getOutputStream();
                        outputStream.write(mybytearrayIV, 0, mybytearrayIV.length);
                        System.out.println("Iv sent to client");
                        outputStream.flush();

                        str = dataInputStream.readLine();

                        if(str.equals("received iv ok")) //only if the client has received the iv
                        {
                            //the server sends the iv to the client
                            File myFileMac = new File(directoryName+"/mac.txt");
                            byte[] mybytearrayMac= new byte[(int) myFileMac.length()];
                            bufferedInputStream = new BufferedInputStream(new FileInputStream(myFileMac));
                            bufferedInputStream.read(mybytearrayMac, 0, mybytearrayMac.length);
                            outputStream = socket.getOutputStream();
                            outputStream.write(mybytearrayMac, 0, mybytearrayMac.length);
                            System.out.println("Mac sent to client");
                            outputStream.flush();
                        }
                        else
                        {
                            System.out.println("IV not received from client!!");
                            socket.close();
                        }

                        str = dataInputStream.readLine();

                        if(str.equals("received mac ok"))
                        {
                            //the server sends the encrypted file back to the client
                            File myFile = new File(directoryName+"/clients_file.txt");
                            byte[] mybytearray = new byte[(int) myFile.length()];
                            bufferedInputStream = new BufferedInputStream(new FileInputStream(myFile));
                            bufferedInputStream.read(mybytearray, 0, mybytearray.length);
                            outputStream = socket.getOutputStream();
                            outputStream.write(mybytearray, 0, mybytearray.length);
                            System.out.println("File sent to client");
                            outputStream.flush();
                        }
                        else
                        {
                            System.out.println("Mac not received from client!!");
                            socket.close();
                        }
                    }
                }while(!str.equals("end")); //if the client sends end the communication will terminate

                fileOutputStream.close();
                bufferedOutputStream.close();
                bufferedInputStream.close();
                outputStream.close();
                inputStream.close();
                dataInputStream.close();
                dataOutputStream.close();
                socket.close();
            }
        }
        catch(IOException ex) {
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }
}