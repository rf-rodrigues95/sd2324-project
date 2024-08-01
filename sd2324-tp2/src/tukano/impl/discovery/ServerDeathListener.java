package tukano.impl.discovery;

import java.net.URI;

public interface ServerDeathListener {
		void onServerDeath(URI uri);
	}