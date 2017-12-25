package org.fartpig.incrementarchive;

import org.apache.commons.io.FilenameUtils;
import org.fartpig.incrementarchive.constant.GlobalConst;
import org.fartpig.incrementarchive.util.ToolException;
import org.fartpig.incrementarchive.util.ToolLogger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public AppTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		ToolLogger.getInstance().setCurrentPhase(GlobalConst.PHASE_INIT_PARAMS);
	}

	public void testSvn() {
		String[] args = { "-zfn", "test.zip", "-sourceType", "change_log_svn", "-t", "output", "-name", "linfeng",
				"-password", "linfeng", "-prefixPath", "/23_源代码/ecifweb/ecifconsole", "-url",
				"https://192.32.1.47/svn/edcifwsseb/ecifconsole/",
				"-sv", "5420", "D:\\ecif\\ecifconsole",
				"D:\\ecif\\ecifconsole-target" };
		App.main(args);
	}

	public void tesGit() {
		String[] args = { "-zfn", "test.zip", "-sourceType", "change_log_git", "-t", "output", "-prefixPath", "/",
				"-url", "D:/cminfeng/.git", "-sv", "b3e098cb4433e76b43246a43afb5cb04a40acdba",
				"D:/cminfeng", "D:/cmis-daybat-linfeng-target" };
		App.main(args);
	}

	public void tesGit2() {
		String[] args = { "-zfn", "test.zip", "-sourceType", "change_log_git", "-t", "SOURCE", "-prefixPath", "/",
				"-url", "D:/workspace-my/incrementArchive/.git", "-sv", "52c66ee0bd27554de3b43989286c3da75d936c4e",
				"D:/workspace-my/incrementArchive/", "D:/workspace-my/incrementArchive-target" };
		App.main(args);
	}

	public void testAppText() {
		String[] args = { "-zfn", "test.zip", "-st", "change_log_txt", "-t", "output", "-s",
				"D:\\workspace-my\\incrementArchive\\incrementArchive-jar\\target\\test-classes\\file.txt", "D:\\",
				"D:\\incrementArchive-target" };
		App.main(args);
	}

	public void testAppFjcmisText() {
		String[] args = { "-zfn", "fjcmis.zip", "-st", "change_log_txt", "-t", "output", "-s",
				"D:\\workspace-my\\incrementArchive\\incrementArchive-jar\\target\\test-classes\\fjcmis-file.txt",
				"D:\\", "D:\\fjcmis-target" };
		App.main(args);
	}

	public void testAppXml() {
		String[] args = { "-zfn", "test.zip", "-st", "change_log_xml", "-t", "output", "-s",
				"D:\\workspace-my\\incrementArchive\\incrementArchive-jar\\target\\test-classes\\file.xml",
				"D:\\workspace-my\\incrementArchive\\incrementArchive-jar\\target\\classes",
				"D:\\workspace-my\\incrementArchive\\target\\incrementArchive-target" };
		App.main(args);
	}

	public void testAppXmlForSrc() {
		String[] args = { "-exts", "jsp,java, properties ", "-zfn", "test.zip", "-st", "change_log_xml", "-t", "output",
				"-s", "D:\\workspace-my\\incrementArchive\\incrementArchive-jar\\target\\test-classes\\file-src.xml",
				"D:\\workspace-my\\incrementArchive\\incrementArchive-jar",
				"D:\\workspace-my\\incrementArchive\\target\\incrementArchive-target" };
		App.main(args);
	}

	public void testArgsResolvel() {
		String[] args = { "D:\\workspace-my\\incrementArchive\\incrementArchive-jar\\target\\classes",
				"D:\\workspace-my\\incrementArchive\\target\\incrementArchive-target" };
		App.argsResolve(args);
	}

	public void testAppArgs1() {
		String[] args = { "-zfn", "test.zip", "-st", "change_log_txt", "-t", "output", "-s",
				"D:\\workspace-my\\incrementArchive\\incrementArchive-jar\\target\\test-classes\\file.txt",
				"D:\\workspace-my\\incrementArchive\\incrementArchive-jar\\target\\classes",
				"D:\\workspace-my\\incrementArchive\\target\\incrementArchive-target" };
		App.argsResolve(args);
	}

	public void testAppArgs2() {
		try {
			String[] args = { "fda", "fdaxs", "-st" };
			App.argsResolve(args);
			fail("Expected a ToolException to be throw");
		} catch (ToolException e) {
			e.printStackTrace();
			assertEquals(GlobalConst.PHASE_INIT_PARAMS, e.getPhase());
			assertEquals("please set the right args", e.getMessage());
		}
	}

	public void testAppArgs3() {
		try {
			String[] args = { "fda", "fdaxs", "-zfn" };
			App.argsResolve(args);
			fail("Expected a ToolException to be throw");
		} catch (ToolException e) {
			e.printStackTrace();
			assertEquals(GlobalConst.PHASE_INIT_PARAMS, e.getPhase());
			assertEquals("please set the right args", e.getMessage());
		}
	}

	public void testAppArgs4() {
		try {
			String[] args = { "fda", "fdaxs", "-t" };
			App.argsResolve(args);
			fail("Expected a ToolException to be throw");
		} catch (ToolException e) {
			e.printStackTrace();
			assertEquals(GlobalConst.PHASE_INIT_PARAMS, e.getPhase());
			assertEquals("please set the right args", e.getMessage());
		}
	}

	public void testAppArgs5() {
		try {
			String[] args = { "fda", "fdaxs", "-s" };
			App.argsResolve(args);
			fail("Expected a ToolException to be throw");
		} catch (ToolException e) {
			e.printStackTrace();
			assertEquals(GlobalConst.PHASE_INIT_PARAMS, e.getPhase());
			assertEquals("please set the right args", e.getMessage());
		}
	}

	public void testAppArgs6() {
		try {
			String[] args = { "-zfn", "test.zip", "-st", "change_log_txt", "-t", "output", "-s",
					"D:\\workspace-my\\incrementArchive\\incrementArchive-jar\\target\\test-classes\\file.txt",
					"D:\\workspace-my\\incrementArchive\\incrementArchive-jar\\target\\classes" };
			App.argsResolve(args);
			fail("Expected a ToolException to be throw");
		} catch (ToolException e) {
			e.printStackTrace();
			assertEquals(GlobalConst.PHASE_INIT_PARAMS, e.getPhase());
			assertEquals("please set the intputDir and outputDir", e.getMessage());
		}
	}

	public void testAppArgs7() {
		try {
			String[] args = { "-zfn", "test.zip", "-st", "change_log_txt", "-t", "output", "-s",
					"D:\\workspace-my\\incrementArchive\\incrementArchive-jar\\target\\test-classes\\file.txt" };
			App.argsResolve(args);
			fail("Expected a ToolException to be throw");
		} catch (ToolException e) {
			e.printStackTrace();
			assertEquals(GlobalConst.PHASE_INIT_PARAMS, e.getPhase());
			assertEquals("please set the intputDir and outputDir", e.getMessage());
		}
	}

	public void testAppArgs8() {
		try {
			String[] args = { "-zfn", "test.zip", "-st", "change_log_txt", "-t", "output", "-s",
					"D:\\workspace-my\\incrementArchive\\incrementArchive-jar\\target\\test-classes\\file.txt", "-fdsa",
					"-vc" };
			App.argsResolve(args);
			fail("Expected a ToolException to be throw");
		} catch (ToolException e) {
			e.printStackTrace();
			assertEquals(GlobalConst.PHASE_INIT_PARAMS, e.getPhase());
			assertEquals("please set the right args", e.getMessage());
		}
	}

	public void testAppArgs9() {
		try {
			String[] args = { "-exts", "xml,jsp", "-st", "change_log_txt", "-t", "output", "-s",
					"D:\\workspace-my\\incrementArchive\\incrementArchive-jar\\target\\test-classes\\file.txt", "-fdsa",
					"-vc" };
			App.argsResolve(args);
			fail("Expected a ToolException to be throw");
		} catch (ToolException e) {
			e.printStackTrace();
			assertEquals(GlobalConst.PHASE_INIT_PARAMS, e.getPhase());
			assertEquals("please set the right args", e.getMessage());
		}
	}

	public void testAppArgs10() {
		String[] args = { "-exts", "xml,jsp", "D:\\clinfeng", "D:\\clinfeng-target" };
		App.argsResolve(args);
	}

	public void testFileNameUtil() {
		System.out.println(FilenameUtils.wildcardMatch("/bbs/java/src/com/do/Back.java", "*/java/src/*"));
	}

}
