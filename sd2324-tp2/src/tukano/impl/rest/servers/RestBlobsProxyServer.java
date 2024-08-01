package tukano.impl.rest.servers;

import java.util.logging.Logger;

import org.glassfish.jersey.server.ResourceConfig;

import tukano.api.java.Blobs;
import tukano.impl.rest.servers.utils.CustomLoggingFilter;
import tukano.impl.rest.servers.utils.GenericExceptionMapper;
import utils.Args;
import utils.ProxyState;


public class RestBlobsProxyServer extends AbstractRestServer {
	public static final int PORT = 8765;
	
	private static Logger Log = Logger.getLogger(RestBlobsProxyServer.class.getName());

	RestBlobsProxyServer(int port) {
		super( Log, Blobs.NAME, port);
		
	}
	
	
	@Override
	void registerResources(ResourceConfig config) {
		config.property("authProxy", true);
		config.register( RestBlobsResource.class ); 
		config.register(new GenericExceptionMapper());
		config.register(new CustomLoggingFilter());
	}
	
	public static void main(String[] args) {
		Args.use(args);
		
		String result = Args.valueOf(args, 0, "false");
		
		ProxyState.set( Boolean.parseBoolean(result));
		
		new RestBlobsProxyServer(Args.valueOf("-port", PORT)).start();
	}	
}