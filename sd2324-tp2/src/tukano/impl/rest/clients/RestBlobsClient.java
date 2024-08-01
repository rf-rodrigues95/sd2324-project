package tukano.impl.rest.clients;

import static java.lang.String.format;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.rest.RestBlobs;
import tukano.impl.api.java.ExtendedBlobs;
import tukano.impl.api.rest.RestExtendedBlobs;
import tukano.impl.discovery.Discovery;
import utils.DB;

public class RestBlobsClient extends RestClient implements ExtendedBlobs {

	private final String serverUrl;
	
	public RestBlobsClient(String serverURI) {
		super(serverURI, RestBlobs.PATH);
		this.serverUrl = serverURI;
	}

	private Result<Void> _upload(String blobId, byte[] bytes) {
		return super.toJavaResult(
				target.path(blobId)
				.request()
				.post( Entity.entity(bytes, MediaType.APPLICATION_OCTET_STREAM)));
	}

	private Result<byte[]> _download(String blobId) {
		return super.toJavaResult(
				target.path(blobId)
				.request()
				.accept(MediaType.APPLICATION_OCTET_STREAM)
				.get(), byte[].class);
	}

	private Result<Void> _delete(String blobURL, String token) {
		return super.toJavaResult(
				client.target( blobURL )
				.queryParam( RestExtendedBlobs.TOKEN, token )
				.request()
				.delete());
	}
	
	private Result<Void> _deleteAllBlobs(String userId, String token) {
		return super.toJavaResult(
				target.path(userId)
				.path(RestExtendedBlobs.BLOBS)
				.queryParam( RestExtendedBlobs.TOKEN, token )
				.request()
				.delete());
	}
	
	@Override
	public Result<Void> upload(String blobId, byte[] bytes) {

		var result = super.reTry( () -> _upload(blobId, bytes));

		if (!result.isOK()) {
			URI[] knownUris = Discovery.getInstance().knownUrisOf(Blobs.NAME, 2);

			for(URI uri: knownUris) {
				if (!uri.equals(URI.create(serverUrl))) {
					return (new RestBlobsClient(uri.toString())).upload(blobId, bytes);
				}
			}		
			return Result.error(ErrorCode.INTERNAL_ERROR);
		} else { return result;}
	}

	@Override
	public Result<byte[]> download(String blobId) {
		var result = super.reTry( () -> _download(blobId));
		
		if (!result.isOK()) {
			URI[] knownUris = Discovery.getInstance().knownUrisOf(Blobs.NAME, 2);
			
			for(URI uri: knownUris) {
				if (!uri.equals(URI.create(serverUrl))) {
					return (new RestBlobsClient(uri.toString())).download(blobId);
				}
			}
			
			return Result.error(ErrorCode.INTERNAL_ERROR);
		} else { return result;}
	}

	@Override
	public Result<Void> delete(String blobId, String token) {
		var result = super.reTry( () -> _delete(blobId, token));
		if (!result.isOK()) {
			URI[] knownUris = Discovery.getInstance().knownUrisOf(Blobs.NAME, 2);
			
			for(URI uri: knownUris) {
				if (!uri.equals(URI.create(serverUrl))) {
					return (new RestBlobsClient(uri.toString())).delete(blobId, token);
				}
			}
			
			return Result.error(ErrorCode.INTERNAL_ERROR);
		} else { return result;}
	}
	
	@Override
	public Result<Void> deleteAllBlobs(String userId, String password) {
		var result = super.reTry( () -> _deleteAllBlobs(userId, password)); 
		if (!result.isOK()) {
			URI[] knownUris = Discovery.getInstance().knownUrisOf(Blobs.NAME, 2);
			
			for(URI uri: knownUris) {
				if (!uri.equals(URI.create(serverUrl))) {
					return (new RestBlobsClient(uri.toString())).deleteAllBlobs(userId, password);
				}
			}
			
			return Result.error(ErrorCode.INTERNAL_ERROR);
		} else { return result;} 
	}
}
