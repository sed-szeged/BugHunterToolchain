package bughunter.issueminer;

import uk.co.flamingpenguin.jewel.cli.Option;

public interface Cmd {

	@Option(shortName = "r", description = "The raw data xml file")
	String getRawData();
	
	@Option(shortName = "l", description = "The output file for the last referenced or closed event's commit ids of the issues.")
	String getLastCommitsOutFile();
	boolean isLastCommitsOutFile();
	
	@Option(shortName = "g", description = "The output file for the first commit ids of the issues.")
	String getFirstCommitsOutFile();
	boolean isFirstCommitsOutFile();
	
	@Option(shortName = "b", description = "The output file for the commit ids whose are before the first commit of the issues.")
	String getBeforeCommitsOutFile();
	boolean isBeforeCommitsOutFile();
	
	@Option(shortName = "k", description = "The output file for the commit ids whose are before the first commit of the issues with the last commit of the issue.")
	String getBeforeAndLastCommitsOutFile();
	boolean isBeforeAndLastCommitsOutFile();
	
	@Option(shortName = "f", description = "The output file for the commit ids whose are referencing issues.")
	String getReferencedCommitsOutFile();
	boolean isReferencedCommitsOutFile();

	@Option(shortName = "s", description = "The output file for the statistics.")
	String getStatOutFile();
	boolean isStatOutFile();
	
	@Option(shortName = "w", description = "Working directory for checking commit.")
	String getWorkDir();
	boolean isWorkDir();
	
	@Option(shortName = "a", description = "all commit.")
	String getAllCommitFile();
	boolean isAllCommitFile();
	
	@Option(shortName = "m", description = "The output file for commit ids that belong to multiple issues.")
	String getCommitsToMultipleIssuesOutFile();
	boolean isCommitsToMultipleIssuesOutFile();

	@Option(helpRequest = true, shortName = "h", description = "Print help")
	boolean getHelp();
}
