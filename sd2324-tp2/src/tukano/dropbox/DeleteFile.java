package tukano.dropbox;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;

import tukano.dropbox.msgs.DeleteFolderV2Args;

public class DeleteFile extends AbstractDropboxOperations {

	private static final String DELETE_FOLDER_V2_URL = "https://api.dropboxapi.com/2/files/delete_v2";
	
	private static final String CONTENT_TYPE_HDR = "Content-Type";
	private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	
		
	public DeleteFile() {
		super();
	}
	
	public void execute( String directoryName ) throws Exception {
		
		var delFolder = new OAuthRequest(Verb.POST, DELETE_FOLDER_V2_URL);
		delFolder.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

		delFolder.setPayload(json.toJson(new DeleteFolderV2Args(directoryName)));

		service.signRequest(accessToken, delFolder);
		
		Response r = service.execute(delFolder);
		if (r.getCode() != HTTP_SUCCESS) 
			throw new RuntimeException(String.format("Failed to delete: %s, Status: %d, \nReason: %s\n", directoryName, r.getCode(), r.getBody()));
	}

}
