package utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import tukano.dropbox.*;

final public class IOAuth {

	public static boolean fileExists(String path, String file) {
		try {
			ListDirectory dir = new ListDirectory();
			
			List<String> directories = dir.execute(path);
			for(String directory : directories) {
				if(directory.equals(file))
					return true;
			}
			
			return false;
		} catch( Exception x ) {
			x.printStackTrace();
			return false;
		}
	}
	
	public static List<String> listDirFiles(String path) {
		try {
			ListDirectory dir = new ListDirectory();
			
			return dir.execute(path);
		} catch( Exception x ) {
			x.printStackTrace();
			return null;
		}
	}
	
	public static void write( String path, byte[] data ) {
		try {
			UploadFile up = new UploadFile();
			
			up.execute(path, data);
			//Files.write( out.toPath(), data);
		} catch( Exception x ) {
			x.printStackTrace();
		}
	}

	public static InputStream readStream( String from) {
		try {
			DownloadFile down = new DownloadFile();
			
			return down.execute(from);
		} catch( Exception x ) {
			x.printStackTrace();
			return null;
		}
	}
	
	public static byte[] readBytes( String from) {
		try {
			DownloadFile down = new DownloadFile();
			
			return readBytes(down.execute(from));
		} catch( Exception x ) {
			x.printStackTrace();
			return null;
		}
	}
	
	public static boolean delete( String from) {
		try {
			DeleteFile del = new DeleteFile();
			del.execute(from);
		} catch( Exception x ) {
			x.printStackTrace();
		}
		return false;
	}

	public static byte[] readBytes( InputStream in ) {		
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()){
			int n;
			byte[] tmp = new byte[1024];
			while( (n = in.read(tmp)) > 0 )
				baos.write( tmp, 0, n);	
			return baos.toByteArray();
		} catch(IOException x) {
			x.printStackTrace();
			return new byte[0];
		}
		
	}
}
