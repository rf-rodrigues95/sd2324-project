package tukano.impl.rest.servers;

import tukano.impl.api.java.ExtendedBlobs;
import tukano.impl.api.rest.RestExtendedBlobs;
import tukano.impl.java.servers.JavaBlobs;
import tukano.impl.java.servers.JavaBlobsAuth;

import org.glassfish.jersey.server.ResourceConfig;

import jakarta.inject.Inject;

public class RestBlobsResource extends RestResource implements RestExtendedBlobs {

	final ExtendedBlobs impl;
	private final boolean authProxy;
	
	@Inject
	public RestBlobsResource(ResourceConfig cfg) {
		this.authProxy = (boolean) cfg.getProperty("authProxy");
		if (authProxy)
			this.impl = new JavaBlobsAuth();
		else
			this.impl = new JavaBlobs();
	}
	
	@Override
	public void upload(String blobId, byte[] bytes) {
		super.resultOrThrow( impl.upload(blobId, bytes));
	}

	@Override
	public byte[] download(String blobId) {
		return super.resultOrThrow( impl.download( blobId ));
	}

	@Override
	public void delete(String blobId, String token) {
		super.resultOrThrow( impl.delete( blobId, token ));
	}
	
	@Override
	public void deleteAllBlobs(String userId, String password) {
		super.resultOrThrow( impl.deleteAllBlobs( userId, password ));
	}
}
