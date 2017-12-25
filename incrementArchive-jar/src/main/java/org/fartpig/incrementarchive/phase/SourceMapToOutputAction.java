package org.fartpig.incrementarchive.phase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.fartpig.incrementarchive.constant.GlobalConst;
import org.fartpig.incrementarchive.entity.ChangeLogEntry;
import org.fartpig.incrementarchive.entity.ChangeLogFileEntry;
import org.fartpig.incrementarchive.entity.ChangeLogManifest;
import org.fartpig.incrementarchive.entity.ChangeLogPathEntry;
import org.fartpig.incrementarchive.entity.FileMappingMetaInfo;
import org.fartpig.incrementarchive.util.PathUtil;
import org.fartpig.incrementarchive.util.ToolLogger;

public class SourceMapToOutputAction {

	private static final String CURRENT_PHASE = GlobalConst.PHASE_SOURCEMAPTOOUTPUT;

	public SourceMapToOutputAction() {
		ToolLogger.getInstance().setCurrentPhase(CURRENT_PHASE);
	}

	public void sourceToOutput(ChangeLogManifest manifest, String inputDir,
			List<FileMappingMetaInfo> fileMappingMetaInfos) {
		// 清单数据源路径转化，将路径切换成 OUTPUT 模式
		// only map once
		List<ChangeLogEntry> addEntries = manifest.getAddEntries();
		List<ChangeLogEntry> modifyEntries = manifest.getModifyEntries();
		List<ChangeLogEntry> removeEntries = manifest.getRemoveEntries();

		ArrayList<ChangeLogEntry> allEntry = new ArrayList<ChangeLogEntry>();
		allEntry.addAll(addEntries);
		allEntry.addAll(modifyEntries);
		allEntry.addAll(removeEntries);

		for (FileMappingMetaInfo aMetaInfo : fileMappingMetaInfos) {
			batchMapToOutput(inputDir, allEntry, aMetaInfo);
		}
	}

	private void batchMapToOutput(String inputDir, List<ChangeLogEntry> entries, FileMappingMetaInfo mappingMetaInfo) {
		for (ChangeLogEntry aEntry : entries) {

			if (aEntry.isFileMapping()) {
				continue;
			}

			// git和svn的源码到目标的处理
			// get the relative path
			String relativePath = PathUtil.getRelativePath(aEntry, inputDir);

			// add the separator for match src
			if (aEntry instanceof ChangeLogPathEntry) {
				relativePath = PathUtil.formatFolderPath(relativePath);
			} else if (aEntry instanceof ChangeLogFileEntry) {
				relativePath = PathUtil.formatFilePath(relativePath);
			}

			String srcPath = mappingMetaInfo.getSrcPath();
			srcPath = FilenameUtils.separatorsToSystem(srcPath);
			// 统一添加通配符，减少匹配难度
			srcPath = "*" + srcPath;

			String targetPath = mappingMetaInfo.getTargetPath();
			targetPath = FilenameUtils.separatorsToSystem(targetPath);

			List<MatchGroup> matchGroups = new ArrayList<MatchGroup>();

			srcPath = formatWildcard(srcPath);
			boolean matchSrcPath = myWildcardMatch(relativePath, srcPath, IOCase.SENSITIVE, matchGroups);
			if (matchSrcPath && matchGroups.size() > 0) {
				StringBuffer resultPath = new StringBuffer();
				resultPath.append(inputDir);
				resultPath.append(File.separator);
				// fill the target path with the wild card match
				targetPath = "*" + targetPath;
				targetPath = formatWildcard(targetPath);
				int srcWildcardCount = countWildcard(srcPath);
				int targetWildcardCount = countWildcard(targetPath);

				if (srcWildcardCount == targetWildcardCount) {
					String[] wildcardMatchStr = new String[srcWildcardCount];
					int wildcardMatchIndex = 0;
					for (int i = 0; i < matchGroups.size(); i++) {
						MatchGroup aGroup = matchGroups.get(i);
						String token = aGroup.getWildcardStr();
						if ("*".equals(token) || "?".equals(token)) {
							wildcardMatchStr[wildcardMatchIndex] = aGroup.getMatchTargetStr();
							wildcardMatchIndex++;
						}
					}

					wildcardMatchIndex = 0;
					String[] targetTokens = splitOnTokens(targetPath);
					for (String aToken : targetTokens) {
						if ("*".equals(aToken) || "?".equals(aToken)) {
							resultPath.append(wildcardMatchStr[wildcardMatchIndex]);
							wildcardMatchIndex++;
						} else {
							resultPath.append(aToken);
						}
					}

					String result = resultPath.toString();

					if (aEntry instanceof ChangeLogPathEntry) {
						((ChangeLogPathEntry) aEntry).setFilePath(result);
					} else if (aEntry instanceof ChangeLogFileEntry) {
						File resultFile = new File(result);
						ChangeLogFileEntry fileEntry = ((ChangeLogFileEntry) aEntry);
						String baseName = FilenameUtils.getBaseName(result);
						String fileEx = FilenameUtils.getExtension(result);
						fileEntry.setFileBaseName(baseName);
						fileEntry.setFileEx(fileEx);
						fileEntry.setFilePath(resultFile.getParent());
					}

					ToolLogger.getInstance().info(String.format("change path to %s", aEntry.absolutePath()));
					aEntry.setFileMapping(true);

				} else {
					ToolLogger.getInstance().info(
							String.format("source map srcPath:%s to targetPath %s invalid ", srcPath, targetPath));
				}
			}
		}

	}

	public static class MatchGroup {

		private String wildcardStr;
		private String matchTargetStr;

		private int wildIndex;

		private int targetStartIndex;
		private int targetEndIndex;

		private boolean isAnyChars = false;

		public String toString() {
			return wildcardStr + " @ " + matchTargetStr + " @ " + wildIndex + " @ " + targetStartIndex + " @ "
					+ targetEndIndex;
		}

		public String getWildcardStr() {
			return wildcardStr;
		}

		public void setWildcardStr(String wildcardStr) {
			this.wildcardStr = wildcardStr;
		}

		public String getMatchTargetStr() {
			return matchTargetStr;
		}

		public void setMatchTargetStr(String matchTargetStr) {
			this.matchTargetStr = matchTargetStr;
		}

		public int getWildIndex() {
			return wildIndex;
		}

		public void setWildIndex(int wildIndex) {
			this.wildIndex = wildIndex;
		}

		public int getTargetStartIndex() {
			return targetStartIndex;
		}

		public void setTargetStartIndex(int targetStartIndex) {
			this.targetStartIndex = targetStartIndex;
		}

		public int getTargetEndIndex() {
			return targetEndIndex;
		}

		public void setTargetEndIndex(int targetEndIndex) {
			this.targetEndIndex = targetEndIndex;
		}

		public boolean isAnyChars() {
			return isAnyChars;
		}

		public void setAnyChars(boolean isAnyChars) {
			this.isAnyChars = isAnyChars;
		}
	}

	public String formatWildcard(String wildcarMatcherStr) {
		final StringBuilder buffer = new StringBuilder();
		char prevChar = 0;
		char[] array = wildcarMatcherStr.toCharArray();
		for (final char ch : array) {
			if (ch == '?' || ch == '*') {
				if (ch == '?') {
					buffer.append("?");
				} else if (prevChar != '*') {// ch == '*' here; check if
												// previous char was '*'
					buffer.append("*");
				}
			} else {
				buffer.append(ch);
			}
			prevChar = ch;
		}

		return buffer.toString();
	}

	public int countWildcard(String wildcarMatcherStr) {
		int num = 0;
		char prevChar = 0;
		char[] array = wildcarMatcherStr.toCharArray();
		for (final char ch : array) {
			if (ch == '?' || ch == '*') {
				if (ch == '?') {
					num++;
				} else if (prevChar != '*') {// ch == '*' here; check if
												// previous char was '*'
					num++;
				}
			}
			prevChar = ch;
		}

		return num;
	}

	// my wildcard match from the original {@link
	// #FilenameUtil.wildcardMatch(String,String,IOCase)}
	public boolean myWildcardMatch(final String filename, final String wildcardMatcher, IOCase caseSensitivity,
			List<MatchGroup> matchGroups) {
		if (filename == null && wildcardMatcher == null) {
			return true;
		}
		if (filename == null || wildcardMatcher == null) {
			return false;
		}
		if (caseSensitivity == null) {
			caseSensitivity = IOCase.SENSITIVE;
		}
		final String[] wcs = splitOnTokens(wildcardMatcher);

		boolean anyChars = false;
		int anyCharStart = -1;
		int textIdx = 0;
		int wcsIdx = 0;
		final Stack<int[]> backtrack = new Stack<int[]>();

		// loop around a backtrack stack, to handle complex * matching
		do {
			if (backtrack.size() > 0) {
				final int[] array = backtrack.pop();
				wcsIdx = array[0];
				textIdx = array[1];
				anyChars = true;

				// change the last anychar match group
				int backIndex = 0;
				for (int i = matchGroups.size() - 1; i >= 0; i--) {
					MatchGroup tempMatchGroup = matchGroups.get(i);
					if (tempMatchGroup.isAnyChars()) {
						backIndex = i;
						break;
					}
				}
				anyCharStart = matchGroups.get(backIndex).getTargetStartIndex();

				if (backIndex == 0) {
					matchGroups.clear();
				} else {
					for (int i = matchGroups.size() - 1; i >= backIndex; i--) {
						matchGroups.remove(i);
					}
				}

			}

			// loop whilst tokens and text left to process
			while (wcsIdx < wcs.length) {

				if (wcs[wcsIdx].equals("?")) {

					// close the before anychars
					if (anyChars && anyCharStart != -1) {
						MatchGroup matchGroup = new MatchGroup();
						matchGroup.setTargetStartIndex(anyCharStart);
						matchGroup.setTargetEndIndex(textIdx - 1);
						matchGroup.setMatchTargetStr(filename.substring(anyCharStart, textIdx));
						matchGroup.setWildcardStr(wcs[wcsIdx - 1]);
						matchGroup.setWildIndex(wcsIdx - 1);
						matchGroup.setAnyChars(true);

						matchGroups.add(matchGroup);
					}

					// ? so move to next text char
					MatchGroup matchGroup = new MatchGroup();
					matchGroup.setTargetStartIndex(textIdx);
					matchGroup.setTargetEndIndex(textIdx);
					matchGroup.setMatchTargetStr(filename.substring(textIdx, textIdx + 1));
					matchGroup.setWildcardStr(wcs[wcsIdx]);
					matchGroup.setWildIndex(wcsIdx);
					matchGroup.setAnyChars(false);

					matchGroups.add(matchGroup);
					textIdx++;
					if (textIdx > filename.length()) {
						break;
					}
					anyChars = false;
					anyCharStart = -1;

				} else if (wcs[wcsIdx].equals("*")) {
					// set any chars status
					anyChars = true;
					anyCharStart = textIdx;
					if (wcsIdx == wcs.length - 1) {
						textIdx = filename.length();
						// match to the end file name
						MatchGroup matchGroup = new MatchGroup();
						matchGroup.setTargetStartIndex(anyCharStart);
						matchGroup.setTargetEndIndex(textIdx - 1);
						matchGroup.setMatchTargetStr(filename.substring(anyCharStart, textIdx));
						matchGroup.setWildcardStr(wcs[wcsIdx]);
						matchGroup.setWildIndex(wcsIdx);
						matchGroup.setAnyChars(true);

						matchGroups.add(matchGroup);

						anyChars = false;
						anyCharStart = -1;
					}

				} else {
					// matching text token
					if (anyChars) {
						// any chars then try to locate text token
						textIdx = caseSensitivity.checkIndexOf(filename, textIdx, wcs[wcsIdx]);
						if (textIdx == NOT_FOUND) {
							// token not found
							break;
						}
						// try increment textIdX by repeat chars
						final int repeat = caseSensitivity.checkIndexOf(filename, textIdx + 1, wcs[wcsIdx]);
						if (repeat >= 0) {
							// record the first search place
							backtrack.push(new int[] { wcsIdx, repeat });
						}
					} else {
						// matching from current position
						if (!caseSensitivity.checkRegionMatches(filename, textIdx, wcs[wcsIdx])) {
							// couldnt match token
							break;
						}
					}

					// matched text token, move text index to end of matched
					// token
					int originalTextIdx = textIdx;
					textIdx += wcs[wcsIdx].length();

					if (anyChars && anyCharStart != -1) {
						// add last any char group
						MatchGroup anyCharMatchGroup = new MatchGroup();
						anyCharMatchGroup.setTargetStartIndex(anyCharStart);
						anyCharMatchGroup.setTargetEndIndex(originalTextIdx - 1);
						anyCharMatchGroup.setMatchTargetStr(filename.substring(anyCharStart, originalTextIdx));
						anyCharMatchGroup.setAnyChars(true);
						anyCharMatchGroup.setWildcardStr(wcs[wcsIdx - 1]);
						anyCharMatchGroup.setWildIndex(wcsIdx - 1);

						matchGroups.add(anyCharMatchGroup);
					}

					// add the current matchgroup
					MatchGroup matchGroup = new MatchGroup();
					matchGroup.setTargetStartIndex(originalTextIdx);
					matchGroup.setTargetEndIndex(textIdx - 1);
					matchGroup.setMatchTargetStr(filename.substring(originalTextIdx, textIdx));
					matchGroup.setAnyChars(false);
					matchGroup.setWildcardStr(wcs[wcsIdx]);
					matchGroup.setWildIndex(wcsIdx);

					matchGroups.add(matchGroup);

					anyChars = false;
					anyCharStart = -1;
				}

				wcsIdx++;
			}

			// full match
			if (wcsIdx == wcs.length && textIdx == filename.length()) {
				return true;
			}

		} while (backtrack.size() > 0);

		return false;
	}

	/**
	 * Splits a string into a number of tokens. The text is split by '?' and
	 * '*'. Where multiple '*' occur consecutively they are collapsed into a
	 * single '*'.
	 *
	 * @param text
	 *            the text to split
	 * @return the array of tokens, never null
	 */
	private String[] splitOnTokens(final String text) {
		// used by wildcardMatch
		// package level so a unit test may run on this

		if (text.indexOf('?') == NOT_FOUND && text.indexOf('*') == NOT_FOUND) {
			return new String[] { text };
		}

		final char[] array = text.toCharArray();
		final ArrayList<String> list = new ArrayList<String>();
		final StringBuilder buffer = new StringBuilder();
		char prevChar = 0;
		for (final char ch : array) {
			if (ch == '?' || ch == '*') {
				if (buffer.length() != 0) {
					list.add(buffer.toString());
					buffer.setLength(0);
				}
				if (ch == '?') {
					list.add("?");
				} else if (prevChar != '*') {// ch == '*' here; check if
												// previous char was '*'
					list.add("*");
				}
			} else {
				buffer.append(ch);
			}
			prevChar = ch;
		}
		if (buffer.length() != 0) {
			list.add(buffer.toString());
		}

		return list.toArray(new String[list.size()]);
	}

	private final int NOT_FOUND = -1;

}
