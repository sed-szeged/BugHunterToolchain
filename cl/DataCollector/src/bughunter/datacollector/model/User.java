package bughunter.datacollector.model;

import java.io.Serializable;

public class User implements Serializable {

	private static final long serialVersionUID = 6192897743359734477L;
	private int id;
	private int commits;

	public User() {

	}

	public User(int id, int commits) {

		this.id = id;
		this.commits = commits;

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCommits() {
		return commits;
	}

	public void setCommits(int commits) {
		this.commits = commits;
	}
}
