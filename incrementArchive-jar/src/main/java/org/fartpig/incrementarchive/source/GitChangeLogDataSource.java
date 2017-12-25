package org.fartpig.incrementarchive.source;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.fartpig.incrementarchive.constant.GitConfig;
import org.fartpig.incrementarchive.constant.GlobalConfig;
import org.fartpig.incrementarchive.entity.ChangeLogEntry;
import org.fartpig.incrementarchive.entity.ChangeLogFileEntry;
import org.fartpig.incrementarchive.entity.ChangeLogManifest;
import org.fartpig.incrementarchive.util.ToolLogger;

public class GitChangeLogDataSource extends AbstractChangeLogDataSource implements ChangeLogDataSource {

	@Override
	public ChangeLogManifest loadChangeLog() {
		GlobalConfig config = GlobalConfig.instance();
		GitConfig gitConfig = config.getGitConfig();

		ChangeLogManifest manifest = new ChangeLogManifest();
		manifest.setLogType(typeEnum);

		Set<String> addFileSet = new HashSet<String>();
		Set<String> removeFileSet = new HashSet<String>();
		Set<String> modifyFileSet = new HashSet<String>();

		try {
			// now open the resulting repository with a FileRepositoryBuilder
			File repoDir = new File(gitConfig.getUrlPath());
			FileRepositoryBuilder builder = new FileRepositoryBuilder();
			Repository repository = builder.setGitDir(repoDir).readEnvironment().findGitDir().build();
			ToolLogger.getInstance().info("Having repository: " + repository.getDirectory());

			// the Ref holds an ObjectId for any type of object (tree, commit,
			// blob,
			// tree)
			String branchName = repository.getBranch();
			ToolLogger.getInstance().info("branchName: " + branchName);

			Git git = new Git(repository);
			String endVersion = null;
			if (gitConfig.getEndVersion() == null) {
				ObjectId head = repository.resolve(Constants.HEAD);
				endVersion = head.getName();
			} else {
				endVersion = gitConfig.getEndVersion();
			}
			List<DiffEntry> logEntries = listDiff(repository, git, gitConfig.getStartVersion(), endVersion);

			// 由于SVN 采用的按照原来到最终的模式，需要将中间的结果进行合并
			for (Iterator<DiffEntry> entries = logEntries.iterator(); entries.hasNext();) {
				DiffEntry logEntry = entries.next();

				ToolLogger.getInstance().info(logEntry.toString());
				ChangeType type = logEntry.getChangeType();
				String oldPath = logEntry.getOldPath();
				String newPath = logEntry.getNewPath();

				if (type == ChangeType.ADD) {
					ToolLogger.getInstance().info(String.format("file: %s status: C ", newPath));
					addFileSet.add(newPath);
				} else if (type == ChangeType.DELETE) {
					ToolLogger.getInstance().info(String.format("file: %s status: D ", oldPath));
					removeFileSet.add(oldPath);
				} else if (type == ChangeType.MODIFY) {
					ToolLogger.getInstance().info(String.format("file: %s status: M ", oldPath));
					modifyFileSet.add(oldPath);
				} else if (type == ChangeType.RENAME) {
					ToolLogger.getInstance().info(String.format("file: %s rename to %s ", oldPath, newPath));
					removeFileSet.add(oldPath);
					addFileSet.add(newPath);
				} else if (type == ChangeType.COPY) {
					ToolLogger.getInstance().info(String.format("file: %s copy to %s ", oldPath, newPath));
					addFileSet.add(newPath);
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

	private List<DiffEntry> listDiff(Repository repository, Git git, String oldCommit, String newCommit)
			throws GitAPIException, IOException {
		List<DiffEntry> diffs = git.diff().setOldTree(prepareTreeParser(repository, oldCommit))
				.setNewTree(prepareTreeParser(repository, newCommit)).call();
		ToolLogger.getInstance().info("Found: " + diffs.size() + " differences");
		return diffs;
	}

	private AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
		// from the commit we can build the tree which allows us to construct
		// the TreeParser
		// noinspection Duplicates
		RevWalk walk = new RevWalk(repository);
		RevCommit commit = walk.parseCommit(repository.resolve(objectId));
		RevTree tree = walk.parseTree(commit.getTree().getId());

		CanonicalTreeParser treeParser = new CanonicalTreeParser();
		ObjectReader reader = repository.newObjectReader();
		treeParser.reset(reader, tree.getId());

		walk.dispose();

		return treeParser;
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
