package cn.bidlink.framework.search.engine.connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

public class FileConnectionPool {
	
	private static Hashtable<String, FileConnection> pool = new Hashtable<String, FileConnection>();
	private static final int DEFAULT_BUFFER_SIZE = 5 * 1024;
	
	public static FileConnection getFileConnection(String filePath) throws IOException,FileNotFoundException{
		if(pool.containsKey(filePath)){
			return pool.get(filePath);
		}else{
			FileConnection fileConnection = new FileConnection();
			File file = new File(filePath);
			if(!file.exists()){
				file.createNewFile();
			}
			fileConnection.setFile(file);
			fileConnection.setBreader(new BufferedReader(new FileReader(file), DEFAULT_BUFFER_SIZE));
			fileConnection.setBwriter(new BufferedWriter(new FileWriter(file, true), DEFAULT_BUFFER_SIZE));
			pool.put(filePath, fileConnection);
			return fileConnection;
		}
	}
	
	public static void write(String filePath, String context) throws IOException{
		BufferedWriter bwriter = getFileConnection(filePath).getBwriter();
		bwriter.write(context);
	}
	
	public static void closeFileConnection(String filePath) throws IOException{
		if(pool.containsKey(filePath)){
			FileConnection fileConnection = pool.get(filePath);
			if(fileConnection != null){
				BufferedReader breader = fileConnection.getBreader();
				if(breader != null){
					breader.close();
				}
				BufferedWriter bwriter = fileConnection.getBwriter();
				if(bwriter != null){
					bwriter.flush();
					bwriter.close();
				}
				pool.remove(filePath);
			}
		}
	}
}
