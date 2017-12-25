package org.fartpig.incrementarchive;

import java.io.File;

import org.fartpig.incrementarchive.constant.ChangeLogSourceEnum;
import org.fartpig.incrementarchive.constant.ChangeLogTypeEnum;
import org.fartpig.incrementarchive.constant.GlobalConfig;
import org.fartpig.incrementarchive.constant.OutputEnum;

import junit.framework.TestCase;

public class AssembleGenerateTest extends TestCase {

	public void testAssembleGenerate() {

		System.out.println(GlobalConfig.getApprootpath());
		String parentFilePath = new File(GlobalConfig.getApprootpath()).getParent();
		String rootPath = new File(GlobalConfig.getApprootpath()).getParentFile().getParent();
		String testClassFilePath = parentFilePath + File.separator + "test-classes";
		String sourceFilePath = testClassFilePath + File.separator + "assemble.txt";

		GlobalConfig config = GlobalConfig.instanceByFile(null,
				new File(testClassFilePath + File.separator + "my-filemapping.properties"));
		config.setTypeEnum(ChangeLogTypeEnum.SOURCE);
		config.setInputDir(rootPath);
		config.setSourceEnum(ChangeLogSourceEnum.CHANGE_LOG_TXT);
		// config.setChangeLogSourceFile(sourceFilePath);
		config.setChangeLogSourceFile("C:\\Users\\fdsa\\Desktop\\增量打包清单.txt");
		config.setPrefixPath("/awp");

		config.setOutputDir(parentFilePath);
		config.setOutputEnum(OutputEnum.OUTPUT_ASSEMBLE);
		config.setAssembleTemplate(testClassFilePath + File.separator + "my-assemble.ftl");

		App.invokeByGlobalConfig(config);

	}

}
