package tukano.impl.java.clients;

import java.net.URI;

import tukano.impl.discovery.Discovery;
import tukano.impl.rest.clients.RestUsersClient;
import tukano.api.java.Users;


public class UsersClientFactory {

	public static Users getClient() {
		URI[] uris = Discovery.getInstance().knownUrisOf(Users.NAME, 0);

		if (uris == null || uris.length == 0) {
			System.out.println("No server found for service Discover.");
			return null;
		}
		
		var serverURI = uris[0];
		
		return new RestUsersClient(serverURI.toString());
		
	}
}