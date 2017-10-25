package org.fartpig.incrementarchive.maven;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

public class IncrementArchiveMojoTest extends AbstractMojoTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testAppPom() throws Exception {
		File pom = getTestFile("src/test/resources/test-pom.xml");
		assertNotNull(pom);
		assertTrue(pom.exists());

		IncrementArchiveMojo mojo = (IncrementArchiveMojo) lookupMojo("incrementArchive", pom);
		assertNotNull(mojo);
		mojo.execute();

	}

	public void testAppExtensionPom() throws Exception {
		File pom = getTestFile("src/test/resources/test-extensions-pom.xml");
		assertNotNull(pom);
		assertTrue(pom.exists());

		IncrementArchiveMojo mojo = (IncrementArchiveMojo) lookupMojo("incrementArchive", pom);
		assertNotNull(mojo);
		mojo.execute();

	}

	public void testAppSvnPom() throws Exception {
		File pom = getTestFile("src/test/resources/test-svn-pom.xml");
		assertNotNull(pom);
		assertTrue(pom.exists());

		IncrementArchiveMojo mojo = (IncrementArchiveMojo) lookupMojo("incrementArchive", pom);
		assertNotNull(mojo);
		mojo.execute();

	}

	public void testAppGitPom() throws Exception {
		File pom = getTestFile("src/test/resources/test-git-pom.xml");
		assertNotNull(pom);
		assertTrue(pom.exists());

		IncrementArchiveMojo mojo = (IncrementArchiveMojo) lookupMojo("incrementArchive", pom);
		assertNotNull(mojo);
		mojo.execute();

	}

	public void testAppGitPomWithFileMapping() throws Exception {
		File pom = getTestFile("src/test/resources/test-git-file-mapping-pom.xml");
		assertNotNull(pom);
		assertTrue(pom.exists());

		IncrementArchiveMojo mojo = (IncrementArchiveMojo) lookupMojo("incrementArchive", pom);
		assertNotNull(mojo);
		mojo.execute();

	}
}
