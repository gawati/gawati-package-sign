package org.gawati.crypto;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class GwDigitalSignatureTest {
	GwKeyPairManager gwkpg = null;
	GwDigitalSignature gwDS = null;
	
	String sPathFileToSign = "";
	String sPathDetachedSignature = "";
	String sPublicKey = "";

	public GwDigitalSignatureTest() throws NoSuchAlgorithmException {

	}
	
	private void testGenerateKeyPair() throws NoSuchAlgorithmException {
		gwkpg = new GwKeyPairManager();
		gwkpg.generateKeyPair();
		System.out.println(" OUT = " + gwkpg);
		try {
			gwkpg.serializeKeyPair();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	private String testDataPath(){ 
		return System.getProperty("user.dir") + File.separator + "test_data" ;
	}
	
	@Before
	public void setUp() throws Exception {
		gwDS = new GwDigitalSignature();
		sPathFileToSign = testDataPath() + File.separator + "akn_mu_act_2004-04-30_bill_no_11-2004_eng_main.xml";
		sPathDetachedSignature = testDataPath() + File.separator + "akn_mu_act_2004-04-30_bill_no_11-2004_eng_main.sig";
		sPublicKey = System.getProperty("user.dir") + File.separator + "keys" + File.separator + "id.public";
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGenerateDigitalSignature() {
		try {
		testGenerateKeyPair();
		gwDS.generateDigitalSignature(sPathFileToSign, sPathDetachedSignature, gwkpg);
		} catch (Exception e) {
			e.printStackTrace();
			org.junit.Assert.fail("Failed while generating digital signature");
		}
	}
	

	@Test
	public void testValidateDigitalSignature() {
		try {
		testGenerateKeyPair();	
		gwDS.generateDigitalSignature(sPathFileToSign, sPathDetachedSignature, gwkpg);
		boolean valid = gwDS.validateDigitalSignature(sPathDetachedSignature, sPublicKey);
		System.out.println(" Valid = " + valid);
		org.junit.Assert.assertTrue("The test failed", valid);
		} catch (Exception e) {
			e.printStackTrace();
			org.junit.Assert.fail("Failed while generating digital signature");
		}
	}

}
