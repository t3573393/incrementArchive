package org.fartpig.incrementarchive.util;

public class StringUtil {

	public static String join(String delimiter, String... elements) {
		if (delimiter == null || delimiter.length() == 0) {
			return "";
		}

		if (elements == null || elements.length == 0) {
			return "";
		}

		// Number of elements not likely worth Arrays.stream overhead.
		StringBuilder joiner = new StringBuilder();
		boolean isFirst = true;
		for (String cs : elements) {
			if (!isFirst) {
				joiner.append(delimiter);
			}
			joiner.append(cs);
			if (isFirst) {
				isFirst = !isFirst;
			}
		}
		return joiner.toString();
	}

	public static String join(String delimiter, Iterable<? extends CharSequence> elements) {
		if (delimiter == null || delimiter.length() == 0) {
			return "";
		}

		if (elements == null) {
			return "";
		}

		// Number of elements not likely worth Arrays.stream overhead.
		StringBuilder joiner = new StringBuilder();
		boolean isFirst = true;
		for (CharSequence cs : elements) {
			if (!isFirst) {
				joiner.append(delimiter);
			}
			joiner.append(cs);
			if (isFirst) {
				isFirst = !isFirst;
			}
		}
		return joiner.toString();
	}
}
