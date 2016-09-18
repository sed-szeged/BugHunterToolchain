package bughunter.graphextractor.metrics;

import bughunter.graphextractor.Metric;

public class CodeChurn extends Metric {

	public CodeChurn() {
		super("Code Churn");
	}

	@Override
	public String getFileQuery(String sha) {
		return "match (n:FILE)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[ch:CHANGED]-(cc:COMMIT) "
				+"where c.created_ts >= cc.created_ts "
				+"return n.filename as name, SUM(ch.added - ch.deleted) as `" + name + "`";
	}

	@Override
	public String getMethodQuery(String sha) {
		return "match (n:METHOD)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[ch:CHANGED]-(cc:COMMIT) "
				+"where c.created_ts >= cc.created_ts "
				+"return n.name as name, SUM(ch.added - ch.deleted) as `" + name + "`";
	}

	@Override
	public String getClassQuery(String sha) {
		return "match (n:CLASS)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[ch:CHANGED]-(cc:COMMIT) "
				+"where c.created_ts >= cc.created_ts "
				+"return n.name as name, SUM(ch.added - ch.deleted) as `" + name + "`";
	}

}