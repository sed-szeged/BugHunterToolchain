package bughunter.datacollector.model;

import graphlib.Attribute;
import graphlib.Attribute.aType;
import graphlib.AttributeComposite;
import graphlib.AttributeInt;
import graphlib.AttributeString;
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
import bughunter.datacollector.StrTable;
import bughunter.datacollector.Utils;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

public class Commit implements Serializable {

	private static final long serialVersionUID = 1834195323256932310L;
	private String sha;
	private Date created;
	private List<FileChange> changes;
	private List<File> files;
	private Package root;
	private boolean fix;
	private int numberOfBugs;

	private Commit parent;
	private User author;

	private Graph graph;

	private StrTable strTable;

	private Project project;

	private int buggy_methods;
	private int buggy_classes;
	private int buggy_files;

	public void setProject(Project project) {
		this.project = project;
	}

	/**
	 * longname-bol nodeid lekeresehez
	 */
	private Map<Integer, Integer> longnameNodeIdIndexMethods;
	private Map<Integer, Integer> longnameNodeIdIndexClasses;
	private Map<Integer, Integer> longnameNodeIdIndexFiles;

	private Map<Integer, Set<Integer>> traditionalRelatedIssues;
	
	private boolean isRelease;
	
	/**
	 * longnameid-hez tartozo bugok szama
	 */
	private Map<Integer, Integer> bugs;

	public Map<Integer, Integer> getFileBugs() {
		return fileBugs;
	}

	private Map<Integer, Integer> fileBugs;

	/**
	 * a commit szintu metrikak (nyitott issue-k)
	 */
	private Map<Integer, Integer> commitMetrics;

	/**
	 * fajlokhoz tartozo metrikak, filenameid-vel azonositva
	 */
	private Map<Integer, Map<Integer, Float>> fileMetrics;

	private Map<Integer, Set<Integer>> fileModUser;
	private Map<Integer, Integer> lastModUser;

	/**
	 * a commit altal modositott forraskod elemek longnameid-ja
	 */
	private Set<Integer> modifiedMethods;
	private Set<Integer> modifiedClasses;
	private Set<Integer> modifiedFiles;

	private Set<Integer> modifiedMethodsMMID;

	private Set<Integer> fileMetricNames;

	/**
	 * diff-connect statisztikak
	 */
	private transient int S1c;
	private transient int S1m;
	private transient int S2c;
	private transient int S2m;
	private transient int S3c;
	private transient int S3m;
	private transient int Deltas;
	private transient boolean connected;

	private transient static int BACKLASH = 3;

	public Commit() {
		changes = new ArrayList<FileChange>();
		files = new ArrayList<File>();
		// warnings = new HashSet<String>();
		// metrics = new HashSet<Integer>();
		bugs = new HashMap<Integer, Integer>();
		modifiedMethods = new HashSet<Integer>();
		modifiedClasses = new HashSet<Integer>();
		modifiedFiles = new HashSet<Integer>();
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
	}

	public Map<Integer, Integer> getLongnameNodeIdIndexFiles() {
		return longnameNodeIdIndexFiles;
	}

	public Commit(String sha, Date created, List<FileChange> changes,
			List<File> files, boolean fix, User author) {

		this.sha = sha;
		this.created = created;
		this.changes = changes;
		this.files = files;
		this.fix = fix;
		this.author = author;

		// warnings = new HashSet<String>();
		// metrics = new HashSet<Integer>();
		bugs = new HashMap<Integer, Integer>();
		modifiedMethods = new HashSet<Integer>();
		modifiedClasses = new HashSet<Integer>();
		modifiedFiles = new HashSet<Integer>();
		commitMetrics = new HashMap<Integer, Integer>();
		fileMetrics = new HashMap<Integer, Map<Integer, Float>>();
		longnameNodeIdIndexMethods = new HashMap<Integer, Integer>();
		longnameNodeIdIndexClasses = new HashMap<Integer, Integer>();
		longnameNodeIdIndexFiles = new HashMap<Integer, Integer>();
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
	}

	public Map<Integer, Set<Integer>> getFileModUser() {
		return fileModUser;
	}

	public Map<Integer, Integer> getLastModUser() {
		return lastModUser;
	}

	public String getSha() {
		return sha;
	}

	public void setSha(String sha) {
		this.sha = sha;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public Commit getParent() {
		return parent;
	}

	public void setParent(Commit parent) {
		this.parent = parent;
	}

	public List<File> getFiles() {
		return files;
	}

	public void setFiles(List<File> files) {
		this.files = files;
	}

	public List<FileChange> getChanges() {
		return changes;
	}

	public void setChanges(List<FileChange> changes) {
		this.changes = changes;
	}

	public Package getRoot() {
		return root;
	}

	public void setRoot(Package root) {
		this.root = root;
	}

	public boolean isFix() {
		return fix;
	}

	public void setFix(boolean fix) {
		this.fix = fix;
	}

	public int getNumberOfBugs() {
		return numberOfBugs;
	}

	public void setNumberOfBugs(int numberOfBugs) {
		this.numberOfBugs = numberOfBugs;
	}

	public File getFile(String path) {
		for (File f : files) {
			if (f.getFilename().equals(path))
				return f;
		}
		return null;
	}

	/**
	 * osszegyujti a modosított forraskod elemek longnameid-jat jelenleg: method
	 */
	/*
	 * public void collectModifiedElements() { for (FileChange ch :
	 * getChanges()) { modifiedMethods.addAll(ch.getModifiedMethods());
	 * modifiedClasses.addAll(ch.getModifiedClasses());
	 * modifiedFiles.add(ch.getModifiedFile().getFilenameId()); } }
	 */

	/**
	 * kimenti a grafbol a parameterul kapott longnameid-val rendelkezo node-ok
	 * metrikait, es ezt a parost eltarolja ily modon nincs szukseg a grafra
	 * 
	 * @param longnameids
	 *            kimentendo metrikak node-jainak longnameid-je
	 */
	/*
	 * public void collectElementMetrics(Set<Integer> longnameids) { for(Integer
	 * lnid : longnameids) { if(longnameNodeIdIndex.containsKey(lnid)) {
	 * if(nodes.get(longnameNodeIdIndex.get(lnid)) == null )
	 * System.out.println("null " + sha); elementsMetrics.put(lnid,
	 * nodes.get(longnameNodeIdIndex.get(lnid)).getMetrics()); } } //
	 * for(SourceElement se: nodes.values()) {
	 * 
	 * elementsMetrics.put(se.getLongNameId(), se.getMetrics());
	 * 
	 * } }
	 */

	/**
	 * visszaadja az issue-nak a modositott forraskod elemek longnameid-jat ezt
	 * hasznaja fel az issue, a bug altal erintett elemek kigyujtesere
	 * 
	 * @return modositott elemek longnameid-ja
	 */
	public Set<Integer> getModifiedMethods() {
		return modifiedMethods;
	}

	public Set<Integer> getModifiedClasses() {
		return modifiedClasses;
	}

	public Set<Integer> getModifiedFiles() {
		return modifiedFiles;
	}

	/**
	 * kigyujti a commit altal modositott forraskodelemeket method, class
	 */
	public void connectDiff() {
		if (connected) {
			System.out.println("Modifiel elements already connected: " + sha);
			return;
		}
		List<Node> classNodes = graph.findNodes(new NodeType(
				Metric.NTYPE_LIM_CLASS));
		List<Node> methodNodes = graph.findNodes(new NodeType(
				Metric.NTYPE_LIM_METHOD));
		for (FileChange f : getChanges()) {
			List<String> ud = new ArrayList<String>();
			ud.add("+++");
			ud.addAll(Arrays.asList(f.getDiff().split("\n")));
			Patch p = DiffUtils.parseUnifiedDiff(ud);

			if (project.isFiltered(f.getModifiedFile().getFilename()))
				continue;

			modifiedFiles.add(f.getModifiedFile().getFilenameId());

			for (Delta d : p.getDeltas()) {
				Deltas++;
				int begin = 1 + d.getRevised().getPosition();
				int end = d.getRevised().getPosition() + d.getRevised().size();
				if (begin + BACKLASH > end - BACKLASH) {
					if (begin < 4)
						end -= BACKLASH;
					else
						begin += BACKLASH;
				} else {
					begin += BACKLASH;
					end -= BACKLASH;
				}

				// bejárni a method-okat, classokat

				for (Node n : classNodes) {
					List<Attribute> attrs = n.findAttribute(aType.atComposite,
							Metric.ATTR_POSITION, Metric.CONTEXT_ATTRIBUTE);
					AttributeComposite pos = (AttributeComposite) attrs.get(0);

					attrs = pos.findAttribute(aType.atString, Metric.ATTR_PATH,
							"");
					String path = ((AttributeString) attrs.get(0)).getValue();

					path = path.replace("\\", "/");

					if (!path.equals(f.getModifiedFile().getFilename()))
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

					if (project.isFiltered(nameAttr))
						continue;

					int name = strTable.set(nameAttr);

					attrs = n.findAttribute(aType.atString, Metric.ATTR_NAME,
							Metric.CONTEXT_ATTRIBUTE);
					nameAttr = ((AttributeString) attrs.get(0)).getValue();

					if (beginLine <= begin && endLine >= end) {
						modifiedClasses.add(name);
						S1c++;
					} else if (beginLine >= begin && endLine <= end) {
						modifiedClasses.add(name);
						S2c++;
					} else if ((beginLine > begin && beginLine <= end)
							|| (endLine >= begin && endLine < end)) {
						modifiedClasses.add(name);
						S3c++;
					}
				}
				for (Node n : methodNodes) {
					List<Attribute> attrs = n.findAttribute(aType.atComposite,
							Metric.ATTR_POSITION, Metric.CONTEXT_ATTRIBUTE);
					AttributeComposite pos = (AttributeComposite) attrs.get(0);

					attrs = pos.findAttribute(aType.atString, Metric.ATTR_PATH,
							"");
					String path = ((AttributeString) attrs.get(0)).getValue();

					path = path.replace("\\", "/");

					if (!path.equals(f.getModifiedFile().getFilename()))
						continue;

					attrs = pos
							.findAttribute(aType.atInt, Metric.ATTR_LINE, "");
					int beginLine = ((AttributeInt) attrs.get(0)).getValue();

					attrs = pos.findAttribute(aType.atInt, Metric.ATTR_ENDLINE,
							"");
					int endLine = ((AttributeInt) attrs.get(0)).getValue();

					attrs = n.findAttribute(aType.atString,
							Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
					String longnameAttr = ((AttributeString) attrs.get(0))
							.getValue();

					if (project.isFiltered(longnameAttr))
						continue;

					// attrs = n.findAttribute(aType.atString, Metric.ATTR_NAME,
					// Metric.CONTEXT_ATTRIBUTE);
					// String nameAttr =
					// ((AttributeString)attrs.get(0)).getValue();

					int name = strTable.set(longnameAttr);

					if (beginLine <= begin && endLine >= end) {
						modifiedMethods.add(name);
						S1m++;
					} else if (beginLine >= begin && endLine <= end) {
						modifiedMethods.add(name);
						S2m++;
					} else if ((beginLine > begin && beginLine <= end)
							|| (endLine >= begin && endLine < end)) {
						modifiedMethods.add(name);
						S3m++;
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
				if (longnameid == project.getStrTable().get(longnameAttr)) {
					String uid = n.getUID();
					return Integer.parseInt(uid.substring(1, uid.length()));
				}
			} else if (attrs.size() > 1) {
				System.out.println("attrs size > 1");
			}
		}
		return 0;
	}

	public void connectDiffMMID() {
		if (connected) {
			System.out.println("Modifiel elements already connected: " + sha);
			return;
		}
		List<Node> methodNodes = graph.findNodes(new NodeType(
				Metric.NTYPE_LIM_METHOD));
		for (FileChange f : getChanges()) {
			List<String> ud = new ArrayList<String>();
			ud.add("+++");
			ud.addAll(Arrays.asList(f.getDiff().split("\n")));
			Patch p = DiffUtils.parseUnifiedDiff(ud);

			for (Delta d : p.getDeltas()) {

				int begin = 1 + d.getRevised().getPosition();
				int end = d.getRevised().getPosition() + d.getRevised().size();
				if (begin + BACKLASH > end - BACKLASH) {
					if (begin < 4)
						end -= BACKLASH;
					else
						begin += BACKLASH;
				} else {
					begin += BACKLASH;
					end -= BACKLASH;
				}

				for (Node n : methodNodes) {
					List<Attribute> attrs = n.findAttribute(aType.atComposite,
							Metric.ATTR_POSITION, Metric.CONTEXT_ATTRIBUTE);
					AttributeComposite pos = (AttributeComposite) attrs.get(0);

					attrs = pos.findAttribute(aType.atString, Metric.ATTR_PATH,
							"");
					String path = ((AttributeString) attrs.get(0)).getValue();

					path = path.replace("\\", "/");

					if (!path.equals(f.getModifiedFile().getFilename()))
						continue;

					attrs = pos
							.findAttribute(aType.atInt, Metric.ATTR_LINE, "");
					int beginLine = ((AttributeInt) attrs.get(0)).getValue();

					attrs = pos.findAttribute(aType.atInt, Metric.ATTR_ENDLINE,
							"");
					int endLine = ((AttributeInt) attrs.get(0)).getValue();

					attrs = n.findAttribute(aType.atString, Metric.ATTR_NAME,
							Metric.CONTEXT_ATTRIBUTE);
					String name = ((AttributeString) attrs.get(0)).getValue();

					attrs = n.findAttribute(aType.atString,
							Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
					String nameAttr = ((AttributeString) attrs.get(0))
							.getValue();

					if (project.isFiltered(nameAttr))
						continue;

					// int nameL = strTable.set(nameAttr);

					int uid = Integer.parseInt(n.getUID().substring(1,
							n.getUID().length()));

					if (beginLine <= begin && endLine >= end) {
						modifiedMethodsMMID.add(uid);
					} else if (beginLine >= begin && endLine <= end) {
						modifiedMethodsMMID.add(uid);
					} else if ((beginLine > begin && beginLine <= end)
							|| (endLine >= begin && endLine < end)) {
						modifiedMethodsMMID.add(uid);
					}
				}
			}
		}
		connected = true;
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

			int name = strTable.set(nameAttr);

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

			int name = strTable.set(nameAttr);

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
			int name = strTable.set(nameAttr);

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

	public Set<Integer> getFileMetricNames() {
		return fileMetricNames;
	}

	/**
	 * miutan kigyujtottunk mindent a grafbol, felszabaditjuk, hogy legyen
	 * memoria a tobbi grafnak is
	 */
	public void free() {
		root = null;
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
				int lnid = project.getStrTable().get(longnameAttr);

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
				sb.append("\"" + project.getStrTable().get(cmid) + "\"");
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
				sb.append("\"" + project.getStrTable().get(mn) + "\"");
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

	public Map<Integer, Integer> getBugs() {
		return bugs;
	}

	public void setBugs(Map<Integer, Integer> bugs) {
		this.bugs = bugs;
	}

	/*
	 * public Map<Integer, Integer> getLongNameNodeIdIndex() { return
	 * longnameNodeIdIndex; }
	 */

	public StrTable getStrTable() {
		return strTable;
	}

	public Map<Integer, Integer> getLongnameNodeIdIndexMethods() {
		return longnameNodeIdIndexMethods;
	}

	public Map<Integer, Integer> getLongnameNodeIdIndexClasses() {
		return longnameNodeIdIndexClasses;
	}

	public void setStrTable(StrTable strTable) {
		this.strTable = strTable;
	}

	public Map<Integer, Map<Integer, Float>> getFileMetrics() {
		return fileMetrics;
	}

	public void setFileMetrics(Map<Integer, Map<Integer, Float>> fileMetrics) {
		this.fileMetrics = fileMetrics;
	}

	public int getS1c() {
		return S1c;
	}

	public void setS1c(int s1c) {
		S1c = s1c;
	}

	public int getS1m() {
		return S1m;
	}

	public void setS1m(int s1m) {
		S1m = s1m;
	}

	public int getS2c() {
		return S2c;
	}

	public void setS2c(int s2c) {
		S2c = s2c;
	}

	public int getS2m() {
		return S2m;
	}

	public void setS2m(int s2m) {
		S2m = s2m;
	}

	public int getS3c() {
		return S3c;
	}

	public void setS3c(int s3c) {
		S3c = s3c;
	}

	public int getS3m() {
		return S3m;
	}

	public void setS3m(int s3m) {
		S3m = s3m;
	}

	public int getDeltas() {
		return Deltas;
	}

	public void setDeltas(int deltas) {
		Deltas = deltas;
	}

	public boolean isConnected() {
		return connected;
	}

	public Map<Integer, Integer> getCommitMetrics() {
		return commitMetrics;
	}

	public void setCommitMetrics(Map<Integer, Integer> commitMetrics) {
		this.commitMetrics = commitMetrics;
	}

	public void setGraph(Graph graph) {
		this.graph = graph;
	}

	public Graph getGraph() {
		return graph;
	}

	public Set<Integer> getModifiedMethodsMMID() {
		return modifiedMethodsMMID;
	}

	public boolean hasLicense() {
		if (graph == null)
			return true;
		return graph.getHeaderInfo("FaultHunter-mode").equals("full");
	}

	public int getBuggy_methods() {
		return buggy_methods;
	}

	public void setBuggy_methods(int buggy_methods) {
		this.buggy_methods = buggy_methods;
	}

	public int getBuggy_classes() {
		return buggy_classes;
	}

	public void setBuggy_classes(int buggy_classes) {
		this.buggy_classes = buggy_classes;
	}

	public int getBuggy_files() {
		return buggy_files;
	}

	public void setBuggy_files(int buggy_files) {
		this.buggy_files = buggy_files;
	}

	public Map<Integer, Set<Integer>> getTraditionalRelatedIssues() {
		return traditionalRelatedIssues;
	}

	public boolean isRelease() {
		return isRelease;
	}

	public void setRelease(boolean isRelease) {
		this.isRelease = isRelease;
	}

	public void addIssueBug(int issueId, int longnameid) {
		if(!traditionalRelatedIssues.containsKey(issueId)) {
			traditionalRelatedIssues.put(issueId, new HashSet<Integer>());
		}
		traditionalRelatedIssues.get(issueId).add(longnameid);
	}
}
