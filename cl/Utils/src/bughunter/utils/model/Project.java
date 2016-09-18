package bughunter.utils.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.Serializable;

import bughunter.utils.StringStorage;

public class Project implements Serializable {

	private static final long serialVersionUID = 378807597375897346L;
	
	public String user;
	public String repo;
	public Map<String, Commit> commits;
	public Map<Integer, User> contributors;
	public Map<Integer, Issue> issues;

	public StringStorage strTable;
	
	public Set<String> neededCommits;
	
	public List<Commit> sortedCommits;
	
	public Set<String> testFilters;
	
	public Project(StringStorage str) {
		this.strTable = str;
		this.commits = new HashMap<>();
		this.contributors = new HashMap<>();
		this.issues = new HashMap<>();
		this.testFilters = new HashSet<>();
		this.sortedCommits = new ArrayList<>();
		this.neededCommits = new HashSet<>();
	}
	
	public void collectNeededCommtis() {
		for (Issue issue : issues.values()) {
			if(issue.prev == null && issue.last == null)
				continue;
			neededCommits.add(issue.prev.sha);
			neededCommits.add(issue.last.sha);
		}
	}
	
	public void setCommits(Map<String, Commit> commits) {
		this.commits = commits;
		sortedCommits = new ArrayList<Commit>(commits.values());
		Collections.sort(sortedCommits, new Comparator<Commit>() {
			public int compare(Commit o1, Commit o2) {
				if (o1.created.after(o2.created)) return 1;
				else if (o1.created.before(o2.created)) return -1;
				else return 0;
			}
		});
	}

	public boolean isFiltered(String nameAttr) {
		for(String ss : testFilters) {
			if(nameAttr.matches(ss)) {
				return true;
			}
		}
		return false;
	}
}
