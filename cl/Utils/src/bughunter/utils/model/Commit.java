package bughunter.utils.model;

import graphlib.Attribute;
import graphlib.Attribute.aType;
import graphlib.AttributeComposite;
import graphlib.AttributeInt;
import graphlib.AttributeString;
import graphlib.Edge;
import graphlib.Edge.EdgeType;
import graphlib.Graph;
import graphlib.Node;
import graphlib.Node.NodeType;
import graphsupportlib.Metric;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bughunter.utils.ChangeInfo;
import bughunter.utils.StringStorage;
import bughunter.utils.Utils;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

public class Commit implements Serializable {

	private static final long serialVersionUID = 1834195323256932310L;
	public String sha;
	public Date created;
	public List<FileChange> changes;
	public Map<Integer, File> files;
	public boolean fix;
	public int numberOfBugs;

	public List<String> parents;
	public User author;

	public Graph graph;
	public Graph prevGraph;

	public Project project;

	public int buggy_methods;
	public int buggy_classes;
	public int buggy_files;

	/**
	 * longname-bol nodeid lekeresehez
	 */
	public Map<Integer, Integer> longnameNodeIdIndexMethods;
	public Map<Integer, Integer> longnameNodeIdIndexClasses;
	public Map<Integer, Integer> longnameNodeIdIndexFiles;

	public Map<Integer, Set<Integer>> traditionalRelatedIssues;
	
	public boolean isRelease;
	
	/**
	 * longnameid-hez tartozo bugok szama
	 */
	public Map<Integer, Integer> bugs;

	public Map<Integer, Integer> fileBugs;

	/**
	 * a commit szintu metrikak (nyitott issue-k)
	 */
	public Map<Integer, Integer> commitMetrics;

	/**
	 * fajlokhoz tartozo metrikak, filenameid-vel azonositva
	 */
	public Map<Integer, Map<Integer, Float>> fileMetrics;

	public Map<Integer, Set<Integer>> fileModUser;
	public Map<Integer, Integer> lastModUser;

	/**
	 * a commit altal modositott forraskod elemek longnameid-ja
	 */
	//public Map<Integer, Set<java.util.Map.Entry<Integer, Integer>>> modifiedMethods;
	//public Map<Integer, Set<java.util.Map.Entry<Integer, Integer>>> modifiedClasses;
	//public Map<Integer, Set<java.util.Map.Entry<Integer, Integer>>> modifiedFiles;
	public Map<Integer, ChangeInfo> modifiedMethods;
	public Map<Integer, ChangeInfo> modifiedClasses;
	public Map<Integer, ChangeInfo> modifiedFiles;
	
	public Map<Integer, Integer> modifiedMethodParent;
	public Map<Integer, Integer> modifiedClassParent;

	public Set<Integer> modifiedMethodsMMID;

	public Set<Integer> fileMetricNames;

	/**
	 * diff-connect statisztikak
	 */
	public transient int S1c;
	public transient int S1m;
	public transient int S2c;
	public transient int S2m;
	public transient int S3c;
	public transient int S3m;
	public transient int Deltas;
	public transient boolean connected;

	private transient static int BACKLASH = 3;

	public Commit() {
		changes = new ArrayList<FileChange>();
		files = new HashMap<Integer, File>();
		bugs = new HashMap<Integer, Integer>();
		//modifiedMethods = new HashMap<Integer, Set<java.util.Map.Entry<Integer, Integer>>>();
		//modifiedClasses = new HashMap<Integer, Set<java.util.Map.Entry<Integer, Integer>>>();
		//modifiedFiles = new HashMap<Integer, Set<java.util.Map.Entry<Integer, Integer>>>();
		modifiedMethods = new HashMap<Integer, ChangeInfo>();
		modifiedClasses = new HashMap<Integer, ChangeInfo>();
		modifiedFiles = new HashMap<Integer, ChangeInfo>();
		longnameNodeIdIndexMethods = new HashMap<Integer, Integer>();
		longnameNodeIdIndexClasses = new HashMap<Integer, Integer>();
		longnameNodeIdIndexFiles = new HashMap<Integer, Integer>();
		commitMetrics = new HashMap<Integer, Integer>();
		fileMetrics = new HashMap<Integer, Map<Integer, Float>>();
		fileBugs = new HashMap<Integer, Integer>();
		fileModUser = new HashMap<Integer, Set<Integer>>();
		lastModUser = new HashMap<Integer, Integer>();
		modifiedMethodsMMID = new HashSet<Integer>();
		fileMetricNames = new HashSet<Integer>();
		traditionalRelatedIssues = new HashMap<Integer, Set<Integer>>();
		buggy_methods = 0;
		buggy_classes = 0;
		buggy_files = 0;
		isRelease = false;
		parents = new ArrayList<>();
		modifiedMethodParent = new HashMap<Integer, Integer>();
		modifiedClassParent = new HashMap<Integer, Integer>();
		prevGraph = null;
	}

	public Map<Integer, Integer> getLongnameNodeIdIndexFiles() {
		return longnameNodeIdIndexFiles;
	}

	public File getFile(String path) {
		for (File f : files.values()) {
			if (f.getFilename().equals(path))
				return f;
		}
		return null;
	}
	
	public void loadFiles() {
		List<Node> fileNodes = graph.findNodes(new NodeType(
				Metric.NTYPE_LIM_FILE));
		for (Node n : fileNodes) {
			
			List<Attribute> nameA = n.findAttribute(aType.atString, Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
			
			String name = ((AttributeString) nameA.get(0)).getValue();

			File f = getFile(name);
			if (f == null) {
				f = new File(project.strTable);
				f.setFilename(name);
				files.put(f.filename, f);
			}

			List<Attribute> locA = n.findAttribute(aType.atInt, "LOC", Metric.CONTEXT_METRIC);
			
			int loc = ((AttributeInt) locA.get(0)).getValue();
			
			f.loc = loc;
		}
	}
	
	private void updateChangeInfo(Map<Integer, ChangeInfo> map, Integer id, Delta.TYPE type, int begin, int end) {
		ChangeInfo s = null;
		if(map.containsKey(id)) {
			s = map.get(id);
		} else {
			s = new ChangeInfo();
			s.modified = 0;
			s.added = 0;
			s.deleted = 0;
		}
		if (type == Delta.TYPE.CHANGE) {
			s.modified += end - begin + 1;
		} else if (type == Delta.TYPE.DELETE) {
			s.deleted += end - begin + 1;
		} else if (type == Delta.TYPE.INSERT) {
			s.added += end - begin + 1;
		}
		map.put(id, s);
	}
	
	private void updateOldChangeInfo(Map<Integer, ChangeInfo> map, Integer id, Delta.TYPE type, int begin, int end, Node onode) {
		List<Attribute> attrs = onode.findAttribute(aType.atComposite,
				Metric.ATTR_POSITION, Metric.CONTEXT_ATTRIBUTE);
		AttributeComposite pos = (AttributeComposite) attrs.get(0);

		attrs = pos
				.findAttribute(aType.atInt, Metric.ATTR_LINE, "");
		int beginLine = ((AttributeInt) attrs.get(0)).getValue();

		attrs = pos.findAttribute(aType.atInt, Metric.ATTR_ENDLINE,
				"");
		int endLine = ((AttributeInt) attrs.get(0)).getValue();
		
		int from = 0, to = 0;
		if (beginLine <= begin && endLine >= end) {
			from = begin;
			to = end;
		} else if (beginLine >= begin && endLine <= end) {
			from = beginLine;
			to = endLine;
		} else if (beginLine > begin && beginLine <= end) {
			from = beginLine;
			to = end;
		} else if (endLine >= begin && endLine < end) {
			from = begin;
			to = endLine;
		}
		updateChangeInfo(map, id, type, from, to);
	}
	
	private Node findNodeByLongName(List<Node> nodes, int name) {
		for(Node n : nodes) {
			List<Attribute> attrs = n.findAttribute(aType.atString,
					Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
			String nameAttr = ((AttributeString) attrs.get(0))
					.getValue();

			int oname = project.strTable.set(nameAttr);
			if (oname == name) {
				return n;
			}
		}
		return null;
	}
	
	private Node findMethodNodeByLongName(Node oclassnode, int name) {
		List<Edge> edges =  oclassnode.findOutEdges(new EdgeType(Metric.ETYPE_LIM_LOGICALTREE, Edge.eDirectionType.edtDirectional));
		for (Edge e : edges) {
			Node m = e.getToNode();
			if (m.getType().getType().equals(Metric.NTYPE_LIM_METHOD)) {
				List<Attribute> attrs = m.findAttribute(aType.atString,
						Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
				String nameAttr = ((AttributeString) attrs.get(0))
						.getValue();
	
				int oname = project.strTable.set(nameAttr);
				if (oname == name) {
					return m;
				}
			}
		}
		return null;
	}

	/**
	 * kigyujti a commit altal modositott forraskodelemeket method, class
	 */
	public void connectDiff(boolean filter) {
		List<Node> classNodes = graph.findNodes(new NodeType(
				Metric.NTYPE_LIM_CLASS));
		List<Node> oldClassNodes = null;
		if (this.prevGraph != null) {
			oldClassNodes = prevGraph.findNodes(new NodeType(
					Metric.NTYPE_LIM_CLASS));
		}
		for (FileChange f : changes) {
			List<String> ud = new ArrayList<String>();
			ud.add("+++");
			ud.addAll(Arrays.asList(f.diff.split("\n")));
			Patch p = DiffUtils.parseUnifiedDiff(ud);

			if (filter && project.isFiltered(f.modifiedFile.getFilename()))
				continue;

			for (Delta d : p.getDeltas()) {
				Deltas++;
				int begin = 1 + d.getRevised().getPosition();
				int end = d.getRevised().getPosition() + d.getRevised().size();
				boolean limited = false; // begin or end of file, or empty
				if (begin + BACKLASH > end - BACKLASH) {
					limited = true;
					if (begin < 4)
						end -= BACKLASH;
					else
						begin += BACKLASH;
					if (begin > end) {
						begin = end;
					}
				} else {
					begin += BACKLASH;
					end -= BACKLASH;
				}

				int obegin = 1 + d.getOriginal().getPosition();
				int oend = d.getOriginal().getPosition() + d.getOriginal().size();
				boolean olimited = false; // begin or end of file, or empty
				if (obegin + BACKLASH > oend - BACKLASH) {
					olimited = true;
					if (obegin < 4)
						oend -= BACKLASH;
					else
						obegin += BACKLASH;
					if (obegin > oend) {
						obegin = oend;
					}
				} else {
					obegin += BACKLASH;
					oend -= BACKLASH;
				}
				
				Delta.TYPE type = d.getType();
				if (olimited && !limited) {
					// az eredeti kod merete 0, az uje pedig >0
					type = Delta.TYPE.INSERT;
				} else if (!olimited && limited) {
					// az eredeti kod merete > 0, az uje pedig 0
					type = Delta.TYPE.DELETE;
				} else {
					type = Delta.TYPE.CHANGE;
				}
				
				//Set<java.util.Map.Entry<Integer, Integer>> s = null;
				//if(modifiedFiles.containsKey(f.modifiedFile.filename)) {
				//	s = modifiedFiles.get(f.modifiedFile.filename);
				//} else {
				//	s = new HashSet<>();
				//}
				//s.add(new java.util.AbstractMap.SimpleEntry<Integer, Integer>(begin, end));
				//modifiedFiles.put(f.modifiedFile.filename, s );
				/*
				int s = 0;
				if(modifiedFiles.containsKey(f.modifiedFile.filename)) {
					s = modifiedFiles.get(f.modifiedFile.filename);
				}
				s += end - begin + 1;
				modifiedFiles.put(f.modifiedFile.filename, s );
				*/
				if (type == Delta.TYPE.DELETE) {
					updateChangeInfo(modifiedFiles, f.modifiedFile.filename, type, obegin, oend);
				} else {
					updateChangeInfo(modifiedFiles, f.modifiedFile.filename, type, begin, end);
				}

				// bejárni a method-okat, classokat

				for (Node n : classNodes) {
					Node oclassnode = null;
					int name = 0;
					{

						List<Attribute> attrs = n.findAttribute(aType.atComposite,
								Metric.ATTR_POSITION, Metric.CONTEXT_ATTRIBUTE);
						AttributeComposite pos = (AttributeComposite) attrs.get(0);

						attrs = pos.findAttribute(aType.atString, Metric.ATTR_PATH,
								"");
						String path = ((AttributeString) attrs.get(0)).getValue();

						path = path.replace("\\", "/");

						if (!path.equals(f.modifiedFile.getFilename()))
							continue;

						attrs = pos
								.findAttribute(aType.atInt, Metric.ATTR_LINE, "");
						int beginLine = ((AttributeInt) attrs.get(0)).getValue();

						attrs = pos.findAttribute(aType.atInt, Metric.ATTR_ENDLINE,
								"");
						int endLine = ((AttributeInt) attrs.get(0)).getValue();

						attrs = n.findAttribute(aType.atString,
								Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
						String nameAttr = ((AttributeString) attrs.get(0))
								.getValue();

						if (filter && project.isFiltered(nameAttr))
							continue;

						name = project.strTable.set(nameAttr);

						int modLines = 0;
						int from = 0, to = 0;
						if (beginLine <= begin && endLine >= end) {
							modLines = end-begin+1;
							from = begin;
							to = end;
							S1c++;
						} else if (beginLine >= begin && endLine <= end) {
							modLines = endLine-beginLine+1;
							from = beginLine;
							to = endLine;
							S2c++;
						} else if (beginLine > begin && beginLine <= end) {
							modLines = end-beginLine+1;
							from = beginLine;
							to = end;
							S3c++;
						} else if (endLine >= begin && endLine < end) {
							modLines = endLine-begin+1;
							from = begin;
							to = endLine;
							S3c++;
						}
						if (modLines > 0) {
							//s = null;
							//if(modifiedClasses.containsKey(name)) {
							//	s = modifiedClasses.get(name);
							//} else {
							//	s = new HashSet<>();
							//}
							//s.add(new java.util.AbstractMap.SimpleEntry<Integer, Integer>(from, to));
							//modifiedClasses.put(name, s );
							/*
							s = 0;
							if(modifiedClasses.containsKey(name)) {
								s = modifiedClasses.get(name);
							}
							s += to - from +1;
							modifiedClasses.put(name, s );
							*/
							if (type == Delta.TYPE.DELETE && this.prevGraph != null) {
								oclassnode = findNodeByLongName(oldClassNodes, name);
								if (oclassnode != null) {
									updateOldChangeInfo(modifiedClasses, name, type, obegin, oend, oclassnode);
								}
							} else {
								updateChangeInfo(modifiedClasses, name, type, from, to);
							}
							modifiedClassParent.put(name, f.modifiedFile.getFilenameId());
						}
						
					}

					List<Edge> edges =  n.findOutEdges(new EdgeType(Metric.ETYPE_LIM_LOGICALTREE, Edge.eDirectionType.edtDirectional));

					
					for (Edge e : edges) {
						Node m = e.getToNode();
						Node omethodnode = null;
						if (m.getType().getType().equals(Metric.NTYPE_LIM_METHOD)) {
							List<Attribute> mattrs = m.findAttribute(aType.atComposite,
									Metric.ATTR_POSITION, Metric.CONTEXT_ATTRIBUTE);
							AttributeComposite mpos = (AttributeComposite) mattrs.get(0);

							mattrs = mpos
									.findAttribute(aType.atInt, Metric.ATTR_LINE, "");
							int mbeginLine = ((AttributeInt) mattrs.get(0)).getValue();

							mattrs = mpos.findAttribute(aType.atInt, Metric.ATTR_ENDLINE,
									"");
							int mendLine = ((AttributeInt) mattrs.get(0)).getValue();

							mattrs = m.findAttribute(aType.atString,
									Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
							String mlongnameAttr = ((AttributeString) mattrs.get(0))
									.getValue();

							if (filter && project.isFiltered(mlongnameAttr))
								continue;
							
							int mname = project.strTable.set(mlongnameAttr);

							int mmodLines = 0;
							int from = 0, to = 0;
							if (mbeginLine <= begin && mendLine >= end) {
								//modifiedMethods.put(mname, 0);
								//modifiedMethodParent.put(mname, name);
								mmodLines = end-begin+1;
								from = begin;
								to = end;
								S1m++;
							} else if (mbeginLine >= begin && mendLine <= end) {
								//modifiedMethods.put(mname, 0);
								//modifiedMethodParent.put(mname, name);
								mmodLines = mendLine-mbeginLine+1;
								from = mbeginLine;
								to = mendLine;
								S2m++;
							} else if (mbeginLine > begin && mbeginLine <= end) {
								//modifiedMethods.put(mname, 0);
								//modifiedMethodParent.put(mname, name);
								mmodLines = end-mbeginLine+1;
								from = mbeginLine;
								to = end;
								S3m++;
							} else if (mendLine >= begin && mendLine < end) {
								//modifiedMethods.put(mname, 0);
								//modifiedMethodParent.put(mname, name);
								mmodLines = mendLine-begin+1;
								from = begin;
								to = mendLine;
								S3m++;
							}
							if (mmodLines > 0) {
								//s = null;
								//if(modifiedMethods.containsKey(mname)) {
								//	s = modifiedMethods.get(mname);
								//} else {
								//	s = new HashSet<>();
								//}
								//s.add(new java.util.AbstractMap.SimpleEntry<Integer, Integer>(from, to));
								//modifiedMethods.put(mname, s );
								/*
								s = 0;
								if(modifiedMethods.containsKey(mname)) {
									s = modifiedMethods.get(mname);
								}
								modifiedMethods.put(mname, s );
								*/
								//updateMethodChangeInfo(modifiedMethods, mname, type, from, to);
								if (type == Delta.TYPE.DELETE && oclassnode != null) {
									omethodnode = findMethodNodeByLongName(oclassnode, mname);
									if (omethodnode != null) {
										updateOldChangeInfo(modifiedMethods, mname, type, obegin, oend, omethodnode);
									}
								} else {
									updateChangeInfo(modifiedMethods, mname, type, from, to);
								}
								modifiedMethodParent.put(mname, name);
							}
							
						}
					}
					
					
					
				}
			}
		}
		connected = true;
		/*
		 * LOGGER.info("\nCommit: " + commit.getSha() +
		 * "\nNumber of connected classes: " + NOC +
		 * "\nNumber of connected methods: " + NOM +
		 * "\nNumber of unclear compliance: " + NOI + "\nNumber of deltas: " +
		 * NOD);
		 */

	}

	public int findNodeId(int longnameid) {
		if (graph == null)
			return 0;
		List<Node> nodes = graph.getNodes();
		for (Node n : nodes) {
			List<Attribute> attrs = n.findAttribute(aType.atString,
					Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
			if (attrs.size() == 1) {
				String longnameAttr = ((AttributeString) attrs.get(0))
						.getValue();
				if (longnameid == project.strTable.get(longnameAttr)) {
					String uid = n.getUID();
					return Integer.parseInt(uid.substring(1, uid.length()));
				}
			} else if (attrs.size() > 1) {
				System.out.println("attrs size > 1");
			}
		}
		return 0;
	}

	public void buildLongNameNodeIdMap() {

		List<Node> nodes = graph
				.findNodes(new NodeType(Metric.NTYPE_LIM_CLASS));
		for (Node n : nodes) {

			String uid = n.getUID();
			int id = Integer.parseInt(uid.substring(1, uid.length()));

			List<Attribute> attrs = n.findAttribute(aType.atString,
					Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
			String nameAttr = ((AttributeString) attrs.get(0)).getValue();

			if (nameAttr.matches(".*Test.*"))
				continue;

			int name = project.strTable.set(nameAttr);

			longnameNodeIdIndexClasses.put(name, id);

		}
		nodes = graph.findNodes(new NodeType(Metric.NTYPE_LIM_METHOD));
		for (Node n : nodes) {

			String uid = n.getUID();
			int id = Integer.parseInt(uid.substring(1, uid.length()));

			List<Attribute> attrs = n.findAttribute(aType.atString,
					Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
			String nameAttr = ((AttributeString) attrs.get(0)).getValue();

			attrs = n.findAttribute(aType.atString, Metric.ATTR_NAME,
					Metric.CONTEXT_ATTRIBUTE);
			String nameAttr2 = ((AttributeString) attrs.get(0)).getValue();

			if (nameAttr2.matches("test.*"))
				continue;

			int name = project.strTable.set(nameAttr);

			longnameNodeIdIndexMethods.put(name, id);

		}
		nodes = graph.findNodes(new NodeType(Metric.NTYPE_LIM_FILE));
		for (Node n : nodes) {

			String uid = n.getUID();
			int id = Integer.parseInt(uid.substring(1, uid.length()));

			List<Attribute> attrs = n.findAttribute(aType.atString,
					Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
			String nameAttr = ((AttributeString) attrs.get(0)).getValue();
			nameAttr = nameAttr.replace("/", "\\");
			int name = project.strTable.set(nameAttr);

			longnameNodeIdIndexFiles.put(name, id);

		}
	}

	/*
	 * public Set<String> getWarnings() { return warnings; }
	 * 
	 * public void setWarnings(Set<String> warnings) { this.warnings = warnings;
	 * }
	 */
	/*
	 * public Set<Integer> getMetrics() { return metrics; }
	 * 
	 * public void setMetrics(Set<Integer> metrics) { this.metrics = metrics; }
	 */
	public Set<Integer> collectFileMetricNames() {
		Set<Integer> s = new HashSet<Integer>();
		for (Map<Integer, Float> i : fileMetrics.values()) {
			s.addAll(i.keySet());
			break;
		}
		return s;
	}

	/**
	 * miutan kigyujtottunk mindent a grafbol, felszabaditjuk, hogy legyen
	 * memoria a tobbi grafnak is
	 */
	public void free() {
		// files.clear();
		// warnings.clear();
		// metrics.clear();
		// changes.clear();
		graph = null;
		// fileMetrics.clear();
		longnameNodeIdIndexMethods.clear();
		longnameNodeIdIndexClasses.clear();
		longnameNodeIdIndexFiles.clear();

		/*
		 * modifiedClasses.clear(); modifiedMethods.clear();
		 * modifiedFiles.clear();
		 */
	}

	public void fullFree() {
		free();
		fileMetrics.clear();
		modifiedClasses.clear();
		modifiedMethods.clear();
		modifiedFiles.clear();
		bugs.clear();
		commitMetrics.clear();
		connected = false;
		fileBugs.clear();
		fileModUser.clear();
		lastModUser.clear();
		modifiedMethodsMMID.clear();
	}

	public void computeBugsStat() {
		if (graph == null)
			return;

		List<Node> nodes = graph.getNodes();
		for (Node n : nodes) {
			List<Attribute> attrs = n.findAttribute(aType.atString,
					Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
			if (attrs.size() == 1) {
				String longnameAttr = ((AttributeString) attrs.get(0))
						.getValue();
				int lnid = project.strTable.get(longnameAttr);

				if (bugs.keySet().contains(lnid)) {
					if (n.getType().getType().equals(Metric.NTYPE_LIM_CLASS)) {
						buggy_classes++;
					} else if (n.getType().getType()
							.equals(Metric.NTYPE_LIM_METHOD)) {
						buggy_methods++;
					}
				}
				if (fileBugs.keySet().contains(lnid)) {
					buggy_files++;
				}
			}
		}
	}

	public void saveCommitData(String dir) {
		saveCommitDataElement(dir);
		saveCommitDataFile(dir);
	}

	private void saveCommitDataElement(String dir) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(
					new java.io.File(dir + "\\" + sha + "-element.csv")));

			StringBuilder sb = new StringBuilder();

			// Header
			sb.append("\"lim_id\",");
			for (Integer cmid : commitMetrics.keySet()) {
				sb.append("\"" + project.strTable.get(cmid) + "\"");
				sb.append(",");
			}

			bw.write(sb.toString());

			bw.write("\"Number of bugs\"\n");
			
			Map<Integer, Set<Integer>> map_nodeid_issueid = new HashMap<Integer, Set<Integer>>();

			for (Integer lnid : bugs.keySet()) {

				int nodeid = 0;
				nodeid = findNodeId(lnid);

				if (nodeid != 0) {
					
					for(Integer issueid : traditionalRelatedIssues.keySet()) {
						if(traditionalRelatedIssues.get(issueid).contains(lnid)) {
							if(!map_nodeid_issueid.containsKey(nodeid))
								map_nodeid_issueid.put(nodeid, new HashSet<Integer>());
							map_nodeid_issueid.get(nodeid).add(issueid);
						}
					}

					sb = new StringBuilder();
					sb.append("\"L" + Integer.toString(nodeid) + "\",");

					for (Integer cmid : commitMetrics.keySet()) {
						sb.append("\"" + commitMetrics.get(cmid) + "\"");
						sb.append(",");
					}
					bw.write(sb.toString());

					bw.append("\"" + Integer.toString(bugs.get(lnid)) + "\"\n");
				}
			}
			
			writeNodeIdIssueIdMap(map_nodeid_issueid, dir, "element");

			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveCommitDataFile(String dir) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(
					new java.io.File(dir + "\\" + sha + "-file.csv")));

			StringBuilder sb = new StringBuilder();

			// Header
			sb.append("\"lim_id\",");
			for (Integer mn : fileMetricNames) {
				sb.append("\"" + project.strTable.get(mn) + "\"");
				sb.append(",");
			}/*
			 * for(Integer cmid : commitMetrics.keySet()) { sb.append("\"" +
			 * project.getStrTable().get(cmid) + "\""); sb.append(","); }
			 */

			bw.write(sb.toString());

			bw.write("\"Number of bugs\"\n");

			/*
			 * for(Integer lnid : fileBugs.keySet()) {
			 * 
			 * int nodeid = 0; nodeid = findNodeId(lnid);
			 * 
			 * // ha megtalalhato az erintett elem a legelso elotti verzioban,
			 * egyebkent skip if(nodeid != 0){
			 * 
			 * sb = new StringBuilder(); sb.append("\"L" +
			 * Integer.toString(nodeid) + "\","); for(Integer mn :
			 * fileMetricNames) { Map<Integer,Float> mmm =
			 * fileMetrics.get(lnid); if(mmm != null) { Float m = mmm.get(mn);
			 * if(m != null) { sb.append("\"" + Utils.format(m) + "\""); } else
			 * { sb.append("\"0\""); } } else { sb.append("\"0\""); }
			 * sb.append(","); } //for(Integer cmid : commitMetrics.keySet()) {
			 * // sb.append("\"" + commitMetrics.get(cmid) + "\""); //
			 * sb.append(","); //} bw.write(sb.toString());
			 * 
			 * bw.append("\"" + Integer.toString(fileBugs.get(lnid)) + "\"\n");
			 * } }
			 */

			Map<Integer, Set<Integer>> map_nodeid_issueid = new HashMap<Integer, Set<Integer>>();

			for (Integer lnid : fileMetrics.keySet()) {

				int nodeid = 0;
				nodeid = findNodeId(lnid);

				if (nodeid != 0) {
					
					for(Integer issueid : traditionalRelatedIssues.keySet()) {
						if(traditionalRelatedIssues.get(issueid).contains(lnid)) {
							if(!map_nodeid_issueid.containsKey(nodeid))
								map_nodeid_issueid.put(nodeid, new HashSet<Integer>());
							map_nodeid_issueid.get(nodeid).add(issueid);
						}
					}
					
					sb = new StringBuilder();
					sb.append("\"L" + Integer.toString(nodeid) + "\",");
					for (Integer mn : fileMetricNames) {
						Map<Integer, Float> mmm = fileMetrics.get(lnid);
						if (mmm != null) {
							Float m = mmm.get(mn);
							if (m != null) {
								sb.append("\"" + Utils.format(m) + "\"");
							} else {
								sb.append("\"0\"");
							}
						} else {
							sb.append("\"0\"");
						}
						sb.append(",");
					}/*
					 * for(Integer cmid : commitMetrics.keySet()) {
					 * sb.append("\"" + commitMetrics.get(cmid) + "\"");
					 * sb.append(","); }
					 */
					bw.write(sb.toString());

					bw.append("\""
							+ Integer.toString(fileBugs.containsKey(lnid) ? fileBugs
									.get(lnid) : 0) + "\"\n");
				}
			}

			writeNodeIdIssueIdMap(map_nodeid_issueid, dir, "file");
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeNodeIdIssueIdMap(Map<Integer, Set<Integer>> map, String dir, String postfix) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(
					new java.io.File(dir + "\\node_issue_" + sha + "-" + postfix + ".csv")));

			for(Integer nodeid : map.keySet()) {
				bw.append("" + nodeid + ",");
				for(Integer issueid : map.get(nodeid))
					bw.append(issueid + ",");
				bw.append("\n");
			}

			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * public void dumpDataToCsv(String file) throws IOException {
	 * 
	 * if(graph == null) { return; }
	 * 
	 * BufferedWriter bw = new BufferedWriter( new FileWriter(new
	 * java.io.File(file + "\\" + sha + "-dump.csv")) );
	 * 
	 * StringBuilder sb = new StringBuilder();
	 * 
	 * // Header sb.append("hash,timestamp,name,count\n");
	 * 
	 * bw.write(sb.toString());
	 * 
	 * List<Node> nodes = graph.findNodes(new NodeType(Metric.NTYPE_LIM_CLASS));
	 * nodes.addAll(graph.findNodes(new NodeType(Metric.NTYPE_LIM_METHOD)));
	 * 
	 * Calendar c = Calendar.getInstance(); c.setTime(created); long time =
	 * c.getTimeInMillis();
	 * 
	 * for(Node node : nodes) { List<Attribute> attrs =
	 * node.findAttribute(aType.atString, Metric.ATTR_LONGNAME,
	 * Metric.CONTEXT_ATTRIBUTE); String nameAttr =
	 * ((AttributeString)attrs.get(0)).getValue();
	 * 
	 * int id = strTable.get(nameAttr); if(!bugs.containsKey(id) ||
	 * (!longnameNodeIdIndexClasses.containsKey(id) &&
	 * !longnameNodeIdIndexMethods.containsKey(id)) ) continue; int bugsNum =
	 * bugs.get(id); if(bugsNum > 0) { sb = new StringBuilder();
	 * 
	 * sb.append(sha); sb.append(","); sb.append(time); sb.append(",");
	 * sb.append(nameAttr); sb.append(","); sb.append(bugsNum); sb.append("\n");
	 * 
	 * bw.write(sb.toString()); } }
	 * 
	 * bw.flush(); bw.close(); }
	 */

	/*
	 * public Map<Integer, Integer> getLongNameNodeIdIndex() { return
	 * longnameNodeIdIndex; }
	 */

	public boolean hasLicense() {
		if (graph == null)
			return true;
		return graph.getHeaderInfo("FaultHunter-mode").equals("full");
	}

	public void addIssueBug(int issueId, int longnameid) {
		if(!traditionalRelatedIssues.containsKey(issueId)) {
			traditionalRelatedIssues.put(issueId, new HashSet<Integer>());
		}
		traditionalRelatedIssues.get(issueId).add(longnameid);
	}
}
