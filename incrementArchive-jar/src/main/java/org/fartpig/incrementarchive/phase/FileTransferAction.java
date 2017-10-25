package org.fartpig.incrementarchive.phase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.fartpig.incrementarchive.constant.GlobalConfig;
import org.fartpig.incrementarchive.constant.GlobalConst;
import org.fartpig.incrementarchive.entity.ChangeLogEntry;
import org.fartpig.incrementarchive.entity.ChangeLogFileEntry;
import org.fartpig.incrementarchive.entity.ChangeLogManifest;
import org.fartpig.incrementarchive.entity.ChangeLogPathEntry;
import org.fartpig.incrementarchive.util.PathUtil;
import org.fartpig.incrementarchive.util.ToolLogger;

public class FileTransferAction {

	private static final String CURRENT_PHASE = GlobalConst.PHASE_INCREMENT_OUTPUT;

	private Pattern classNamePattern = null;

	public FileTransferAction() {
		ToolLogger.getInstance().setCurrentPhase(CURRENT_PHASE);
		classNamePattern = Pattern.compile("(\\S+)[$]\\d");
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

	private List<ChangeLogFileEntry> addExtraEntryForClass(ChangeLogFileEntry fileEntry) {
		List<ChangeLogFileEntry> tempFileEntries = new ArrayList<ChangeLogFileEntry>();
		tempFileEntries.add(fileEntry);

		// handle the special class entry : ${name}$1.class
		String fileEx = fileEntry.getFileEx();
		String baseName = fileEntry.getFileBaseName();
		if ("class".equals(fileEx)) {
			Matcher matcher = classNamePattern.matcher(baseName);
			String classBaseName = baseName;
			if (matcher.find()) {
				classBaseName = matcher.group(0);
			}

			// try find the other class file: max to the 20
			for (int i = 1; i <= 20; i++) {
				String tempClassName = String.format("%s$%d", classBaseName, i);

				ChangeLogFileEntry tempfileEntry = new ChangeLogFileEntry();
				tempfileEntry.setFileInputName(fileEntry.getFileInputName());
				tempfileEntry.setFileBaseName(tempClassName);
				tempfileEntry.setFileEx(fileEx);
				tempfileEntry.setFilePath(fileEntry.getFilePath());
				tempfileEntry.setRelative(fileEntry.isRelative());
				tempfileEntry.setFileMapping(fileEntry.isFileMapping());

				File tempPath = new File(tempfileEntry.absolutePath());
				// no 1 , no later ones
				if (tempPath.exists()) {
					tempFileEntries.add(tempfileEntry);
				} else {
					break;
				}
			}
		}

		return tempFileEntries;
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
				e.printStackTrace();
				ToolLogger.getInstance().error("error:", e);
			}
		}
	}

	private List<ChangeLogEntry> filterEntryByExtensions(List<ChangeLogEntry> entries, String[] extensions) {

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
