package org.fartpig.incrementarchive.source;

import org.fartpig.incrementarchive.constant.ChangeLogSourceEnum;
import org.fartpig.incrementarchive.util.ToolException;
import org.fartpig.incrementarchive.util.ToolLogger;

public abstract class DataSourceFactory {

	public static ChangeLogDataSource getDataSource(ChangeLogSourceEnum sourceEnum) {
		ChangeLogDataSource result = null;
		if (sourceEnum == ChangeLogSourceEnum.CHANGE_LOG_TXT) {
			result = new TxtChangeLogDataSource();
		} else if (sourceEnum == ChangeLogSourceEnum.CHANGE_LOG_XML) {
			result = new XmlChangeLogDataSource();
		} else if (sourceEnum == ChangeLogSourceEnum.CHANGE_LOG_SVN) {
			result = new SvnChangeLogDataSource();
		} else if (sourceEnum == ChangeLogSourceEnum.CHANGE_LOG_GIT) {
			result = new GitChangeLogDataSource();
		} else {
			throw new ToolException(ToolLogger.getInstance().getCurrentPhase(), "dataSource not found");
		}

		return result;
	}

}
