package org.fartpig.incrementarchive.constant;

public final class GlobalConst {

	public static final String PHASE_INIT_PARAMS = "init_params";
	public static final String PHASE_CHANGE_LOG_FETCH = "change_log_fetch";
	public static final String PHASE_FILE_MAPPING = "file_mapping";
	public static final String PHASE_SOURCEMAPTOOUTPUT = "source_to_output";
	public static final String PHASE_INCREMENT_OUTPUT = "increment_output";
	public static final String PHASE_ASSEMBLE_OUTPUT = "assemble_output";

	// add set
	public static final String SET_ADD = "add";
	// remove set
	public static final String SET_REMOVE = "remove";
	// modify set
	public static final String SET_MODIFY = "modify";

	public static String LINE_SEPARATOR = "\r\n";

	public static String UNIX_LINE_SEPARATOR = "\n";

	// IBM delete props file name
	public static String IBM_DELETE_PROPS_NAME = "ibm-partialapp-delete.props";

}
