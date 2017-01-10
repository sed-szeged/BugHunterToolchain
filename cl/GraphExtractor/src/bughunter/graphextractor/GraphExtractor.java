package bughunter.graphextractor;

import graphlib.Graph;
import graphlib.GraphlibException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import bughunter.graphextractor.metrics.Age;
import bughunter.graphextractor.metrics.AverageNumberOfAddedLines;
import bughunter.graphextractor.metrics.AverageNumberOfDeletedLines;
import bughunter.graphextractor.metrics.AverageNumberOfElementsModifiedTogether;
import bughunter.graphextractor.metrics.AverageTimeBetweenChanges;
import bughunter.graphextractor.metrics.CodeChurn;
import bughunter.graphextractor.metrics.LastContributorCommits;
import bughunter.graphextractor.metrics.MaximumNumberOfAddedLines;
import bughunter.graphextractor.metrics.MaximumNumberOfDeletedLines;
import bughunter.graphextractor.metrics.MaximumNumberOfElementsModifiedTogether;
import bughunter.graphextractor.metrics.NumberOfAdditions;
import bughunter.graphextractor.metrics.NumberOfBugs;
import bughunter.graphextractor.metrics.NumberOfContributorChanges;
import bughunter.graphextractor.metrics.NumberOfContributors;
import bughunter.graphextractor.metrics.NumberOfDeletions;
import bughunter.graphextractor.metrics.NumberOfFixes;
import bughunter.graphextractor.metrics.NumberOfFixesInTheLastMonths;
import bughunter.graphextractor.metrics.NumberOfModifications;
import bughunter.graphextractor.metrics.NumberOfModificationsInTheLastMonths;
import bughunter.graphextractor.metrics.NumberOfVersions;
import bughunter.graphextractor.metrics.SumOfAddedLines;
import bughunter.graphextractor.metrics.SumOfDeletedLines;
import bughunter.graphextractor.metrics.TimePassedSinceLastChange;
import bughunter.graphextractor.metrics.WeightedAge;

public class GraphExtractor {

	private final static Logger LOGGER = Logger.getLogger(GraphExtractor.class.getName());

	private Options options;
	private GraphDatabaseService graphDb;
	private List<String> releases;
	private List<Metric> metrics;
	private List<ComplexMetric> complexMetrics;

	private static final String DATABASE = "\\\\tsclient\\C\\Users\\gypeti\\Documents\\Neo4j\\bughunter";
	
	public GraphExtractor(Options options) {
		this.options = options;
		releases = new ArrayList<>();
		metrics = new ArrayList<>();
		complexMetrics = new ArrayList<>();
		
		metrics.add(new NumberOfFixes());
		metrics.add(new NumberOfFixesInTheLastMonths());
		metrics.add(new NumberOfModifications());
		metrics.add(new NumberOfModificationsInTheLastMonths());
		metrics.add(new Age());
		metrics.add(new AverageNumberOfAddedLines());
		metrics.add(new AverageNumberOfDeletedLines());
		metrics.add(new AverageNumberOfElementsModifiedTogether());
		metrics.add(new AverageTimeBetweenChanges());
		//metrics.add(new CodeChurn());
		metrics.add(new LastContributorCommits());
		metrics.add(new MaximumNumberOfAddedLines());
		metrics.add(new MaximumNumberOfDeletedLines());
		metrics.add(new MaximumNumberOfElementsModifiedTogether());
		metrics.add(new NumberOfAdditions());
		metrics.add(new NumberOfContributorChanges());
		metrics.add(new NumberOfContributors());
		metrics.add(new NumberOfDeletions());
		metrics.add(new NumberOfVersions());
		metrics.add(new SumOfAddedLines());
		metrics.add(new SumOfDeletedLines());
		metrics.add(new TimePassedSinceLastChange());
		metrics.add(new WeightedAge());
		metrics.add(new NumberOfBugs());
		
		//complexMetrics.add(new WeightedAge());
	}
	
	/*public void test() {
		WeightedAge t = new WeightedAge();
		t.init(100);
		t.setMaxAge(5);
		t.step("a", "a", 100, 30, 40, 4);
		t.print();
		t.step("a", "a", 100, 51, 60, 1);
		t.print();
		t.step("a", "a", 100, 35, 55, 2);
		t.print();
		t.step("a", "a", 100, 15, 70, 1);
		t.print();
		t.step("a", "a", 100, 40, 50, 0);
		t.print("a");
		
		t.step("b", "a", 100, 11, 20, 4);
		t.step("b", "a", 100, 31, 40, 4);
		t.step("b", "a", 100, 51, 60, 4);
		t.step("b", "a", 100, 71, 80, 4);
		t.step("b", "a", 100, 91, 100, 4);
		t.print("b");
		
		t.step("c", "a", 100, 11, 20, 6);
		t.step("c", "a", 100, 31, 40, 6);
		t.step("c", "a", 100, 51, 60, 6);
		t.step("c", "a", 100, 71, 80, 6);
		t.step("c", "a", 100, 91, 100, 6);
		t.print("c");

		t.step("d", "a", 100, 41, 50, 3);
		t.print("d");
		t.step("d", "a", 100, 21, 70, 4);
		t.print("d");
	}*/
	
	public void run() {
		loadReleaseVersions();
		startDB();
		for (String sha : releases) {
			String output = options.out + "\\" + sha + "\\";
			String graphSha = options.project + ":" + sha;

			for(Metric m : metrics) {
				//LOGGER.info("Computing: " + m.getName());
				saveResults(m.getFileQuery(graphSha), output + "file\\" + m.getFilename() + ".csv");
				saveResults(m.getMethodQuery(graphSha), output + "method\\" + m.getFilename() + ".csv");
				saveResults(m.getClassQuery(graphSha), output + "class\\" + m.getFilename() + ".csv");
				//LOGGER.info(m.getFileQuery(graphSha));
				//LOGGER.info(m.getMethodQuery(graphSha));
				//LOGGER.info(m.getClassQuery(graphSha));
			}
			for(ComplexMetric m : complexMetrics) {
				try {
					File file = new File(output + "file\\" + m.getFilename() + ".csv");
					file.getParentFile().mkdirs();
					BufferedWriter bw = new BufferedWriter( new FileWriter(file));
					m.computeFileMetrics(graphDb, graphSha, bw);
					bw.flush();
					bw.close();
					
					file = new File(output + "method\\" + m.getFilename() + ".csv");
					file.getParentFile().mkdirs();
					bw = new BufferedWriter( new FileWriter(file));
					m.computeMethodMetrics(graphDb, graphSha, bw);
					bw.flush();
					bw.close();
					
					file = new File(output + "class\\" + m.getFilename() + ".csv");
					file.getParentFile().mkdirs();
					bw = new BufferedWriter( new FileWriter(file));
					m.computeClassMetrics(graphDb, graphSha, bw);
					bw.flush();
					bw.close();
					
				} catch (IOException e) {
					LOGGER.severe(e.getMessage());
				}
			}
		}
		
	}
	
	private void saveResults(String query, String output) {

		try {
			File file = new File(output);
			file.getParentFile().mkdirs();
			BufferedWriter bw = new BufferedWriter( new FileWriter(file));
			

			try ( Transaction ignored = graphDb.beginTx();
				  Result result = graphDb.execute(query) )
				{
					for(int i = 0; i < result.columns().size(); i++) {
						bw.write("\"" + result.columns().get(i) + "\"");
						if(i < result.columns().size()-1)
							bw.write(",");
					}
					bw.write("\n");
				    while ( result.hasNext() )
				    {
				        Map<String,Object> row = result.next();
				        for(int i = 0; i < result.columns().size(); i++) {
				        	bw.write("\"" + row.get(result.columns().get(i)) + "\"");
							if(i < result.columns().size()-1)
								bw.write(",");
						}
				        bw.write("\n");
				    }
				}
			
			bw.flush();
			bw.close();
			
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
			
	}

	public void loadReleaseVersions() {
		LOGGER.info("Loading release versions");
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(options.releases));
			String line;
			while ((line = br.readLine()) != null) {
				releases.add(line);
			}
			br.close();
		} catch (NumberFormatException e) {
			LOGGER.severe(e.getMessage());
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}
	
	private void startDB() {
		LOGGER.info("Starting neo4j");
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( new File(DATABASE + "-" + options.project));

		Runtime.getRuntime().addShutdownHook( new Thread()
	    {
	        @Override
	        public void run()
	        {
	            graphDb.shutdown();
	        }
	    });
	}
}
