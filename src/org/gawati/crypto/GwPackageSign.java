package org.gawati.crypto;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class GwPackageSign {

	public GwPackageSign() {
		
	}
	
	public boolean packageSign(
			String sPackageToSign, 
			String sPathtoSignedPackage, 
			GwKeyPairManager kpm
			) throws Exception {
		// check if package is signed
		GwCryptoUtils utils = new GwCryptoUtils();
		Document docPackageToSign = null;
		try {
			docPackageToSign = utils.getXmlDocument(sPackageToSign);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new Exception("Unable to read package file ", e);
		}
		boolean isSigned = utils.isDocumentSigned(docPackageToSign);
		if (!isSigned) {
			throw new Exception("File is already signed");
		}
	}
	
	
}
