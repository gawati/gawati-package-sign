package org.gawati.crypto;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

@Path("/doc")
public class GwSignAdapter {
	private String thisClassName = GwSignAdapter.class.getName();
	
	private static final Logger log = 
			 Logger.getLogger(GwSignAdapter.class.getName());
	
	GwDigitalSignature gwDS = null;
	Double randomNum ; 
	private String serviceVersion = "1.0.1";
	public GwSignAdapter() {
	    gwDS = new GwDigitalSignature();
	    randomNum = Math.random();
		log.info("VERSION: " + thisClassName+ " /doc : constructing, version :" + serviceVersion);
	}
	
	public class SignProcessRequest {
		final InputStream xmlFile;
		
		public SignProcessRequest(InputStream xmlFile) {
			this.xmlFile = xmlFile;
		}
		
		public boolean isValid() {
			return (this.xmlFile != null);
		}
	}
	
	private String testDataPath(){ 
		return System.getProperty("user.dir") + File.separator + "test_data" ;
	}
	
	@POST
	@Path("/sign")
	@Produces(MediaType.TEXT_XML)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response restApplySign(FormDataMultiPart formData) throws IOException {
		FormDataBodyPart filePart = formData.getField("input_file");
		ContentDisposition fileHeader = filePart.getContentDisposition();
		String fileName = fileHeader.getFileName();
        String reply = "File confirmed: " + fileName;
        
        InputStream readFormStream = filePart.getValueAs(InputStream.class);
        
//		final InputStream isFile =  new ByteArrayInputStream(input_file.getBytes(StandardCharsets.UTF_8));
		
		StreamingOutput stream =  new StreamingOutput(){

			@Override
			public void write(OutputStream out) throws IOException, WebApplicationException {
				try {
//					String sKeyFolder = System.getProperty("user.dir") + File.separator + "test_keys";
//					String sPathFileToSign = testDataPath() + File.separator + "akn_mu_act_2004-04-30_bill_no_11-2004_eng_main.xml";
					String sPathDetachedSignature = testDataPath() + File.separator + "akn_mu_act_2004-04-30_bill_no_11-2004_eng_main.sig";
//
//					GwKeyPairManager gwkpg = new GwKeyPairManager();
//					gwkpg.generateKeyPair();
//					try {
//						gwkpg.serializeKeyPair(sKeyFolder);
//					} catch (IOException e) {
//						e.printStackTrace();
//					}	
//					gwDS.generateDigitalSignature(sPathFileToSign, sPathDetachedSignature, gwkpg);
					System.out.println(" Signed = " + fileName);
				} catch(Exception e){
					throw new WebApplicationException(e);
				}
			}
		};
		return Response.ok(reply).build();
//		return Response.ok(stream).build();
	}	
	
	@GET
	@Path("/test")
	public Response getTest() throws URISyntaxException{
		/**
		 * Check for thread safety ...every request generates a random number
		 */
		String s = "TEST (/doc/test) !  = " +  this.randomNum + " VERSION = " + this.serviceVersion ;
		return Response.ok(s).build();
	}

}
