package org.fartpig.incrementarchive.phase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fartpig.incrementarchive.constant.GlobalConfig;
import org.fartpig.incrementarchive.constant.GlobalConst;
import org.fartpig.incrementarchive.entity.ChangeLogEntry;
import org.fartpig.incrementarchive.entity.ChangeLogFileEntry;
import org.fartpig.incrementarchive.entity.ChangeLogManifest;
import org.fartpig.incrementarchive.util.PathUtil;
import org.fartpig.incrementarchive.util.ToolException;
import org.fartpig.incrementarchive.util.ToolLogger;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class AssembleFileGenerateAction extends AbstractFileOpAction {

	private static final String CURRENT_PHASE = GlobalConst.PHASE_GENERATE_ASSEMBLE;

	public AssembleFileGenerateAction() {
		ToolLogger.getInstance().setCurrentPhase(CURRENT_PHASE);
	}

	public void generateAssembleFile(String inputDir, String outputDir, ChangeLogManifest manifest,
			GlobalConfig config) {
		// 根据change log清单，按照规则进行复制
		// filter the output file by the extensions
		String[] extensions = config.getExtensions();
		List<ChangeLogEntry> addEntries = manifest.getAddEntries();
		List<ChangeLogEntry> modifyEntries = manifest.getModifyEntries();
		List<ChangeLogEntry> removeEntries = manifest.getRemoveEntries();

		if (config.isFilterExtension()) {
			addEntries = filterEntryByExtensions(addEntries, extensions);
			modifyEntries = filterEntryByExtensions(modifyEntries, extensions);
			removeEntries = filterEntryByExtensions(removeEntries, extensions);
		}

		// only handle the add and modify entry
		// render the file info to the assemble file, with freemarker template
		Configuration cfg = new Configuration();
		try {
			String templateFilePath = config.getAssembleTemplate();
			File templateFile = new File(templateFilePath);
			if (!templateFile.exists()) {
				throw new ToolException(CURRENT_PHASE, "assemble template not found");
			}

			FileTemplateLoader loader = new FileTemplateLoader(templateFile.getParentFile());
			cfg.setTemplateLoader(loader);
			Template template = cfg.getTemplate(templateFile.getName());

			Map<String, Object> data = new HashMap<String, Object>();
			List<String> filePaths = new ArrayList<String>();

			List<ChangeLogEntry> allEntries = new ArrayList<ChangeLogEntry>();
			List<ChangeLogEntry> tempEntries = new ArrayList<ChangeLogEntry>();
			tempEntries.addAll(addEntries);
			tempEntries.addAll(modifyEntries);

			for (ChangeLogEntry aEntry : tempEntries) {
				if (aEntry instanceof ChangeLogFileEntry) {
					List<ChangeLogFileEntry> tempFielEntries = addExtraEntryForClass((ChangeLogFileEntry) aEntry);
					allEntries.addAll(tempFielEntries);
				} else {
					allEntries.add(aEntry);
				}
			}

			for (ChangeLogEntry aEntry : allEntries) {

				String relativePath = PathUtil.getRelativePath(aEntry.absolutePath(), inputDir);
				// remove the first separator
				if (relativePath.charAt(0) == File.separatorChar) {
					relativePath = relativePath.substring(1);
				}

				// replace the \ to /
				relativePath = relativePath.replace('\\', '/');

				ToolLogger.getInstance().info("modify relativePath:" + relativePath);
				filePaths.add(relativePath);
			}

			data.put("filePaths", filePaths);

			String outputFileName = outputDir + File.separator + "assemble.xml";
			ToolLogger.getInstance().info("assemble file path:" + outputFileName);
			Writer file = new FileWriter(new File(outputFileName));
			template.process(data, file);
			file.flush();
			file.close();

		} catch (IOException e) {
			ToolLogger.getInstance().error("error:", e);
		} catch (TemplateException e) {
			ToolLogger.getInstance().error("error:", e);
		}

	}
}
