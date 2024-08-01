package tukano.dropbox;

import java.util.ArrayList;
import java.util.List;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;

import tukano.dropbox.msgs.ListFolderArgs;
import tukano.dropbox.msgs.ListFolderContinueArgs;
import tukano.dropbox.msgs.ListFolderReturn;

public class ListDirectory extends AbstractDropboxOperations {

	private static final String LIST_FOLDER_URL = "https://api.dropboxapi.com/2/files/list_folder";
	private static final String LIST_FOLDER_CONTINUE_URL = "https://api.dropboxapi.com/2/files/list_folder/continue";

	private static final String CONTENT_TYPE_HDR = "Content-Type";
	private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";

	public ListDirectory() {
		super();
	}

	public List<String> execute(String directoryName) throws Exception {
		var directoryContents = new ArrayList<String>();

		var listDirectory = new OAuthRequest(Verb.POST, LIST_FOLDER_URL);
		listDirectory.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);
		listDirectory.setPayload(json.toJson(new ListFolderArgs(directoryName)));

		service.signRequest(accessToken, listDirectory);

		Response r = service.execute(listDirectory);;
		if (r.getCode() != HTTP_SUCCESS) 
			throw new RuntimeException(String.format("Failed to list directory: %s, Status: %d, \nReason: %s\n", directoryName, r.getCode(), r.getBody()));

		var reply = json.fromJson(r.getBody(), ListFolderReturn.class);
		reply.getEntries().forEach( e -> directoryContents.add( e.toString() ) );
		
		while( reply.has_more() ) {
			listDirectory = new OAuthRequest(Verb.POST, LIST_FOLDER_CONTINUE_URL);
			listDirectory.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);
			
			// In this case the arguments is just an object containing the cursor that was
			// returned in the previous reply.
			listDirectory.setPayload(json.toJson(new ListFolderContinueArgs(reply.getCursor())));
			service.signRequest(accessToken, listDirectory);

			r = service.execute(listDirectory);
			
			if (r.getCode() != HTTP_SUCCESS) 
				throw new RuntimeException(String.format("Failed to list directory: %s, Status: %d, \nReason: %s\n", directoryName, r.getCode(), r.getBody()));
			
			reply = json.fromJson(r.getBody(), ListFolderReturn.class);
			reply.getEntries().forEach( e -> directoryContents.add( e.toString() ) );
		}
				
		return directoryContents;
	}

}
