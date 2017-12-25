package org.fartpig.incrementarchive.phase;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.fartpig.incrementarchive.constant.GlobalConfig;
import org.fartpig.incrementarchive.constant.GlobalConst;
import org.fartpig.incrementarchive.entity.ChangeLogEntry;
import org.fartpig.incrementarchive.entity.ChangeLogFileEntry;
import org.fartpig.incrementarchive.entity.ChangeLogManifest;
import org.fartpig.incrementarchive.entity.ChangeLogPathEntry;
import org.fartpig.incrementarchive.util.PathUtil;
import org.fartpig.incrementarchive.util.ToolLogger;

public class FileTransferAction extends AbstractFileOpAction {

	private static final String CURRENT_PHASE = GlobalConst.PHASE_INCREMENT_OUTPUT;

	public FileTransferAction() {
		ToolLogger.getInstance().setCurrentPhase(CURRENT_PHASE);
	}

	public void transferFiles(String inputDir, String outputDir, ChangeLogManifest manifest, GlobalConfig config) {
		// 根据change log清单，按照规则进行复制
		// filter the output file by the extensions
		String[] extensions = config.getExtensions();
		List<ChangeLogEntry> addEntries = manifest.getAddEntries();
		List<ChangeLogEntry> modifyEntries = manifest.getModifyEntries();
		List<ChangeLogEntry> removeEntries = manifest.getRemoveEntries();

		if (config.isFilterExtension()) {
			addEntries = filterEntryByExtensions(addEntries, extensions);
			modifyEntries = filterEntryByExtensions(modifyEntries, extensions);
			removeEntries = filterEntryByExtensions(removeEntries, extensions);
		}

		ToolLogger.getInstance().info("handle addEntries size:" + addEntries.size());
		for (ChangeLogEntry entry : addEntries) {
			if (entry instanceof ChangeLogFileEntry) {
				handleFile(GlobalConst.SET_ADD, (ChangeLogFileEntry) entry, inputDir, outputDir);
			} else if (entry instanceof ChangeLogPathEntry) {
				handlePath(GlobalConst.SET_ADD, (ChangeLogPathEntry) entry, inputDir, outputDir);
			}
		}

		ToolLogger.getInstance().info("handle removeEntries size:" + removeEntries.size());
		for (ChangeLogEntry entry : removeEntries) {
			if (entry instanceof ChangeLogFileEntry) {
				handleFile(GlobalConst.SET_REMOVE, (ChangeLogFileEntry) entry, inputDir, outputDir);
			} else if (entry instanceof ChangeLogPathEntry) {
				handlePath(GlobalConst.SET_REMOVE, (ChangeLogPathEntry) entry, inputDir, outputDir);
			}
		}

		ToolLogger.getInstance().info("handle modifyEntries size:" + modifyEntries.size());
		for (ChangeLogEntry entry : modifyEntries) {
			if (entry instanceof ChangeLogFileEntry) {
				handleFile(GlobalConst.SET_MODIFY, (ChangeLogFileEntry) entry, inputDir, outputDir);
			} else if (entry instanceof ChangeLogPathEntry) {
				handlePath(GlobalConst.SET_MODIFY, (ChangeLogPathEntry) entry, inputDir, outputDir);
			}
		}
	}

	private void handleFile(String opType, ChangeLogFileEntry srfFileEntry, String inputDir, String outputDir) {

		List<ChangeLogFileEntry> tempFielEntries = addExtraEntryForClass(srfFileEntry);

		for (ChangeLogFileEntry fileEntry : tempFielEntries) {
			String relativePath = PathUtil.getRelativePath(fileEntry.absolutePath(), inputDir);
			ToolLogger.getInstance().info("relativePath:" + relativePath);
			String targetPath = String.format("%s%s", outputDir, relativePath);
			ToolLogger.getInstance().info("targetPath:" + targetPath);

			try {
				if (GlobalConst.SET_ADD.equals(opType)) {
					ToolLogger.getInstance().info("copyFile from:" + fileEntry.absolutePath() + "- to:" + targetPath);
					FileUtils.copyFile(new File(fileEntry.absolutePath()), new File(targetPath));
				} else if (GlobalConst.SET_REMOVE.equals(opType)) {
					ToolLogger.getInstance().info("deleteFile :" + targetPath);
					FileUtils.deleteQuietly(new File(targetPath));
				} else if (GlobalConst.SET_MODIFY.equals(opType)) {
					ToolLogger.getInstance().info("modifyFile :" + targetPath);
					FileUtils.deleteQuietly(new File(targetPath));
					ToolLogger.getInstance().info("copyFile from:" + fileEntry.absolutePath() + "- to:" + targetPath);
					FileUtils.copyFile(new File(fileEntry.absolutePath()), new File(targetPath));
				}
			} catch (IOException e) {
				// e.printStackTrace();
				ToolLogger.getInstance()
						.error("operation file error from:" + fileEntry.absolutePath() + "- to:" + targetPath);
			}
		}
	}

	private void handlePath(String opType, ChangeLogPathEntry pathEntry, String inputDir, String outputDir) {
		String relativePath = PathUtil.getRelativePath(pathEntry, inputDir);
		ToolLogger.getInstance().info("relativePath:" + relativePath);
		String targetPath = String.format("%s%s", outputDir, relativePath);
		ToolLogger.getInstance().info("targetPath:" + targetPath);

		try {
			if (GlobalConst.SET_ADD.equals(opType)) {
				ToolLogger.getInstance().info("copyDirectory from:" + pathEntry.absolutePath() + "- to:" + targetPath);
				FileUtils.copyDirectory(new File(pathEntry.absolutePath()), new File(targetPath));
			} else if (GlobalConst.SET_REMOVE.equals(opType)) {
				ToolLogger.getInstance().info("deleteDirectory :" + targetPath);
				FileUtils.deleteDirectory(new File(targetPath));
			} else if (GlobalConst.SET_MODIFY.equals(opType)) {
				ToolLogger.getInstance().info("deleteDirectory :" + targetPath);
				FileUtils.deleteDirectory(new File(targetPath));
				ToolLogger.getInstance().info("copyDirectory from:" + pathEntry.absolutePath() + "- to:" + targetPath);
				FileUtils.copyDirectory(new File(pathEntry.absolutePath()), new File(targetPath));
			}
		} catch (IOException e) {
			e.printStackTrace();
			ToolLogger.getInstance().error("error:", e);
		}
	}
}
