package org.fartpig.incrementarchive;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOCase;
import org.fartpig.incrementarchive.phase.SourceMapToOutputAction;

import junit.framework.TestCase;

public class SourceMapToOutputActionTest extends TestCase {

	public void testMyWildcardMatch1() {
		List<SourceMapToOutputAction.MatchGroup> matchGroups = new ArrayList<SourceMapToOutputAction.MatchGroup>();
		SourceMapToOutputAction action = new SourceMapToOutputAction();
		action.myWildcardMatch("src/main/java/test/com.java", "*/main/java/*.java", IOCase.SENSITIVE, matchGroups);

		for (SourceMapToOutputAction.MatchGroup aGroup : matchGroups) {
			System.out.println(aGroup);
		}
	}

	public void testMyWildcardMatch2() {
		List<SourceMapToOutputAction.MatchGroup> matchGroups = new ArrayList<SourceMapToOutputAction.MatchGroup>();
		SourceMapToOutputAction action = new SourceMapToOutputAction();
		action.myWildcardMatch("src/main/java/test/com.java.cc.java", "*/main/java/*.java", IOCase.SENSITIVE,
				matchGroups);
		for (SourceMapToOutputAction.MatchGroup aGroup : matchGroups) {
			System.out.println(aGroup);
		}
	}

	public void testMyWildcardMatch3() {
		List<SourceMapToOutputAction.MatchGroup> matchGroups = new ArrayList<SourceMapToOutputAction.MatchGroup>();
		SourceMapToOutputAction action = new SourceMapToOutputAction();
		action.myWildcardMatch("src/main/java/test/com.java.cc.java", "*src/main*/java/*.java", IOCase.SENSITIVE,
				matchGroups);
		for (SourceMapToOutputAction.MatchGroup aGroup : matchGroups) {
			System.out.println(aGroup);
		}
	}
}
