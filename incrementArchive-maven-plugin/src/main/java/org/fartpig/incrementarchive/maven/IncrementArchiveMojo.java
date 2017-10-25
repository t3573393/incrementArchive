package org.fartpig.incrementarchive.maven;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.fartpig.incrementarchive.App;
import org.fartpig.incrementarchive.constant.ChangeLogSourceEnum;
import org.fartpig.incrementarchive.constant.ChangeLogTypeEnum;
import org.fartpig.incrementarchive.constant.GitConfig;
import org.fartpig.incrementarchive.constant.GlobalConfig;
import org.fartpig.incrementarchive.constant.SvnConfig;
import org.fartpig.incrementarchive.entity.OrderedProperties;
import org.fartpig.incrementarchive.maven.util.Constants;

import com.google.common.base.Throwables;

@Mojo(name = "incrementArchive", defaultPhase = LifecyclePhase.PACKAGE, requiresDirectInvocation = true, threadSafe = false)
public class IncrementArchiveMojo extends AbstractMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info(Constants.PLUGIN_ID + " - resolve");
		try {

			GlobalConfig globalConfig = GlobalConfig.instanceByFile(globalConfigPropertyFile, fileMappingPropertyFile);

			globalConfig.setInputDir(inputDir);
			globalConfig.setOutputDir(outputDir);

			if (!StringUtils.isEmpty(sourceEnum)) {
				globalConfig.setSourceEnum(ChangeLogSourceEnum.valueOf(sourceEnum));
			}

			if (!StringUtils.isEmpty(changeLogSourceFile)) {
				globalConfig.setChangeLogSourceFile(changeLogSourceFile);
			}

			if (!StringUtils.isEmpty(typeEnum)) {
				globalConfig.setTypeEnum(ChangeLogTypeEnum.valueOf(typeEnum));
			}

			if (!StringUtils.isEmpty(zipFileName)) {
				globalConfig.setZipFileName(zipFileName);
			}

			if (!StringUtils.isEmpty(extensions)) {
				globalConfig.setFilterExtension(true);
				globalConfig.fillExtensions(extensions);
			}

			SvnConfig svnConfig = null;
			GitConfig gitConfig = null;
			if (globalConfig.getSourceEnum() == ChangeLogSourceEnum.CHANGE_LOG_SVN) {
				svnConfig = new SvnConfig();
				svnConfig.setName(name);
				svnConfig.setPassword(password);
				svnConfig.setUrlPath(url);
				svnConfig.setStartVersion(Long.valueOf(startVersion));
				svnConfig.setPrefixPath(prefixPath == null ? "/" : prefixPath);
				svnConfig.setEndVersion(endVersion == null ? -1 : Long.valueOf(endVersion));
				globalConfig.setSvnConfig(svnConfig);
			} else if (globalConfig.getSourceEnum() == ChangeLogSourceEnum.CHANGE_LOG_GIT) {
				gitConfig = new GitConfig();
				gitConfig.setUrlPath(url);
				gitConfig.setStartVersion(startVersion);
				gitConfig.setPrefixPath(prefixPath == null ? "/" : prefixPath);
				gitConfig.setEndVersion(endVersion);
				globalConfig.setGitConfig(gitConfig);
			}

			if (globalConfig.getTypeEnum() == ChangeLogTypeEnum.SOURCE) {
				OrderedProperties tempProperties = new OrderedProperties();
				tempProperties.putAll(fileMapping);
				globalConfig.setFileMapping(tempProperties);
			}

			App.invokeByGlobalConfig(globalConfig);
			getLog().info(Constants.PLUGIN_ID + " - resolve - end");

		} catch (Throwable t) {
			if (failOnError) {
				throw new MojoFailureException("execute fail ", t);
			} else {
				getLog().error("##############  Exception occurred during incrementArchive ###############");
				getLog().error(Throwables.getStackTraceAsString(t));
			}
		}
	}

	@Parameter(defaultValue = "${project}", readonly = true)
	protected MavenProject project;

	@Component
	protected MavenProjectHelper projectHelper;

	@Parameter(defaultValue = "${plugin.artifacts}")
	protected List<Artifact> pluginArtifacts;

	@Parameter(defaultValue = "${project.basedir}/tools.properties")
	protected File globalConfigPropertyFile;

	@Parameter(defaultValue = "${project.basedir}/file_mapping.properties")
	protected File fileMappingPropertyFile;

	@Parameter(required = true)
	protected String inputDir;

	@Parameter(required = true)
	protected String outputDir;

	@Parameter
	protected String sourceEnum;

	@Parameter
	protected String changeLogSourceFile;

	@Parameter
	protected String zipFileName;

	@Parameter
	protected String typeEnum;

	@Parameter
	protected String extensions;

	@Parameter
	private String name;

	@Parameter
	private String password;

	@Parameter
	private String url;

	@Parameter
	private String prefixPath;

	@Parameter
	private String startVersion;

	@Parameter
	private String endVersion = null;

	@Parameter
	private Properties fileMapping;

	@Parameter(defaultValue = "false")
	protected boolean failOnError;

}
