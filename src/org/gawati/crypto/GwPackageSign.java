package org.gawati.crypto;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.ParserConfigurationException;

import org.gawati.crypto.GwPackageFile.GwAknMetadata;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
/**
 * 
 * <gw:digitalPkg xmlns:gw="http://gawati.org/ns/1.0">
 *   	<gw:aknMetadata uri="/akn/mu/act/2004-04-30/bill_no_11-2004/eng@/!main" href="./akn_mu_act_2004-04-30_bill_no_11-2004_eng_main.xml" />
 *    		<gw:digitalArtifacts>
 *        		<gw:digitalArtifact format="pdf" href="./akn_mu_act_2004-04-30_bill_no_11-2004_eng_main.pdf" />
 *    		</gw:digitalArtifacts>
 *	</gw:digitalPkg>
 *
 * 
 * @author ashok
 *
 */
public class GwPackageSign {
	
	GwCryptoUtils theUtils = new GwCryptoUtils();

	private boolean signAknMetadata(GwAknMetadata gwMeta) {
		return false;
	}
	
	private boolean signDigitalArtifacts(GwAknMetadata gwMeta) {
		return false;
	}
	
	public boolean packageSign(
			String sPackageToSign, 
			String sPathtoSignedPackage, 
			GwKeyPairManager kpm
			) throws Exception {
		Document docPackageToSign = null;
		try {
			docPackageToSign = theUtils.getXmlDocument(sPackageToSign);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new Exception("Unable to read package file ", e);
		}
		boolean isSigned = theUtils.isDocumentSigned(docPackageToSign);
		if (!isSigned) {
			throw new Exception("File is already signed");
		}
		GwPackageFile gwPkgFile = new GwPackageFile(docPackageToSign);
		GwAknMetadata gwMeta = gwPkgFile.getPackageInfo();
		//sign metadata
		signAknMetadata(gwMeta);
		signDigitalArtifacts(gwMeta);

		return isSigned;
	}
	

	public static void main(String[] args) {
		Path path1 = Paths.get("C:\\Users\\Java\\examples");
		// Output is C:\Users\Java\examples\Test.java
		System.out.println(path1.resolve("./Test.java"));
	}
	
}
