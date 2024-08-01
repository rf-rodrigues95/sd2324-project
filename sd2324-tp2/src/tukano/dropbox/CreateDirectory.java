package tukano.dropbox;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;

import tukano.dropbox.msgs.CreateFolderV2Args;

public class CreateDirectory extends AbstractDropboxOperations {

	private static final String CREATE_FOLDER_V2_URL = "https://api.dropboxapi.com/2/files/create_folder_v2";
	
	private static final String CONTENT_TYPE_HDR = "Content-Type";
	private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	
		
	public CreateDirectory() {
		super();
	}
	
	public void execute( String directoryName ) throws Exception {
		var createFolder = new OAuthRequest(Verb.POST, CREATE_FOLDER_V2_URL);
		createFolder.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

		createFolder.setPayload(json.toJson(new CreateFolderV2Args(directoryName, false)));

		service.signRequest(accessToken, createFolder);
		
		Response r = service.execute(createFolder);
		if (r.getCode() != HTTP_SUCCESS) 
			throw new RuntimeException(String.format("Failed to create directory: %s, Status: %d, \nReason: %s\n", directoryName, r.getCode(), r.getBody()));
	}
}
