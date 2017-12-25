package org.fartpig.incrementarchive.phase;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.fartpig.incrementarchive.constant.GlobalConst;
import org.fartpig.incrementarchive.entity.ChangeLogEntry;
import org.fartpig.incrementarchive.entity.ChangeLogManifest;
import org.fartpig.incrementarchive.util.FolderZipUtil;
import org.fartpig.incrementarchive.util.PathUtil;
import org.fartpig.incrementarchive.util.ToolException;
import org.fartpig.incrementarchive.util.ToolLogger;

public class IncrementArchiveAction {

	private static final String CURRENT_PHASE = GlobalConst.PHASE_ASSEMBLE_OUTPUT;

	public IncrementArchiveAction() {
		ToolLogger.getInstance().setCurrentPhase(CURRENT_PHASE);
	}

	public void incrementArchiveToPath(String inputPath, String targetPath, String zipFileName,
			ChangeLogManifest manifest) {
		// 增量包输出(修改和新增的文件输出打包，was删除文件清单)
		List<ChangeLogEntry> removeEntries = manifest.getRemoveEntries();
		if (removeEntries.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (ChangeLogEntry aEntry : removeEntries) {
				String relativePath = PathUtil.getRelativePath(aEntry, inputPath);
				relativePath = FilenameUtils.separatorsToUnix(relativePath);
				if (relativePath.charAt(0) == '/') {
					relativePath = relativePath.substring(1);
				}
				ToolLogger.getInstance().info("add delete path to props:" + relativePath);
				sb.append(relativePath);
				sb.append(GlobalConst.UNIX_LINE_SEPARATOR);
			}
			String deleteProps = String.format("%s%s", targetPath, GlobalConst.IBM_DELETE_PROPS_NAME);
			try {
				FileUtils.write(new File(deleteProps), sb.toString(), "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
				ToolLogger.getInstance().error("error:", e);
			}
		}
		File targetFile = new File(targetPath);
		String destZipFile = String.format("%s%s%s", targetFile.getParent(), File.separator, zipFileName);
		try {
			File destFile = new File(destZipFile);
			if (destFile.exists()) {
				destFile.delete();
			}
			ToolLogger.getInstance().info("zip src folder:" + targetPath + " - destZipFile:" + destZipFile);
			FolderZipUtil.zipFolder(targetPath, destZipFile, false);
		} catch (Exception e) {
			e.printStackTrace();
			ToolLogger.getInstance().error("error:", e);
			throw new ToolException(ToolLogger.getInstance().getCurrentPhase(), "error");
		}
	}
}
