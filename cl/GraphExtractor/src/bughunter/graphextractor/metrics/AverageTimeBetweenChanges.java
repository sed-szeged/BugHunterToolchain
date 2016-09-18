package bughunter.graphextractor.metrics;

import bughunter.graphextractor.Metric;

public class AverageTimeBetweenChanges extends Metric {

	public AverageTimeBetweenChanges() {
		super("Average Time Between Changes");
	}

	@Override
	public String getFileQuery(String sha) {
		return "match (n:FILE)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (cc2:COMMIT)-[:CHANGED]->(n)<-[:CHANGED]-(cc:COMMIT) "
				+"where c.created_ts >= cc.created_ts and cc2.created_ts < cc.created_ts "
				+"with n.filename as name, (cc.created_ts - max(cc2.created_ts))/1000/60/60 as t "
				+"return name, round(avg(t)) as `" + name + "`";
	}

	@Override
	public String getMethodQuery(String sha) {
		return "match (n:METHOD)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (cc2:COMMIT)-[:CHANGED]->(n)<-[:CHANGED]-(cc:COMMIT) "
				+"where c.created_ts >= cc.created_ts and cc2.created_ts < cc.created_ts "
				+"with n.name as name, (cc.created_ts - max(cc2.created_ts))/1000/60/60 as t "
				+"return name, round(avg(t)) as `" + name + "`";
	}

	@Override
	public String getClassQuery(String sha) {
		return "match (n:CLASS)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (cc2:COMMIT)-[:CHANGED]->(n)<-[:CHANGED]-(cc:COMMIT) "
				+"where c.created_ts >= cc.created_ts and cc2.created_ts < cc.created_ts "
				+"with n.name as name, (cc.created_ts - max(cc2.created_ts))/1000/60/60 as t "
				+"return name, round(avg(t)) as `" + name + "`";
	}

}
