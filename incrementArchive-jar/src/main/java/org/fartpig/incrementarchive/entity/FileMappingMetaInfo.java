package org.fartpig.incrementarchive.entity;

public class FileMappingMetaInfo {

	private String srcPath;
	private boolean isSrcDir = true;

	private String targetPath;
	private boolean isTargetDir = true;

	public String getSrcPath() {
		return srcPath;
	}

	public void setSrcPath(String srcPath) {
		this.srcPath = srcPath;
	}

	public boolean isSrcDir() {
		return isSrcDir;
	}

	public void setSrcDir(boolean isSrcDir) {
		this.isSrcDir = isSrcDir;
	}

	public String getTargetPath() {
		return targetPath;
	}

	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}

	public boolean isTargetDir() {
		return isTargetDir;
	}

	public void setTargetDir(boolean isTargetDir) {
		this.isTargetDir = isTargetDir;
	}

}
