<assembly
	xmlns="http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.0 http://maven.apache.org/xsd/assembly-1.1.0.xsd">
	
	<id>add</id>
	<formats>
		<format>zip</format>
	</formats>
	<includeBaseDirectory>true</includeBaseDirectory>

	<fileSets>
		<fileSet>
			<outputDirectory></outputDirectory>
			<directory>${r'${project.build.directory}/${project.build.finalName}'}</directory>
			<includes>
				<#list filePaths as aFilePath>
				<include>${aFilePath}</include>
				</#list>
			</includes>
		</fileSet>
	</fileSets>
	
</assembly>
