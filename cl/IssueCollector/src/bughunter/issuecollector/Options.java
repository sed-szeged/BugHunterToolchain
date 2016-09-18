package bughunter.issuecollector;

import java.util.List;

public class Options {

	private String repo;
	private String repouser;
	private String username;
	private String password;
	private List<String> labels;
	private String out;

	public Options() {

	}

	public Options(String repo, String repouser, String username, String password, List<String> labels, String out) {
		super();
		this.repo = repo;
		this.repouser = repouser;
		this.username = username;
		this.password = password;
		this.labels = labels;
		this.out = out;
	}

	public String getRepo() {
		return repo;
	}

	public void setRepo(String repo) {
		this.repo = repo;
	}

	public String getRepouser() {
		return repouser;
	}

	public void setRepouser(String repouser) {
		this.repouser = repouser;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

	public String getOut() {
		return out;
	}

	public void setOut(String out) {
		this.out = out;
	}

}
