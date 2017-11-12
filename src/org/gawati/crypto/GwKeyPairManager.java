package org.gawati.crypto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides access to a DSA Key Pair used for signing. 
 * 
 * Keys can be generated using generateKeyPair()
 * 
 * or 
 * 
 * Keys can be loaded using loadKeyPair()
 * 
 * @author ASHOK HARIHARAN
 *
 */
public class GwKeyPairManager {
	
	private static final Logger logg = 
			Logger.getLogger(GwKeyPairManager.class.getName());

	
	KeyPair theKeyPair;
	KeyPairGenerator theKeyPairGenerator;
	
	PrivateKey thePrivateKey;
	PublicKey thePublicKey;
	
	/**
	 * The class is setup using the default algorithms
	 * @throws NoSuchAlgorithmException
	 */
	public GwKeyPairManager() throws NoSuchAlgorithmException {
		GwAlgorithmInfo algoInfo = new GwAlgorithmInfo();
		theKeyPairGenerator = KeyPairGenerator.getInstance(algoInfo.getAlgorithmName());
		theKeyPairGenerator.initialize(algoInfo.getLength());
	}
	
	public PublicKey publicKey() {
		return thePublicKey;
	}
	
	public PrivateKey privateKey() {
		return thePrivateKey;
	}

	/**
	 * Generates a key pair and makes them available via member variables
	 */
	public void generateKeyPair() {
		theKeyPair = theKeyPairGenerator.generateKeyPair();
		thePrivateKey = theKeyPair.getPrivate();
		thePublicKey = theKeyPair.getPublic();
	}
	
	public KeyPair getKeyPair() {
		return theKeyPair;
	}
	
	public void loadKeyPair(String sPrivateKeyPath, String sPublicKeyPath) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		GwCryptoUtils utils = new GwCryptoUtils();
		thePrivateKey = utils.readPrivateKeyFromFile(sPrivateKeyPath);
		thePublicKey = utils.readPublicKeyFromFile(sPublicKeyPath);
	}
	
	/**
	 * Save the Key Pair into the Keys folder
	 * @throws IOException
	 */
	public void serializeKeyPair() throws IOException {
		if (theKeyPair != null) {
			GwCryptoUtils utils = new GwCryptoUtils();
			String sKeyFolder = utils.getKeyStorePath();
			File keyFolder = new File(sKeyFolder);
			if (!(keyFolder).exists()) {
				keyFolder.mkdirs();
			}
			serializeKey(sKeyFolder + File.separator + "id.public", theKeyPair.getPublic());
			serializeKey(sKeyFolder + File.separator + "id.private", theKeyPair.getPrivate());
		} else {
			logg.log(Level.INFO, "KeyPair is null");
		}
	}
	
	/**
	 * Serializes the key to the file system
	 * @param filePath
	 * @param theKey
	 * @throws IOException
	 */
    private void serializeKey(String filePath, Key theKey) throws IOException {
        byte[] keyEncoded = theKey.getEncoded();
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(filePath);
            outStream.write(keyEncoded);
        } catch (IOException e) {
        	throw e; 
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                	throw e;
                }
            }
        }
    }
	
}
