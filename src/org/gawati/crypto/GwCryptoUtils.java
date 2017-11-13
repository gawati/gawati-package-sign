package org.gawati.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Utility functions used by the library
 * @author ashok
 *
 */
public class GwCryptoUtils {
	
	private static final Logger logg = 
			Logger.getLogger(GwCryptoUtils.class.getName());

	
	public GwCryptoUtils() {
		// default constructor
	}
	

	
	/**
	 * Read Private Key from storage
	 * @param privateKeyPath
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public PrivateKey readPrivateKeyFromFile(String privateKeyPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        PrivateKey privateKey = null;
        
        byte[] privateKeyBytes = Files.readAllBytes(new File(privateKeyPath).toPath());
        PKCS8EncodedKeySpec encodedPrivateKey = new PKCS8EncodedKeySpec(privateKeyBytes);
        
        KeyFactory keyFactory = null;
        GwAlgorithmInfo algoInfo = new GwAlgorithmInfo();
        keyFactory = KeyFactory.getInstance(algoInfo.getAlgorithmName());
        privateKey = keyFactory.generatePrivate(encodedPrivateKey);
        
        return privateKey;		
	}
	
	
	/**
	 * Read Public Key from storage
	 * @param publicKeyPath
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws IOException
	 */
	public PublicKey readPublicKeyFromFile(String publicKeyPath) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        PublicKey publicKey = null;
        
        byte[] publicKeyBytes = Files.readAllBytes(new File(publicKeyPath).toPath());

        KeyFactory keyFactory = null;
        GwAlgorithmInfo algoInfo = new GwAlgorithmInfo();
        keyFactory = KeyFactory.getInstance(algoInfo.getAlgorithmName());
        X509EncodedKeySpec encodedPublicKey = new X509EncodedKeySpec(publicKeyBytes);
        publicKey = keyFactory.generatePublic(encodedPublicKey);
        return publicKey;
	 }
	
	
	/**
	 * Checks if the document is signed
	 * @param doc
	 * @return
	 */
	public boolean isDocumentSigned(Document doc) {
		// see if there are any XML signature blocks
		NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
	    if (nl.getLength() == 0) {
	    	return false;
	    } else {
	    	return true;
	    }
	}
	
	
	/**
	 * Get the XML document to be signed
	 *
	 * @param xmlFilePath , file path of the XML document
	 * @return Document
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws FileNotFoundException 
	 */
	public Document getXmlDocument(final String xmlFilePath) throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
	    Document doc = null;
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    dbf.setNamespaceAware(true);
        doc = dbf.newDocumentBuilder().parse(
        		new FileInputStream(xmlFilePath)
        	);
       
        logg.log(Level.INFO, "root element = " + doc.getDocumentElement().getNodeName());
	    return doc;
	}

}
