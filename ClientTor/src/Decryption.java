import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Decryption
{
    private SecretKeySpec secretKey;
    private Cipher cipher;
    private byte [] key ;
    private FileSharing fileSharing;
    private SecretKeySpec Mackey;
    private Mac mac;

    public Decryption() { }

    public Decryption(String secret, int length, String algorithm)throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException
    {
        fileSharing = new FileSharing();;
        key = fixSecret(secret , length);
        this.secretKey = new SecretKeySpec(key,algorithm);
        this.cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING"); //AES cipher is used to decrypt the content of the file in the CBC mode using the PKCS5 padding scheme
        mac = Mac.getInstance("HmacSHA256");//create a mac instance
        Mackey = new SecretKeySpec(key, algorithm); //create and initialize the mac key
        mac.init(Mackey);
    }

    private byte[] fixSecret (String s, int length) throws UnsupportedEncodingException
    {
        if(s.length()< length)
        {
            int mis= length-s.length();
            for (int i=0; i<mis; i++)
            {
                s+=" ";
            }
        }
        return s.substring(0,length).getBytes("UTF-8");
    }

    public void decryptFile(File f) throws InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException
    {
        String ivString = fileSharing.getIv();
        IvParameterSpec iv = new IvParameterSpec(ivString.getBytes());

        //we need to read the mac the server has sent
        File macF = new File("mac.txt");
        FileInputStream in= new FileInputStream(macF);
        byte macFromServer[]=new byte[(int) macF.length()];
        in.read(macFromServer);

        //we need to mac the ciphertext the server has sent
        String ciphertext = readFromFile(f);
        byte[] macBytes = mac.doFinal(ciphertext.getBytes());

        //only if the two MACs match we should decrypt the file
        if(Arrays.equals(macFromServer, macBytes))
        {
            try
            {
                System.out.println("Decrypting file...: " + f.getName());
                this.cipher.init(Cipher.DECRYPT_MODE, this.secretKey,iv);
            }
            catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }

            this.writeToFile(f);
        }
        else
        {
            System.out.println("Generated and received macs do not match. Don't decrypt the file!!!");
        }

        in.close();
    }

    public void writeToFile(File f) throws IOException, IllegalBlockSizeException, BadPaddingException
    {
        FileInputStream in= new FileInputStream(f);
        byte [] input = new byte[(int) f.length()];
        in.read(input);

        FileOutputStream out= new FileOutputStream(f);
        byte [] output= this.cipher.doFinal(input);
        out.write(output);

        out.flush();
        out.close();
        in.close();
    }

    public String readFromFile(File f) //function that reads a string from a file and returns it
    {
        String inString = null;
        try
        {
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String txt;
            while ((txt = br.readLine()) != null)
            {
                inString = txt;
            }

            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  inString;
    }
}