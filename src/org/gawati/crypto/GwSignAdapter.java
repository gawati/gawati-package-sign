package org.gawati.crypto;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
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

import org.apache.commons.io.FilenameUtils;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

@Path("/doc")
public class GwSignAdapter {
	private String thisClassName = GwSignAdapter.class.getName();
	private static final Logger log = Logger.getLogger(GwSignAdapter.class.getName());
	
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
	
	private String tmpDataPath(){
		String tmpDirPath = System.getProperty("user.dir") + File.separator + "tmp_data";
		File tmpDir = new File(tmpDirPath);
		if (!tmpDir.exists()) tmpDir.mkdirs();
		return tmpDirPath;
	}

	private String getSigPath(String ipFilename) {
		return tmpDataPath() + File.separator + FilenameUtils.getBaseName(ipFilename) + ".sig";
	}

	private String getKeysPath() {
		return System.getProperty("user.dir") + File.separator + "test_keys";
	}

	// Saves uploaded file to path
	private void writeToFile(InputStream ipStream, String ipPath) {
		try {
			OutputStream out = new FileOutputStream(new File(
					ipPath));
			int read = 0;
			byte[] bytes = new byte[1024];

			out = new FileOutputStream(new File(ipPath));
			while ((read = ipStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (IOException e) {

			e.printStackTrace();
		}

	}
	
	@POST
	@Path("/sign")
	@Produces(MediaType.TEXT_XML)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response restApplySign(FormDataMultiPart formData) throws IOException {
		FormDataBodyPart filePart = formData.getField("input_file");
		ContentDisposition fileHeader = filePart.getContentDisposition();
		String fileName = fileHeader.getFileName();
        
        String sPathFileToSign = tmpDataPath() + File.separator + fileName;
        final InputStream ipStream = filePart.getValueAs(InputStream.class);
		writeToFile(ipStream, sPathFileToSign);

		String sKeyFolder = getKeysPath();
		String sPathDetachedSignature = getSigPath(sPathFileToSign);
		log.info("Sig filepath: " + sPathDetachedSignature + " | Keys Path: " + sKeyFolder);

		StreamingOutput stream =  new StreamingOutput(){

			@Override
			public void write(OutputStream out) throws IOException, WebApplicationException {
				try {
					GwKeyPairManager gwkpg = new GwKeyPairManager();
					gwkpg.generateKeyPair();
					try {
						gwkpg.serializeKeyPair(sKeyFolder);
					} catch (IOException e) {
						e.printStackTrace();
					}
					gwDS.generateDigitalSignature(sPathFileToSign, sPathDetachedSignature, gwkpg);
					out.write(String.format("Signed file at: %s\n", sPathDetachedSignature).getBytes());
					out.flush();
				} catch(Exception e){
					throw new WebApplicationException(e);
				}
			}
		};
		return Response.ok(stream).build();
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
