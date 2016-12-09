package bughunter.issueminer;

public class Options {

	private String rawData;
	private String lastCommitsOutFile;
	private String firstCommitsOutFile;
	private String beforeCommitsOutFile;
	private String referencedCommitsOutFile;
	private String beforeAndLastCommitsOutFile;
	private String statOutFile;
	private String commitsToMultipleIssuesOutFile;
	private String workDir;
	private String allCommitFile;

	public Options() {

	}

	

	public Options(String rawData, String lastCommitsOutFile,
			String firstCommitsOutFile, String beforeCommitsOutFile,
			String referencedCommitsOutFile,
			String beforeAndLastCommitsOutFile, String statOutFile,
			String commitsToMultipleIssuesOutFile, String workDir,
			String allCommitFile) {
		super();
		this.rawData = rawData;
		this.lastCommitsOutFile = lastCommitsOutFile;
		this.firstCommitsOutFile = firstCommitsOutFile;
		this.beforeCommitsOutFile = beforeCommitsOutFile;
		this.referencedCommitsOutFile = referencedCommitsOutFile;
		this.beforeAndLastCommitsOutFile = beforeAndLastCommitsOutFile;
		this.statOutFile = statOutFile;
		this.commitsToMultipleIssuesOutFile = commitsToMultipleIssuesOutFile;
		this.workDir = workDir;
		this.allCommitFile = allCommitFile;
	}



	public String getAllCommitFile() {
		return allCommitFile;
	}



	public void setAllCommitFile(String allCommitFile) {
		this.allCommitFile = allCommitFile;
	}



	public String getWorkDir() {
		return workDir;
	}

	public void setWorkDir(String workDir) {
		this.workDir = workDir;
	}

	public String getCommitsToMultipleIssuesOutFile() {
		return commitsToMultipleIssuesOutFile;
	}

	public void setCommitsToMultipleIssuesOutFile(
			String commitsToMultipleIssuesOutFile) {
		this.commitsToMultipleIssuesOutFile = commitsToMultipleIssuesOutFile;
	}

	public String getRawData() {
		return rawData;
	}

	public void setRawData(String rawData) {
		this.rawData = rawData;
	}

	public String getLastCommitsOutFile() {
		return lastCommitsOutFile;
	}

	public void setLastCommitsOutFile(String lastCommitsOutFile) {
		this.lastCommitsOutFile = lastCommitsOutFile;
	}

	public String getFirstCommitsOutFile() {
		return firstCommitsOutFile;
	}

	public void setFirstCommitsOutFile(String firstCommitsOutFile) {
		this.firstCommitsOutFile = firstCommitsOutFile;
	}

	public String getBeforeCommitsOutFile() {
		return beforeCommitsOutFile;
	}

	public void setBeforeCommitsOutFile(String beforeCommitsOutFile) {
		this.beforeCommitsOutFile = beforeCommitsOutFile;
	}

	public String getReferencedCommitsOutFile() {
		return referencedCommitsOutFile;
	}

	public void setReferencedCommitsOutFile(String referencedCommitsOutFile) {
		this.referencedCommitsOutFile = referencedCommitsOutFile;
	}

	public String getBeforeAndLastCommitsOutFile() {
		return beforeAndLastCommitsOutFile;
	}

	public void setBeforeAndLastCommitsOutFile(
			String beforeAndLastCommitsOutFile) {
		this.beforeAndLastCommitsOutFile = beforeAndLastCommitsOutFile;
	}

	public String getStatOutFile() {
		return statOutFile;
	}

	public void setStatOutFile(String statOutFile) {
		this.statOutFile = statOutFile;
	}

}
