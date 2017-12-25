package org.fartpig.incrementarchive.entity;

public class ChangeLogPathEntry extends ChangeLogEntry {

	private String filePath;

	public int hashCode() {
		return filePath.hashCode();
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String absolutePath() {
		return filePath;
	}

}
