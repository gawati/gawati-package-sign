package org.gawati.crypto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <gw:digitalPkg xmlns:gw="http://gawati.org/ns/1.0">
    <gw:aknMetadata uri="/akn/mu/act/2004-04-30/bill_no_11-2004/eng@/!main" href="./akn_mu_act_2004-04-30_bill_no_11-2004_eng_main.xml" />
    	<gw:digitalArtifacts>
        	<gw:digitalArtifact format="pdf" href="./akn_mu_act_2004-04-30_bill_no_11-2004_eng_main.pdf" />
    	</gw:digitalArtifacts>
	</gw:digitalPkg>

 * @author ashok
 *
 */
public class GwPackageFile {
	
	
	private static final Logger logg = 
			Logger.getLogger(GwPackageFile.class.getName());
	
	/**
	 * These are java objects representing the Package
	 * @author ashok
	 *
	 */
	public class GwAknMetadata {
		String uri;
		String fileName;
		String folderPath;
		List<GwAknArtifact> artifacts ;
		
		public GwAknMetadata() {
			artifacts = new ArrayList<>(0);
		}
		
	};
	
	public class GwAknArtifact {
		String format;
		String fileName;
		String folderPath;
	};
	
	String thePackageFileFolder ;
	GwAknMetadata theAknMetadata;
	GwCryptoUtils theUtils = new GwCryptoUtils();
	
	public GwPackageFile(Document docPackageToSign) throws Exception {
		theAknMetadata = new GwAknMetadata();
		// load the POJOs
		try {
			loadAknMetadataFromPackage(docPackageToSign);
		} catch (Exception e) {
			throw new Exception("Unable to read package file", e);
		}
	}

	
	public GwAknMetadata getPackageInfo() {
		return theAknMetadata;
	}
	
	
	/**
	 * Loads the Package information into POJOs
	 * @param sPackageToSign
	 * @param docPackageToSign
	 * @throws Exception
	 */
	private void loadAknMetadataFromPackage(Document docPackageToSign) throws Exception {
		NodeList nlAknMeta = docPackageToSign.getElementsByTagNameNS(GwConstants.GW_XMLNS, "aknMetadata");
		
		if (nlAknMeta.getLength() > 0 ) {
			 Element aknMetaNode = (Element) nlAknMeta.item(0);
			 
			 String sItemUri = aknMetaNode.getAttribute("uri");
			 String sHref = aknMetaNode.getAttribute("href");
			 File fHref = new File(sHref);
			 
			 theAknMetadata.uri = sItemUri;
			 theAknMetadata.fileName = fHref.getName();
			 theAknMetadata.folderPath = fHref.getParentFile().getPath();
			 
			 NodeList nldigitalArtifacts = docPackageToSign.getElementsByTagNameNS(GwConstants.GW_XMLNS, "digitalArtifact");

			 if (nldigitalArtifacts.getLength() > 0 ) {
				 for (int j=0 ; j < nldigitalArtifacts.getLength(); j++) {
					 GwAknArtifact gwArtifact = new GwAknArtifact();
					 Element digArtNode = (Element) nldigitalArtifacts.item(j);
					
					 gwArtifact.format= digArtNode.getAttribute("format");
					 String sDigHref = digArtNode.getAttribute("href");
					 File fDigiRef = new File(sDigHref);
					 gwArtifact.fileName = fDigiRef.getName();
					 gwArtifact.folderPath = fHref.getParentFile().getPath();
					 theAknMetadata.artifacts.add(gwArtifact);
				 }
			 }

		 }
	}
	
	public static void main(String[] args) throws Exception {
		GwPackageFile pkg = new GwPackageFile("D:\\develop\\gawati-pkg-sign\\test_data\\pkg.xml");
	}
}
