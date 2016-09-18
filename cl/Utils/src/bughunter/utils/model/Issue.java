package bughunter.utils.model;

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

import bughunter.utils.Utils;

public class Issue implements Serializable {

	private static final long serialVersionUID = 2172019442577791239L;
	public int id;
	public Date opened;
	public Date closed;
	
	public Commit first;
	public Commit prev;
	public Commit last;
	public Commit prevRelease;

	public Map<String, Commit> commits;
	public List<Commit> sortedCommits;

	public Set<Integer> modifiedMethods;
	public Set<Integer> modifiedClasses;
	public Set<Integer> modifiedFiles;
	public Set<Integer> methodMetricNames;
	public Set<Integer> classMetricNames;
	public Set<Integer> fileMetricNames;
	
	
	/**
	 * a legelso commit es a bejelentes datuma kozotti commitok
	 * ezekben talalhato meg a bug elvileg
	 */
	public List<Commit> beforeCommits;
	
	public Project project;

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
				if (o1.created.after(o2.created)) return 1;
				else if (o1.created.before(o2.created)) return -1;
				else return 0;
			}
		});
	}

	public void collectModifiedElements() {
		for(Commit c : commits.values()) {
			modifiedMethods.addAll(c.modifiedMethods.keySet());
			modifiedClasses.addAll(c.modifiedClasses.keySet());
			modifiedFiles.addAll(c.modifiedFiles.keySet());
			//warnings.addAll(c.getWarnings());
			//metrics.addAll(c.getMetrics());
			//fileMetrics.addAll(c.getFileMetricNames());
		}
	}
	
	public void setPrevAndLastCommit(String prevSha, String lastSha) {
		prev = project.commits.get(prevSha);
		last = project.commits.get(lastSha);
		//last.setFix(true);
		if(prev == null)
			System.out.println(prevSha + " commit not found");
		if(last == null)
			System.out.println(lastSha + " commit not found");
	}
	
	/**
	 * bejelentes datuma es a legelso commit kozotti commitok kigyujtese
	 */
	public void collectBeforeCommits() {
		Date firstCreated = first.created;
		
		for(Commit comm : project.commits.values()){
			if(opened.before(comm.created) && comm.created.before(firstCreated) && prev != comm){
				beforeCommits.add(comm);
				if(commits.containsKey(comm.sha)) {
					System.out.println("before commit az issue commitok kozott");
					System.out.println(comm.sha + " " + first.sha);
				}
			}
		}
	}

	public void setFirstCommit(String sha) {
		first = project.commits.get(sha);
		if(first == null)
			System.out.println("Missing first commit: " + sha);
	}

	public void collectMetricNames() {

		for(int nameId : modifiedMethods) {
			if(prev.longnameNodeIdIndexMethods.containsKey(nameId)) {
				int nodeid = prev.longnameNodeIdIndexMethods.get(nameId);
				Node node = prev.graph.findNode("L" + nodeid);
				List<Attribute> attrs = node.findAttributeByContext(Metric.CONTEXT_METRIC);
				attrs.addAll(node.findAttributeByContext(Metric.CONTEXT_METRICGROUP));
				for(Attribute attr : attrs) {
					methodMetricNames.add(project.strTable.set(attr.getName()));
				}
			}
			if(last.longnameNodeIdIndexMethods.containsKey(nameId)) {
				int nodeid = last.longnameNodeIdIndexMethods.get(nameId);
				Node node = last.graph.findNode("L" + nodeid);
				List<Attribute> attrs = node.findAttributeByContext(Metric.CONTEXT_METRIC);
				attrs.addAll(node.findAttributeByContext(Metric.CONTEXT_METRICGROUP));
				for(Attribute attr : attrs) {
					methodMetricNames.add(project.strTable.set(attr.getName()));
				}
			}
		}
		for(int nameId : modifiedClasses) {
			if(prev.longnameNodeIdIndexClasses.containsKey(nameId)) {
				int nodeid = prev.longnameNodeIdIndexClasses.get(nameId);
				Node node = prev.graph.findNode("L" + nodeid);
				List<Attribute> attrs = node.findAttributeByContext(Metric.CONTEXT_METRIC);
				attrs.addAll(node.findAttributeByContext(Metric.CONTEXT_METRICGROUP));
				for(Attribute attr : attrs) {
					classMetricNames.add(project.strTable.set(attr.getName()));
				}

			}
			if(last.longnameNodeIdIndexClasses.containsKey(nameId)) {
				int nodeid = last.longnameNodeIdIndexClasses.get(nameId);
				Node node = last.graph.findNode("L" + nodeid);
				List<Attribute> attrs = node.findAttributeByContext(Metric.CONTEXT_METRIC);
				attrs.addAll(node.findAttributeByContext(Metric.CONTEXT_METRICGROUP));
				for(Attribute attr : attrs) {
					classMetricNames.add(project.strTable.set(attr.getName()));
				}
			}
		}
		for(int nameId : modifiedFiles) {
			if(prev.getLongnameNodeIdIndexFiles().containsKey(nameId)) {
				int nodeid = prev.getLongnameNodeIdIndexFiles().get(nameId);
				Node node = prev.graph.findNode("L" + nodeid);
				List<Attribute> attrs = node.findAttributeByContext(Metric.CONTEXT_METRIC);
				attrs.addAll(node.findAttributeByContext(Metric.CONTEXT_METRICGROUP));
				Map<Integer, Float> tm;
				if(prev.fileMetrics.containsKey(nameId))
					tm = prev.fileMetrics.get(nameId);
				else
					tm = new HashMap<Integer, Float>();
				for(Attribute attr : attrs) {
					int id = project.strTable.set(attr.getName());
					fileMetricNames.add(id);
					tm.put(id, ((AttributeInt)attr).getValue()*1f);
				}
				prev.fileMetrics.put(nameId, tm);

			}
			if(last.getLongnameNodeIdIndexFiles().containsKey(nameId)) {
				int nodeid = last.getLongnameNodeIdIndexFiles().get(nameId);
				Node node = last.graph.findNode("L" + nodeid);
				List<Attribute> attrs = node.findAttributeByContext(Metric.CONTEXT_METRIC);
				attrs.addAll(node.findAttributeByContext(Metric.CONTEXT_METRICGROUP));
				Map<Integer, Float> tm;
				if(last.fileMetrics.containsKey(nameId))
					tm = last.fileMetrics.get(nameId);
				else
					tm = new HashMap<Integer, Float>();
				for(Attribute attr : attrs) {
					int id = project.strTable.set(attr.getName());
					fileMetricNames.add(id);
					tm.put(id, ((AttributeInt)attr).getValue()*1f);
				}
				last.fileMetrics.put(nameId, tm);
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
				if(!project.neededCommits.contains(c.sha))
					continue;
				if(c.bugs.containsKey(modElementLNID)) {
					c.bugs.put(modElementLNID, c.bugs.get(modElementLNID) + 1);
				} else {
					c.bugs.put(modElementLNID, 1);
				}
				if(c.isRelease) {
					c.addIssueBug(id, modElementLNID);
				}
			}
			if(prev.bugs.containsKey(modElementLNID)) {
				prev.bugs.put(modElementLNID, prev.bugs.get(modElementLNID) + 1);
			} else {
				prev.bugs.put(modElementLNID, 1);
			}
			if(prev.isRelease) {
				prev.addIssueBug(id, modElementLNID);
			}
			if(prevRelease != null && prev != prevRelease) {
				if(prevRelease.bugs.containsKey(modElementLNID)) {
					prevRelease.bugs.put(modElementLNID, prevRelease.bugs.get(modElementLNID) + 1);
				} else {
					prevRelease.bugs.put(modElementLNID, 1);
				}

				if(prevRelease.isRelease) {
					prevRelease.addIssueBug(id, modElementLNID);
				}
			}
			if(!last.bugs.containsKey(modElementLNID)) {
				last.bugs.put(modElementLNID, 0);
			}
		}
	}
	private void collectMethodbugNumbersIncremental() {
		// a köztes verziókban csak az számít hibásnak, amit még javítani fognak a jövőben
		Set<Integer> collected = new HashSet<Integer>();
		for(int j = sortedCommits.size() - 1; j >= 0; j--){
			Commit c = sortedCommits.get(j);
			Set<Integer> modified = new HashSet<Integer>();
			modified.addAll(c.modifiedMethods.keySet());
			modified.removeAll(collected);
			
			// szükséges verziók: pre+last+traditional
			for(String ncs : project.neededCommits) {
				Commit nc = project.commits.get(ncs);
				if(nc == null)
					continue;
				if(nc.sha.equals(last.sha))// a legutolsóban már számoltuk
					continue;
				// az első után, viszont még az éppen vizsgált kapcsolódó commit előtt
				if(nc.created.after(prev.created) && nc.created.before(c.created)) {
					for(Integer mod : modified) {
						if(nc.bugs.containsKey(mod)) {
							nc.bugs.put(mod, nc.bugs.get(mod) + 1);
						} else {
							nc.bugs.put(mod, 1);
						}
						if(nc.isRelease) {
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
				if(!project.neededCommits.contains(c.sha))
					continue;
				if(c.bugs.containsKey(modElementLNID)) {
					c.bugs.put(modElementLNID, c.bugs.get(modElementLNID) + 1);
				} else {
					c.bugs.put(modElementLNID, 1);
				}
				if(c.isRelease) {
					c.addIssueBug(id, modElementLNID);
				}
			}
			if(prev.bugs.containsKey(modElementLNID)) {
				prev.bugs.put(modElementLNID, prev.bugs.get(modElementLNID) + 1);
			} else {
				prev.bugs.put(modElementLNID, 1);
			}
			if(prev.isRelease) {
				prev.addIssueBug(id, modElementLNID);
			}
			if(prevRelease != null && prev != prevRelease) {
				if(prevRelease.bugs.containsKey(modElementLNID)) {
					prevRelease.bugs.put(modElementLNID, prevRelease.bugs.get(modElementLNID) + 1);
				} else {
					prevRelease.bugs.put(modElementLNID, 1);
				}
				if(prevRelease.isRelease) {
					prevRelease.addIssueBug(id, modElementLNID);
				}
			}
			if(!last.bugs.containsKey(modElementLNID)) {
				last.bugs.put(modElementLNID, 0);
			}
		}
	}
	private void collectClassbugNumbersIncremental() {
		// a köztes verziókban csak az számít hibásnak, amit még javítani fognak a jövőben
		Set<Integer> collected = new HashSet<Integer>();
		for(int j = sortedCommits.size() - 1; j >= 0; j--){
			Commit c = sortedCommits.get(j);
			Set<Integer> modified = new HashSet<Integer>();
			modified.addAll(c.modifiedClasses.keySet());
			modified.removeAll(collected);
			
			for(String ncs : project.neededCommits) {
				Commit nc = project.commits.get(ncs);
				if(nc == null)
					continue;
				if(nc.sha.equals(last.sha))
					continue;
				// a commit a hiba bejelentés után keletkezett, viszont még az éppen vizsgált kapcsolódó commit előtt
				if(nc.created.after(prev.created) && nc.created.before(c.created)) {
					for(Integer mod : modified) {
						if(nc.bugs.containsKey(mod)) {
							nc.bugs.put(mod, nc.bugs.get(mod) + 1);
						} else {
							nc.bugs.put(mod, 1);
						}
						if(nc.isRelease) {
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
				if(!project.neededCommits.contains(c.sha))
					continue;
				if(c.fileBugs.containsKey(modFileLNID)) {
					c.fileBugs.put(modFileLNID, c.fileBugs.get(modFileLNID) + 1);
				} else {
					c.fileBugs.put(modFileLNID, 1);
				}
				if(c.isRelease) {
					c.addIssueBug(id, modFileLNID);
				}
			}
			if(prev.fileBugs.containsKey(modFileLNID)) {
				prev.fileBugs.put(modFileLNID, prev.fileBugs.get(modFileLNID) + 1);
			} else {
				prev.fileBugs.put(modFileLNID, 1);
			}
			if(prev.isRelease) {
				prev.addIssueBug(id, modFileLNID);
			}
			if(prevRelease != null && prev != prevRelease) {
				if(prevRelease.fileBugs.containsKey(modFileLNID)) {
					prevRelease.fileBugs.put(modFileLNID, prevRelease.fileBugs.get(modFileLNID) + 1);
				} else {
					prevRelease.fileBugs.put(modFileLNID, 1);
				}
				if(prevRelease.isRelease) {
					prevRelease.addIssueBug(id, modFileLNID);
				}
			}
			if(!last.fileBugs.containsKey(modFileLNID)) {
				last.fileBugs.put(modFileLNID, 0);
			}
		}
	}
	private void collectFilebugNumbersIncremental() {
		// a köztes verziókban csak az számít hibásnak, amit még javítani fognak a jövőben
		Set<Integer> collected = new HashSet<Integer>();
		for(int j = sortedCommits.size() - 1; j >= 0; j--){
			Commit c = sortedCommits.get(j);
			Set<Integer> modified = new HashSet<Integer>();
			modified.addAll(c.modifiedFiles.keySet());
			modified.removeAll(collected);
			
			for(String ncs : project.neededCommits) {
				Commit nc = project.commits.get(ncs);
				if(nc == null)
					continue;
				if(nc.sha.equals(last.sha))
					continue;
				// a commit a hiba bejelentés után keletkezett, viszont még az éppen vizsgált kapcsolódó commit előtt
				if(nc.created.after(prev.created) && nc.created.before(c.created)) {
					for(Integer mod : modified) {
						if(nc.fileBugs.containsKey(mod)) {
							nc.fileBugs.put(mod, nc.fileBugs.get(mod) + 1);
						} else {
							nc.fileBugs.put(mod, 1);
						}
						if(nc.isRelease) {
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

		if(prev == null && last == null)
			return;
		
		BufferedWriter cbw = new BufferedWriter( new FileWriter(new java.io.File(csvdir + "\\" + id + "-method.csv" )) );;
		
		StringBuilder sb = new StringBuilder();

		// Header
		sb.append("\"Commit ID\",\"name\",\"Issue ID\",\"node_id\","); 
		/*for(Integer mn : methodMetricNames) {
			sb.append(project.strTable.get(mn));
			sb.append(",");
		}
		for(Integer cmid : prev.commitMetrics.keySet()) {
			sb.append("\"" + project.strTable.get(cmid) + "\"");
			sb.append(",");
		}*/

		cbw.write(sb.toString());

		cbw.write("\"Number of bugs\"\n");

		for(Integer modElementLNID : modifiedMethods) {

			int nodeid = 0;
			nodeid = prev.findNodeId(modElementLNID);

			// ha megtalalhato az erintett elem a legelso elotti verzioban, egyebkent skip
			if(nodeid != 0){

				//int nodeid = prev.longnameNodeIdIndexMethods.get(modElementLNID);

				sb = new StringBuilder();
				
				// node megkeresése
				//Node node = prev.graph.findNode("L"+nodeid);

				//List<Attribute> attrs = node.findAttribute(aType.atString, Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
				//String nameAttr = ((AttributeString)attrs.get(0)).getValue();
								
				sb.append("\"" + prev.sha + "\",\"" + project.strTable.get(modElementLNID) + "\",\"" + Integer.toString(id) + "\",\"" + "L"+nodeid + "\"");
				sb.append(",");
				
				// metrikak
				/*for(Integer mn : methodMetricNames) {
					String metricName = project.strTable.get(mn);
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
				for(Integer cmid : prev.commitMetrics.keySet()) {
					sb.append("\"" + prev.commitMetrics.get(cmid) + "\"");
					sb.append(",");
				}*/
				cbw.write(sb.toString());

				cbw.append("\"" + Integer.toString(prev.bugs.get(modElementLNID)) + "\"\n");

			}

			nodeid = last.findNodeId(modElementLNID);
			// ha megtalalhato az erintett elem az utolso verzioban, egyebkent skip
			if(nodeid != 0) {
			
				//int nodeid = last.longnameNodeIdIndexMethods.get(modElementLNID);
				
				sb = new StringBuilder();
				
				// node megkeresése
				//Node node = last.graph.findNode("L"+nodeid);

				//List<Attribute> attrs = node.findAttribute(aType.atString, Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
				//String nameAttr = ((AttributeString)attrs.get(0)).getValue();
				
				sb.append("\"" + last.sha + "\",\"" + project.strTable.get(modElementLNID) + "\",\"" + Integer.toString(id) + "\",\"" + "L"+nodeid + "\"");
				sb.append(",");
				
				// metrikak
				/*for(Integer mn : methodMetricNames) {
					String metricName = project.strTable.get(mn);
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
				for(Integer cmid : last.commitMetrics.keySet()) {
					sb.append("\"" + last.commitMetrics.get(cmid) + "\"");
					sb.append(",");
				}*/
				cbw.write(sb.toString());
				
				cbw.append("\"" + Integer.toString(last.bugs.get(modElementLNID)) + "\"\n");
			}
		}
		cbw.flush();
		cbw.close();
		
	}

	private void saveClassDataToCsv(String csvdir) throws IOException {

		if(prev == null && last == null)
			return;
		
		BufferedWriter cbw = new BufferedWriter( new FileWriter(new java.io.File(csvdir + "\\" + id + "-class.csv" )) );;
		
		StringBuilder sb = new StringBuilder();

		// Header
		sb.append("\"Commit ID\",\"name\",\"Issue ID\",\"node_id\","); 
		/*for(Integer mn : classMetricNames) {
			sb.append(project.strTable.get(mn));
			sb.append(",");
		}
		for(Integer cmid : prev.commitMetrics.keySet()) {
			sb.append("\"" + project.strTable.get(cmid) + "\"");
			sb.append(",");
		}*/

		cbw.write(sb.toString());

		cbw.write("\"Number of bugs\"\n");

		for(Integer modElementLNID : modifiedClasses) {

			int nodeid = 0;
			nodeid = prev.findNodeId(modElementLNID);
			
			// ha megtalalhato az erintett elem a legelso elotti verzioban, egyebkent skip
			if(nodeid != 0){
			
				//int nodeid = prev.longnameNodeIdIndexClasses.get(modElementLNID);

				sb = new StringBuilder();
				
				// node megkeresése
				//Node node = prev.graph.findNode("L"+nodeid);

				//List<Attribute> attrs = node.findAttribute(aType.atString, Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
				//String nameAttr = ((AttributeString)attrs.get(0)).getValue();
							
				sb.append("\"" + prev.sha + "\",\"" + project.strTable.get(modElementLNID) + "\",\"" + Integer.toString(id) + "\",\"" + "L"+nodeid + "\"");
				sb.append(",");
				
				// metrikak
				/*for(Integer mn : classMetricNames) {
					String metricName = project.strTable.get(mn);
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
				for(Integer cmid : prev.commitMetrics.keySet()) {
					sb.append("\"" + prev.commitMetrics.get(cmid) + "\"");
					sb.append(",");
				}*/
				cbw.write(sb.toString());

				cbw.append("\"" + Integer.toString(prev.bugs.get(modElementLNID)) + "\"\n");

			}

			nodeid = last.findNodeId(modElementLNID);
			// ha megtalalhato az erintett elem az utolso verzioban, egyebkent skip
			if(nodeid != 0) {
			
				//int nodeid = last.longnameNodeIdIndexClasses.get(modElementLNID);
				
				sb = new StringBuilder();
				
				// node megkeresése
				//Node node = last.graph.findNode("L"+nodeid);

				//List<Attribute> attrs = node.findAttribute(aType.atString, Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
				//String nameAttr = ((AttributeString)attrs.get(0)).getValue();
							
				sb.append("\"" + last.sha + "\",\"" + project.strTable.get(modElementLNID) + "\",\"" + Integer.toString(id) + "\",\"" + "L"+nodeid + "\"");
				sb.append(",");
				
				// metrikak
				/*for(Integer mn : classMetricNames) {
					String metricName = project.strTable.get(mn);
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
				for(Integer cmid : last.commitMetrics.keySet()) {
					sb.append("\"" + last.commitMetrics.get(cmid) + "\"");
					sb.append(",");
				}*/
				cbw.write(sb.toString());
				
				cbw.append("\"" + Integer.toString(last.bugs.get(modElementLNID)) + "\"\n");
			}
		}
		cbw.flush();
		cbw.close();
		
	}
	private void saveFileDataToCsv(String csvdir) throws IOException {

		if(prev == null && last == null)
			return;
		
		BufferedWriter cbw = new BufferedWriter( new FileWriter(new java.io.File(csvdir + "\\" + id + "-file.csv" )) );
		
		StringBuilder sb = new StringBuilder();
		Map<Integer, Map<Integer, Float>> fm;

		// Header
		sb.append("\"Commit ID\",\"name\",\"Issue ID\",\"node_id\","); 
		for(Integer mn : fileMetricNames) {
			sb.append("\"" + project.strTable.get(mn) + "\"");
			sb.append(",");
		}
		for(Integer cmid : prev.commitMetrics.keySet()) {
			sb.append("\"" + project.strTable.get(cmid) + "\"");
			sb.append(",");
		}

		cbw.write(sb.toString());

		cbw.write("\"Number of bugs\"\n");
		
		for(Integer modFileLNID : modifiedFiles) {

			int nodeid = 0;
			nodeid = prev.findNodeId(modFileLNID);

			// ha megtalalhato az erintett elem a legelso elotti verzioban, egyebkent skip
			if(nodeid != 0){

				//int nodeid = prev.getLongnameNodeIdIndexFiles().get(modFileLNID);
				
				fm = prev.fileMetrics;
				
				sb = new StringBuilder();

				//Node node = prev.graph.findNode("L"+nodeid);

				//List<Attribute> attrs = node.findAttribute(aType.atString, Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
				//String nameAttr = ((AttributeString)attrs.get(0)).getValue();
							
				// id: sha+filename
				sb.append("\"" + prev.sha + "\",\"" + project.strTable.get(modFileLNID) + "\",\"" + Integer.toString(id) + "\",\"" + "L"+nodeid + "\"");
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
				for(Integer cmid : prev.commitMetrics.keySet()) {
					sb.append("\"" + prev.commitMetrics.get(cmid) + "\"");
					sb.append(",");
				}
				cbw.write(sb.toString());

				cbw.append("\"" + Integer.toString(prev.fileBugs.get(modFileLNID)) + "\"\n");
			}

			nodeid = last.findNodeId(modFileLNID);
			// ha megtalalhato az erintett elem az utolso verzioban, egyebkent skip
			if(nodeid != 0) {

				//int nodeid = last.getLongnameNodeIdIndexFiles().get(modFileLNID);
				
				fm = last.fileMetrics;
				
				sb = new StringBuilder();

				//Node node = last.graph.findNode("L"+nodeid);

				//List<Attribute> attrs = node.findAttribute(aType.atString, Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
				//String nameAttr = ((AttributeString)attrs.get(0)).getValue();
				
				// id: sha+filename
				sb.append("\"" + last.sha + "\",\"" + project.strTable.get(modFileLNID) + "\",\"" + Integer.toString(id) + "\",\"" + "L"+nodeid + "\"");
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
				for(Integer cmid : last.commitMetrics.keySet()) {
					sb.append("\"" + last.commitMetrics.get(cmid) + "\"");
					sb.append(",");
				}
				cbw.write(sb.toString());
				
				cbw.append("\"" + Integer.toString(last.fileBugs.get(modFileLNID)) + "\"\n");
			}
		}
		cbw.flush();
		cbw.close();
		
	
	}
}
