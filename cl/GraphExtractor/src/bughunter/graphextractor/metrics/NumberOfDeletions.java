package bughunter.graphextractor.metrics;

import bughunter.graphextractor.Metric;

public class NumberOfDeletions extends Metric {

	public NumberOfDeletions() {
		super("Number Of Deletions");
	}

	@Override
	public String getFileQuery(String sha) {
		return "match (n:FILE)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[ch:CHANGED]-(cc:COMMIT) "
				+"where c.created_ts >= cc.created_ts and ch.deleted > 0 "
				+"return n.filename as name, COUNT(cc) as `" + name + "`";
	}

	@Override
	public String getMethodQuery(String sha) {
		return "match (n:METHOD)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[ch:CHANGED]-(cc:COMMIT) "
				+"where c.created_ts >= cc.created_ts and ch.deleted > 0 "
				+"return n.name as name, COUNT(cc) as `" + name + "`";
	}

	@Override
	public String getClassQuery(String sha) {
		return "match (n:CLASS)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[ch:CHANGED]-(cc:COMMIT) "
				+"where c.created_ts >= cc.created_ts and ch.deleted > 0 "
				+"return n.name as name, COUNT(cc) as `" + name + "`";
	}

}
