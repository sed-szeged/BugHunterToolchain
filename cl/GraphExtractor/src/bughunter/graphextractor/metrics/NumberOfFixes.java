package bughunter.graphextractor.metrics;

import bughunter.graphextractor.Metric;

public class NumberOfFixes extends Metric {

	public NumberOfFixes() {
		super("Number Of Fixes");
	}

	@Override
	public String getFileQuery(String sha) {
		return "match (n:FILE)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[:CHANGED]-(cc:COMMIT{fix:true}) "
				+"where c.created_ts >= cc.created_ts "
				+"return n.filename as name, count(cc) as `" + name + "`";
	}

	@Override
	public String getMethodQuery(String sha) {
		return "match (n:METHOD)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[:CHANGED]-(cc:COMMIT{fix:true}) "
				+"where c.created_ts >= cc.created_ts "
				+"return n.name as name, count(cc) as `" + name + "`";
	}

	@Override
	public String getClassQuery(String sha) {
		return "match (n:CLASS)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[:CHANGED]-(cc:COMMIT{fix:true}) "
				+"where c.created_ts >= cc.created_ts "
				+"return n.name as name, count(cc) as `" + name + "`";
	}

}
