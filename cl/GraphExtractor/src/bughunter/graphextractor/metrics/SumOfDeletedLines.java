package bughunter.graphextractor.metrics;

import bughunter.graphextractor.Metric;

public class SumOfDeletedLines extends Metric {

	public SumOfDeletedLines() {
		super("Sum Of Deleted Lines");
	}

	@Override
	public String getFileQuery(String sha) {
		return "match (n:FILE)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[ch:CHANGED]-(cc:COMMIT) "
				+"where c.created_ts >= cc.created_ts "
				+"return n.filename as name, SUM(ch.deleted) as `" + name + "`";
	}

	@Override
	public String getMethodQuery(String sha) {
		return "match (n:METHOD)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[ch:CHANGED]-(cc:COMMIT) "
				+"where c.created_ts >= cc.created_ts "
				+"return n.name as name, SUM(ch.deleted) as `" + name + "`";
	}

	@Override
	public String getClassQuery(String sha) {
		return "match (n:CLASS)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[ch:CHANGED]-(cc:COMMIT) "
				+"where c.created_ts >= cc.created_ts "
				+"return n.name as name, SUM(ch.deleted) as `" + name + "`";
	}

}
