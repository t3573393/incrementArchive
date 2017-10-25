package org.fartpig.incrementarchive.phase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fartpig.incrementarchive.constant.GlobalConfig;
import org.fartpig.incrementarchive.constant.GlobalConst;
import org.fartpig.incrementarchive.entity.FileMappingMetaInfo;
import org.fartpig.incrementarchive.entity.OrderedProperties;
import org.fartpig.incrementarchive.util.ToolLogger;

public class FileMappingMetaCreateAction {

	private static final String CURRENT_PHASE = GlobalConst.PHASE_FILE_MAPPING;

	public FileMappingMetaCreateAction() {
		ToolLogger.getInstance().setCurrentPhase(CURRENT_PHASE);
	}

	public List<FileMappingMetaInfo> createFileMappingMetaInfos() {
		List<FileMappingMetaInfo> fileMappingMetaInfos = new ArrayList<FileMappingMetaInfo>();

		GlobalConfig config = GlobalConfig.getGlobalConfig();
		OrderedProperties properties = config.getFileMapping();

		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			String srcPath = (String) entry.getKey();
			String targetPath = (String) entry.getValue();

			FileMappingMetaInfo mapping = new FileMappingMetaInfo();

			mapping.setSrcDir(isPath(srcPath));
			mapping.setSrcPath(srcPath);
			mapping.setTargetDir(isPath(targetPath));
			mapping.setTargetPath(targetPath);
			fileMappingMetaInfos.add(mapping);

			ToolLogger.getInstance().info(String.format("file mapping from %s to %s ", srcPath, targetPath));
		}

		return fileMappingMetaInfos;
	}

	private boolean isPath(String filePath) {
		return filePath.endsWith("/");
	}

}
