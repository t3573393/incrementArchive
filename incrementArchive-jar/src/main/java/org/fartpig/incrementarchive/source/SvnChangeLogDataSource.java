package org.fartpig.incrementarchive.source;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fartpig.incrementarchive.constant.GlobalConfig;
import org.fartpig.incrementarchive.constant.SvnConfig;
import org.fartpig.incrementarchive.entity.ChangeLogEntry;
import org.fartpig.incrementarchive.entity.ChangeLogFileEntry;
import org.fartpig.incrementarchive.entity.ChangeLogManifest;
import org.fartpig.incrementarchive.util.ToolLogger;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

public class SvnChangeLogDataSource extends AbstractChangeLogDataSource implements ChangeLogDataSource {

	@Override
	public ChangeLogManifest loadChangeLog() {
		GlobalConfig config = GlobalConfig.instance();
		SvnConfig svnConfig = config.getSvnConfig();

		ChangeLogManifest manifest = new ChangeLogManifest();
		manifest.setLogType(typeEnum);

		// example: <files type="SOURCE"><set name="add" relative="true"><file
		// relative="false">/fdas/fdas</file></set></files>

		DAVRepositoryFactory.setup();

		String url = svnConfig.getUrlPath();
		String name = svnConfig.getName();
		String password = svnConfig.getPassword();
		long startRevision = svnConfig.getStartVersion();
		long endRevision = svnConfig.getEndVersion();

		Map<String, String> globalFileStatus = new HashMap<String, String>();
		Set<String> addFileSet = new HashSet<String>();
		Set<String> removeFileSet = new HashSet<String>();
		Set<String> modifyFileSet = new HashSet<String>();

		SVNRepository repository = null;
		try {
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(name,
					password.toCharArray());
			repository.setAuthenticationManager(authManager);

			if (endRevision == -1) {
				endRevision = repository.getLatestRevision();
			}

			@SuppressWarnings("unchecked")
			Collection<SVNLogEntry> logEntries = repository.log(new String[] { "" }, null, startRevision, endRevision,
					true, true);

			// 由于SVN 采用的按照原来到最终的模式，需要将中间的结果进行合并
			for (Iterator<SVNLogEntry> entries = logEntries.iterator(); entries.hasNext();) {
				SVNLogEntry logEntry = entries.next();
				Map<String, SVNLogEntryPath> data = logEntry.getChangedPaths();

				ToolLogger.getInstance()
						.info(String.format("revision: %d, date %s", logEntry.getRevision(), logEntry.getDate()));
				for (SVNLogEntryPath aEntry : data.values()) {
					ToolLogger.getInstance().info(aEntry.toString());
					char type = aEntry.getType();
					String filePath = aEntry.getPath();
					// 处理复杂的历史文件状态情况
					if (type == SVNLogEntryPath.TYPE_ADDED) {
						if (!globalFileStatus.containsKey(filePath)) {
							addFileSet.add(filePath);
							globalFileStatus.put(filePath, "C");
							ToolLogger.getInstance().info(String.format("file: %s status: C ", filePath));
						} else {
							String oldStatus = globalFileStatus.get(filePath);
							if (oldStatus.equals("D")) {
								addFileSet.add(filePath);
								removeFileSet.remove(filePath);
								globalFileStatus.put(filePath, "C");
								ToolLogger.getInstance().info(String.format(
										"file: %s, move from old status: %s to new status: C ", filePath, oldStatus));
							} else {
								ToolLogger.getInstance().error(String
										.format("file: %s, fail old status: %s, new status: C ", filePath, oldStatus));
							}
						}
					} else if (type == SVNLogEntryPath.TYPE_DELETED) {
						if (!globalFileStatus.containsKey(filePath)) {
							removeFileSet.add(filePath);
							globalFileStatus.put(filePath, "D");
							ToolLogger.getInstance().info(String.format("file: %s status: D ", filePath));
						} else {
							String oldStatus = globalFileStatus.get(filePath);
							if (oldStatus.equals("C")) {
								removeFileSet.add(filePath);
								addFileSet.remove(filePath);
								globalFileStatus.put(filePath, "D");
								ToolLogger.getInstance().info(String.format(
										"file: %s, move from old status: %s to new status: D ", filePath, oldStatus));
							} else if (oldStatus.equals("M")) {
								removeFileSet.add(filePath);
								modifyFileSet.remove(filePath);
								globalFileStatus.put(filePath, "D");
								ToolLogger.getInstance().info(String.format(
										"file: %s, move from old status: %s to new status: D ", filePath, oldStatus));
							} else {
								ToolLogger.getInstance().error(String
										.format("file: %s, fail old status: %s, new status: D ", filePath, oldStatus));
							}
						}

					} else if (type == SVNLogEntryPath.TYPE_MODIFIED || type == SVNLogEntryPath.TYPE_REPLACED) {
						if (!globalFileStatus.containsKey(filePath)) {
							modifyFileSet.add(filePath);
							globalFileStatus.put(filePath, "M");
							ToolLogger.getInstance().info(String.format("file: %s status: M ", filePath));
						} else {
							String oldStatus = globalFileStatus.get(filePath);
							if (oldStatus.equals("C")) {
								globalFileStatus.put(filePath, "C");
								ToolLogger.getInstance()
										.info(String.format("file: %s, keep old status: %s ", filePath, oldStatus));
							} else if (oldStatus.equals("M")) {
								globalFileStatus.put(filePath, "M");
								ToolLogger.getInstance().info(String.format(
										"file: %s, move from old status: %s to new status: M ", filePath, oldStatus));
							} else {
								ToolLogger.getInstance().error(String
										.format("file: %s, fail old status: %s, new status: M ", filePath, oldStatus));
							}
						}
					}
				}
			}
		} catch (Exception e) {
			ToolLogger.getInstance().error("error", e);
		}
		// 将聚集的结果，添加到结果集合中
		Set<String> fileNameSet = new HashSet<String>();
		addFileNamesToLogEntry(addFileSet, fileNameSet, config, manifest.getAddEntries());
		addFileNamesToLogEntry(removeFileSet, fileNameSet, config, manifest.getRemoveEntries());
		addFileNamesToLogEntry(modifyFileSet, fileNameSet, config, manifest.getModifyEntries());

		return manifest;
	}

	private void addFileNamesToLogEntry(Set<String> fileNames, Set<String> fileNameSet, GlobalConfig config,
			List<ChangeLogEntry> targetEntries) {
		for (String fileName : fileNames) {
			// remove the prefix path
			String prefixPath = config.getPrefixPath();
			if (prefixPath != null && fileName.startsWith(prefixPath)) {
				fileName = fileName.substring(prefixPath.length());
			}

			List<ChangeLogEntry> entries = convertFromFileNameToChangeLogEntry(true, fileName, config);
			for (ChangeLogEntry logEntry : entries) {
				if (fileNameSet.contains(logEntry.absolutePath())) {
					ToolLogger.getInstance()
							.info("duplicate file name:" + fileName + "- absolute name:" + logEntry.absolutePath());
					continue;
				}

				// only handle files
				if (logEntry instanceof ChangeLogFileEntry) {
					fileNameSet.add(logEntry.absolutePath());
					targetEntries.add(logEntry);
				}
			}
		}
	}

}
