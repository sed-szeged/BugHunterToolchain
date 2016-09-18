package bughunter.graphextractor.metrics;

import bughunter.graphextractor.Metric;

public class NumberOfContributorChanges extends Metric {

	public NumberOfContributorChanges() {
		super("Number Of Contributor Changes");
	}

	@Override
	public String getFileQuery(String sha) {
		return "match (n:FILE)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (cc2:COMMIT)-[:CHANGED]->(n)<-[:CHANGED]-(cc:COMMIT) "
				+"where c.created_ts >= cc.created_ts and cc2.created_ts < cc.created_ts "
				+"with n, cc, max(cc2.created_ts) as mm "
				+"match (u1:USER)<-[:AUTHOR]-(cc)-[:CHANGED]->(n)<-[:CHANGED]-(cc2:COMMIT)-[:AUTHOR]->(u2:USER) "
				+"where cc2.created_ts = mm and u1 <> u2 "
				+"with n, cc, cc2 "
				+"return n.filename as name, count (cc2) as `" + name + "`";
	}

	@Override
	public String getMethodQuery(String sha) {
		return "match (n:METHOD)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (cc2:COMMIT)-[:CHANGED]->(n)<-[:CHANGED]-(cc:COMMIT) "
				+"where c.created_ts >= cc.created_ts and cc2.created_ts < cc.created_ts "
				+"with n, cc, max(cc2.created_ts) as mm "
				+"match (u1:USER)<-[:AUTHOR]-(cc)-[:CHANGED]->(n)<-[:CHANGED]-(cc2:COMMIT)-[:AUTHOR]->(u2:USER) "
				+"where cc2.created_ts = mm and u1 <> u2 "
				+"with n, cc, cc2 "
				+"return n.name as name, count (cc2) as `" + name + "`";
	}

	@Override
	public String getClassQuery(String sha) {
		return "match (n:CLASS)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (cc2:COMMIT)-[:CHANGED]->(n)<-[:CHANGED]-(cc:COMMIT) "
				+"where c.created_ts >= cc.created_ts and cc2.created_ts < cc.created_ts "
				+"with n, cc, max(cc2.created_ts) as mm "
				+"match (u1:USER)<-[:AUTHOR]-(cc)-[:CHANGED]->(n)<-[:CHANGED]-(cc2:COMMIT)-[:AUTHOR]->(u2:USER) "
				+"where cc2.created_ts = mm and u1 <> u2 "
				+"with n, cc, cc2 "
				+"return n.name as name, count (cc2) as `" + name + "`";
	}

}
