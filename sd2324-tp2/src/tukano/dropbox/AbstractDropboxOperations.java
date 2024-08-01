package tukano.dropbox;

import org.pac4j.scribe.builder.api.DropboxApi20;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;

import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;

public abstract class AbstractDropboxOperations {
	private static final String apiKey = "6ace8lzioa49gnd";
	private static final String apiSecret = "nx0lha07s6bxz3p";
	private static final String accessTokenStr = "sl.B2TpOJrF6Biok7cCHFTr18dEFRnudWbR_wDCGp4dRhNDFIMzzfk48UCcbSEkk9ZAwr-SAvegxt63nOqVLHDvUFQYkMM0MWAL1CROqXmaHHgDfC8GD6mfyjvjMc39WrnMwNwKUYIZE7cjS6I";
	
	protected static final int HTTP_SUCCESS = 200;
	
	protected final Gson json;
	protected final OAuth20Service service;
	protected final OAuth2AccessToken accessToken;
	
	protected AbstractDropboxOperations() {
		json = new Gson();
		accessToken = new OAuth2AccessToken(accessTokenStr);
		service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
	}

}


