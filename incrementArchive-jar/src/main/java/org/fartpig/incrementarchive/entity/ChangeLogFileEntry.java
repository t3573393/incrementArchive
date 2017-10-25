package org.fartpig.incrementarchive.entity;

import java.io.File;

public class ChangeLogFileEntry extends ChangeLogEntry {

	private String fileBaseName;
	private String fileEx;
	private String filePath;

	public int hashCode() {
		return this.toString().hashCode();
	}

	public String getFileBaseName() {
		return fileBaseName;
	}

	public void setFileBaseName(String fileBaseName) {
		this.fileBaseName = fileBaseName;
	}

	public String getFileEx() {
		return fileEx;
	}

	public void setFileEx(String fileEx) {
		this.fileEx = fileEx;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String absolutePath() {
		return String.format("%s%s%s.%s", filePath, File.separator, fileBaseName, fileEx);
	}
}
