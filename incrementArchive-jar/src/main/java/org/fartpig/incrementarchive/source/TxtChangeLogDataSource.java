package org.fartpig.incrementarchive.source;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.fartpig.incrementarchive.constant.GlobalConfig;
import org.fartpig.incrementarchive.entity.ChangeLogEntry;
import org.fartpig.incrementarchive.entity.ChangeLogManifest;
import org.fartpig.incrementarchive.util.ToolException;
import org.fartpig.incrementarchive.util.ToolLogger;

public class TxtChangeLogDataSource extends AbstractChangeLogDataSource implements ChangeLogDataSource {

	@Override
	public ChangeLogManifest loadChangeLog() {
		GlobalConfig config = GlobalConfig.instance();
		String sourceFilePath = config.getChangeLogSourceFile();
		ChangeLogManifest manifest = new ChangeLogManifest();
		manifest.setLogType(typeEnum);
		try {
			manifest.setLogType(config.getTypeEnum());
			LineIterator iterator = FileUtils.lineIterator(new File(sourceFilePath), "UTF-8");
			// when the text just use the modify set
			while (iterator.hasNext()) {
				String line = iterator.nextLine();
				ToolLogger.getInstance().info("load from file:" + line);
				List<ChangeLogEntry> entries = convertFromFileNameToChangeLogEntry(true, line, config);
				manifest.getModifyEntries().addAll(entries);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ToolLogger.getInstance().error("error:", e);

			throw new ToolException(ToolLogger.getInstance().getCurrentPhase(), "loadChangeLog error");
		}
		return manifest;
	}

}
