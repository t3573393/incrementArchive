package org.fartpig.incrementarchive.entity;

import java.util.ArrayList;
import java.util.List;

import org.fartpig.incrementarchive.constant.ChangeLogTypeEnum;

public class ChangeLogManifest {

	private ChangeLogTypeEnum logType;

	private List<ChangeLogEntry> addEntries = new ArrayList<ChangeLogEntry>();
	private List<ChangeLogEntry> removeEntries = new ArrayList<ChangeLogEntry>();
	private List<ChangeLogEntry> modifyEntries = new ArrayList<ChangeLogEntry>();

	public ChangeLogTypeEnum getLogType() {
		return logType;
	}

	public void setLogType(ChangeLogTypeEnum logType) {
		this.logType = logType;
	}

	public List<ChangeLogEntry> getAddEntries() {
		return addEntries;
	}

	public void setAddEntries(List<ChangeLogEntry> addEntries) {
		this.addEntries = addEntries;
	}

	public List<ChangeLogEntry> getRemoveEntries() {
		return removeEntries;
	}

	public void setRemoveEntries(List<ChangeLogEntry> removeEntries) {
		this.removeEntries = removeEntries;
	}

	public List<ChangeLogEntry> getModifyEntries() {
		return modifyEntries;
	}

	public void setModifyEntries(List<ChangeLogEntry> modifyEntries) {
		this.modifyEntries = modifyEntries;
	}

}
