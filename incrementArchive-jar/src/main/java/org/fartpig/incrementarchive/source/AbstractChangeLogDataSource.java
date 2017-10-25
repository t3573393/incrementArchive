package org.fartpig.incrementarchive.source;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.fartpig.incrementarchive.constant.ChangeLogTypeEnum;
import org.fartpig.incrementarchive.constant.GlobalConfig;
import org.fartpig.incrementarchive.entity.ChangeLogEntry;
import org.fartpig.incrementarchive.entity.ChangeLogFileEntry;
import org.fartpig.incrementarchive.entity.ChangeLogPathEntry;
import org.fartpig.incrementarchive.util.ToolLogger;

public abstract class AbstractChangeLogDataSource {

	protected ChangeLogTypeEnum typeEnum;

	public AbstractChangeLogDataSource() {
	}

	public void setChangeLogTypeEnum(ChangeLogTypeEnum typeEnum) {
		this.typeEnum = typeEnum;
	}

	protected List<ChangeLogEntry> convertFromFileNameToChangeLogEntry(boolean isRelative, String fileName,
			GlobalConfig config) {
		String fileInputName = FilenameUtils.separatorsToSystem(fileName);
		String filePath = null;
		if (isRelative) {
			filePath = String.format("%s%s%s", config.getInputDir(), File.separator, fileName);
			filePath = FilenameUtils.normalize(filePath);
			// remove the fist separator for support the relative format
			if (fileInputName.startsWith(File.separator)) {
				fileInputName = fileInputName.substring(1);
			}
		} else {
			filePath = FilenameUtils.normalize(fileName);
		}

		File temp = new File(filePath);
		List<ChangeLogEntry> entries = new ArrayList<ChangeLogEntry>();
		if (temp.isDirectory()) {
			ChangeLogPathEntry pathEntry = new ChangeLogPathEntry();
			pathEntry.setFileInputName(fileInputName);
			pathEntry.setFilePath(filePath);
			pathEntry.setRelative(isRelative);

			ToolLogger.getInstance().info("add path entry:" + pathEntry);
			entries.add(pathEntry);
		} else {

			ChangeLogFileEntry fileEntry = new ChangeLogFileEntry();
			fileEntry.setFileInputName(fileInputName);
			String baseName = FilenameUtils.getBaseName(filePath);
			String fileEx = FilenameUtils.getExtension(filePath);
			fileEntry.setFileBaseName(baseName);
			fileEntry.setFileEx(fileEx);
			fileEntry.setFilePath(temp.getParent());
			fileEntry.setRelative(isRelative);

			ToolLogger.getInstance().info("add file entry:" + fileEntry);
			entries.add(fileEntry);

		}

		return entries;
	}

}
