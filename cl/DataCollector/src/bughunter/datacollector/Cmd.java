package bughunter.datacollector;

import java.util.List;

import uk.co.flamingpenguin.jewel.cli.Option;

public interface Cmd {

	@Option(shortName = "r", description = "The raw data xml file")
	String getRawdata();

	boolean isRawdata();

	@Option(shortName = "l", description = "The list of lims")
	List<String> getLimlist();

	boolean isLimlist();

	@Option(shortName = "f", description = "The list file of lims")
	String getLimlistfile();

	boolean isLimlistfile();

	@Option(shortName = "b", description = "The binary data file")
	String getBinary();

	boolean isBinary();
	
	@Option(shortName = "d", description = "The dump file name")
	String getDumpfile();

	boolean isDumpfile();

	@Option(shortName = "o", description = "Save binary")
	String getOutput();

	boolean isOutput();

	@Option(description = "Dump before commits")
	String getDumpbeforefile();

	boolean isDumpbeforefile();

	@Option(description = "Save sourcelement-diff stats")
	String getDiffstat();

	boolean isDiffstat();

	@Option(description = "Csv output directory")
	String getCsvdir();

	boolean isCsvdir();

	@Option(description = "Traditional output directory")
	String getTraditionaldir();

	boolean isTraditionaldir();

	@Option(description = "Prev and last commit list")
	String getPrevlast();

	boolean isPrevlast();
	
	@Option(description = "First commits list")
	String getFirstCommits();

	boolean isFirstCommits();

	@Option(description = "Traditional commit list")
	String getTraditionalCommits();

	boolean isTraditionalCommits();

	@Option(description = "Dump data to csv")
	String getDumpDataCSV();

	boolean isDumpDataCSV();

	@Option(description = "Dump commits")
	String getDumpCommits();

	boolean isDumpCommits();

	@Option(description = "Dump issue commits")
	String getDumpIssueCommits();

	boolean isDumpIssueCommits();
	
	@Option(shortName = "c", description = "Compute metrics and bug numbers")
	boolean isComputemetrics();

	@Option(description = "Test filter list file")
	String getTestFilter();
	
	boolean isTestFilter();

	@Option(description = "No license list")
	String getNolicense();

	boolean isNolicense();

	@Option(description = "Missing lim list")
	String getMissinglim();

	boolean isMissinglim();
	
	@Option(description = "Release stat output")
	String getReleaseStat();

	boolean isReleaseStat();

	@Option(description = "Duplicated lims list")
	String getDuplicatedlims();

	boolean isDuplicatedlims();

	@Option(description = "Traditional timestamps")
	String getTraditionalTimestamp();

	boolean isTraditionalTimestamp();
	
	@Option(description = "Change path to", defaultValue = "")
	String getChangepathto();
	
	boolean isChangepathfrom();

	
	@Option(helpRequest = true, shortName = "h", description = "Print help")
	boolean getHelp();
}
