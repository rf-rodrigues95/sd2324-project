package tukano.impl.rest.servers;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import tukano.impl.discovery.Discovery;
import tukano.impl.java.servers.AbstractServer;
import utils.IP;


public abstract class AbstractRestServer extends AbstractServer {
	private static final String SERVER_BASE_URI = "https://%s:%s%s";
	private static final String REST_CTX = "/rest";
	
	//private static final String SERVER_URI_FMT = "https://%s:%s/rest";

	protected AbstractRestServer(Logger log, String service, int port) {
		super(log, service, String.format(SERVER_BASE_URI, IP.hostAddress(), port, REST_CTX));
	}

	protected void start() {
		//TLS IN --> changed to https and commented 31 and implemented sslcontext.
		try {
			ResourceConfig config = new ResourceConfig();
			
			registerResources( config );
			
			//JdkHttpServerFactory.createHttpServer( URI.create(serverURI.replace(IP.hostAddress(), INETADDR_ANY)), config);

			JdkHttpServerFactory.createHttpServer( URI.create(serverURI.replace(IP.hostAddress(), INETADDR_ANY)), 
					config, SSLContext.getDefault());
			
			
			Discovery.getInstance().announce(service, super.serverURI);
			
			
			
			Log.info(String.format("%s Server ready @ %s\n",  service, serverURI));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	abstract void registerResources( ResourceConfig config );
}
