package bughunter.graphextractor.metrics;

import bughunter.graphextractor.Metric;

public class LastContributorCommits extends Metric {

	public LastContributorCommits() {
		super("Last Contributor Commits");
	}

	@Override
	public String getFileQuery(String sha) {
		return "match (n:FILE)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[:CHANGED]-(cc:COMMIT) "
				+"where c.created_ts >= cc.created_ts "
				+"with n, max(cc.created_ts) as mm "
				+"match (n)<-[:CHANGED]-(cc:COMMIT)-[:AUTHOR]->(u:USER) "
				+"where cc.created_ts = mm "
				+"return n.filename as name, max(u.number_of_commits) as `" + name + "`";
	}

	@Override
	public String getMethodQuery(String sha) {
		return "match (n:METHOD)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[:CHANGED]-(cc:COMMIT) "
				+"where c.created_ts >= cc.created_ts "
				+"with n, max(cc.created_ts) as mm "
				+"match (n)<-[:CHANGED]-(cc:COMMIT)-[:AUTHOR]->(u:USER) "
				+"where cc.created_ts = mm "
				+"return n.name as name, max(u.number_of_commits) as `" + name + "`";
	}

	@Override
	public String getClassQuery(String sha) {
		return "match (n:CLASS)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[:CHANGED]-(cc:COMMIT) "
				+"where c.created_ts >= cc.created_ts "
				+"with n, max(cc.created_ts) as mm "
				+"match (n)<-[:CHANGED]-(cc:COMMIT)-[:AUTHOR]->(u:USER) "
				+"where cc.created_ts = mm "
				+"return n.name as name, max(u.number_of_commits) as `" + name + "`";
	}

}
