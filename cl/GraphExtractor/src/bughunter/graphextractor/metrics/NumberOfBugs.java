package bughunter.graphextractor.metrics;

import bughunter.graphextractor.Metric;

public class NumberOfBugs extends Metric {

	public NumberOfBugs() {
		super("Number Of Bugs");
	}

	@Override
	public String getFileQuery(String sha) {
		return "match (n:FILE{filtered:false})<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[:CHANGED]-(cc:COMMIT)<-[:REFERENCED_FROM]-(i:ISSUE)-[:PREV]->(prev:COMMIT) "
				+"where c.created_ts < cc.created_ts and (i.opened_ts < c.created_ts or prev = c) "
				+"return n.filename as name, count(DISTINCT i) as `" + name + "`";
	}

	@Override
	public String getMethodQuery(String sha) {
		return "match (n:METHOD{filtered:false})<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[:CHANGED]-(cc:COMMIT)<-[:REFERENCED_FROM]-(i:ISSUE)-[:PREV]->(prev:COMMIT) "
				+"where c.created_ts < cc.created_ts and (i.opened_ts < c.created_ts or prev = c) "
				+"return n.name as name, count(DISTINCT i) as `" + name + "`";
	}

	@Override
	public String getClassQuery(String sha) {
		return "match (n:CLASS{filtered:false})<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[:CHANGED]-(cc:COMMIT)<-[:REFERENCED_FROM]-(i:ISSUE)-[:PREV]->(prev:COMMIT) "
				+"where c.created_ts < cc.created_ts and (i.opened_ts < c.created_ts or prev = c) "
				+"return n.name as name, count(DISTINCT i) as `" + name + "`";
	}

}
