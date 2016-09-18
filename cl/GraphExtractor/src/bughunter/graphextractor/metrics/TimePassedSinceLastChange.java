package bughunter.graphextractor.metrics;

import bughunter.graphextractor.Metric;

public class TimePassedSinceLastChange extends Metric {

	public TimePassedSinceLastChange() {
		super("Time Past Since Last Change");
	}

	@Override
	public String getFileQuery(String sha) {
		return "match (n:FILE)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[:CHANGED]-(cc:COMMIT) "
				+"where c.created_ts >= cc.created_ts "
				+"return n.filename as name, (c.created_ts - max(cc.created_ts))/1000/60/60 as `" + name + "`";
	}

	@Override
	public String getMethodQuery(String sha) {
		return "match (n:METHOD)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[:CHANGED]-(cc:COMMIT) "
				+"where c.created_ts >= cc.created_ts "
				+"return n.name as name, (c.created_ts - max(cc.created_ts))/1000/60/60 as `" + name + "`";
	}

	@Override
	public String getClassQuery(String sha) {
		return "match (n:CLASS)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[:CHANGED]-(cc:COMMIT) "
				+"where c.created_ts >= cc.created_ts "
				+"return n.name as name, (c.created_ts - max(cc.created_ts))/1000/60/60 as `" + name + "`";
	}

}
