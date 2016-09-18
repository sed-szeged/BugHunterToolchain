package bughunter.graphextractor.metrics;

import bughunter.graphextractor.Metric;

public class Age extends Metric {

	public Age() {
		super("Age");
	}

	@Override
	public String getFileQuery(String sha) {
		return "match (n:FILE)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n:FILE)<-[:CHANGED]-(cc:COMMIT) "
				+"where cc.created_ts <= c.created_ts "
				+"return n.filename as name, (c.created_ts - MIN(cc.created_ts))/1000/60/60/24 as `" + name + "`";
	}

	@Override
	public String getMethodQuery(String sha) {
		return "match (n:METHOD)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[:CHANGED]-(cc:COMMIT) "
				+"where cc.created_ts <= c.created_ts "
				+"return n.name as name, (c.created_ts - MIN(cc.created_ts))/1000/60/60/24 as `" + name + "`";
	}

	@Override
	public String getClassQuery(String sha) {
		return "match (n:CLASS)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[:CHANGED]-(cc:COMMIT) "
				+"where cc.created_ts <= c.created_ts "
				+"return n.name as name, (c.created_ts - MIN(cc.created_ts))/1000/60/60/24 as `" + name + "`";
	}

}
