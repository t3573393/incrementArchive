package org.fartpig.incrementarchive.entity;

public abstract class ChangeLogEntry {

	protected String fileInputName;

	protected boolean isRelative = true;

	protected boolean isFileMapping = false;

	public String getFileInputName() {
		return fileInputName;
	}

	public void setFileInputName(String fileInputName) {
		this.fileInputName = fileInputName;
	}

	public boolean isRelative() {
		return isRelative;
	}

	public void setRelative(boolean isRelative) {
		this.isRelative = isRelative;
	}

	public String toString() {
		return String.format("%s-%s", isRelative, this.absolutePath());
	}

	public boolean isFileMapping() {
		return isFileMapping;
	}

	public void setFileMapping(boolean isFileMapping) {
		this.isFileMapping = isFileMapping;
	}

	public abstract String absolutePath();

}
