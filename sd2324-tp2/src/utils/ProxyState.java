package utils;

public class ProxyState {

	private static boolean val;
	
	public static void set(boolean ignore) {
		val = ignore;
	}
	
	public static boolean isStateless(){
		return val;
	}
	
}
