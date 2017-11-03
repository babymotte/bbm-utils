package net.bbmsoft.bbm.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHelper {
	
	public static final String USER_HOME = System.getProperty("user.home");
	
	private Path currentPath;
	
	public static FileHelper get() {
		return new FileHelper();
	}
	
	public FileHelper userHome() {
		this.currentPath = Paths.get(USER_HOME);
		return this;
	}
	
	public FileHelper getDir(String path) {
		this.currentPath = this.currentPath != null ? this.currentPath.resolve(path) : Paths.get(path);
		return this;
	}
	
	public File resolve(String file) {
		if(getOrCreateDir(this.currentPath) != null) {
			return this.currentPath.resolve(file).toFile();
		}
		return null;
	}
	
	public File resolve() {
		return getOrCreateDir(this.currentPath);
	}

	private static File getOrCreateDir(Path path) {
		
		if(path == null) {
			return null;
		}
		
		File file = path.toFile();
		if(!file.exists()) {
			file.mkdirs();
		}
		return file;
	}
	
	public static void clearDirctory(File dir) {
		deleteR(dir, false);
	}
	
	public static void deleteR(File dir) {
		deleteR(dir, true);
	}
	
	private static void deleteR(File file, boolean deleteRoot) {

		File[] listFiles = file.listFiles();

		if (listFiles != null) {
			for (File child : listFiles) {
				deleteR(child, true);
			}
		}

		if (deleteRoot) {
			file.delete();
		}
	}
}
