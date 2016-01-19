package cn.bidlink.framework.search.engine.connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

public class FileConnection {
	
	private File file;
	private BufferedReader breader;
	private BufferedWriter bwriter;
	
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public BufferedReader getBreader() {
		return breader;
	}
	public void setBreader(BufferedReader breader) {
		this.breader = breader;
	}
	public BufferedWriter getBwriter() {
		return bwriter;
	}
	public void setBwriter(BufferedWriter bwriter) {
		this.bwriter = bwriter;
	}
}
