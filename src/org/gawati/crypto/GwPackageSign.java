package org.gawati.crypto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.tomcat.dbcp.dbcp2.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
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

	
	private List<Document> getAknMetadataFilesInPackage(String sPackageToSign, Document docPackageToSign) throws Exception {
		List<Document> docs = new ArrayList<Document>(0); 
		NodeList nlAknMeta = docPackageToSign.getElementsByTagNameNS(GwConstants.GW_XMLNS, "aknMetadata");
		 if (nlAknMeta.getLength() > 0 ) {
			 File fpackagePath = new File(sPackageToSign);
			 String sPackageFolder = fpackagePath.getParentFile().getPath();
			 Path packageFolderPath =  Paths.get(sPackageFolder);
			 for(int i=0; i < nlAknMeta.getLength(); i++) {
				 Element aknMetaNode = (Element) nlAknMeta.item(i);
				 String sItemUri = aknMetaNode.getAttribute("uri");
				 String sHref = aknMetaNode.getAttribute("href");
				 if (sHref.startsWith("./")) {
					 // relative path 
					Path itemPath = packageFolderPath.resolve(sHref);
					try {
						Document doc = theUtils.getXmlDocument(itemPath.toString());
						docs.add(doc);
					} catch (SAXException | IOException | ParserConfigurationException e) {
						// TODO Auto-generated catch block
						throw new Exception("Error while reading metadata document", e);
					}
				 }
			 }
		 }
		 return docs;
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
		
		return isSigned;
	}
	

	public static void main(String[] args) {
		Path path1 = Paths.get("C:\\Users\\Java\\examples");
		// Output is C:\Users\Java\examples\Test.java
		System.out.println(path1.resolve("./Test.java"));
	}
	
}
