package org.fartpig.incrementarchive.source;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fartpig.incrementarchive.constant.ChangeLogTypeEnum;
import org.fartpig.incrementarchive.constant.GlobalConfig;
import org.fartpig.incrementarchive.constant.GlobalConst;
import org.fartpig.incrementarchive.entity.ChangeLogEntry;
import org.fartpig.incrementarchive.entity.ChangeLogManifest;
import org.fartpig.incrementarchive.util.ToolException;
import org.fartpig.incrementarchive.util.ToolLogger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class XmlChangeLogDataSource extends AbstractChangeLogDataSource implements ChangeLogDataSource {

	@Override
	public ChangeLogManifest loadChangeLog() {
		GlobalConfig config = GlobalConfig.instance();
		String sourceFilePath = config.getChangeLogSourceFile();
		ChangeLogManifest manifest = new ChangeLogManifest();
		manifest.setLogType(typeEnum);

		// example: <files type="SOURCE"><set name="add" relative="true"><file
		// relative="false">/fdas/fdas</file></set></files>
		Set<String> fileNameSet = new HashSet<String>();

		SAXBuilder jdomBuilder = new SAXBuilder();
		try {
			Document dom = jdomBuilder.build(sourceFilePath);

			Element root = dom.getRootElement();
			ChangeLogTypeEnum typeEnum = config.getTypeEnum();
			Attribute typeAttr = root.getAttribute("type");
			if (typeAttr != null) {
				String typeValue = typeAttr.getValue();
				typeEnum = ChangeLogTypeEnum.valueOf(typeValue.toUpperCase());
			}
			config.setTypeEnum(typeEnum);
			manifest.setLogType(typeEnum);

			List<Element> setElements = root.getChildren("set");

			for (Element aElement : setElements) {
				Attribute nameAttr = aElement.getAttribute("name");
				if (nameAttr == null && setElements.size() > 1) {
					throw new ToolException(ToolLogger.getInstance().getCurrentPhase(),
							"not name found in the set element");
				}
				String setValue = GlobalConst.SET_MODIFY;
				if (nameAttr != null) {
					setValue = nameAttr.getValue();
				}

				Attribute relativeAttr = aElement.getAttribute("relative");
				boolean isRelative = true;
				if (relativeAttr != null) {
					isRelative = Boolean.valueOf(relativeAttr.getValue());
				}

				List<Element> fileElements = aElement.getChildren("file");
				for (Element aFileElement : fileElements) {

					Attribute fileRelativeAttr = aFileElement.getAttribute("relative");
					if (fileRelativeAttr != null) {
						isRelative = Boolean.valueOf(fileRelativeAttr.getValue());
					}

					String fileName = aFileElement.getText();
					// remove the prefix path
					String prefixPath = config.getPrefixPath();
					if (fileName.startsWith(prefixPath)) {
						fileName = fileName.substring(prefixPath.length());
					}
					List<ChangeLogEntry> entries = convertFromFileNameToChangeLogEntry(isRelative, fileName, config);

					for (ChangeLogEntry logEntry : entries) {
						if (fileNameSet.contains(logEntry.absolutePath())) {
							ToolLogger.getInstance().info(
									"duplicate file name:" + fileName + "- absolute name:" + logEntry.absolutePath());
							continue;
						}

						fileNameSet.add(logEntry.absolutePath());

						if (GlobalConst.SET_ADD.equalsIgnoreCase(setValue)) {
							manifest.getAddEntries().add(logEntry);
						} else if (GlobalConst.SET_REMOVE.equalsIgnoreCase(setValue)) {
							manifest.getRemoveEntries().add(logEntry);
						} else if (GlobalConst.SET_MODIFY.equalsIgnoreCase(setValue)) {
							manifest.getModifyEntries().add(logEntry);
						} else {
							ToolLogger.getInstance().info("not entry set found - file name:" + fileName);
						}
					}
				}
			}
		} catch (JDOMException e) {
			ToolLogger.getInstance().error("error:", e);
			throw new ToolException(ToolLogger.getInstance().getCurrentPhase(), "error parse xml dom");
		} catch (IOException e) {
			ToolLogger.getInstance().error("error:", e);
			throw new ToolException(ToolLogger.getInstance().getCurrentPhase(), "xml file not found");
		}

		return manifest;
	}

}
