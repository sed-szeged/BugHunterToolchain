package bughunter.graphextractor.metrics;

import bughunter.graphextractor.Metric;

public class WeightedAge extends Metric {

	public WeightedAge() {
		super("Weighted Age");
	}

	@Override
	public String getFileQuery(String sha) {
		return "match (n:FILE)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n:FILE)<-[ch:CHANGED]-(cc:COMMIT) "
				+"where cc.created_ts <= c.created_ts "
				+"with n, c, ch, cc "
				+"with n.filename as name, SUM((c.created_ts - cc.created_ts)/1000/60/60/24 * ch.added) as wa1, SUM(ch.added) as wa2 "
				+"where wa2 > 0 "
				+"return name, wa1 / wa2 as `" + name + "`";
	}

	@Override
	public String getMethodQuery(String sha) {
		return "match (n:METHOD)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[ch:CHANGED]-(cc:COMMIT) "
				+"where cc.created_ts <= c.created_ts "
				+"with n, c, ch, cc "
				+"with n.name as name, SUM((c.created_ts - cc.created_ts)/1000/60/60/24 * ch.added) as wa1, SUM(ch.added) as wa2 "
				+"where wa2 > 0 "
				+"return name, wa1 / wa2 as `" + name + "`";
	}

	@Override
	public String getClassQuery(String sha) {
		return "match (n:CLASS)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[ch:CHANGED]-(cc:COMMIT) "
				+"where cc.created_ts <= c.created_ts "
				+"with n, c, ch, cc "
				+"with n.name as name, SUM((c.created_ts - cc.created_ts)/1000/60/60/24 * ch.added) as wa1, SUM(ch.added) as wa2 "
				+"where wa2 > 0 "
				+"return name, wa1 / wa2 as `" + name + "`";
	}

}
