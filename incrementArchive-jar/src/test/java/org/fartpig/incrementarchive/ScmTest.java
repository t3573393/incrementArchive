package org.fartpig.incrementarchive;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.ReflogEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import junit.framework.TestCase;

public class ScmTest extends TestCase {

	public void testSvn() {
		DAVRepositoryFactory.setup();

		String url = "https://192.XX.1.57/svn/ECIFFDS/";
		String name = "linfeng";
		String password = "linfeng";
		long startRevision = 4995;
		long endRevision = 1000002; // HEAD (the latest) revision

		SVNRepository repository = null;
		try {
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(name,
					password.toCharArray());
			repository.setAuthenticationManager(authManager);

			endRevision = repository.getLatestRevision();

			Collection<SVNLogEntry> logEntries = repository.log(new String[] { "" }, null, startRevision, endRevision,
					true, true);

			for (Iterator<SVNLogEntry> entries = logEntries.iterator(); entries.hasNext();) {
				SVNLogEntry logEntry = entries.next();
				Map<String, SVNLogEntryPath> data = logEntry.getChangedPaths();
				System.out.println(String.format("revision: %d, date %s", logEntry.getRevision(), logEntry.getDate()));
				for (Map.Entry<String, SVNLogEntryPath> aEntry : data.entrySet()) {
					System.out.println(String.format("key: %s, data %s", aEntry.getKey(), aEntry.getValue()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void testGit() throws IOException, GitAPIException {
		// now open the resulting repository with a FileRepositoryBuilder
		File repoDir = new File("D:/cmt-linfeng/.git");
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		Repository repository = builder.setGitDir(repoDir).readEnvironment().findGitDir().build();
		System.out.println("Having repository: " + repository.getDirectory());

		// the Ref holds an ObjectId for any type of object (tree, commit, blob,
		// tree)
		String branchName = repository.getBranch();
		Git git = new Git(repository);
		List<Ref> refs = git.branchList().call();
		for (Ref ref : refs) {
			System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());

			listReflog(git, ref);
		}

		List<Ref> call = git.tagList().call();
		for (Ref ref : call) {
			System.out.println("Tag: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());

			listReflog(git, ref);
		}

		System.out.println("branchName: " + branchName);
		Map<String, Ref> refMaps = repository.getAllRefs();

		ObjectId head = repository.resolve(Constants.HEAD);

		List<Ref> allRefs = git.branchList().call();
		for (Ref aRef : allRefs) {
			System.out.println(aRef.getName() + ":" + aRef.getObjectId().getName());
			listDiff(repository, git, "b3e098cb4433e76b43246a43afb5cb04a40acdba", head.getName());
			git.reflog().setRef(aRef.getName()).call();
		}
	}

	private static void listReflog(Git git, Ref ref) throws GitAPIException {
		/*
		 * Ref head = repository.getRef(ref.getName()); RevWalk walk = new
		 * RevWalk(repository); RevCommit commit =
		 * walk.parseCommit(head.getObjectId());
		 */

		Collection<ReflogEntry> call = git.reflog().setRef(ref.getName()).call();
		for (ReflogEntry reflog : call) {
			System.out.println("Reflog: " + reflog);
		}
	}

	private void listDiff(Repository repository, Git git, String oldCommit, String newCommit)
			throws GitAPIException, IOException {
		final List<DiffEntry> diffs = git.diff().setOldTree(prepareTreeParser(repository, oldCommit))
				.setNewTree(prepareTreeParser(repository, newCommit)).call();

		System.out.println("Found: " + diffs.size() + " differences");
		for (DiffEntry diff : diffs) {
			System.out.println("Diff: " + diff.getChangeType() + ": " + (diff.getOldPath().equals(diff.getNewPath())
					? diff.getNewPath() : diff.getOldPath() + " -> " + diff.getNewPath()));
		}
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
}
