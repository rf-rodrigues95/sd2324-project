package tukano.dropbox;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;

import tukano.dropbox.msgs.UploadFileArgs;

public class UploadFile extends AbstractDropboxOperations {

	private static final String UPLOAD_FILE_URL = "https://content.dropboxapi.com/2/files/upload";
	
	private static final String CONTENT_TYPE_HDR = "Content-Type";
	private static final String JSON_CONTENT_TYPE = "application/octet-stream";
	
	public UploadFile() {
		super();
	}
	
	public void execute( String filePath, byte[] data) throws Exception {
		
		var uploadFile = new OAuthRequest(Verb.POST, UPLOAD_FILE_URL);
		String uploadArgsJson = json.toJson(new UploadFileArgs(false, "add", false, filePath, false));
		uploadFile.addHeader("Dropbox-API-Arg", uploadArgsJson);
		uploadFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);
		// Set the payload with the file content
	    uploadFile.setPayload(data);

		service.signRequest(accessToken, uploadFile);
		
		Response r = service.execute(uploadFile);
		if (r.getCode() != HTTP_SUCCESS) 
			throw new RuntimeException(String.format("Failed to upload file: %s, Status: %d, \nReason: %s\n", filePath, r.getCode(), r.getBody()));
	}

}
