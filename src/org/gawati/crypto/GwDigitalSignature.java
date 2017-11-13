package org.gawati.crypto;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.logging.Logger;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GwDigitalSignature {

	private static final Logger logg = 
			Logger.getLogger(GwDigitalSignature.class.getName());
	
	/**
	 * Signs the file, and embeds the signature in the XML file itself
	 * @param sPathFileToSign
	 * @param sPathSignedFile
	 * @param kpm
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws FileNotFoundException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws MarshalException
	 * @throws XMLSignatureException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidAlgorithmParameterException
	 * @throws KeyException
	 * @throws TransformerException
	 */
	public void generateDigitalSignature(
			String sPathFileToSign, 
			String sPathSignedFile, 
			GwKeyPairManager kpm
			) throws InstantiationException, IllegalAccessException, ClassNotFoundException, FileNotFoundException, SAXException, IOException, ParserConfigurationException, MarshalException, XMLSignatureException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, KeyException, TransformerException {
		XMLSignatureFactory fac = getSignatureFactory();
		// Create a Reference to an external URI that will be digested
		// using the SHA1 digest algorithm
		SignedInfo signedInfo = getSignedInfo(fac);
		// xml signature
		XMLSignature signature = initXmlSignature(kpm, fac, signedInfo);

		GwCryptoUtils utils = new GwCryptoUtils();
		Document docToBeSigned = utils.getXmlDocument(sPathFileToSign);
		//Document docDetachedSignature = detachedSignatureDocument();
    	DOMSignContext signContext = new DOMSignContext(
    			kpm.privateKey(), 
    			docToBeSigned.getDocumentElement());
    	
    	signature.sign(signContext);

    	writeSignature(sPathSignedFile, docToBeSigned);
	}	
	
	/**
	 * Validates the digital signature in the file
	 * @param sSignedXmlFilePath
	 * @param sPublicKeyPath
	 * @return
	 * @throws Exception
	 */
	public boolean validateDigitalSignature(String sSignedXmlFilePath, String sPublicKeyPath) throws Exception  {
        boolean isValid = false;
        GwCryptoUtils utils = new GwCryptoUtils();
        Document doc;
		try {
			doc = utils.getXmlDocument(sSignedXmlFilePath);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			throw new Exception("Unable to read signed XML document", e);
		}
        NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() == 0) {
            throw new Exception("No XML Digital Signature Found, document is discarded");
        }
        PublicKey publicKey;
		try {
			publicKey = utils.readPublicKeyFromFile(sPublicKeyPath);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
			// TODO Auto-generated catch block
			throw new Exception("Unable to read public key", e);
		}
        DOMValidateContext valContext = new DOMValidateContext(publicKey, nl.item(0));
        XMLSignatureFactory fac;
		try {
			fac = getSignatureFactory();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			throw new Exception("Failed to get signature factory", e);
		}
        XMLSignature signature;
		try {
			signature = fac.unmarshalXMLSignature(valContext);
		} catch (MarshalException e) {
			// TODO Auto-generated catch block
			throw new Exception("Unable to unmarshall signature", e);
		}
        try {
			isValid = signature.validate(valContext);
		} catch (XMLSignatureException e) {
			// TODO Auto-generated catch block
			throw new Exception("Unable to validate signature", e);
		}
        return isValid;		
	}
	
	private XMLSignatureFactory getSignatureFactory() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		String providerName = System.getProperty
			    ("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
		Provider provider = (Provider) Class.forName(providerName).newInstance();
		XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM", provider);
		return fac;
	}

	private SignedInfo getSignedInfo(XMLSignatureFactory fac) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
		
		Reference ref = fac.newReference("", 	    
				fac.newDigestMethod(DigestMethod.SHA1, null),
				Collections.singletonList(
						fac.newTransform(
								Transform.ENVELOPED,
								(TransformParameterSpec) null
						)), 
				null, 
				null);
		CanonicalizationMethod cmethod = fac.newCanonicalizationMethod(
				CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS, 
				(C14NMethodParameterSpec) null
				);

		// Create the SignedInfo
		SignedInfo signedInfo = fac.newSignedInfo(
		    cmethod,
		    fac.newSignatureMethod(SignatureMethod.DSA_SHA1, null),
		    Collections.singletonList(ref)
		);
		return signedInfo;
	}
	
	private XMLSignature initXmlSignature(GwKeyPairManager kpm, XMLSignatureFactory fac, SignedInfo signedInfo) throws KeyException {
		KeyInfoFactory kif = fac.getKeyInfoFactory();
		KeyValue kvPublic = kif.newKeyValue(kpm.publicKey());
        // Create a KeyInfo and add the KeyValue to it
        KeyInfo kiPublic = kif.newKeyInfo(Collections.singletonList(kvPublic));
        // Create XMLSignature
        XMLSignature signature = fac.newXMLSignature(signedInfo, kiPublic, null, null, null);
        return signature;
	}
	

	private void writeSignature(String sPathDetachedSignature, Document docDetachedSignature) throws FileNotFoundException, TransformerException {
    	OutputStream os = new FileOutputStream(sPathDetachedSignature);
    	TransformerFactory tf = TransformerFactory.newInstance();
    	Transformer trans = tf.newTransformer();
    	trans.transform(new DOMSource(docDetachedSignature), new StreamResult(os)); 	    
	}
	
	
}
