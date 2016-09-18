package bughunter.graphextractor.metrics;

import bughunter.graphextractor.Metric;

public class NumberOfVersions extends Metric {

	public NumberOfVersions() {
		super("Number Of Versions");
	}

	@Override
	public String getFileQuery(String sha) {
		return "match (n:FILE)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n:FILE)<-[:CHANGED]-(cc:COMMIT) "
				+"where cc.created_ts <= c.created_ts "
				+"with  n, c, MIN(cc.created_ts) as min "
				+"match (ccc:COMMIT)-[:CHANGED]-(n) where "
				+"ccc.created_ts = min "
				+"with n, c, ccc "
				+"match p=shortestPath((c)-[:PARENT*]->(ccc)) "
				+"return n.filename as name, MIN(length(p) + 1) as `" + name + "`";
	}

	@Override
	public String getMethodQuery(String sha) {
		return "match (n:METHOD)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[:CHANGED]-(cc:COMMIT) "
				+"where cc.created_ts <= c.created_ts "
				+"with  n, c, MIN(cc.created_ts) as min "
				+"match (ccc:COMMIT)-[:CHANGED]-(n) where "
				+"ccc.created_ts = min "
				+"with n, c, ccc "
				+"match p=shortestPath((c)-[:PARENT*]->(ccc)) "
				+"return n.name as name, MIN(length(p) + 1) as `" + name + "`";
	}

	@Override
	public String getClassQuery(String sha) {
		return "match (n:CLASS)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[:CHANGED]-(cc:COMMIT) "
				+"where cc.created_ts <= c.created_ts "
				+"with  n, c, MIN(cc.created_ts) as min "
				+"match (ccc:COMMIT)-[:CHANGED]-(n) where "
				+"ccc.created_ts = min "
				+"with n, c, ccc "
				+"match p=shortestPath((c)-[:PARENT*]->(ccc)) "
				+"return n.name as name, MIN(length(p) + 1) as `" + name + "`";
	}

}
