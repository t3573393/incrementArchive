package org.fartpig.incrementarchive.constant;

public class SvnConfig {

	private String name;
	private String password;
	private String urlPath;
	private String prefixPath;

	private long startVersion;
	private long endVersion = -1;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrlPath() {
		return urlPath;
	}

	public void setUrlPath(String urlPath) {
		this.urlPath = urlPath;
	}

	public long getStartVersion() {
		return startVersion;
	}

	public void setStartVersion(long startVersion) {
		this.startVersion = startVersion;
	}

	public long getEndVersion() {
		return endVersion;
	}

	public void setEndVersion(long endVersion) {
		this.endVersion = endVersion;
	}

	public String toString() {
		return name + ":" + password + "@" + urlPath + "@" + startVersion + "-" + endVersion + "@" + prefixPath;
	}

	public String getPrefixPath() {
		return prefixPath;
	}

	public void setPrefixPath(String prefixPath) {
		this.prefixPath = prefixPath;
	}

}
