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
import org.json.JSONException;
import org.json.JSONObject;

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

	private String getFileToSignPath(String filename) {
		 return tmpDataPath() + File.separator + filename;
	}

	private String getSigPath(String ipFilename) {
		return tmpDataPath() + File.separator + FilenameUtils.getBaseName(ipFilename) + ".sig";
	}

	private String getKeysPath() {
		return System.getProperty("user.dir") + File.separator + "test_keys";
	}

	private String getPublicKeyPath() {
		return getKeysPath() + File.separator + "id.pub";
	}

	private String getPrivateKeyPath() {
		return getKeysPath() + File.separator + "id.pri";
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
	
	/**
	 * Sign the given document.
	 * Post Params:
	 * `input_file`: File to be signed
	 * `public_key`: Public key file
	 * `private_key`: Private key file
	 * @throws IOException, WebApplicationException
	 */
	@POST
	@Path("/sign")
	@Produces(MediaType.TEXT_XML)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response restApplySign(FormDataMultiPart formData) throws IOException, WebApplicationException {
		FormDataBodyPart filePart = formData.getField("input_file");
		ContentDisposition fileHeader = filePart.getContentDisposition();
		String ipFilename = fileHeader.getFileName();
		final InputStream ipStream = filePart.getValueAs(InputStream.class);

		FormDataBodyPart pubKey = formData.getField("public_key");
		final InputStream pubKeyStream = pubKey.getValueAs(InputStream.class);
		FormDataBodyPart priKey = formData.getField("private_key");
		final InputStream priKeyStream = priKey.getValueAs(InputStream.class);

        String sPathFileToSign = getFileToSignPath(ipFilename);
		String sPathDetachedSignature = getSigPath(sPathFileToSign);
		log.info("Sign: " + ipFilename);

		try {
			writeToFile(ipStream, sPathFileToSign);
			writeToFile(pubKeyStream, getPublicKeyPath());
			writeToFile(priKeyStream, getPrivateKeyPath());

			GwKeyPairManager gwkpg = new GwKeyPairManager();
			gwkpg.loadKeyPair(getPrivateKeyPath(), getPublicKeyPath());
			gwDS.generateDigitalSignature(sPathFileToSign, sPathDetachedSignature, gwkpg);
		} catch(Exception e){
			log.info("Sign Exception: " + e);
			throw new WebApplicationException(e);
		}

		return Response.ok(new File(sPathDetachedSignature), MediaType.APPLICATION_OCTET_STREAM)
	            .header("content-disposition", "attachment; filename = "+ FilenameUtils.getName(sPathDetachedSignature))
	            .build();
	}

	/**
	 * Validates a signed document.
	 * Post Params:
	 * `sig_file`: Signed file to be validated
	 * `public_key`: Public key file
	 * @throws IOException, WebApplicationException, JSONException
	 */
	@POST
	@Path("/validate")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response restValidate(FormDataMultiPart formData) throws IOException, WebApplicationException, JSONException {
		FormDataBodyPart sigFilePart = formData.getField("sig_file");
		String sigFilename = sigFilePart.getContentDisposition().getFileName();
		final InputStream sigStream = sigFilePart.getValueAs(InputStream.class);

		FormDataBodyPart pubKey = formData.getField("public_key");
		final InputStream pubKeyStream = pubKey.getValueAs(InputStream.class);

		String sPathDetachedSignature = getSigPath(sigFilename);
		log.info("Validate: " + sigFilename);

		JSONObject valid = new JSONObject();
		valid.put("valid", false);

		try {
			writeToFile(sigStream, sPathDetachedSignature);
			writeToFile(pubKeyStream, getPublicKeyPath());

			boolean v = gwDS.validateDigitalSignature(sPathDetachedSignature, getPublicKeyPath());
			log.info("Valid = " + v);
			valid.put("valid", v);
		} catch(Exception e){
			log.info("Validate Exception: " + e);
			throw new WebApplicationException(e);
		}
		return Response.ok(valid.toString()).build();
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
