package org.fartpig.incrementarchive.source;

import org.fartpig.incrementarchive.constant.ChangeLogTypeEnum;
import org.fartpig.incrementarchive.entity.ChangeLogManifest;

public interface ChangeLogDataSource {
	public ChangeLogManifest loadChangeLog();

	public void setChangeLogTypeEnum(ChangeLogTypeEnum typeEnum);
}
