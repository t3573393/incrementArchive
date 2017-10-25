package org.fartpig.incrementarchive.util;

public class ToolException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1834222662981460609L;

	private String phase;

	public ToolException(String phase, String message) {
		super(message);
		this.phase = phase;
	}

	public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

}
