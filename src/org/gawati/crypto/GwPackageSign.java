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

	private boolean signAknMetadata(Document doc, String sPathToSignedPackage, GwKeyPairManager kpm, GwPackageFile pkg) {
		
		// get dom doc of akn metadata
		GwAknMetadata aknMeta = pkg.getPackageInfo();
		
		// path to aknmeta file to be signed
		Path pAknMetaPath = Paths.get(pkg.getPackageFileFolder(), aknMeta.folderPath, aknMeta.fileName);
		
		// path to new aknmeta file which has been signed
		String sFolderSignedPackage = theUtils.packageFolder(sPathToSignedPackage);
		Path pAknMetaPathSigned = Paths.get(sFolderSignedPackage, aknMeta.folderPath, aknMeta.fileName);
		
		
		//Path pSignedAknMetaPath = Paths.get(uri)
		//Document docAknMeta = theUtils.getXmlDocument(pAknMetaPath.toString());
		
		
		// sign dom package in target path
		
	
		
		return false;
	}
	
	private boolean signDigitalArtifacts(Document doc, String sPathToSignedPackage, GwKeyPairManager kpm, GwPackageFile pkg) {
		return false;
	}
	
	/**
	 * Signs a package and outputs signed package to a different package
	 * @param sPackageToSign path to pkg.xml
	 * @param sPathtoSignedPackage path to output pkg.xml
	 * @param kpm Key pair info
	 * @return
	 * @throws Exception
	 */
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
		
		GwPackageFile gwPkgFile = new GwPackageFile(sPackageToSign, docPackageToSign);
		//GwAknMetadata gwMeta = gwPkgFile.getPackageInfo();
		//sign metadata
		signAknMetadata(docPackageToSign, sPathtoSignedPackage, kpm, gwPkgFile);
		signDigitalArtifacts(docPackageToSign, sPathtoSignedPackage, kpm, gwPkgFile);

		return isSigned;
	}
	

	
}
