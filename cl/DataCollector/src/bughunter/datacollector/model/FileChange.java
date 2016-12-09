package bughunter.datacollector.model;

import java.io.Serializable;

public class FileChange implements Serializable {

	private static final long serialVersionUID = -8636011459211818226L;
	private String diff;
	private Status status;
	private int additions;
	private int deletions;
	private int changes;

	private File modifiedFile;


	public enum Status {
		Modified, Deleted, Added
	}

	public FileChange() {
	}

	public FileChange(String diff, Status status, int additions, int deletions, int changes, File modifiedFile) {

		this.diff = diff;
		this.status = status;
		this.additions = additions;
		this.deletions = deletions;
		this.changes = changes;
		this.modifiedFile = modifiedFile;

	}

	public String getDiff() {
		return diff;
	}

	public void setDiff(String diff) {
		this.diff = diff;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public int getAdditions() {
		return additions;
	}

	public void setAdditions(int additions) {
		this.additions = additions;
	}

	public int getDeletions() {
		return deletions;
	}

	public void setDeletions(int deletions) {
		this.deletions = deletions;
	}

	public int getChanges() {
		return changes;
	}

	public void setChanges(int changes) {
		this.changes = changes;
	}

	public File getModifiedFile() {
		return modifiedFile;
	}

	public void setModifiedFile(File modifiedFile) {
		this.modifiedFile = modifiedFile;
	}
}
