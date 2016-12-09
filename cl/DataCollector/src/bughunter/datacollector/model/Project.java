package bughunter.datacollector.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.Serializable;

import bughunter.datacollector.StrTable;

public class Project implements Serializable {

	private static final long serialVersionUID = 378807597375897346L;
	private String user;
	private String repo;
	private Map<String, Commit> commits;
	private Map<Integer, User> contributors;
	private Map<Integer, Issue> issues;

	private StrTable strTable;
	
	private Set<String> neededCommits;
	
	private List<Commit> sortedCommits;
	
	public List<Commit> getSortedCommits() {
		return sortedCommits;
	}

	public void setSortedCommits(List<Commit> sortedCommits) {
		this.sortedCommits = sortedCommits;
	}

	public Set<String> getNeededCommits() {
		return neededCommits;
	}

	public void setNeededCommits(Set<String> neededCommits) {
		this.neededCommits = neededCommits;
	}

	private Set<String> testFilters;
	
	public Set<String> getTestFilters() {
		return testFilters;
	}

	public Project() {
		
	}
	
	public Project(StrTable str) {
		this.strTable = str;
		commits = new HashMap<String, Commit>();
		contributors = new HashMap<Integer, User>();
		issues = new HashMap<Integer, Issue>();
		testFilters = new HashSet<String>();
		sortedCommits = new ArrayList<Commit>();
		neededCommits = new HashSet<String>();
	}

	public Project(StrTable str, String user, String repo, Map<String, Commit> commits, Map<Integer, User> contributors,
			Map<Integer, Issue> issues) {

		this.strTable = str;
		
		this.user = user;
		this.repo = repo;
		this.commits = commits;
		this.contributors = contributors;
		this.issues = issues;
		testFilters = new HashSet<String>();
		neededCommits = new HashSet<String>();
		sortedCommits = new ArrayList<Commit>(commits.values());
	}

	public void collectNeededCommtis() {
		for (Issue issue : issues.values()) {
			if(issue.getPrev() == null && issue.getLast() == null)
				continue;
			neededCommits.add(issue.getPrev().getSha());
			neededCommits.add(issue.getLast().getSha());
		}
	}
	
	public StrTable getStrTable() {
		return strTable;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getRepo() {
		return repo;
	}

	public void setRepo(String repo) {
		this.repo = repo;
	}

	public Map<String, Commit> getCommits() {
		return commits;
	}

	public void setCommits(Map<String, Commit> commits) {
		this.commits = commits;
		sortedCommits = new ArrayList<Commit>(commits.values());
		Collections.sort(sortedCommits, new Comparator<Commit>() {
			public int compare(Commit o1, Commit o2) {
				if (o1.getCreated().after(o2.getCreated())) return 1;
				else if (o1.getCreated().before(o2.getCreated())) return -1;
				else return 0;
			}
		});
	}

	public Map<Integer, User> getContributors() {
		return contributors;
	}

	public void setContributors(Map<Integer, User> contributors) {
		this.contributors = contributors;
	}

	public Map<Integer, Issue> getIssues() {
		return issues;
	}

	public void setIssues(Map<Integer, Issue> issues) {
		this.issues = issues;
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
