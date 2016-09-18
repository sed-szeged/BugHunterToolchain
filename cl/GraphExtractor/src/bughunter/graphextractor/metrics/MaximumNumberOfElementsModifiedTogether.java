package bughunter.graphextractor.metrics;

import bughunter.graphextractor.Metric;

public class MaximumNumberOfElementsModifiedTogether extends Metric {

	public MaximumNumberOfElementsModifiedTogether() {
		super("Maximum Number Of Elements Modified Together");
	}

	@Override
	public String getFileQuery(String sha) {
		return "match (n:FILE)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[:CHANGED]-(cc:COMMIT)-[:CHANGED]->(n2:FILE) "
				+"where c.created_ts >= cc.created_ts "
				+"with n.filename as name, cc.hash as hash, count(DISTINCT n2) as nodes "
				+"return name, max(nodes) as `" + name + "`";
	}

	@Override
	public String getMethodQuery(String sha) {
		return "match (n:METHOD)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[:CHANGED]-(cc:COMMIT)-[:CHANGED]->(n2:METHOD) "
				+"where c.created_ts >= cc.created_ts "
				+"with n.name as name, cc.hash as hash, count(DISTINCT n2) as nodes "
				+"return name, max(nodes) as `" + name + "`";
	}

	@Override
	public String getClassQuery(String sha) {
		return "match (n:CLASS)<-[:CONTAINS]-(c:COMMIT{hash:'" + sha + "'}), (n)<-[:CHANGED]-(cc:COMMIT)-[:CHANGED]->(n2:CLASS) "
				+"where c.created_ts >= cc.created_ts "
				+"with n.name as name, cc.hash as hash, count(DISTINCT n2) as nodes "
				+"return name, max(nodes) as `" + name + "`";
	}

}
