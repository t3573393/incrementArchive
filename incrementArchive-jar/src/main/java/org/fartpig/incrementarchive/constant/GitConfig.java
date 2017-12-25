package org.fartpig.incrementarchive.constant;

public class GitConfig {

	private String urlPath;

	private String startVersion;
	private String endVersion = null;

	public String getUrlPath() {
		return urlPath;
	}

	public void setUrlPath(String urlPath) {
		this.urlPath = urlPath;
	}

	public String getStartVersion() {
		return startVersion;
	}

	public void setStartVersion(String startVersion) {
		this.startVersion = startVersion;
	}

	public String getEndVersion() {
		return endVersion;
	}

	public void setEndVersion(String endVersion) {
		this.endVersion = endVersion;
	}

	public String toString() {
		return urlPath + "@" + startVersion + "-" + endVersion;
	}

}
