# incrementArchive

this tools used for increment archive the zip output.

It aim to  the websphere increment deployment.    
* support the file extensions filter.  
* support retrieve changlong from the text, svn, git, xml format.   
* support the src file to target file mapping for the folder or file both. 

# usage

this lib include two ways:

## one:
use this by a executable jar in command line model:

commands usage->

	  java -jar incrementArchive.jar [-exts ${the file need to increment archive}] [-fil ${the switch for the filter}] [-s ${the changelog text file path}] [-st ${changelog source type:CHANGE_LOG_XML, CHANGE_LOG_TXT, CHANGE_LOG_GIT, CHANGE_LOG_SVN}] [-t ${sourceType type:SOURCE, OUTPUT}] [-zfn ${the result zip file name}] [-n ${the name for svn or git}] [-p ${the password for svn or git}] [-u ${svn or git url}] [-sv ${svn or git start version}] [-ev ${svn or git end version}] [-pp ${svn prefix path to substr}] ${inputDir} ${outputDir}
	
	  -exts : the file extensions for increment archive, use the comma to split the extension  
	  -fil: the switch bool to filter file by extensions[true, false]  
	  -s: the changlog text file path  
	  -st: changlog source type [CHANGE_LOG_XML, CHANGE_LOG_TXT, CHANGE_LOG_GIT, CHANGE_LOG_SVN]   
	  -t :the file source type [SOURCE, OUTPUT], if use the SOURCE should need use the mapping  
	  -zfn: the increment archive zip file name  
	  -n: the svn or git usename
	  -p: the svn or git passowrd
	  -u :the svn or git url
	  -sv: the svn or git start version(NOTE: svn use the number, git use the hash string)  
	  -ev: the svn or git end version, this can not be set, default the latest version
	  -pp: the prefix path for substr
	  inputDir: the input path for the file
	  outputDir: the out path for the increment archive

besides you can use a file named tools.properties to set the params:

	inputDir: the same with the inputDir above
	outputDir: the same with the outputDir above
	zipFileName: the same with the -zfn above
	sourceEnum: the same with the -t above
	changeLogSourceFile: the same with the -st above
	typeEnum: the same with the -t above
	extensions: the same with the -exts above
	name : the same with the -n above
	password: the same with the -p above
	urlPath: the same with the -u above
	startVersion: the same with the -sv above
	endVersion: the same with the -ev above
	prefixPath: the same with the -pp above  

also you can set the filemapping.properties, this use the wildcard the match the word or char(? or *), for example:  

	/src/main/java/*.java=/target/classes/*.class

    
## two
use the maven plugin to do, but the plugin you should put to your private respository. It is not in the center repo.

example:

	<plugin>
          <groupId>org.fartpig</groupId>
          <artifactId>incrementArchive-maven-plugin</artifactId>
          <version>0.2.0-RELEASE</version>
          <executions>
			<execution>
				<id>test-incrementArchive</id>
				<goals>
					<goal>
						incrementArchive
					</goal>
				</goals>
			</execution>
		</executions>
         <configuration>
			<inputDir>D:/cmis-main.ear</inputDir>
			<outputDir>${basedir}/fjcmis-target</outputDir>
			<changeLogSourceFile>D:/workspace-my/fjcmis-file.txt</changeLogSourceFile>
			<zipFileName>fjcmis.zip</zipFileName>
		</configuration>
	</plugin>
