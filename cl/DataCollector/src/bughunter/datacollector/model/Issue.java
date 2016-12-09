package bughunter.datacollector.model;

import graphlib.Attribute;
import graphlib.AttributeInt;
import graphlib.Node;
import graphsupportlib.Metric;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bughunter.datacollector.Utils;

public class Issue implements Serializable {

	private static final long serialVersionUID = 2172019442577791239L;
	private int id;
	private Date opened;
	private Date closed;
	
	private Commit first;
	private Commit prev;
	private Commit last;
	private Commit prevRelease;

	private Map<String, Commit> commits;
	private List<Commit> sortedCommits;

	private Set<Integer> modifiedMethods;
	private Set<Integer> modifiedClasses;
	private Set<Integer> modifiedFiles;
	//private Set<String> warnings;
	private Set<Integer> methodMetricNames;
	private Set<Integer> classMetricNames;
	private Set<Integer> fileMetricNames;
	
	
	/**
	 * a legelso commit es a bejelentes datuma kozotti commitok
	 * ezekben talalhato meg a bug elvileg
	 */
	private List<Commit> beforeCommits;
	
	private Project project;

	public Issue() {
		commits = new HashMap<String, Commit>();
		modifiedMethods = new HashSet<Integer>();
		modifiedClasses = new HashSet<Integer>();
		modifiedFiles = new HashSet<Integer>();
		//warnings = new HashSet<String>();
		classMetricNames = new HashSet<Integer>();
		methodMetricNames = new HashSet<Integer>();
		fileMetricNames = new HashSet<Integer>();
		beforeCommits = new ArrayList<Commit>();
		sortedCommits = new ArrayList<Commit>();
	}

	public Issue(int id, Date opened, Date closed, Map<String, Commit> commits) {

		this.id = id;
		this.opened = opened;
		this.closed = closed;
		this.commits = commits;
		modifiedMethods = new HashSet<Integer>();
		modifiedClasses = new HashSet<Integer>();
		modifiedFiles = new HashSet<Integer>();
		//warnings = new HashSet<String>();
		classMetricNames = new HashSet<Integer>();
		methodMetricNames = new HashSet<Integer>();
		fileMetricNames = new HashSet<Integer>();
		beforeCommits = new ArrayList<Commit>();
		sortedCommits = new ArrayList<Commit>();
		sortedCommits.addAll(commits.values());
		Collections.sort(sortedCommits, new Comparator<Commit>() {
			public int compare(Commit o1, Commit o2) {
				if (o1.getCreated().after(o2.getCreated())) return 1;
				else if (o1.getCreated().before(o2.getCreated())) return -1;
				else return 0;
			}
		});
	}

	public void collectModifiedElements() {
		for(Commit c : commits.values()) {
			modifiedMethods.addAll(c.getModifiedMethods());
			modifiedClasses.addAll(c.getModifiedClasses());
			modifiedFiles.addAll(c.getModifiedFiles());
			//warnings.addAll(c.getWarnings());
			//metrics.addAll(c.getMetrics());
			//fileMetrics.addAll(c.getFileMetricNames());
		}
	}
	
	public void setPrevAndLastCommit(String prevSha, String lastSha) {
		prev = project.getCommits().get(prevSha);
		last = project.getCommits().get(lastSha);
		//last.setFix(true);
		if(prev == null)
			System.out.println(prevSha + " commit not found");
		if(last == null)
			System.out.println(lastSha + " commit not found");
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getOpened() {
		return opened;
	}

	public void setOpened(Date opened) {
		this.opened = opened;
	}

	public Date getClosed() {
		return closed;
	}

	public void setClosed(Date closed) {
		this.closed = closed;
	}

	public Map<String, Commit> getCommits() {
		return commits;
	}
/*
	public void setCommits(Map<String, Commit> commits) {
		this.commits = commits;
	}*/

	public Commit getPrev() {
		return prev;
	}

	public void setPrev(Commit prev) {
		this.prev = prev;
	}

	public Commit getLast() {
		return last;
	}

	public void setLast(Commit last) {
		this.last = last;
	}

	public Set<Integer> getModifiedMethods() {
		return modifiedMethods;
	}

	public void setModifiedMethods(Set<Integer> modified) {
		this.modifiedMethods = modified;
	}
/*
	public Set<String> getWarnings() {
		return warnings;
	}

	public void setWarnings(Set<String> warnings) {
		this.warnings = warnings;
	}*/

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	/**
	 * bejelentes datuma es a legelso commit kozotti commitok kigyujtese
	 */
	public void collectBeforeCommits() {
		Date firstCreated = first.getCreated();
		
		for(Commit comm : project.getCommits().values()){
			if(opened.before(comm.getCreated()) && comm.getCreated().before(firstCreated) && prev != comm){
				beforeCommits.add(comm);
				if(commits.containsKey(comm.getSha())) {
					System.out.println("before commit az issue commitok kozott");
					System.out.println(comm.getSha() + " " + first.getSha());
				}
			}
		}
	}

	public void setFirstCommit(String sha) {
		first = project.getCommits().get(sha);
		if(first == null)
			System.out.println("Missing first commit: " + sha);
	}

	public Commit getFirst() {
		return first;
	}

	public void setFirst(Commit first) {
		this.first = first;
	}

	public List<Commit> getBeforeCommits() {
		return beforeCommits;
	}

	public void setBeforeCommits(List<Commit> beforeCommits) {
		this.beforeCommits = beforeCommits;
	}

	public void collectMetricNames() {

		for(int nameId : modifiedMethods) {
			if(getPrev().getLongnameNodeIdIndexMethods().containsKey(nameId)) {
				int nodeid = getPrev().getLongnameNodeIdIndexMethods().get(nameId);
				Node node = getPrev().getGraph().findNode("L" + nodeid);
				List<Attribute> attrs = node.findAttributeByContext(Metric.CONTEXT_METRIC);
				attrs.addAll(node.findAttributeByContext(Metric.CONTEXT_METRICGROUP));
				for(Attribute attr : attrs) {
					methodMetricNames.add(project.getStrTable().set(attr.getName()));
				}
			}
			if(getLast().getLongnameNodeIdIndexMethods().containsKey(nameId)) {
				int nodeid = getLast().getLongnameNodeIdIndexMethods().get(nameId);
				Node node = getLast().getGraph().findNode("L" + nodeid);
				List<Attribute> attrs = node.findAttributeByContext(Metric.CONTEXT_METRIC);
				attrs.addAll(node.findAttributeByContext(Metric.CONTEXT_METRICGROUP));
				for(Attribute attr : attrs) {
					methodMetricNames.add(project.getStrTable().set(attr.getName()));
				}
			}
		}
		for(int nameId : modifiedClasses) {
			if(getPrev().getLongnameNodeIdIndexClasses().containsKey(nameId)) {
				int nodeid = getPrev().getLongnameNodeIdIndexClasses().get(nameId);
				Node node = getPrev().getGraph().findNode("L" + nodeid);
				List<Attribute> attrs = node.findAttributeByContext(Metric.CONTEXT_METRIC);
				attrs.addAll(node.findAttributeByContext(Metric.CONTEXT_METRICGROUP));
				for(Attribute attr : attrs) {
					classMetricNames.add(project.getStrTable().set(attr.getName()));
				}

			}
			if(getLast().getLongnameNodeIdIndexClasses().containsKey(nameId)) {
				int nodeid = getLast().getLongnameNodeIdIndexClasses().get(nameId);
				Node node = getLast().getGraph().findNode("L" + nodeid);
				List<Attribute> attrs = node.findAttributeByContext(Metric.CONTEXT_METRIC);
				attrs.addAll(node.findAttributeByContext(Metric.CONTEXT_METRICGROUP));
				for(Attribute attr : attrs) {
					classMetricNames.add(project.getStrTable().set(attr.getName()));
				}
			}
		}
		for(int nameId : modifiedFiles) {
			if(getPrev().getLongnameNodeIdIndexFiles().containsKey(nameId)) {
				int nodeid = getPrev().getLongnameNodeIdIndexFiles().get(nameId);
				Node node = getPrev().getGraph().findNode("L" + nodeid);
				List<Attribute> attrs = node.findAttributeByContext(Metric.CONTEXT_METRIC);
				attrs.addAll(node.findAttributeByContext(Metric.CONTEXT_METRICGROUP));
				Map<Integer, Float> tm;
				if(getPrev().getFileMetrics().containsKey(nameId))
					tm = getPrev().getFileMetrics().get(nameId);
				else
					tm = new HashMap<Integer, Float>();
				for(Attribute attr : attrs) {
					int id = project.getStrTable().set(attr.getName());
					fileMetricNames.add(id);
					tm.put(id, ((AttributeInt)attr).getValue()*1f);
				}
				getPrev().getFileMetrics().put(nameId, tm);

			}
			if(getLast().getLongnameNodeIdIndexFiles().containsKey(nameId)) {
				int nodeid = getLast().getLongnameNodeIdIndexFiles().get(nameId);
				Node node = getLast().getGraph().findNode("L" + nodeid);
				List<Attribute> attrs = node.findAttributeByContext(Metric.CONTEXT_METRIC);
				attrs.addAll(node.findAttributeByContext(Metric.CONTEXT_METRICGROUP));
				Map<Integer, Float> tm;
				if(getLast().getFileMetrics().containsKey(nameId))
					tm = getLast().getFileMetrics().get(nameId);
				else
					tm = new HashMap<Integer, Float>();
				for(Attribute attr : attrs) {
					int id = project.getStrTable().set(attr.getName());
					fileMetricNames.add(id);
					tm.put(id, ((AttributeInt)attr).getValue()*1f);
				}
				getLast().getFileMetrics().put(nameId, tm);
			}
		}
	}

	/**
	 * az issue-hoz tartozo commitok altal erintett forraskod elemeket
	 * a beforecommits listaban levo commitokban bejeloli hibasnak
	 */
	public void collectBugNumbers() {
		collectMethodBugNumbersPriv();
		collectMethodbugNumbersIncremental();
		collectClassBugNumbersPriv();
		collectClassbugNumbersIncremental();
		collectFileBugNumbersPriv();
		collectFilebugNumbersIncremental();
	}
	private void collectMethodBugNumbersPriv() {
		// minden érintett forráskód elem
		for(Integer modElementLNID : modifiedMethods) {
			for(Commit c : beforeCommits) {
				if(!project.getNeededCommits().contains(c.getSha()))
					continue;
				if(c.getBugs().containsKey(modElementLNID)) {
					c.getBugs().put(modElementLNID, c.getBugs().get(modElementLNID) + 1);
				} else {
					c.getBugs().put(modElementLNID, 1);
				}
				if(c.isRelease()) {
					c.addIssueBug(id, modElementLNID);
				}
			}
			if(prev.getBugs().containsKey(modElementLNID)) {
				prev.getBugs().put(modElementLNID, prev.getBugs().get(modElementLNID) + 1);
			} else {
				prev.getBugs().put(modElementLNID, 1);
			}
			if(prev.isRelease()) {
				prev.addIssueBug(id, modElementLNID);
			}
			if(prevRelease != null && prev != prevRelease) {
				if(prevRelease.getBugs().containsKey(modElementLNID)) {
					prevRelease.getBugs().put(modElementLNID, prevRelease.getBugs().get(modElementLNID) + 1);
				} else {
					prevRelease.getBugs().put(modElementLNID, 1);
				}

				if(prevRelease.isRelease()) {
					prevRelease.addIssueBug(id, modElementLNID);
				}
			}
			if(!last.getBugs().containsKey(modElementLNID)) {
				last.getBugs().put(modElementLNID, 0);
			}
		}
	}
	private void collectMethodbugNumbersIncremental() {
		// a köztes verziókban csak az számít hibásnak, amit még javítani fognak a jövõben
		Set<Integer> collected = new HashSet<Integer>();
		for(int j = sortedCommits.size() - 1; j >= 0; j--){
			Commit c = sortedCommits.get(j);
			Set<Integer> modified = new HashSet<Integer>();
			modified.addAll(c.getModifiedMethods());
			modified.removeAll(collected);
			
			// szükséges verziók: pre+last+traditional
			for(String ncs : project.getNeededCommits()) {
				Commit nc = project.getCommits().get(ncs);
				if(nc == null)
					continue;
				if(nc.getSha().equals(last.getSha()))// a legutolsóban már számoltuk
					continue;
				// az elsõ után, viszont még az éppen vizsgált kapcsolódó commit elõtt
				if(nc.getCreated().after(prev.getCreated()) && nc.getCreated().before(c.getCreated())) {
					for(Integer mod : modified) {
						if(nc.getBugs().containsKey(mod)) {
							nc.getBugs().put(mod, nc.getBugs().get(mod) + 1);
						} else {
							nc.getBugs().put(mod, 1);
						}
						if(nc.isRelease()) {
							nc.addIssueBug(id, mod);
						}
					}
				}
			}
			collected.addAll(modified);
		}		
	}
	
	private void collectClassBugNumbersPriv() {
		for(Integer modElementLNID : modifiedClasses) {
			for(Commit c : beforeCommits) {
				if(!project.getNeededCommits().contains(c.getSha()))
					continue;
				if(c.getBugs().containsKey(modElementLNID)) {
					c.getBugs().put(modElementLNID, c.getBugs().get(modElementLNID) + 1);
				} else {
					c.getBugs().put(modElementLNID, 1);
				}
				if(c.isRelease()) {
					c.addIssueBug(id, modElementLNID);
				}
			}
			if(prev.getBugs().containsKey(modElementLNID)) {
				prev.getBugs().put(modElementLNID, prev.getBugs().get(modElementLNID) + 1);
			} else {
				prev.getBugs().put(modElementLNID, 1);
			}
			if(prev.isRelease()) {
				prev.addIssueBug(id, modElementLNID);
			}
			if(prevRelease != null && prev != prevRelease) {
				if(prevRelease.getBugs().containsKey(modElementLNID)) {
					prevRelease.getBugs().put(modElementLNID, prevRelease.getBugs().get(modElementLNID) + 1);
				} else {
					prevRelease.getBugs().put(modElementLNID, 1);
				}
				if(prevRelease.isRelease()) {
					prevRelease.addIssueBug(id, modElementLNID);
				}
			}
			if(!last.getBugs().containsKey(modElementLNID)) {
				last.getBugs().put(modElementLNID, 0);
			}
		}
	}
	private void collectClassbugNumbersIncremental() {
		// a köztes verziókban csak az számít hibásnak, amit még javítani fognak a jövõben
		Set<Integer> collected = new HashSet<Integer>();
		for(int j = sortedCommits.size() - 1; j >= 0; j--){
			Commit c = sortedCommits.get(j);
			Set<Integer> modified = new HashSet<Integer>();
			modified.addAll(c.getModifiedClasses());
			modified.removeAll(collected);
			
			for(String ncs : project.getNeededCommits()) {
				Commit nc = project.getCommits().get(ncs);
				if(nc == null)
					continue;
				if(nc.getSha().equals(last.getSha()))
					continue;
				// a commit a hiba bejelentés után keletkezett, viszont még az éppen vizsgált kapcsolódó commit elõtt
				if(nc.getCreated().after(prev.getCreated()) && nc.getCreated().before(c.getCreated())) {
					for(Integer mod : modified) {
						if(nc.getBugs().containsKey(mod)) {
							nc.getBugs().put(mod, nc.getBugs().get(mod) + 1);
						} else {
							nc.getBugs().put(mod, 1);
						}
						if(nc.isRelease()) {
							nc.addIssueBug(id, mod);
						}
					}
				}
			}
			collected.addAll(modified);
		}		
	}
	
	private void collectFileBugNumbersPriv() {
		for(Integer modFileLNID : modifiedFiles) {
			for(Commit c : beforeCommits) {
				if(!project.getNeededCommits().contains(c.getSha()))
					continue;
				if(c.getFileBugs().containsKey(modFileLNID)) {
					c.getFileBugs().put(modFileLNID, c.getFileBugs().get(modFileLNID) + 1);
				} else {
					c.getFileBugs().put(modFileLNID, 1);
				}
				if(c.isRelease()) {
					c.addIssueBug(id, modFileLNID);
				}
			}
			if(prev.getFileBugs().containsKey(modFileLNID)) {
				prev.getFileBugs().put(modFileLNID, prev.getFileBugs().get(modFileLNID) + 1);
			} else {
				prev.getFileBugs().put(modFileLNID, 1);
			}
			if(prev.isRelease()) {
				prev.addIssueBug(id, modFileLNID);
			}
			if(prevRelease != null && prev != prevRelease) {
				if(prevRelease.getFileBugs().containsKey(modFileLNID)) {
					prevRelease.getFileBugs().put(modFileLNID, prevRelease.getFileBugs().get(modFileLNID) + 1);
				} else {
					prevRelease.getFileBugs().put(modFileLNID, 1);
				}
				if(prevRelease.isRelease()) {
					prevRelease.addIssueBug(id, modFileLNID);
				}
			}
			if(!last.getFileBugs().containsKey(modFileLNID)) {
				last.getFileBugs().put(modFileLNID, 0);
			}
		}
	}
	private void collectFilebugNumbersIncremental() {
		// a köztes verziókban csak az számít hibásnak, amit még javítani fognak a jövõben
		Set<Integer> collected = new HashSet<Integer>();
		for(int j = sortedCommits.size() - 1; j >= 0; j--){
			Commit c = sortedCommits.get(j);
			Set<Integer> modified = new HashSet<Integer>();
			modified.addAll(c.getModifiedFiles());
			modified.removeAll(collected);
			
			for(String ncs : project.getNeededCommits()) {
				Commit nc = project.getCommits().get(ncs);
				if(nc == null)
					continue;
				if(nc.getSha().equals(last.getSha()))
					continue;
				// a commit a hiba bejelentés után keletkezett, viszont még az éppen vizsgált kapcsolódó commit elõtt
				if(nc.getCreated().after(prev.getCreated()) && nc.getCreated().before(c.getCreated())) {
					for(Integer mod : modified) {
						if(nc.getFileBugs().containsKey(mod)) {
							nc.getFileBugs().put(mod, nc.getFileBugs().get(mod) + 1);
						} else {
							nc.getFileBugs().put(mod, 1);
						}
						if(nc.isRelease()) {
							nc.addIssueBug(id, mod);
						}
					}
				}
			}
			collected.addAll(modified);
		}		
	}
	
	public void saveDataToCsv(String csvdir) {
		try {
			saveClassDataToCsv(csvdir);
			saveMethodDataToCsv(csvdir);
			saveFileDataToCsv(csvdir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void saveMethodDataToCsv(String csvdir) throws IOException {

		if(getPrev() == null && getLast() == null)
			return;
		
		BufferedWriter cbw = new BufferedWriter( new FileWriter(new java.io.File(csvdir + "\\" + getId() + "-method.csv" )) );;
		
		StringBuilder sb = new StringBuilder();

		// Header
		sb.append("\"Commit ID\",\"name\",\"Issue ID\",\"node_id\","); 
		/*for(Integer mn : methodMetricNames) {
			sb.append(project.getStrTable().get(mn));
			sb.append(",");
		}
		for(Integer cmid : getPrev().getCommitMetrics().keySet()) {
			sb.append("\"" + project.getStrTable().get(cmid) + "\"");
			sb.append(",");
		}*/

		cbw.write(sb.toString());

		cbw.write("\"Number of bugs\"\n");

		for(Integer modElementLNID : modifiedMethods) {

			int nodeid = 0;
			nodeid = getPrev().findNodeId(modElementLNID);

			// ha megtalalhato az erintett elem a legelso elotti verzioban, egyebkent skip
			if(nodeid != 0){

				//int nodeid = getPrev().getLongnameNodeIdIndexMethods().get(modElementLNID);

				sb = new StringBuilder();
				
				// node megkeresése
				//Node node = getPrev().getGraph().findNode("L"+nodeid);

				//List<Attribute> attrs = node.findAttribute(aType.atString, Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
				//String nameAttr = ((AttributeString)attrs.get(0)).getValue();
								
				sb.append("\"" + getPrev().getSha() + "\",\"" + project.getStrTable().get(modElementLNID) + "\",\"" + Integer.toString(id) + "\",\"" + "L"+nodeid + "\"");
				sb.append(",");
				
				// metrikak
				/*for(Integer mn : methodMetricNames) {
					String metricName = project.getStrTable().get(mn);
					//metrika lekérése
					List<Attribute> attrs = node.findAttributeByName(metricName);
					if(attrs.size()>0) {
						Attribute attr = attrs.get(0);
						if(attr.getType() == aType.atFloat)
							sb.append(((AttributeFloat)attr).getValue());
						else if(attr.getType() == aType.atInt)
							sb.append(((AttributeInt)attr).getValue());
					} else {
						sb.append(0);
					}
					sb.append(",");
				}
				for(Integer cmid : getPrev().getCommitMetrics().keySet()) {
					sb.append("\"" + getPrev().getCommitMetrics().get(cmid) + "\"");
					sb.append(",");
				}*/
				cbw.write(sb.toString());

				cbw.append("\"" + Integer.toString(getPrev().getBugs().get(modElementLNID)) + "\"\n");

			}

			nodeid = getLast().findNodeId(modElementLNID);
			// ha megtalalhato az erintett elem az utolso verzioban, egyebkent skip
			if(nodeid != 0) {
			
				//int nodeid = getLast().getLongnameNodeIdIndexMethods().get(modElementLNID);
				
				sb = new StringBuilder();
				
				// node megkeresése
				//Node node = getLast().getGraph().findNode("L"+nodeid);

				//List<Attribute> attrs = node.findAttribute(aType.atString, Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
				//String nameAttr = ((AttributeString)attrs.get(0)).getValue();
				
				sb.append("\"" + getLast().getSha() + "\",\"" + project.getStrTable().get(modElementLNID) + "\",\"" + Integer.toString(id) + "\",\"" + "L"+nodeid + "\"");
				sb.append(",");
				
				// metrikak
				/*for(Integer mn : methodMetricNames) {
					String metricName = project.getStrTable().get(mn);
					//metrika lekérése
					List<Attribute> attrs = node.findAttributeByName(metricName);
					if(attrs.size()>0) {
						Attribute attr = attrs.get(0);
						if(attr.getType() == aType.atFloat)
							sb.append(((AttributeFloat)attr).getValue());
						else if(attr.getType() == aType.atInt)
							sb.append(((AttributeInt)attr).getValue());
					} else {
						sb.append(0);
					}
					sb.append(",");
				}
				for(Integer cmid : getLast().getCommitMetrics().keySet()) {
					sb.append("\"" + getLast().getCommitMetrics().get(cmid) + "\"");
					sb.append(",");
				}*/
				cbw.write(sb.toString());
				
				cbw.append("\"" + Integer.toString(getLast().getBugs().get(modElementLNID)) + "\"\n");
			}
		}
		cbw.flush();
		cbw.close();
		
	}

	private void saveClassDataToCsv(String csvdir) throws IOException {

		if(getPrev() == null && getLast() == null)
			return;
		
		BufferedWriter cbw = new BufferedWriter( new FileWriter(new java.io.File(csvdir + "\\" + getId() + "-class.csv" )) );;
		
		StringBuilder sb = new StringBuilder();

		// Header
		sb.append("\"Commit ID\",\"name\",\"Issue ID\",\"node_id\","); 
		/*for(Integer mn : classMetricNames) {
			sb.append(project.getStrTable().get(mn));
			sb.append(",");
		}
		for(Integer cmid : getPrev().getCommitMetrics().keySet()) {
			sb.append("\"" + project.getStrTable().get(cmid) + "\"");
			sb.append(",");
		}*/

		cbw.write(sb.toString());

		cbw.write("\"Number of bugs\"\n");

		for(Integer modElementLNID : modifiedClasses) {

			int nodeid = 0;
			nodeid = getPrev().findNodeId(modElementLNID);
			
			// ha megtalalhato az erintett elem a legelso elotti verzioban, egyebkent skip
			if(nodeid != 0){
			
				//int nodeid = getPrev().getLongnameNodeIdIndexClasses().get(modElementLNID);

				sb = new StringBuilder();
				
				// node megkeresése
				//Node node = getPrev().getGraph().findNode("L"+nodeid);

				//List<Attribute> attrs = node.findAttribute(aType.atString, Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
				//String nameAttr = ((AttributeString)attrs.get(0)).getValue();
							
				sb.append("\"" + getPrev().getSha() + "\",\"" + project.getStrTable().get(modElementLNID) + "\",\"" + Integer.toString(id) + "\",\"" + "L"+nodeid + "\"");
				sb.append(",");
				
				// metrikak
				/*for(Integer mn : classMetricNames) {
					String metricName = project.getStrTable().get(mn);
					//metrika lekérése
					List<Attribute> attrs = node.findAttributeByName(metricName);
					if(attrs.size()>0) {
						Attribute attr = attrs.get(0);
						if(attr.getType() == aType.atFloat)
							sb.append(((AttributeFloat)attr).getValue());
						else if(attr.getType() == aType.atInt)
							sb.append(((AttributeInt)attr).getValue());
					} else {
						sb.append(0);
					}
					sb.append(",");
				}
				for(Integer cmid : getPrev().getCommitMetrics().keySet()) {
					sb.append("\"" + getPrev().getCommitMetrics().get(cmid) + "\"");
					sb.append(",");
				}*/
				cbw.write(sb.toString());

				cbw.append("\"" + Integer.toString(getPrev().getBugs().get(modElementLNID)) + "\"\n");

			}

			nodeid = getLast().findNodeId(modElementLNID);
			// ha megtalalhato az erintett elem az utolso verzioban, egyebkent skip
			if(nodeid != 0) {
			
				//int nodeid = getLast().getLongnameNodeIdIndexClasses().get(modElementLNID);
				
				sb = new StringBuilder();
				
				// node megkeresése
				//Node node = getLast().getGraph().findNode("L"+nodeid);

				//List<Attribute> attrs = node.findAttribute(aType.atString, Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
				//String nameAttr = ((AttributeString)attrs.get(0)).getValue();
							
				sb.append("\"" + getLast().getSha() + "\",\"" + project.getStrTable().get(modElementLNID) + "\",\"" + Integer.toString(id) + "\",\"" + "L"+nodeid + "\"");
				sb.append(",");
				
				// metrikak
				/*for(Integer mn : classMetricNames) {
					String metricName = project.getStrTable().get(mn);
					//metrika lekérése
					List<Attribute> attrs = node.findAttributeByName(metricName);
					if(attrs.size()>0) {
						Attribute attr = attrs.get(0);
						if(attr.getType() == aType.atFloat)
							sb.append(((AttributeFloat)attr).getValue());
						else if(attr.getType() == aType.atInt)
							sb.append(((AttributeInt)attr).getValue());
					} else {
						sb.append(0);
					}
					sb.append(",");
				}
				for(Integer cmid : getLast().getCommitMetrics().keySet()) {
					sb.append("\"" + getLast().getCommitMetrics().get(cmid) + "\"");
					sb.append(",");
				}*/
				cbw.write(sb.toString());
				
				cbw.append("\"" + Integer.toString(getLast().getBugs().get(modElementLNID)) + "\"\n");
			}
		}
		cbw.flush();
		cbw.close();
		
	}
	private void saveFileDataToCsv(String csvdir) throws IOException {

		if(getPrev() == null && getLast() == null)
			return;
		
		BufferedWriter cbw = new BufferedWriter( new FileWriter(new java.io.File(csvdir + "\\" + getId() + "-file.csv" )) );
		
		StringBuilder sb = new StringBuilder();
		Map<Integer, Map<Integer, Float>> fm;

		// Header
		sb.append("\"Commit ID\",\"name\",\"Issue ID\",\"node_id\","); 
		for(Integer mn : fileMetricNames) {
			sb.append("\"" + project.getStrTable().get(mn) + "\"");
			sb.append(",");
		}
		for(Integer cmid : getPrev().getCommitMetrics().keySet()) {
			sb.append("\"" + project.getStrTable().get(cmid) + "\"");
			sb.append(",");
		}

		cbw.write(sb.toString());

		cbw.write("\"Number of bugs\"\n");
		
		for(Integer modFileLNID : modifiedFiles) {

			int nodeid = 0;
			nodeid = getPrev().findNodeId(modFileLNID);

			// ha megtalalhato az erintett elem a legelso elotti verzioban, egyebkent skip
			if(nodeid != 0){

				//int nodeid = getPrev().getLongnameNodeIdIndexFiles().get(modFileLNID);
				
				fm = getPrev().getFileMetrics();
				
				sb = new StringBuilder();

				//Node node = getPrev().getGraph().findNode("L"+nodeid);

				//List<Attribute> attrs = node.findAttribute(aType.atString, Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
				//String nameAttr = ((AttributeString)attrs.get(0)).getValue();
							
				// id: sha+filename
				sb.append("\"" + getPrev().getSha() + "\",\"" + project.getStrTable().get(modFileLNID) + "\",\"" + Integer.toString(id) + "\",\"" + "L"+nodeid + "\"");
				sb.append(",");
				
				// metrikak
				for(Integer mn : fileMetricNames) {
					Map<Integer,Float> mmm = fm.get(modFileLNID);
					if(mmm != null) {
						Float m = mmm.get(mn);
						if(m != null) {
							sb.append("\"" + Utils.format(m) + "\"");
						} else {
							sb.append("\"0\"");
						}
					} else {
						sb.append("\"0\"");
					}
					sb.append(",");
				}
				for(Integer cmid : getPrev().getCommitMetrics().keySet()) {
					sb.append("\"" + getPrev().getCommitMetrics().get(cmid) + "\"");
					sb.append(",");
				}
				cbw.write(sb.toString());

				cbw.append("\"" + Integer.toString(getPrev().getFileBugs().get(modFileLNID)) + "\"\n");
			}

			nodeid = getLast().findNodeId(modFileLNID);
			// ha megtalalhato az erintett elem az utolso verzioban, egyebkent skip
			if(nodeid != 0) {

				//int nodeid = getLast().getLongnameNodeIdIndexFiles().get(modFileLNID);
				
				fm = getLast().getFileMetrics();
				
				sb = new StringBuilder();

				//Node node = getLast().getGraph().findNode("L"+nodeid);

				//List<Attribute> attrs = node.findAttribute(aType.atString, Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
				//String nameAttr = ((AttributeString)attrs.get(0)).getValue();
				
				// id: sha+filename
				sb.append("\"" + getLast().getSha() + "\",\"" + project.getStrTable().get(modFileLNID) + "\",\"" + Integer.toString(id) + "\",\"" + "L"+nodeid + "\"");
				sb.append(",");
				
				// metrikak
				for(Integer mn : fileMetricNames) {
					Map<Integer,Float> mmm = fm.get(modFileLNID);
					if(mmm != null) {
						Float m = mmm.get(mn);
						if(m != null) {
							sb.append("\"" + Utils.format(m) + "\"");
						} else {
							sb.append("\"0\"");
						}
					} else {
						sb.append("\"0\"");
					}
					sb.append(",");
				}
				for(Integer cmid : getLast().getCommitMetrics().keySet()) {
					sb.append("\"" + getLast().getCommitMetrics().get(cmid) + "\"");
					sb.append(",");
				}
				cbw.write(sb.toString());
				
				cbw.append("\"" + Integer.toString(getLast().getFileBugs().get(modFileLNID)) + "\"\n");
			}
		}
		cbw.flush();
		cbw.close();
		
	
	}
	
	public Set<Integer> getModifiedClasses() {
		return modifiedClasses;
	}

	public void setModifiedClasses(Set<Integer> modifiedClasses) {
		this.modifiedClasses = modifiedClasses;
	}

	public Set<Integer> getModifiedFiles() {
		return modifiedFiles;
	}

	public void setModifiedFiles(Set<Integer> modifiedFiles) {
		this.modifiedFiles = modifiedFiles;
	}

	public Set<Integer> getFileMetricsNames() {
		return fileMetricNames;
	}

	public void setFileMetricsNames(Set<Integer> fileMetrics) {
		this.fileMetricNames = fileMetrics;
	}

	public Commit getPrevRelease() {
		return prevRelease;
	}

	public void setPrevRelease(Commit prevRelease) {
		this.prevRelease = prevRelease;
	}


}
