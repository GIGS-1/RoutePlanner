package hr.java.application.routeplanner.entity;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

public class AES {
    private static final String SECRET_KEY = "OvoJeTajniKljuc";

    private static final String SALT = "NekiRandomSalt";

    public static void encrypt(String inputPath, String outputPath)
    {
        try {
            byte[] iv = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);

            FileInputStream inputStream = new FileInputStream(inputPath);
            FileOutputStream outputStream = new FileOutputStream("dat/tmp.enc");
            byte[] buffer = new byte[64];
            int bytesRead;
            while((bytesRead = inputStream.read(buffer)) != -1){
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null){
                    outputStream.write(output);
                }
            }
            byte[] outputBytes = cipher.doFinal();
            if (outputBytes != null){
                outputStream.write(outputBytes);
            }

            FileInputStream newInputStream = new FileInputStream("dat/tmp.enc");
            FileOutputStream newOutputStream = new FileOutputStream(outputPath);

            newOutputStream.write(iv);
            newOutputStream.write(newInputStream.readAllBytes());

            inputStream.close();
            outputStream.close();
            newInputStream.close();
            newOutputStream.close();

            File file1 = new File(inputPath);
            file1.delete();
            File file2 = new File("dat/tmp.enc");
            file2.delete();
        }
        catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
    }

    public static void decrypt(String inputPath, String outputPath)
    {
        try {
            FileInputStream inputStream = new FileInputStream(inputPath);
            FileOutputStream outputStream = new FileOutputStream(outputPath);

            byte[] iv = new byte[16];
            inputStream.read(iv);
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);

            byte[] buffer = new byte[64];
            int bytesRead;
            while((bytesRead = inputStream.read(buffer)) != -1){
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null){
                    outputStream.write(output);
                }
            }
            byte[] outputBytes = cipher.doFinal();
            if (outputBytes != null){
                outputStream.write(outputBytes);
            }

            inputStream.close();
            outputStream.close();

            File file1 = new File(inputPath);
            file1.delete();
        }
        catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
    }
}