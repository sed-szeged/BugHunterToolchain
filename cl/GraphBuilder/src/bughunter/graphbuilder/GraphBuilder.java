package bughunter.graphbuilder;

import graphlib.Attribute;
import graphlib.AttributeString;
import graphlib.Graph;
import graphlib.GraphlibException;
import graphlib.Node;
import graphlib.Attribute.aType;
import graphlib.Node.NodeType;
import graphsupportlib.Metric;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import java.util.Set;
import java.util.logging.Logger;

import com.sun.javafx.tk.FileChooserType;

import bughunter.utils.model.*;

import bughunter.utils.Utils;

public class GraphBuilder {

	private final static Logger LOGGER = Logger.getLogger(GraphBuilder.class.getName());

	private Options options;
	private Map<String,String> limMap;
	private List<String> duplicatedLims;
	private List<String> missingLims;
	private Project project;
	
	public GraphBuilder(Options options) {
		this.options = options;
		this.limMap = new HashMap<>();
		this.duplicatedLims = new ArrayList<>();
		this.missingLims = new ArrayList<>();
	}
	
	public void run() {

		createLimMap();
		loadGitHubXML();
		loadTestFilters();
		
		loadPatchInfo();
		
		writeDatabase();
		
		dumpInfo();
		
	}
	
	private void loadPatchInfo() {
		for(Commit c : project.commits.values()) {
			String limFile = this.limMap.get(c.sha);
			
			if(limFile == null) {
				missingLims.add(c.sha);
				continue;
			}
			
			try {
				if (c.parents.size() > 0) {
					c.prevGraph = new Graph();
					if (this.limMap.get(c.parents.get(0)) == null) {
						missingLims.add(c.sha);
						continue;
					}
					c.prevGraph.loadBinary(this.limMap.get(c.parents.get(0)));
				}
				Graph graph = new Graph();
				graph.loadBinary(limFile);
				c.graph = graph;
				c.connectDiff(false);
				c.loadFiles();
				c.graph = null;
				c.prevGraph = null;
			} catch (GraphlibException e) {
				//e.printStackTrace();
				LOGGER.severe(e.getMessage());
				break;
			}
		}
	}
	
	private void loadGraph(Commit c) {
		String limFile = this.limMap.get(c.sha);
		if (limFile == null) {
			missingLims.add(c.sha);
			return;
		}
		try {
			c.graph = new Graph();
			c.graph.loadBinary(limFile);
		} catch (GraphlibException e) {
			//e.printStackTrace();
			LOGGER.severe(e.getMessage());
		}
	}
	
	private void clearGraph(Commit c) {
		c.graph = null;
	}

	private void writeDatabase() {
		writeProject();
		writeUsers();
		writeIssues();
		writeCommits();
		writeCommitParents();
		writeCommitAuthors();
		writeCommitProject();
		writeFiles();
		writeFileCommitContainment();
		/*
		writeClassMethodCommitContainment();
		writeClasses();
		writeMethods();
		*/
		writeClassParents();
		writeMethodParents();
		writeGraphElements();
		writeCommitClasses();
		writeCommitMethods();
		writeCommitFileDeltas();
		writeIssueCommits();
		
	}
	
	private String getCommitId(Commit c) {
		return getCommitId(c.sha);
	}
	
	private String getCommitId(String sha) {
		return project.repo + ":" + sha;
	}
	
	private String getIssueId(Issue is) {
		return project.repo + ":issue:" + Integer.toString(is.id);
	}
	
	private String getFileId(File f) {
		return getFileId(f.filename);
	}
	
	private String getFileId(Integer i) {
		return project.repo + ":file:" + Integer.toString(i);
	}
	
	private String getClassId(Integer i) {
		return project.repo + ":class:" + Integer.toString(i);
	}

	private String getMethodId(Integer i) {
		return project.repo + ":method:" + Integer.toString(i);
	}

	private void writeIssueCommits() {
		LOGGER.info("Writing issue-commit info");
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(options.out + "/db/issue-commit.csv")));

			bw.write(":START_ID,:END_ID,:TYPE\n");
			for(Issue is : project.issues.values()) {
				for(Commit c : is.commits.values()) {
					bw.write(getIssueId(is) + "," + getCommitId(c) + ",REFERENCED_FROM\n");
				}
			}
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}

	private void writeCommitFileDeltas() {
		LOGGER.info("Writing commit-file-delta info");
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(options.out + "/db/commit-file-delta.csv")));

			bw.write(":START_ID,added:long,deleted:long,modified:long,:END_ID,:TYPE\n");
			//bw.write(":START_ID,begin:long,end:long,:END_ID,:TYPE\n");
			for(Commit c : project.commits.values()) {
				/*
				for(FileChange fc : c.changes) {
					if(!fc.modifiedFile.isJava()) {
						continue;
					}
					String type = "";
					if(fc.status == FileChange.Status.Added)
						type = "ADDED";
					if(fc.status == FileChange.Status.Deleted)
						type = "DELETED";
					if(fc.status == FileChange.Status.Modified)
						type = "MODIFIED";
					bw.write(getCommitId(c) + ","
						+ Integer.toString(fc.additions)+ ","
						+ Integer.toString(fc.deletions)+ ","
						+ Integer.toString(fc.changes)+ ","
						+ getFileId(fc.modifiedFile) + ","
						+ type +"\n");
				}
				*/
/*
				for(Integer fid : c.modifiedFiles.keySet()) {
					File f = c.files.get(fid);
					if(!f.isJava()) {
						continue;
					}
					for(java.util.Map.Entry<Integer, Integer> range : c.modifiedFiles.get(fid)) {
						bw.write(getCommitId(c) + ","
							+ Integer.toString(range.getKey())+ ","
							+ Integer.toString(range.getValue())+ ","
							+ getFileId(f) +",CHANGED\n");
					}
				}*/
				for(Integer fid : c.modifiedFiles.keySet()) {
					File f = c.files.get(fid);
					if(!f.isJava()) {
						continue;
					}
					bw.write(getCommitId(c) + ","
						+ Integer.toString(c.modifiedFiles.get(fid).added)+ ","
						+ Integer.toString(c.modifiedFiles.get(fid).deleted)+ ","
						+ Integer.toString(c.modifiedFiles.get(fid).modified)+ ","
						+ getFileId(f) +",CHANGED\n");
				}
			}
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
		
	}

	private void writeCommitMethods() {
		LOGGER.info("Writing commit-method info");
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(options.out + "/db/commit-method.csv")));

			bw.write(":START_ID,added:long,deleted:long,modified:long,:END_ID,:TYPE\n");
			//bw.write(":START_ID,begin:long,end:long,:END_ID,:TYPE\n");
			for(Commit c : project.commits.values()) {
				for(Integer i : c.modifiedMethods.keySet()) {
					/*
					for(java.util.Map.Entry<Integer, Integer> range : c.modifiedMethods.get(i)) {
						bw.write(getCommitId(c) + ","
							+ Integer.toString(range.getKey())+ ","
							+ Integer.toString(range.getValue())+ ","
							+ getMethodId(i) +",CHANGED\n");
					}
					*/
					bw.write(getCommitId(c) + ","
						+ Integer.toString(c.modifiedMethods.get(i).added)+ ","
						+ Integer.toString(c.modifiedMethods.get(i).deleted)+ ","
						+ Integer.toString(c.modifiedMethods.get(i).modified)+ ","
						+ getMethodId(i) +",CHANGED\n");
				}
			}
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
		
	}

	private void writeCommitClasses() {
		LOGGER.info("Writing commit-class info");
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(options.out + "/db/commit-class.csv")));

			bw.write(":START_ID,added:long,deleted:long,modified:long,:END_ID,:TYPE\n");
			//bw.write(":START_ID,begin:long,end:long,:END_ID,:TYPE\n");
			for(Commit c : project.commits.values()) {
				for(Integer i : c.modifiedClasses.keySet()) {
					/*
					for(java.util.Map.Entry<Integer, Integer> range : c.modifiedClasses.get(i)) {
						bw.write(getCommitId(c) + ","
							+ Integer.toString(range.getKey())+ ","
							+ Integer.toString(range.getValue())+ ","
							+ getClassId(i) +",CHANGED\n");
					}*/
					bw.write(getCommitId(c) + ","
						+ Integer.toString(c.modifiedClasses.get(i).added)+ ","
						+ Integer.toString(c.modifiedClasses.get(i).deleted)+ ","
						+ Integer.toString(c.modifiedClasses.get(i).modified)+ ","
						+ getClassId(i) +",CHANGED\n");
				}
			}
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
		
	}

	private void writeMethodParents() {
		LOGGER.info("Writing method-parent info");
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(options.out + "/db/method-class.csv")));

			Set<Integer> written = new HashSet<>();

			bw.write(":START_ID,:END_ID,:TYPE\n");
			for(Commit c : project.commits.values()) {
				for(Integer i : c.modifiedMethods.keySet()) {
					if(!written.contains(i)) {
						bw.write(getMethodId(i) + "," + getClassId(c.modifiedMethodParent.get(i)) + ",PARENT\n");
						written.add(i);
					}
				}
			}
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}

	private void writeClassParents() {
		LOGGER.info("Writing class-parent info");
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(options.out + "/db/class-file.csv")));

			Set<Integer> written = new HashSet<>();

			bw.write(":START_ID,:END_ID,:TYPE\n");
			for(Commit c : project.commits.values()) {
				for(Integer i : c.modifiedClasses.keySet()) {
					if(!written.contains(i)) {
						bw.write(getClassId(i) + "," + getFileId(c.modifiedClassParent.get(i)) + ",PARENT\n");
						written.add(i);
					}
				}
			}
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}
/*
	private void writeMethods() {
		LOGGER.info("Writing method info");
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(options.out + "/db/method.csv")));

			Set<Integer> written = new HashSet<>();
			
			bw.write("id:ID,name,filtered:boolean,:LABEL\n");
			for(Commit c : project.commits.values()) {
				loadGraph(c);
				List<Node> nodes = c.graph.findNodes(new NodeType(Metric.NTYPE_LIM_METHOD));
				for(Node n : nodes) {
					List<Attribute> attrs = n.findAttribute(aType.atString,Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
					String nameAttr = ((AttributeString) attrs.get(0)).getValue();
					int name = project.strTable.set(nameAttr);
					if(!written.contains(name)) {
						bw.write(getMethodId(name) + "," + nameAttr + "," + Boolean.toString(project.isFiltered(nameAttr)) + ",METHOD\n");
						written.add(name);
					}
				}
				clearGraph(c);
				
			}
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}*/
/*
	private void writeClasses() {
		LOGGER.info("Writing class info");
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(options.out + "/db/class.csv")));

			Set<Integer> written = new HashSet<>();
			
			bw.write("id:ID,name,filtered:boolean,:LABEL\n");
			for(Commit c : project.commits.values()) {

				loadGraph(c);
				List<Node> nodes = c.graph.findNodes(new NodeType(Metric.NTYPE_LIM_CLASS));
				for(Node n : nodes) {
					List<Attribute> attrs = n.findAttribute(aType.atString,Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
					String nameAttr = ((AttributeString) attrs.get(0)).getValue();
					int name = project.strTable.set(nameAttr);
					if(!written.contains(name)) {
						bw.write(getClassId(name) + "," + nameAttr + "," + Boolean.toString(project.isFiltered(nameAttr)) + ",CLASS\n");
						written.add(name);
					}
				}
				clearGraph(c);
			}
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}*/

	private void writeFiles() {
		LOGGER.info("Writing file info");
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(options.out + "/db/file.csv")));

			Set<Integer> writtenFiles = new HashSet<>();
			
			bw.write("id:ID,filename,filtered:boolean,:LABEL\n");
			for(Commit c : project.commits.values()) {
				for(File f : c.files.values()) {
					if(!writtenFiles.contains(f.filename)) {
						if(f.isJava()) {
							bw.write(getFileId(f) + "," + f.getFilename() + "," + Boolean.toString(project.isFiltered(f.getFilename())) + ",FILE\n");
							writtenFiles.add(f.filename);
						}
					}
				}
			}
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}

	private void writeFileCommitContainment() {
		LOGGER.info("Writing file-commit containment info");
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(options.out + "/db/file-commit-containment.csv")));

			bw.write(":START_ID,:END_ID,:TYPE\n");
			for(Commit c : project.commits.values()) {
				for(File f : c.files.values()) {
					if(f.isJava()) {
						bw.write(getCommitId(c) + "," + getFileId(f) + ",CONTAINS\n");
					}
				}
			}
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}
	/*
	private void writeClassMethodCommitContainment() {
		LOGGER.info("Writing classmethod-commit containment info");
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(options.out + "/db/classmethod-commit-containment.csv")));

			bw.write(":START_ID,:END_ID,:TYPE\n");
			for(Commit c : project.commits.values()) {
				loadGraph(c);
				List<Node> nodes = c.graph.findNodes(new NodeType(Metric.NTYPE_LIM_CLASS));
				for(Node n : nodes) {
					List<Attribute> attrs = n.findAttribute(aType.atString,Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
					String nameAttr = ((AttributeString) attrs.get(0)).getValue();
					int name = project.strTable.set(nameAttr);
					bw.write(getCommitId(c) + "," + getClassId(name) + ",CONTAINS\n");
				}

				nodes = c.graph.findNodes(new NodeType(Metric.NTYPE_LIM_METHOD));
				for(Node n : nodes) {
					List<Attribute> attrs = n.findAttribute(aType.atString,Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
					String nameAttr = ((AttributeString) attrs.get(0)).getValue();
					int name = project.strTable.set(nameAttr);
					bw.write(getCommitId(c) + "," + getMethodId(name) + ",CONTAINS\n");
				}
				clearGraph(c);
			}
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}*/
	
	private void writeGraphElements() {
		LOGGER.info("Writing graph elements");
		try {
			BufferedWriter classMethodCont = new BufferedWriter( new FileWriter(new java.io.File(options.out + "/db/classmethod-commit-containment.csv")));
			classMethodCont.write(":START_ID,:END_ID,:TYPE\n");
			
			BufferedWriter classes = new BufferedWriter( new FileWriter(new java.io.File(options.out + "/db/class.csv")));
			classes.write("id:ID,name,filtered:boolean,:LABEL\n");
			
			Set<Integer> writtenClasses = new HashSet<>();
			
			BufferedWriter methods = new BufferedWriter( new FileWriter(new java.io.File(options.out + "/db/method.csv")));
			methods.write("id:ID,name,filtered:boolean,:LABEL\n");
			
			Set<Integer> writtenMethods = new HashSet<>();
			
			for(Commit c : project.commits.values()) {
				loadGraph(c);
				if (c.graph == null) {
					continue;
				}
				List<Node> nodes = c.graph.findNodes(new NodeType(Metric.NTYPE_LIM_CLASS));
				for(Node n : nodes) {
					List<Attribute> attrs = n.findAttribute(aType.atString,Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
					String nameAttr = ((AttributeString) attrs.get(0)).getValue();
					int name = project.strTable.set(nameAttr);
					
					classMethodCont.write(getCommitId(c) + "," + getClassId(name) + ",CONTAINS\n");
					
					if(!writtenClasses.contains(name)) {
						classes.write(getClassId(name) + "," + nameAttr + "," + Boolean.toString(project.isFiltered(nameAttr)) + ",CLASS\n");
						writtenClasses.add(name);
					}
				}

				nodes = c.graph.findNodes(new NodeType(Metric.NTYPE_LIM_METHOD));
				for(Node n : nodes) {
					List<Attribute> attrs = n.findAttribute(aType.atString,Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
					String nameAttr = ((AttributeString) attrs.get(0)).getValue();
					int name = project.strTable.set(nameAttr);
					
					classMethodCont.write(getCommitId(c) + "," + getMethodId(name) + ",CONTAINS\n");
					
					if(!writtenMethods.contains(name)) {
						methods.write(getMethodId(name) + "," + nameAttr + "," + Boolean.toString(project.isFiltered(nameAttr)) + ",METHOD\n");
						writtenMethods.add(name);
					}
				}
				clearGraph(c);
				classMethodCont.flush();

				classes.flush();
				
				methods.flush();
			}

			classMethodCont.flush();
			classMethodCont.close();

			classes.flush();
			classes.close();
			
			methods.flush();
			methods.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}
	
	private void writeCommitProject() {
		LOGGER.info("Writing commit-project info");
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(options.out + "/db/commit-project.csv")));

			bw.write(":START_ID,:END_ID,:TYPE\n");
			for(Commit c : project.commits.values()) {
				bw.write(project.repo + "," + getCommitId(c) + ",COMMIT\n");
			}
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}

	private void writeCommitAuthors() {
		LOGGER.info("Writing commit-author info");
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(options.out + "/db/commit-author.csv")));

			bw.write(":START_ID,:END_ID,:TYPE\n");
			for(Commit c : project.commits.values()) {
				if(c.author != null) {
					bw.write(getCommitId(c) + "," + Integer.toString(c.author.id) + ",AUTHOR\n");
				}
			}
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}

	private void writeCommitParents() {
		LOGGER.info("Writing commit-parent info");
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(options.out + "/db/commit-parent.csv")));

			bw.write(":START_ID,:END_ID,:TYPE\n");
			for(Commit c : project.commits.values()) {
				for(String p : c.parents) {
					bw.write(getCommitId(c) + "," + getCommitId(p) + ",PARENT\n");
				}
			}
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}

	private void writeCommits() {
		LOGGER.info("Writing commit info");
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(options.out + "/db/commit.csv")));

			bw.write("hash:ID,created,created_ts:long,fix:boolean,:LABEL\n");
			for(Commit c : project.commits.values()) {
				bw.write(getCommitId(c) + "," + Utils.format(c.created)
						+ "," + Long.toString(c.created.getTime())
						+ "," + Boolean.toString(c.fix) + ",COMMIT\n");
			}
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}

	private void writeIssues() {
		LOGGER.info("Writing issue info");
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(options.out + "/db/issue.csv")));

			bw.write("id:ID,opened,opened_ts:long,closed,closed_ts:long,:LABEL\n");
			for(Issue is : project.issues.values()) {
				bw.write(getIssueId(is) + "," + Utils.format(is.opened)
						+ "," + Long.toString(is.opened.getTime())
						+ "," + (is.closed == null ? "" : Utils.format(is.closed))
						+ "," + (is.closed == null ? "" : Long.toString(is.closed.getTime())) + ",ISSUE\n");
			}
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}

	private void writeUsers() {
		LOGGER.info("Writing user info");
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(options.out + "/db/user.csv")));

			bw.write(":ID,number_of_commits:int,:LABEL\n");
			for(User u : project.contributors.values()) {
				bw.write(Integer.toString(u.id) + "," + Integer.toString(u.commits) + ",USER\n");
			}
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}

	private void writeProject() {
		LOGGER.info("Writing project info");
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(options.out + "/db/project.csv")));
			
			bw.write("user,repo:ID,:LABEL\n");
			bw.write(project.user + "," + project.repo + ",PROJECT\n");
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}

	private void loadGitHubXML() {
		LOGGER.info("Loading GitHub xml");
		project = Utils.parseGitHubXML(options.xml);
	}

	public void loadTestFilters() {
		LOGGER.info("Loading test filters");
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(options.testfilter));
			String line;
			while ((line = br.readLine()) != null) {
				project.testFilters.add(line);
			}
			br.close();
		} catch (NumberFormatException e) {
			LOGGER.severe(e.getMessage());
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}
	
	private void createLimMap() {
		LOGGER.info("Creating lim map");
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(options.lims));

			String line;

			while ((line = br.readLine()) != null) {
				String sha;
				sha = (String) line.subSequence(0, line.lastIndexOf("\\"));
				if (line.contains("\\")) {
					sha = (String) sha.subSequence(sha.lastIndexOf("\\") + 1, sha.length());
				}
				line = line.trim();
				if(limMap.containsKey(sha)) {
					duplicatedLims.add(limMap.get(sha));
					duplicatedLims.add(line);
				}
				limMap.put(sha, line);
			}
			br.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			LOGGER.severe(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.severe(e.getMessage());
		}
	}

	private void dumpInfo() {
		LOGGER.info("Dumping information about the run");
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(options.out + "/duplicatedLims.txt")));
			
			for(String lim : duplicatedLims) {
				bw.write(lim);
				bw.write("\n");
			}
			
			bw.flush();
			bw.close();
			

			bw = new BufferedWriter( new FileWriter(new java.io.File(options.out + "/missingLims.txt")));
			
			for(String lim : missingLims) {
				bw.write(lim);
				bw.write("\n");
			}
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}
	
}
