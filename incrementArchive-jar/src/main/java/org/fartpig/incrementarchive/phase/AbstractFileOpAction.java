package org.fartpig.incrementarchive.phase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.fartpig.incrementarchive.entity.ChangeLogEntry;
import org.fartpig.incrementarchive.entity.ChangeLogFileEntry;
import org.fartpig.incrementarchive.util.ToolLogger;

public abstract class AbstractFileOpAction {

	private Pattern classNamePattern = null;

	public AbstractFileOpAction() {
		classNamePattern = Pattern.compile("(\\S+)[$]\\S+");
	}

	protected List<ChangeLogFileEntry> addExtraEntryForClass(ChangeLogFileEntry fileEntry) {
		List<ChangeLogFileEntry> tempFileEntries = new ArrayList<ChangeLogFileEntry>();
		tempFileEntries.add(fileEntry);
		// hackced:
		// handle the special class entry : ${baseName}$**.class
		String fileEx = fileEntry.getFileEx();
		if ("class".equals(fileEx)) {
			File dirFile = new File(fileEntry.getFilePath());
			if (dirFile.isDirectory()) {
				for (File aFile : dirFile.listFiles()) {
					if (aFile.isFile()) {
						String tempFileName = aFile.getName();
						String tempFileExtension = FilenameUtils.getExtension(tempFileName);
						String tempBaseName = FilenameUtils.getBaseName(tempFileName);
						if (tempFileExtension.equals(fileEx)) {
							Matcher matcher = classNamePattern.matcher(tempBaseName);
							if (matcher.find()) {
								ChangeLogFileEntry tempfileEntry = new ChangeLogFileEntry();
								tempfileEntry.setFileInputName(fileEntry.getFileInputName());
								tempfileEntry.setFileBaseName(tempBaseName);
								tempfileEntry.setFileEx(fileEx);
								tempfileEntry.setFilePath(fileEntry.getFilePath());
								tempfileEntry.setRelative(fileEntry.isRelative());
								tempfileEntry.setFileMapping(fileEntry.isFileMapping());
								tempFileEntries.add(tempfileEntry);
							}
						}
					}
				}
			}
		}

		return tempFileEntries;
	}

	protected List<ChangeLogEntry> filterEntryByExtensions(List<ChangeLogEntry> entries, String[] extensions) {

		List<ChangeLogEntry> result = new ArrayList<ChangeLogEntry>();

		for (ChangeLogEntry aEntry : entries) {
			boolean needFilter = true;
			ToolLogger.getInstance().info("prepare filter:" + aEntry.absolutePath());
			if (aEntry instanceof ChangeLogFileEntry) {
				ChangeLogFileEntry fileEntry = (ChangeLogFileEntry) aEntry;
				for (String aEx : extensions) {
					if (aEx.equals(fileEntry.getFileEx())) {
						needFilter = false;
						ToolLogger.getInstance().info("skip filter:" + fileEntry.absolutePath());
						break;
					}
				}
			} else {
				needFilter = false;
			}
			if (!needFilter) {
				result.add(aEntry);
			}
		}
		return result;
	}

}
