package org.fartpig.incrementarchive.util;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;

import org.fartpig.incrementarchive.App;
import org.fartpig.incrementarchive.entity.ChangeLogEntry;

public final class PathUtil {

	public static String getProjectPath() {
		URL url = App.class.getProtectionDomain().getCodeSource().getLocation();
		String filePath = null;
		try {
			filePath = URLDecoder.decode(url.getPath(), "utf-8");
		} catch (Exception e) {
			ToolLogger.getInstance().error("error:", e);
		}

		if (filePath.endsWith(".jar")) {
			filePath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
		}

		File file = new File(filePath);
		return file.getAbsolutePath();
	}

	public static String getRelativePath(ChangeLogEntry aEntry, String inputDir) {
		String relativePath = null;
		if (aEntry.isRelative()) {
			relativePath = aEntry.getFileInputName();
		} else {
			String fileAbsolutePath = aEntry.absolutePath();
			int index = fileAbsolutePath.indexOf(inputDir);
			if (index != -1) {
				relativePath = fileAbsolutePath.substring(inputDir.length());
			} else {
				relativePath = aEntry.getFileInputName();
			}
		}
		return relativePath;
	}

	public static String getRelativePath(String fileAbsolutePath, String inputDir) {
		String relativePath = null;
		int index = fileAbsolutePath.indexOf(inputDir);
		if (index != -1) {
			relativePath = fileAbsolutePath.substring(inputDir.length());
		} else {
			relativePath = fileAbsolutePath;
		}
		return relativePath;
	}

	public static String formatFolderPath(String folderPath) {
		if (folderPath.charAt(0) != File.separatorChar) {
			folderPath = File.separator + folderPath;
		}

		if (folderPath.charAt(folderPath.length() - 1) != File.separatorChar) {
			folderPath += File.separator;
		}
		return folderPath;
	}

	public static String formatFilePath(String filePath) {
		if (filePath.charAt(0) != File.separatorChar) {
			filePath = File.separator + filePath;
		}
		return filePath;
	}

	public static String convertToSystemSeparator(String path) {
		String aChar = "/";
		String bChar = "\\";
		String systemChar = File.separatorChar + "";

		boolean containsA = path.contains(aChar);
		boolean containsB = path.contains(bChar);
		if (containsA && !containsB && bChar.equals(systemChar)) {
			path = path.replace(aChar, systemChar);
		} else if (containsB && !containsA && aChar.equals(systemChar)) {
			path = path.replace(bChar, systemChar);
		} else if (containsA && containsB) {
			if (aChar.equals(systemChar)) {
				path = path.replace(bChar, systemChar);
			} else if (bChar.equals(systemChar)) {
				path = path.replace(aChar, systemChar);
			}
		}

		return path;
	}

	public static String formatFolderSuffixPath(String folderPath) {
		if (folderPath.charAt(folderPath.length() - 1) != File.separatorChar) {
			folderPath += File.separator;
		}
		return folderPath;
	}
}
