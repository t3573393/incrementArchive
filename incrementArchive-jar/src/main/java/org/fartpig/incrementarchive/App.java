package org.fartpig.incrementarchive;

import java.io.File;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.fartpig.incrementarchive.constant.ChangeLogSourceEnum;
import org.fartpig.incrementarchive.constant.ChangeLogTypeEnum;
import org.fartpig.incrementarchive.constant.GitConfig;
import org.fartpig.incrementarchive.constant.GlobalConfig;
import org.fartpig.incrementarchive.constant.GlobalConst;
import org.fartpig.incrementarchive.constant.SvnConfig;
import org.fartpig.incrementarchive.entity.ChangeLogManifest;
import org.fartpig.incrementarchive.entity.FileMappingMetaInfo;
import org.fartpig.incrementarchive.phase.ChangeLogDataFetchAction;
import org.fartpig.incrementarchive.phase.FileMappingMetaCreateAction;
import org.fartpig.incrementarchive.phase.FileTransferAction;
import org.fartpig.incrementarchive.phase.IncrementArchiveAction;
import org.fartpig.incrementarchive.phase.SourceMapToOutputAction;
import org.fartpig.incrementarchive.util.PathUtil;
import org.fartpig.incrementarchive.util.ToolException;
import org.fartpig.incrementarchive.util.ToolLogger;

/**
 * application entry
 *
 */
public class App {

	public static GlobalConfig argsResolve(String[] args) {
		ToolLogger log = ToolLogger.getInstance();
		GlobalConfig config = GlobalConfig.instance();

		String inputDir = config.getInputDir();
		String outputDir = config.getOutputDir();
		String zipFileName = config.getZipFileName();

		String changeLogSourceFile = config.getChangeLogSourceFile();
		ChangeLogSourceEnum sourceEnum = config.getSourceEnum();
		ChangeLogTypeEnum typeEnum = config.getTypeEnum();

		Options options = new Options();

		Option exts = new Option("exts", "extensions", true, "file extensions");
		exts.setRequired(false);
		options.addOption(exts);

		Option filter = new Option("fil", "filter", true, "filter files by extensions");
		filter.setRequired(false);
		options.addOption(filter);

		Option sourceFile = new Option("s", "source", true, "please set the changeLogSourceFile");
		sourceFile.setRequired(false);
		options.addOption(sourceFile);

		Option changeLogSource = new Option("st", "sourceType", true, "please set the changeLogSourceType");
		changeLogSource.setRequired(false);
		options.addOption(changeLogSource);

		Option changeLogType = new Option("t", "type", true, "please set the changeLogType");
		changeLogType.setRequired(false);
		options.addOption(changeLogType);

		Option zipFile = new Option("zfn", "zipFileName", true, "please set the zipFileName");
		zipFile.setRequired(false);
		options.addOption(zipFile);

		Option name = new Option("n", "name", true, "please set the name");
		name.setRequired(false);
		options.addOption(name);

		Option password = new Option("p", "password", true, "please set the password");
		password.setRequired(false);
		options.addOption(password);

		Option url = new Option("u", "url", true, "please set the url");
		url.setRequired(false);
		options.addOption(url);

		Option startVersion = new Option("sv", "startVersion", true, "please set the startVersion");
		startVersion.setRequired(false);
		options.addOption(startVersion);

		Option endVersion = new Option("ev", "endVersion", true, "please set the endVersion");
		endVersion.setRequired(false);
		options.addOption(endVersion);

		Option prefixPath = new Option("pp", "prefixPath", true, "please set the endVersion");
		prefixPath.setRequired(false);
		options.addOption(prefixPath);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();

		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			ToolLogger.getInstance().error("error", e);
			formatter.printHelp("utility-name", options);
			throw new ToolException(log.getCurrentPhase(), "please set the right args");
		}

		String[] argArray = cmd.getArgs();
		if (argArray.length != 2) {
			throw new ToolException(log.getCurrentPhase(), "please set the intputDir and outputDir");
		}

		inputDir = argArray[0];
		outputDir = argArray[1];

		config.setInputDir(inputDir);
		config.setOutputDir(outputDir);

		changeLogSourceFile = cmd.getOptionValue("source", changeLogSourceFile);
		config.setChangeLogSourceFile(changeLogSourceFile);

		sourceEnum = ChangeLogSourceEnum.valueOf(cmd.getOptionValue("sourceType", sourceEnum.name()).toUpperCase());
		config.setSourceEnum(sourceEnum);

		typeEnum = ChangeLogTypeEnum.valueOf(cmd.getOptionValue("type", typeEnum.name()).toUpperCase());
		config.setTypeEnum(typeEnum);

		String filterStr = cmd.getOptionValue("filter", "false");
		config.setFilterExtension(Boolean.valueOf(filterStr));

		String extsStr = cmd.getOptionValue("extensions");
		if (!StringUtils.isEmpty(extsStr)) {
			config.setFilterExtension(true);
			config.fillExtensions(extsStr);
		}

		SvnConfig svnConfig = null;
		GitConfig gitConfig = null;
		if (sourceEnum == ChangeLogSourceEnum.CHANGE_LOG_SVN) {
			svnConfig = new SvnConfig();
			svnConfig.setName(cmd.getOptionValue("name"));
			svnConfig.setPassword(cmd.getOptionValue("password"));
			svnConfig.setUrlPath(cmd.getOptionValue("url"));
			svnConfig.setStartVersion(Long.valueOf(cmd.getOptionValue("startVersion")));
			svnConfig.setPrefixPath(cmd.getOptionValue("prefixPath", "/"));

			String endVersionStr = cmd.getOptionValue("endVersion");
			if (!StringUtils.isEmpty(endVersionStr)) {
				svnConfig.setEndVersion(Long.valueOf(endVersionStr));
			}
			config.setSvnConfig(svnConfig);
		} else if (sourceEnum == ChangeLogSourceEnum.CHANGE_LOG_GIT) {
			gitConfig = new GitConfig();

			gitConfig.setUrlPath(cmd.getOptionValue("url"));
			gitConfig.setStartVersion(cmd.getOptionValue("startVersion"));
			gitConfig.setPrefixPath(cmd.getOptionValue("prefixPath", "/"));

			String endVersionStr = cmd.getOptionValue("endVersion");
			if (!StringUtils.isEmpty(endVersionStr)) {
				gitConfig.setEndVersion(endVersionStr);
			}
			config.setGitConfig(gitConfig);
		}

		zipFileName = cmd.getOptionValue("zipFileName", changeLogSourceFile);
		config.setZipFileName(zipFileName);

		log.info(String.format("set intputDir:%s", inputDir));
		log.info(String.format("set outputDir:%s", outputDir));

		log.info(String.format("set changeLogSourceFile:%s", changeLogSourceFile));
		log.info(String.format("set sourceType:%s", sourceEnum.name()));
		log.info(String.format("set type:%s", typeEnum.name()));

		log.info(String.format("set zipFileName:%s", zipFileName));
		log.info(String.format("set extensions:%s", extsStr));

		if (svnConfig != null) {
			log.info(String.format("set svnConfig:%s", svnConfig));
		}

		if (gitConfig != null) {
			log.info(String.format("set gitConfig:%s", gitConfig));
		}

		return config;
	}

	public static void main(String[] args) {
		try {
			String phase = GlobalConst.PHASE_INIT_PARAMS;
			ToolLogger log = ToolLogger.getInstance();
			log.setCurrentPhase(phase);
			GlobalConfig config = argsResolve(args);
			invokeByGlobalConfig(config);

		} catch (Exception e) {
			ToolLogger.getInstance().error("error:", e);
		}
	}

	public static void invokeByGlobalConfig(GlobalConfig config) {

		String inputDir = config.getInputDir();
		String outputDir = config.getOutputDir();
		String zipFileName = config.getZipFileName();
		// formate the input and output path
		File inputFile = new File(inputDir);
		if (inputFile.exists()) {
			inputDir = inputFile.getAbsolutePath();
		} else {
			throw new ToolException(ToolLogger.getInstance().getCurrentPhase(), "input dir is not found");
		}

		File outputFile = new File(outputDir);
		if (!outputFile.exists()) {
			if (!outputFile.mkdirs()) {
				throw new ToolException(ToolLogger.getInstance().getCurrentPhase(), "mk output dir fail");
			}
		}
		outputDir = outputFile.getAbsolutePath();
		outputDir = PathUtil.formatFolderSuffixPath(outputDir);

		String changeLogSourceFile = config.getChangeLogSourceFile();
		ChangeLogSourceEnum sourceEnum = config.getSourceEnum();
		ChangeLogTypeEnum typeEnum = config.getTypeEnum();

		// retrieve change log data
		ChangeLogDataFetchAction logDataFetchAction = new ChangeLogDataFetchAction();
		ChangeLogManifest manifest = logDataFetchAction.fetchChangeLogData(typeEnum, changeLogSourceFile, sourceEnum);
		// source map to output
		if (manifest.getLogType() == ChangeLogTypeEnum.SOURCE) {
			// file mapping meta info
			FileMappingMetaCreateAction fileMappingMetaCreateAction = new FileMappingMetaCreateAction();
			List<FileMappingMetaInfo> fileMappingMetaInfos = fileMappingMetaCreateAction.createFileMappingMetaInfos();

			SourceMapToOutputAction sourceMapToOutputAction = new SourceMapToOutputAction();
			sourceMapToOutputAction.sourceToOutput(manifest, inputDir, fileMappingMetaInfos);
		}

		// file tranfer
		FileTransferAction fileTranferAction = new FileTransferAction();
		fileTranferAction.transferFiles(inputDir, outputDir, manifest, config);
		// increment archive
		IncrementArchiveAction incrementArchiveAction = new IncrementArchiveAction();
		incrementArchiveAction.incrementArchiveToPath(inputDir, outputDir, zipFileName, manifest);
	}

}
