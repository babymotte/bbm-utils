package net.bbmsoft.bbm.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Helper class for file operations. The non static functions of this class work
 * on an internal {@link Path} variable that is updated according to what
 * methods are called and that can be retrieved as a {@link File} object by
 * calling the {@link #resolve()} or {@link #resolve(String)} methods.
 * <p>
 * Example:
 * 
 * <pre>
 * File configDir = FileHelper.get().userHome().resolve(".myapp");
 * </pre>
 * 
 * returns a {@link File} pointing to the directory {@code .myapp} in the user's
 * home dir and creates that directory if it does not exist yet.
 * 
 * @author Michael Bachmann
 */
public class FileHelper {

	public static final String USER_HOME = System.getProperty("user.home");

	private Path currentPath;

	/**
	 * Creates and returns a new {@link FileHelper} instance with an empty internal
	 * path.
	 * 
	 * @return returns a new {@link FileHelper} instance with an empty internal path
	 */
	public static FileHelper get() {
		return new FileHelper();
	}

	/**
	 * Points the {@link FileHelper} instance's internal path to the current user's
	 * home directory.
	 * 
	 * @return the {@link FileHelper} instance the method was called on
	 */
	public FileHelper userHome() {
		this.currentPath = Paths.get(USER_HOME);
		return this;
	}

	/**
	 * Resolves the specified path relative to the {@link FileHelper} instance's
	 * internal path if it has one or uses {@link Paths#get(String, String...)} to
	 * resolve the path if the {@link FileHelper} instance has no internal path set
	 * yet.
	 * 
	 * @param path
	 *            the path to resolve
	 * @return the {@link FileHelper} instance the method was called on
	 */
	public FileHelper getDir(String path) {
		this.currentPath = this.currentPath != null ? this.currentPath.resolve(path) : Paths.get(path);
		return this;
	}

	/**
	 * Creates a {@link File} object pointing to the specified directory relative to
	 * this {@link FileHelper} instance's current path, if it has one.
	 * <p>
	 * If the {@link FileHelper} instance does not have an internal path set yet,
	 * {@code null} will be returned.
	 * <p>
	 * If the {@link FileHelper} instance does have an internal path which does not
	 * yet exist on the file system, it will be created.
	 * 
	 * @return a {@link File} object pointing to this {@link FileHelper} instance's
	 *         current path
	 */
	public File resolve(String file) {
		if (getOrCreateDir(this.currentPath) != null) {
			return this.currentPath.resolve(file).toFile();
		}
		return null;
	}

	/**
	 * Creates a {@link File} object pointing to this {@link FileHelper} instance's
	 * current path, if it has one.
	 * <p>
	 * If the {@link FileHelper} instance does not have an internal path set yet,
	 * {@code null} will be returned.
	 * <p>
	 * If the {@link FileHelper} instance does have an internal path which does not
	 * yet exist on the file system, it will be created.
	 * 
	 * @return a {@link File} object pointing to this {@link FileHelper} instance's
	 *         current path
	 */
	public File resolve() {
		return getOrCreateDir(this.currentPath);
	}

	private static File getOrCreateDir(Path path) {

		if (path == null) {
			return null;
		}

		File file = path.toFile();
		if (!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	/**
	 * Deletes the contents of a directory without deleting the directory itself. If
	 * the specified {@link File} points to a file instead of a directory, this
	 * function is a no-op.
	 * 
	 * @param dir
	 *            the directory to be cleared
	 */
	public static void clearDirctory(File dir) {
		deleteR(dir, false);
	}

	/**
	 * Deletes a file or a directory including all its contents.
	 * 
	 * @param dir
	 *            the directory or file to be deleted
	 */
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
