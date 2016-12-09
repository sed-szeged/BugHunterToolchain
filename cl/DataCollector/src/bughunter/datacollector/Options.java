package bughunter.datacollector;

import java.util.ArrayList;
import java.util.List;

public class Options {

	private String rawData;
	private List<String> limFiles;
	private boolean isLimFiles;
	private String binary;
	private boolean computeMetrics;
	private String output;
	private String dumpfile;
	private String csvdir;
	private String prevlast;
	private String firstCommits;
	private String diffstat;
	private String dumpbefore;
	private String dumpDataCSV;
	private String testFilter;
	private String nolicense;
	private String traditionalCommits;
	private String traditionaldir;
	private String duplicatedlims;
	private String missinglimg;
	private String traditionalTimestamp;
	private String releasestat;
	private String dumpCommits;
	private String dumpIssueCommits;

	public String getTraditionalTimestamp() {
		return traditionalTimestamp;
	}

	public void setTraditionalTimestamp(String traditionalTimestamp) {
		this.traditionalTimestamp = traditionalTimestamp;
	}

	public String getDuplicatedlims() {
		return duplicatedlims;
	}

	public void setDuplicatedlims(String duplicatedlims) {
		this.duplicatedlims = duplicatedlims;
	}

	public String getMissinglimg() {
		return missinglimg;
	}

	public void setMissinglimg(String missinglimg) {
		this.missinglimg = missinglimg;
	}

	public String getTraditionaldir() {
		return traditionaldir;
	}

	public void setTraditionaldir(String traditionaldir) {
		this.traditionaldir = traditionaldir;
	}

	public String getTraditionalCommits() {
		return traditionalCommits;
	}

	public void setTraditionalCommits(String traditionalCommits) {
		this.traditionalCommits = traditionalCommits;
	}

	public String getNolicense() {
		return nolicense;
	}

	public void setNolicense(String nolicense) {
		this.nolicense = nolicense;
	}

	public String getTestFilter() {
		return testFilter;
	}

	public void setTestFilter(String testFilter) {
		this.testFilter = testFilter;
	}

	public String getDumpDataCSV() {
		return dumpDataCSV;
	}

	public void setDumpDataCSV(String dumpDataCSV) {
		this.dumpDataCSV = dumpDataCSV;
	}

	public Options() {
		limFiles = new ArrayList<String>();
	}

	public String getRawData() {
		return rawData;
	}

	public void setRawData(String rawData) {
		this.rawData = rawData;
	}

	public List<String> getLimFiles() {
		return limFiles;
	}

	public void setLimFiles(List<String> limFiles) {
		this.limFiles = limFiles;
	}

	public String getBinary() {
		return binary;
	}

	public void setBinary(String binary) {
		this.binary = binary;
	}

	public boolean isLimFiles() {
		return isLimFiles;
	}

	public void setLimFiles(boolean isLimFiles) {
		this.isLimFiles = isLimFiles;
	}

	public boolean isComputeMetrics() {
		return computeMetrics;
	}

	public void setComputeMetrics(boolean comupeMetrics) {
		this.computeMetrics = comupeMetrics;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getDumpfile() {
		return dumpfile;
	}

	public void setDumpfile(String dumpfile) {
		this.dumpfile = dumpfile;
	}

	public String getPrevlast() {
		return prevlast;
	}

	public void setPrevlast(String prevlast) {
		this.prevlast = prevlast;
	}


	public String getFirstCommits() {
		return firstCommits;
	}

	public void setFirstCommits(String firstCommits) {
		this.firstCommits = firstCommits;
	}

	public String getCsvdir() {
		return csvdir;
	}

	public void setCsvdir(String csvdir) {
		this.csvdir = csvdir;
	}

	public String getDumpbefore() {
		return dumpbefore;
	}

	public void setDumpbefore(String dumpbefore) {
		this.dumpbefore = dumpbefore;
	}

	public String getDiffstat() {
		return diffstat;
	}

	public void setDiffstat(String diffstat) {
		this.diffstat = diffstat;
	}

	public String getReleasestat() {
		return releasestat;
	}

	public void setReleasestat(String releasestat) {
		this.releasestat = releasestat;
	}

	public String getDumpCommits() {
		return dumpCommits;
	}

	public void setDumpCommits(String dumpCommits) {
		this.dumpCommits = dumpCommits;
	}

	public String getDumpIssueCommits() {
		return dumpIssueCommits;
	}

	public void setDumpIssueCommits(String dumpIssueCommits) {
		this.dumpIssueCommits = dumpIssueCommits;
	}

}
