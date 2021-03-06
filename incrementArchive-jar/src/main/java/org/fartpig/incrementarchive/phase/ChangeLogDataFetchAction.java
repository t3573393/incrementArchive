package org.fartpig.incrementarchive.phase;

import org.fartpig.incrementarchive.constant.ChangeLogSourceEnum;
import org.fartpig.incrementarchive.constant.ChangeLogTypeEnum;
import org.fartpig.incrementarchive.constant.GlobalConfig;
import org.fartpig.incrementarchive.constant.GlobalConst;
import org.fartpig.incrementarchive.entity.ChangeLogManifest;
import org.fartpig.incrementarchive.source.ChangeLogDataSource;
import org.fartpig.incrementarchive.source.DataSourceFactory;
import org.fartpig.incrementarchive.util.ToolLogger;

public class ChangeLogDataFetchAction {

	private static final String CURRENT_PHASE = GlobalConst.PHASE_CHANGE_LOG_FETCH;

	public ChangeLogDataFetchAction() {
		ToolLogger.getInstance().setCurrentPhase(CURRENT_PHASE);
	}

	public ChangeLogManifest fetchChangeLogData(ChangeLogTypeEnum typeEnum, ChangeLogSourceEnum sourceEnum) {
		// 清单数据源获取(增加，修改，删除的文件清单)
		GlobalConfig config = GlobalConfig.instance();

		ChangeLogDataSource dataSource = DataSourceFactory.getDataSource(sourceEnum);
		dataSource.setChangeLogTypeEnum(config.getTypeEnum());
		ChangeLogManifest manifest = dataSource.loadChangeLog();
		return manifest;
	}

}
