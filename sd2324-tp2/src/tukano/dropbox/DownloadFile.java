package tukano.dropbox;

import java.io.InputStream;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;

import tukano.dropbox.msgs.DownloadFileArgs;

public class DownloadFile extends AbstractDropboxOperations {

	private static final String DOWNLOAD_FILE_URL = "https://content.dropboxapi.com/2/files/download";	
	
	private static final String CONTENT_TYPE_HDR = "Content-Type";
	private static final String JSON_CONTENT_TYPE = "application/octet-stream";
	
	public DownloadFile() {
		super();
	}
	
	public InputStream execute( String filePath) throws Exception {
		
		var downloadFile = new OAuthRequest(Verb.POST, DOWNLOAD_FILE_URL);
		
		String downloadArgsJson = json.toJson(new DownloadFileArgs(filePath));
		downloadFile.addHeader("Dropbox-API-Arg", downloadArgsJson);
		
		downloadFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

		service.signRequest(accessToken, downloadFile);
		
		Response r = service.execute(downloadFile);
		if (r.getCode() != HTTP_SUCCESS) 
			throw new RuntimeException(String.format("Failed to download file: %s, Status: %d, \nReason: %s\n", filePath, r.getCode(), r.getBody()));
	
		
		return r.getStream();
	}

}
