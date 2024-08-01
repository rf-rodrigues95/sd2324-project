package tukano.impl.api.java;

import tukano.api.java.Result;

public interface ExtendedShortsRep extends ExtendedShorts {

	Result<Void> notifyAllDeleteShorts( String userId, String password, String token );
	
}
