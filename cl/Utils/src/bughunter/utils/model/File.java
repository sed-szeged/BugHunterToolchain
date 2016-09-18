package bughunter.utils.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import bughunter.utils.StringStorage;

public class File implements Serializable {

	private static final long serialVersionUID = 8402617846235722916L;
	public int filename;
	public int loc;
	public Map<Integer, Float> metrics;

	public StringStorage strTable;
	
	public int occurence=0;
	public Set<Integer> modifyingUsers;
	//private int usercount = modifyingUsers.size();
	public int lastModifierCommitCount;
	public int fixes=0;
	
	public File(StringStorage str) {
		this.strTable = str;
		metrics = new HashMap<Integer, Float>();
	}

	public File(StringStorage str, String filename) {
		this.strTable = str;
		setFilename(filename);
		metrics = new HashMap<Integer, Float>();
	}
	
	public File(StringStorage str, String filename, Integer LLOC, Integer LOC) {

		this.strTable = str;
		
		setFilename(filename);
		metrics = new HashMap<Integer, Float>();

	}

	public String getFilename() {
		return strTable.get(filename);
	}

	public int getFilenameId() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = strTable.set(filename);
	}

	public boolean isJava() {
		return getFilename().matches(".*\\.java");
	}

}
