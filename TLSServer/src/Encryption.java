import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption
{
    private SecretKeySpec secretKey;
    private Cipher cipher;
    private IvParameterSpec ivParams;
    private String randomString;
    private String directoryName;
    private byte [] key;
    private Mac mac;
    private SecretKeySpec Mackey;

    public Encryption(){ } //default constructor

    public Encryption(String secret, int length, String algorithm) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        // directoryName = "C:/Documents/"+secret;
        directoryName = "D:/MSC/1ο Εξάμηνο/Ασφάλεια/Εργασία Β1/"+secret;
        randomString = randomStringGenerator();
        key = fixSecret(secret , length);
        this.secretKey = new SecretKeySpec(key,algorithm);
        this.cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING"); //AES cipher is used to encrypt the content of the file in the CBC mode using the PKCS5 padding scheme
        ivParams = new IvParameterSpec(randomString.getBytes());
        mac = Mac.getInstance("HmacSHA256"); //create a mac instance
        Mackey = new SecretKeySpec(key, algorithm); //create and initialize the mac key
        mac.init(Mackey);
    }

    public String randomStringGenerator() //generates the random IV string
    {
        int length = 16;
        char[] characters = "~=+%^*/()[]{}/!@#$?|0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
        char[] c = new char[length];
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < length; i++)
        {
            c[i] = characters[random.nextInt(characters.length)];
        }
        return new String(c);
    }

    private byte[] fixSecret (String s, int length) throws UnsupportedEncodingException //generates the secret AES key
    {
        if(s.length()< length)
        {
            int mis = length-s.length();
            for (int i=0; i<mis; i++)
            {
                s+=" ";
            }
        }
        return s.substring(0,length).getBytes("UTF-8");
    }

    public void encryptFile(File f) throws InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException //encryps the file with the AES key and the IV
    {
        try
        {
            System.out.println("The encrypted file is: " + f.getName());
            this.cipher.init(Cipher.ENCRYPT_MODE, this.secretKey,ivParams);
        }
        catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        this.writeToFile(f);
        macFile(f);
    }


    public void writeToFile(File f) throws IOException, IllegalBlockSizeException, BadPaddingException //writes the ciphertext to a file
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

    public void macFile(File f) //hashes the ciphertext to create the mac and saves it
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

            byte[] ciphertext = inString.getBytes();
            byte[] macBytes = mac.doFinal(ciphertext);

            FileOutputStream out = new FileOutputStream(directoryName+"/mac.txt");
            out.write(macBytes);
            out.flush();
            out.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getInitvector()
    {
        return randomString;
    }

}
