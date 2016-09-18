package bughunter.graphextractor.metrics;

import bughunter.graphextractor.Metric;

public class NumberOfContributors extends Metric {

	public NumberOfContributors() {
		super("Number Of Contributors");
	}

	@Override
	public String getFileQuery(String sha) {
		return "match (n:FILE)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[:CHANGED]-(cc:COMMIT)-[:AUTHOR]->(u:USER) "
				+"where cc.created_ts <= c.created_ts "
				+"return n.filename as name, count(DISTINCT u) as `" + name + "`";
	}

	@Override
	public String getMethodQuery(String sha) {
		return "match (n:METHOD)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[:CHANGED]-(cc:COMMIT)-[:AUTHOR]->(u:USER) "
				+"where cc.created_ts <= c.created_ts "
				+"return n.name as name, count(DISTINCT u) as `" + name + "`";
	}

	@Override
	public String getClassQuery(String sha) {
		return "match (n:CLASS)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[:CHANGED]-(cc:COMMIT)-[:AUTHOR]->(u:USER) "
				+"where cc.created_ts <= c.created_ts "
				+"return n.name as name, count(DISTINCT u) as `" + name + "`";
	}

}
