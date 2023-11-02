package com.azureAccelerator.util;

import javax.crypto.Cipher;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public class RSA_Read_Write_Key
{

    public static void genrateKey() throws Exception
    {
        // Get an instance of the RSA key generator
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(4096);

        // Generate the KeyPair
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // Get the public and private key
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        System.out.println("publicKey----->"+publicKey);
        System.out.println("privateKey----->"+privateKey);
        // Get the RSAPublicKeySpec and RSAPrivateKeySpec
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPublicKeySpec publicKeySpec = keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);
        RSAPrivateKeySpec privateKeySpec = keyFactory.getKeySpec(privateKey, RSAPrivateKeySpec.class);
        
        // Saving the Key to the file
        saveKeyToFile("public.key", publicKeySpec.getModulus(), publicKeySpec.getPublicExponent());
        saveKeyToFile("private.key", privateKeySpec.getModulus(), privateKeySpec.getPrivateExponent());

    }
    public static String getEncrypt(String plainText) throws Exception {
        // Encryption
        byte[] cipherTextArray = encrypt(plainText, "E:\\public.key");
        String encryptedText = Base64.getEncoder().encodeToString(cipherTextArray);
        return  encryptedText;
    }
    public static String getDecrypt(String encryptedText ) throws Exception {
        // Encryption
        byte[] cipherTextArray = Base64.getDecoder().decode(encryptedText.getBytes());
        String decryptedText = decrypt(cipherTextArray, "E:\\private.key");
        return  decryptedText;
    }
    public static void saveKeyToFile(String fileName, BigInteger modulus, BigInteger exponent) throws IOException
    {
        ObjectOutputStream ObjOutputStream = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(fileName)));
        try
        {
            ObjOutputStream.writeObject(modulus);
            ObjOutputStream.writeObject(exponent);
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            ObjOutputStream.close();
        }
    }

    public static Key readKeyFromFile(String keyFileName) throws IOException
    {
        Key key = null;
        InputStream inputStream = new FileInputStream(keyFileName);
        ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(inputStream));
        try
        {
            BigInteger modulus = (BigInteger) objectInputStream.readObject();
            BigInteger exponent = (BigInteger) objectInputStream.readObject();
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            if (keyFileName.startsWith("public"))
                key = keyFactory.generatePublic(new RSAPublicKeySpec(modulus, exponent));
            else
                key = keyFactory.generatePrivate(new RSAPrivateKeySpec(modulus, exponent));

        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            objectInputStream.close();
        }
        return key;
    }

    public static byte[] encrypt(String plainText, String fileName) throws Exception
    {
        Key publicKey = readKeyFromFile("public.key");

        // Get Cipher Instance
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING");

        // Initialize Cipher for ENCRYPT_MODE
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        // Perform Encryption
        byte[] cipherText = cipher.doFinal(plainText.getBytes());

        return cipherText;
    }

    public static String decrypt(byte[] cipherTextArray, String fileName) throws Exception
    {
        Key privateKey = readKeyFromFile("private.key");

        // Get Cipher Instance
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING");

        // Initialize Cipher for DECRYPT_MODE
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        // Perform Decryption
        byte[] decryptedTextArray = cipher.doFinal(cipherTextArray);

        return new String(decryptedTextArray);
    }
}