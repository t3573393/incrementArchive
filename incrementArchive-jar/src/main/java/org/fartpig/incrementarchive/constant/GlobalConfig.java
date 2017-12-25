package org.fartpig.incrementarchive.constant;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.fartpig.incrementarchive.entity.OrderedProperties;
import org.fartpig.incrementarchive.util.PathUtil;
import org.fartpig.incrementarchive.util.StringUtil;
import org.fartpig.incrementarchive.util.ToolLogger;

public class GlobalConfig {

	private static final String appRootPath = PathUtil.getProjectPath();
	private static GlobalConfig globalConfig = null;

	public static GlobalConfig instance() {
		if (globalConfig == null) {
			globalConfig = new GlobalConfig("tools.properties");
			globalConfig.loadFileMappings("filemapping.properties");
		}
		return globalConfig;
	}

	public static GlobalConfig instanceByFile(File file, File fileMappingFile) {
		if (globalConfig == null) {
			if (file != null) {
				globalConfig = new GlobalConfig(file);
			} else {
				globalConfig = new GlobalConfig();
			}

			if (fileMappingFile != null) {
				globalConfig.loadFileMappings(fileMappingFile);
			} else {
				globalConfig.loadFileMappings("filemapping.properties");
			}

		}
		return globalConfig;
	}

	private String inputDir = String.format("%s%sclasses", appRootPath, File.separator);
	private String outputDir = String.format("%s%sincrement-archive", appRootPath, File.separator);
	private String zipFileName = "increment-archive.zip";

	private ChangeLogSourceEnum sourceEnum = ChangeLogSourceEnum.CHANGE_LOG_TXT;
	private String changeLogSourceFile = String.format("%s%schange-log.txt", appRootPath, File.separator);

	private ChangeLogTypeEnum typeEnum = ChangeLogTypeEnum.OUTPUT;
	private String[] extensions = { "class", "jsp", "xml", "table", "java", "js", "ecs", "png", "jpg", "swf", "gif",
			"css", "raq", "xls", "jar", "tld", "jnlp", "html", "properties", "psd" };
	private boolean filterExtension = false;

	private OutputEnum outputEnum = OutputEnum.OUTPUT_FILES;
	private String assembleTemplate;

	private SvnConfig svnConfig;
	private GitConfig gitConfig;
	private String prefixPath;

	private OrderedProperties fileMapping;

	private GlobalConfig() {
	}

	private GlobalConfig(File configFile) {
		Properties configProperties = new Properties();
		try {
			configProperties.load(new BufferedInputStream(new FileInputStream(configFile)));

		} catch (FileNotFoundException e) {
			ToolLogger.getInstance().error("error:", e);
		} catch (IOException e) {
			ToolLogger.getInstance().error("error:", e);
		}
		fillDataByProperties(configProperties);
	}

	private GlobalConfig(String configName) {
		Properties configProperties = new Properties();
		try {
			configProperties
					.load(new BufferedInputStream(new FileInputStream(appRootPath + File.separator + configName)));

		} catch (FileNotFoundException e) {
			ToolLogger.getInstance().error("error:", e);
		} catch (IOException e) {
			ToolLogger.getInstance().error("error:", e);
		}
		fillDataByProperties(configProperties);
	}

	public void loadFileMappings(File fileMappingFile) {
		fileMapping = new OrderedProperties();
		try {
			fileMapping.load(new BufferedInputStream(new FileInputStream(fileMappingFile)));
		} catch (FileNotFoundException e) {
			ToolLogger.getInstance().error("error:", e);
		} catch (IOException e) {
			ToolLogger.getInstance().error("error:", e);
		}
	}

	public void loadFileMappings(String configName) {
		fileMapping = new OrderedProperties();
		try {
			fileMapping.load(new BufferedInputStream(new FileInputStream(appRootPath + File.separator + configName)));

		} catch (FileNotFoundException e) {
			ToolLogger.getInstance().error("error:", e);
		} catch (IOException e) {
			ToolLogger.getInstance().error("error:", e);
		}
	}

	private void fillDataByProperties(Properties configProperties) {
		inputDir = configProperties.getProperty("inputDir", inputDir);
		outputDir = configProperties.getProperty("outputDir", outputDir);
		zipFileName = configProperties.getProperty("zipFileName", zipFileName);

		sourceEnum = ChangeLogSourceEnum
				.valueOf(configProperties.getProperty("sourceEnum", sourceEnum.toString()).toUpperCase());
		changeLogSourceFile = configProperties.getProperty("changeLogSourceFile", changeLogSourceFile);
		typeEnum = ChangeLogTypeEnum
				.valueOf(configProperties.getProperty("typeEnum", typeEnum.toString()).toUpperCase());
		String extensionsStr = configProperties.getProperty("extensions", StringUtil.join(",", extensions));
		extensions = extensionsStr.split("[,]");

		outputEnum = OutputEnum
				.valueOf(configProperties.getProperty("outputEnum", outputEnum.toString()).toUpperCase());

		assembleTemplate = configProperties.getProperty("assembleTemplate", assembleTemplate);
		prefixPath = configProperties.getProperty("prefixPath", "/");

		if (sourceEnum == ChangeLogSourceEnum.CHANGE_LOG_SVN) {
			svnConfig = new SvnConfig();

			svnConfig.setName(configProperties.getProperty("name"));
			svnConfig.setPassword(configProperties.getProperty("password"));
			svnConfig.setUrlPath(configProperties.getProperty("urlPath"));
			svnConfig.setStartVersion(Long.valueOf(configProperties.getProperty("startVersion")).longValue());
			svnConfig.setEndVersion(Long.valueOf(configProperties.getProperty("endVersion", "-1")).longValue());

		} else if (sourceEnum == ChangeLogSourceEnum.CHANGE_LOG_GIT) {
			gitConfig = new GitConfig();

			gitConfig.setUrlPath(configProperties.getProperty("urlPath"));
			gitConfig.setStartVersion(configProperties.getProperty("startVersion"));
			gitConfig.setEndVersion(configProperties.getProperty("endVersion"));
		}

		ToolLogger log = ToolLogger.getInstance();
		log.info("inputDir:" + inputDir);
		log.info("outputDir:" + outputDir);
		log.info("zipFileName:" + zipFileName);

		log.info("sourceEnum:" + sourceEnum);
		log.info("changeLogSourceFile:" + changeLogSourceFile);
		log.info("typeEnum:" + typeEnum);
		log.info("extensions:" + extensionsStr);

		log.info("outputEnum:" + outputEnum);
		log.info("assembleTemplate:" + assembleTemplate);

		if (svnConfig != null) {
			log.info("name:" + svnConfig.getName());
			log.info("password:" + svnConfig.getPassword());
			log.info("urlPath:" + svnConfig.getUrlPath());
			log.info("startVersion:" + svnConfig.getStartVersion());
			log.info("endVersion:" + svnConfig.getEndVersion());
		}

	}

	public static GlobalConfig getGlobalConfig() {
		return globalConfig;
	}

	public static void setGlobalConfig(GlobalConfig globalConfig) {
		GlobalConfig.globalConfig = globalConfig;
	}

	public String getInputDir() {
		return inputDir;
	}

	public void setInputDir(String inputDir) {
		this.inputDir = inputDir;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public String getZipFileName() {
		return zipFileName;
	}

	public void setZipFileName(String zipFileName) {
		this.zipFileName = zipFileName;
	}

	public static String getApprootpath() {
		return appRootPath;
	}

	public ChangeLogSourceEnum getSourceEnum() {
		return sourceEnum;
	}

	public void setSourceEnum(ChangeLogSourceEnum sourceEnum) {
		this.sourceEnum = sourceEnum;
	}

	public ChangeLogTypeEnum getTypeEnum() {
		return typeEnum;
	}

	public void setTypeEnum(ChangeLogTypeEnum typeEnum) {
		this.typeEnum = typeEnum;
	}

	public String getChangeLogSourceFile() {
		return changeLogSourceFile;
	}

	public void setChangeLogSourceFile(String changeLogSourceFile) {
		this.changeLogSourceFile = changeLogSourceFile;
	}

	public void fillExtensions(String extensionsStr) {
		if (extensionsStr == null || extensionsStr.length() == 0) {
			return;
		}
		extensions = extensionsStr.split("[,]");
		for (int i = 0; i < extensions.length; i++) {
			extensions[i] = extensions[i].trim();
		}
	}

	public String getExtensionStr() {
		return StringUtil.join(",", extensions);
	}

	public String[] getExtensions() {
		return extensions;
	}

	public void setExtensions(String[] extensions) {
		this.extensions = extensions;
	}

	public boolean isFilterExtension() {
		return filterExtension;
	}

	public void setFilterExtension(boolean filterExtension) {
		this.filterExtension = filterExtension;
	}

	public SvnConfig getSvnConfig() {
		return svnConfig;
	}

	public void setSvnConfig(SvnConfig svnConfig) {
		this.svnConfig = svnConfig;
	}

	public GitConfig getGitConfig() {
		return gitConfig;
	}

	public void setGitConfig(GitConfig gitConfig) {
		this.gitConfig = gitConfig;
	}

	public OrderedProperties getFileMapping() {
		return fileMapping;
	}

	public void setFileMapping(OrderedProperties fileMapping) {
		this.fileMapping = fileMapping;
	}

	public OutputEnum getOutputEnum() {
		return outputEnum;
	}

	public void setOutputEnum(OutputEnum outputEnum) {
		this.outputEnum = outputEnum;
	}

	public String getAssembleTemplate() {
		return assembleTemplate;
	}

	public void setAssembleTemplate(String assembleTemplate) {
		this.assembleTemplate = assembleTemplate;
	}

	public String getPrefixPath() {
		return prefixPath;
	}

	public void setPrefixPath(String prefixPath) {
		this.prefixPath = prefixPath;
	}

}
