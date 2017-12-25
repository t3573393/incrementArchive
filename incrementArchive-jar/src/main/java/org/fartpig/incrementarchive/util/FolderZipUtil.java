package org.fartpig.incrementarchive.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

public class FolderZipUtil {

	public static void zipFolder(String srcFolder, String destZipFile, boolean includeSelfFolder) throws Exception {
		ZipOutputStream zip = null;
		FileOutputStream fileWriter = null;

		fileWriter = new FileOutputStream(destZipFile);
		zip = new ZipOutputStream(fileWriter);

		addFolderToZip("", srcFolder, zip, includeSelfFolder);

		zip.flush();
		zip.close();
	}

	private static void addFileToZip(String path, String srcFile, ZipOutputStream zip, boolean includeSelfFolder)
			throws Exception {
		File folder = new File(srcFile);
		if (folder.isDirectory()) {
			addFolderToZip(path, srcFile, zip, includeSelfFolder);
		} else {
			byte[] buff = new byte[1024];
			int len;
			FileInputStream in = new FileInputStream(srcFile);
			zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
			while ((len = in.read(buff)) > 0) {
				zip.write(buff, 0, len);
			}
			IOUtils.closeQuietly(in);
		}
	}

	private static void addFolderToZip(String path, String srcFolder, ZipOutputStream zip, boolean includeSelfFolder)
			throws Exception {
		File folder = new File(srcFolder);

		for (String fileName : folder.list()) {
			if (path.equals("")) {
				// skip the fist one
				if (!includeSelfFolder) {
					addFileToZip("", srcFolder + "/" + fileName, zip, true);
				} else {
					addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip, true);
				}
			} else {
				// skip the fist one
				if (!includeSelfFolder) {
					addFileToZip(path, srcFolder + "/" + fileName, zip, true);
				} else {
					addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip, true);
				}

			}
		}
	}

}
