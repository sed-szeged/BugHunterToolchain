package bughunter.datacollector.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import bughunter.datacollector.StrTable;

public class File implements Serializable {

	private static final long serialVersionUID = 8402617846235722916L;
	private int filename;
	private Map<Integer, Float> metrics;

	private StrTable strTable;
	
	private int occurence=0;
	private Set<Integer> modifyingUsers;
	//private int usercount = modifyingUsers.size();
	private int lastModifierCommitCount;
	private int fixes=0;
	
	public File(StrTable str) {
		this.strTable = str;
		metrics = new HashMap<Integer, Float>();
	}

	public File(StrTable str, String filename) {
		this.strTable = str;
		setFilename(filename);
		metrics = new HashMap<Integer, Float>();
	}
	
	public File(StrTable str, String filename, Integer LLOC, Integer LOC) {

		this.strTable = str;
		
		setFilename(filename);
		metrics = new HashMap<Integer, Float>();

	}

	public StrTable getStrTable() {
		return strTable;
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

	public Map<Integer, Float> getMetrics() {
		return metrics;
	}

	public void setMetrics(Map<Integer, Float> metrics) {
		this.metrics = metrics;
	}

	public int getOccurence() {
		return occurence;
	}

	public void setOccurence(int occurence) {
		this.occurence = occurence;
	}

	public Set<Integer> getModifyingUsers() {
		return modifyingUsers;
	}

	public void setModifyingUsers(Set<Integer> modifyingUsers) {
		this.modifyingUsers = modifyingUsers;
	}

	public int getLastModifierCommitCount() {
		return lastModifierCommitCount;
	}

	public void setLastModifierCommitCount(int lastModifierCommitCount) {
		this.lastModifierCommitCount = lastModifierCommitCount;
	}

	public int getFixes() {
		return fixes;
	}

	public void setFixes(int fixes) {
		this.fixes = fixes;
	}
}
