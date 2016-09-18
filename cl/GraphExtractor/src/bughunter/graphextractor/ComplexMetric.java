package bughunter.graphextractor;

import java.io.BufferedWriter;
import java.io.IOException;

import org.neo4j.graphdb.GraphDatabaseService;


public abstract class ComplexMetric {
	
	protected String name;
	
	public ComplexMetric(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getFilename() {
		char c[] = name.replace(" ", "").toCharArray();
	    c[0] = Character.toLowerCase(c[0]);
		return new String(c); 
	}

	public void computeFileMetrics(GraphDatabaseService graphDb, String sha, BufferedWriter bw) throws IOException {
	}

	public void computeMethodMetrics(GraphDatabaseService graphDb, String sha, BufferedWriter bw) throws IOException {

	}

	public void computeClassMetrics(GraphDatabaseService graphDb, String sha, BufferedWriter bw) throws IOException {

	}
}
