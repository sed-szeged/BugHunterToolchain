package bughunter.utils.model;

import java.io.Serializable;

public class FileChange implements Serializable {

	private static final long serialVersionUID = -8636011459211818226L;
	public String diff;
	public Status status;
	public int additions;
	public int deletions;
	public int changes;

	public File modifiedFile;


	public enum Status {
		Modified, Deleted, Added
	}

	public FileChange() {
	}

}
