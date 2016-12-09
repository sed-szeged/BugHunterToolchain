package bughunter.datacollector;

import graphlib.Attribute;
import graphlib.AttributeComposite;
import graphlib.AttributeInt;
import graphlib.AttributeString;
import graphlib.Graph;
import graphlib.GraphlibException;
import graphlib.Node;
import graphlib.Attribute.aType;
import graphsupportlib.Metric;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import bughunter.datacollector.model.Commit;
import bughunter.datacollector.model.Dumper;
import bughunter.datacollector.model.File;
import bughunter.datacollector.model.FileChange;
import bughunter.datacollector.model.Issue;
import bughunter.datacollector.model.Project;
import bughunter.datacollector.model.Serializator;
import bughunter.datacollector.model.User;

public class DataCollector {

	private final static Logger LOGGER = Logger.getLogger(DataCollector.class.getName());
	
	private final static String BH_OPEN_ISSUES = "Number of open issues";

	private Project project;


	private Map<String,String> lims;
	
	private Set<String> traditionalCommits;

	private Set<String> missingLims;
	private Set<String> duplicatedLims;

	public DataCollector() {
		lims = new HashMap<String,String>();
		traditionalCommits = new HashSet<String>();
		missingLims = new HashSet<String>();
		duplicatedLims = new HashSet<String>();
	}

	/**
	 * betolti a github adatokbol allo xml-t
	 * @param xmlFileName az input xml
	 * @throws java.text.ParseException
	 */
	public void parseXML(String xmlFileName) throws java.text.ParseException {

		LOGGER.info("Parsing xml: " + xmlFileName);
		
		Document dom;
		Map<Integer, User> contributors = new HashMap<Integer, User>();
		Map<Integer, Issue> issues = new HashMap<Integer, Issue>();
		Map<String, Commit> commits = new HashMap<String, Commit>();

		StrTable strTable = new StrTable();

		try {
			project = new Project(strTable);

			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			DocumentBuilder db = dbf.newDocumentBuilder();
			dom = db.parse(xmlFileName);

			// get the root element
			Element rootElement = dom.getDocumentElement();
			String projectUser = rootElement.getAttribute("user");
			String projectRepo = rootElement.getAttribute("repo");

			// get contributors node
			NodeList contributorsNodes = rootElement.getElementsByTagName("Contributors");

			Element contributorsNode = (Element) contributorsNodes.item(0);

			NodeList userNodes = contributorsNode.getElementsByTagName("User");

			// get user node
			for (int i = 0; i < userNodes.getLength(); i++) {

				Element userNode = (Element) userNodes.item(i);
				String idStr = userNode.getAttribute("id");
				int id = Integer.parseInt(idStr);
				String contributions = userNode.getAttribute("contributions");
				int contint = Integer.parseInt(contributions);

				User user = new User(id, contint);
				contributors.put(id, user);

			}

			// get commits node
			NodeList commitsNodes = rootElement.getElementsByTagName("Commits");
			Element commitsNode = (Element) commitsNodes.item(0);

			// get commit node
			NodeList commitNodes = commitsNode.getElementsByTagName("Commit");

			for (int a = 0; a < commitNodes.getLength(); a++) {

				Element commitNode = (Element) commitNodes.item(a);
				String sha = commitNode.getAttribute("sha");
				String created_atStr = commitNode.getAttribute("date");
				Date created_at = dateformat.parse(created_atStr);
				String authorIdStr = commitNode.getAttribute("author");
				int authorId = -1;
				if (!authorIdStr.equals(""))
					authorId = Integer.parseInt(authorIdStr);

				// create the commit author user
				User author = contributors.get(authorId);

				// get parents node
				NodeList parentsNodes = commitNode.getElementsByTagName("Parents");
				Element parentsNode = (Element) parentsNodes.item(0);

				// get parent node
				NodeList parentNodes = parentsNode.getElementsByTagName("Parent");

				for (int j = 0; j < parentNodes.getLength(); j++) {

					Element parentNode = (Element) parentNodes.item(j);
					String parentSha = parentNode.getAttribute("sha");
					// TODO: add parent
				}

				// get files node
				NodeList filesNodes = commitNode.getElementsByTagName("Files");
				Element filesNode = (Element) filesNodes.item(0);

				// get file node FAJLKEZELES ->>>> DATA.XMLben MEGVAN A HELYES FAJL NEV, ITT NINCS FELDOLGOZVA..?
				// 57 sor addition, 0 deletion, 2 method added, 1 fajl listaban 1 fajl bejegyzes
				// Files/ File -> filename /patch
				NodeList fileNodes = filesNode.getElementsByTagName("File");
				List<File> fileList = new ArrayList<File>();
				List<FileChange> fileChangeList = new ArrayList<FileChange>();

				for (int k = 0; k < fileNodes.getLength(); k++) {

					Element fileNode = (Element) fileNodes.item(k);
					String filename = fileNode.getAttribute("filename");
					String status = fileNode.getAttribute("status");
					int additions = Integer.parseInt(fileNode.getAttribute("additions"));
					int deletions = Integer.parseInt(fileNode.getAttribute("deletions"));
					int changes = Integer.parseInt(fileNode.getAttribute("changes"));

					//filename = filename.replace("/", "\\");
					
					// create files
					File f = new File(strTable, filename);
					fileList.add(f);

					// get patch node
					String patch = fileNode.getElementsByTagName("Patch").item(0).getTextContent();
					patch = patch.replaceAll(" @@([^\n]*)", " @@");

					// create filechanges
					FileChange fg = new FileChange();
					fg.setAdditions(additions);
					fg.setChanges(changes);
					fg.setDeletions(deletions);
					fg.setDiff(patch);
					fg.setModifiedFile(f);

					switch (status.toLowerCase()) {
					case "modified":
					case "renamed":
					case "changed":
						fg.setStatus(FileChange.Status.Modified);
						break;
					case "removed":
						fg.setStatus(FileChange.Status.Deleted);
						break;
					case "added":
						fg.setStatus(FileChange.Status.Added);
						break;
					default:
						System.out.println(status);
						System.exit(1);
					}

					fileChangeList.add(fg);
				}

				// create commits
				Commit commit = new Commit(sha, created_at, fileChangeList, fileList, false, author);
				commit.setProject(project);
				commit.setStrTable(strTable);
				commits.put(sha, commit);
			}

			// get Closedissues node

			NodeList closedIssuesNodes = rootElement.getElementsByTagName("ClosedIssues");

			Element closedIssuesNode = (Element) closedIssuesNodes.item(0);
			String closedIssuesCountStr = ((Element) closedIssuesNode).getAttribute("count");
			int closedIssuesCount = Integer.parseInt(closedIssuesCountStr);
			// TODO: unused

			// get issues node
			NodeList closedIssueNodes = closedIssuesNode.getElementsByTagName("Issue");

			for (int i = 0; i < closedIssueNodes.getLength(); i++) {

				Map<String, Commit> issueCommits = new HashMap<String, Commit>();
				
				Element issueNode = (Element) closedIssueNodes.item(i);
				String numberStr = issueNode.getAttribute("number");
				int number = Integer.parseInt(numberStr);
				String created_atStr = issueNode.getAttribute("created_at");
				Date created_at = dateformat.parse(created_atStr);
				String closed_atStr = issueNode.getAttribute("closed_at");
				Date closed_at = dateformat.parse(closed_atStr);


				NodeList issueCommitNodes = issueNode.getElementsByTagName("Commit");

				for (int j = 0; j < issueCommitNodes.getLength(); j++) {

					Element commitNode = (Element) issueCommitNodes.item(j);
					String sha = commitNode.getAttribute("sha");
					Commit ic = commits.get(sha);
					ic.setFix(true);
					issueCommits.put(sha, ic);

				}

				// create issues
				Issue issue = new Issue(number, created_at, closed_at, issueCommits);
				issue.setProject(project);
				issues.put(number, issue);

			}

			// get Openissues
			NodeList openIssuesNodes = rootElement.getElementsByTagName("OpenIssues");
			Element openIssuesNode = (Element) openIssuesNodes.item(0);
			String openIssuesCountStr = ((Element) openIssuesNode).getAttribute("count");
			int openIssuesCount = Integer.parseInt(openIssuesCountStr);
			// TODO: unused

			// get issues
			NodeList openIssueNodes = ((Element) openIssuesNode).getElementsByTagName("Issue");

			for (int i = 0; i < openIssueNodes.getLength(); i++) {

				Element issueNode = (Element) openIssueNodes.item(i);
				String numberStr = issueNode.getAttribute("number");
				int number = Integer.parseInt(numberStr);
				String created_atStr = issueNode.getAttribute("created_at");
				Date created_at = dateformat.parse(created_atStr);

				Issue issue = new Issue();
				issue.setId(number);
				issue.setOpened(created_at);
				issues.put(number, issue);
			}

			project.setUser(projectUser);
			project.setRepo(projectRepo);
			project.setCommits(commits);
			project.setContributors(contributors);
			project.setIssues(issues);

		} catch (ParserConfigurationException e) {
			LOGGER.severe(e.getMessage());
		} catch (SAXException e) {
			LOGGER.severe(e.getMessage());
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}

	}

	/**
	 * betolti a projectet binarisbol
	 * nem hasznaljuk, mert tul nagy a modell merete, ha minden limet betoltunk
	 * @param file
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public void load(String file) throws ClassNotFoundException, IOException {
		LOGGER.info("Loading binary: " + file);
		project = Serializator.deserialize(file);
	}

	/**
	 * kimenti a projectet binarisba
	 * @param file
	 * @throws IOException
	 */
	public void save(String file) throws IOException {
		LOGGER.info("Saving binary: " + file);
		Serializator.serialize(project, file);
	}
	
	public void collectNeededCommtis() {
		project.collectNeededCommtis();
		project.getNeededCommits().addAll(traditionalCommits);
	}

	/**
	 * betolti a limeket issue-nkent
	 * kiszamitja a szukseges metrikakat, adatokat, melyeket csv-be dumpolunk
	 * felszabaditja a limeket (amennyiben kesobb meg kelleni fog egy issuehoz, akkor nem)
	 * 
	 *   - open issue-k szama egy commitnal
	 *   - before commitok
	 *   - commitok altal erintett forraskodelemek
	 *   - az elobbiek metrikai
	 *   - az ereintett forraskod elemekhez hozzarendeli a hibaszamot
	 */
	public void computeProcessMetrics() {
		LOGGER.info("Computing metrics");

		Set<Integer> neededFiles = new HashSet<Integer>();
		for (Issue issue : project.getIssues().values()) {
			if(issue.getPrev() == null && issue.getLast() == null)
				continue;
			neededFiles.addAll(issue.getModifiedFiles());
		}
		
		// open issuek darabszáma
		int openissuesid = project.getStrTable().set(BH_OPEN_ISSUES);
		for (String sha : project.getNeededCommits()) {
			Commit c = project.getCommits().get(sha);
			if(c == null) {
				LOGGER.info("commit == null : " + sha);
				continue;
			}
			int openi = 0;
			Date created = c.getCreated();

			for (Issue issue : project.getIssues().values()) {

				Date closed = issue.getClosed();
				Date opened = issue.getOpened();

				// ha a commit létrehozásának dátuma megelõzi a lezárást, akkor
				// az issue nyitott volt
				// létrehozási dátum és lezárási dátum közé kell esnie és akkor ++
				if (opened.before(created) &&  (closed == null || created.before(closed))) {
					openi++;
				}
			}
			
			c.getCommitMetrics().put(openissuesid, openi);
		}
		

		int metric_nofc = project.getStrTable().set("Number of previous fixes");
		int metric_nomu = project.getStrTable().set("Number of committers");
		int metric_noclm = project.getStrTable().set("Number of developer commits");
		int metric_noop = project.getStrTable().set("Number of previous modifications");
		
        for (int i=0; i < project.getSortedCommits().size(); i++) {       	
        	
        	for (FileChange fc : project.getSortedCommits().get(i).getChanges()) {

        		// kell ez a fájl?
        		//if(!neededFiles.contains(fc.getModifiedFile().getFilenameId())) {
        		//	continue;
        		//}
            	
        		int filenameid = fc.getModifiedFile().getFilenameId();
        	    for (int j=i; j < project.getSortedCommits().size(); j++) {
        	    	
        	    	if(!project.getNeededCommits().contains(project.getSortedCommits().get(j).getSha())) {
        	    		// ebben a verzióban nem kell a metrika
        	    		continue;
        	    	} else if(!traditionalCommits.contains(project.getSortedCommits().get(j).getSha()) && !neededFiles.contains(fc.getModifiedFile().getFilenameId())) {
        	    		// nem kell a hagyományoshoz, akkor csak a szükséges fájlokra kell számolni
        	    		continue;
        	    	}
        	    	
        	    	// - hagyományos adatbázishoz kell, akkor minden fájlra számolja
        	    	// - ha kell ebben a verzióban számolni, akkor csak a szükséges fájlokra

        	    	Map<Integer, Map<Integer,Float>> fm = project.getSortedCommits().get(j).getFileMetrics();
        	    	if(fm.containsKey(filenameid)) {
        	    		// van már róla metrika
        	    		// de nem bizos hogy a verzióban megtalálható a fájl
        	    		if(fm.get(filenameid).containsKey(metric_noop)) {
        	    			fm.get(filenameid).put(metric_noop, fm.get(filenameid).get(metric_noop)+1);
        	    		} else {
        	    			fm.get(filenameid).put(metric_noop, 1f);
        	    		}
        	    		
        	    		// fix-e?
        	    		if(project.getSortedCommits().get(i).isFix()) {
        	    			if(fm.get(filenameid).containsKey(metric_nofc)) {
            	    			fm.get(filenameid).put(metric_nofc, fm.get(filenameid).get(metric_nofc)+1);
            	    		} else {
            	    			fm.get(filenameid).put(metric_nofc, 1f);
            	    		}
        	    		}
        	    	} else {
        	    		// nincs róla metrika, hozzáadás
        	    		Map<Integer, Float> fmm = new HashMap<Integer, Float>();
        	    		fmm.put(metric_noop, 1f);
        	    		if(project.getSortedCommits().get(i).isFix()) {
        	    			fmm.put(metric_nofc, 1f);
        	    		}
        	    		fm.put(filenameid, fmm);
        	    	}

                	if(project.getSortedCommits().get(i).getAuthor() == null)
                		continue;
        	    	// utoljára módosító felhasználó
        	    	Map<Integer, Set<Integer>> fmu = project.getSortedCommits().get(j).getFileModUser();
        	    	Map<Integer, Integer> lmu = project.getSortedCommits().get(j).getLastModUser();
        	    	if(fmu.containsKey(filenameid)) {
        	    		fmu.get(filenameid).add(project.getSortedCommits().get(i).getAuthor().getId());
        	    	} else {
        	    		Set<Integer> mul = new HashSet<Integer>();
        	    		mul.add(project.getSortedCommits().get(i).getAuthor().getId());
        	    		fmu.put(filenameid, mul);
        	    	}
        	    	lmu.put(filenameid, project.getSortedCommits().get(i).getAuthor().getId());
        	    }
        	}
        }

		for (String sha : project.getNeededCommits()) {
			Commit commit = project.getCommits().get(sha);
			if(commit == null)
				continue;
        	Map<Integer, Map<Integer, Float>> fm = commit.getFileMetrics();
        	Map<Integer, Set<Integer>> fmu = commit.getFileModUser();
	    	Map<Integer, Integer> lmu = commit.getLastModUser();
        	for(Integer id : fmu.keySet()) {
        		fm.get(id).put(metric_nomu, (float)fmu.get(id).size());
        		fm.get(id).put(metric_noclm, (float)project.getContributors().get(lmu.get(id)).getCommits());
        	}
        	// commit szinten is meglegyenek a file metrikák
        	// redundáns lehet, mivel csak azokban kell, ahol direktben commit kiírás van
        	// pl: hagyományos
        	commit.getFileMetricNames().add(metric_nofc);
        	commit.getFileMetricNames().add(metric_nomu);
        	commit.getFileMetricNames().add(metric_noclm);
        	commit.getFileMetricNames().add(metric_noop);
        }
        for(Issue is : project.getIssues().values()) {

			if(is.getPrev() == null && is.getLast() == null)
				continue;

			is.getFileMetricsNames().add(metric_nofc);
			is.getFileMetricsNames().add(metric_nomu);
			is.getFileMetricsNames().add(metric_noclm);
			is.getFileMetricsNames().add(metric_noop);

		}
	}
	

	public void collectModifiedElements() {
		LOGGER.info("Collecting modified elements...");

		for(Issue is : project.getIssues().values()) {
			for(Commit c : is.getCommits().values()) {
				loadLim(c.getSha(), true, false);
				c.free();
			}

			is.collectModifiedElements();
		}
	}
	public void computeBugNumbers() {
		LOGGER.info("Collecting bug numbers...");
		
		for(Issue is : project.getIssues().values()) {

			if(is.getPrev() == null && is.getLast() == null)
				continue;
			
			is.collectBeforeCommits();
			is.collectBugNumbers();
		}
	}
	public void computeCommitBugStat() {
		for(String sha : traditionalCommits) {
			Commit c = project.getCommits().get(sha);
			loadLim(sha, false, false);
			
			c.computeBugsStat();
			
			c.free();
		}
	}
	public void writeDatabase(String csvdir) {
		LOGGER.info("Processing issues...");
		
		Set<String> nolicense = new HashSet<String>();
		
		for(Issue is : project.getIssues().values()) {

			if(is.getPrev() == null && is.getLast() == null) 
				continue;

			loadLim(is.getPrev().getSha(), false, false);
			loadLim(is.getLast().getSha(), false, false);
			
			if(!is.getPrev().hasLicense()) {
				nolicense.add(is.getPrev().getSha());
			}
			if(!is.getLast().hasLicense()) {
				nolicense.add(is.getLast().getSha());
			}
			
			//is.collectMetricNames();
			is.saveDataToCsv(csvdir);

			is.getLast().free();
			is.getPrev().free();
		}

		dumpShas(nolicense, this.nolicense);
		dumpShas(missingLims, this.missinglims);
		dumpShas(duplicatedLims, this.duplicatedlims);
		
	}
	public void writeTraditional(String dir) {
		LOGGER.info("Writing traditional database...");

		Set<String> nolicense = new HashSet<String>();
		
		for(String sha : traditionalCommits) {
			Commit c = project.getCommits().get(sha);
			if(c == null) {
				System.out.println("Traditional not found: " + sha);
				continue;
			}
			loadLim(sha, false, false);

			if(!c.hasLicense()) {
				nolicense.add(c.getSha());
			}
			
			c.saveCommitData(dir);
			c.free();
		}

		dumpShas(nolicense, this.nolicense+"-trad");
		dumpShas(missingLims, this.missinglims+"-trad");
	}

	String nolicense;
	public void setNoLicense(String file) {
		nolicense = file;
	}
	String missinglims;
	public void setMissingLims(String file) {
		missinglims = file;
	}
	String traditionalTimestamp;
	public void setTraditionalTimestamp(String file) {
		traditionalTimestamp = file;
	}
	String duplicatedlims;
	public void setDuplicatedLims(String file) {
		duplicatedlims = file;
	}
	String releasestat;
	public void setReleasestat(String file) {
		releasestat = file;
	}
	private void dumpShas(Set<String> ss, String file) {

		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(file)));

			for(String s : ss) {
				bw.write(s);
				bw.write("\n");
			}
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}
	

	public void dumpData(String dumpCsvdir) throws IOException {
		LOGGER.info("Processing commits...");



		BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(dumpCsvdir + "\\" + project.getRepo() + ".csv")) );
		
		StringBuilder sb = new StringBuilder();

		// Header
		sb.append("\"bug_id\",\"bug_url\",\"bug_report_timestamp\",\"fix_hash\",\"fix_timestamp\", \"method_name\",\"method_qualified_name\",\"file\",\"line\"\n");

		bw.write(sb.toString());
		
		Calendar ca = Calendar.getInstance();
		long time;
		for(Issue is : project.getIssues().values()) {


			if(is.getPrev() == null && is.getLast() == null) {
				System.out.println("Skipping issue: " + is.getId());
				continue;
			}
			
			

		    for(Commit c : is.getCommits().values()) {
		    	loadLimMMID(c.getSha());
	
			    String pre = "\"" + is.getId() + "\",\"" + "https://github.com/" + project.getUser() + "/" + project.getRepo() + "/issues/" + is.getId() + "\",\"";
			    ca.setTime(is.getOpened());
			    time = ca.getTimeInMillis();
			    pre += time + "\",\"" + c.getSha() + "\",\"";
			    ca.setTime(c.getCreated());
			    time = ca.getTimeInMillis();
			    pre += time + "\",\"" ;
	    		
			    for(Integer id : c.getModifiedMethodsMMID()) {
			    	
		    		Node node = c.getGraph().findNode("L" + id);

		    		List<Attribute> attrs = node.findAttribute(aType.atComposite, Metric.ATTR_POSITION, Metric.CONTEXT_ATTRIBUTE);
					AttributeComposite pos = (AttributeComposite) attrs.get(0);
					
					attrs = pos.findAttribute(aType.atString, Metric.ATTR_PATH, "");
					String path = ((AttributeString)attrs.get(0)).getValue();

					attrs = pos.findAttribute(aType.atInt, Metric.ATTR_LINE, "");
					int beginLine = ((AttributeInt)attrs.get(0)).getValue();

					attrs = node.findAttribute(aType.atString, Metric.ATTR_NAME, Metric.CONTEXT_ATTRIBUTE);
					String name = ((AttributeString)attrs.get(0)).getValue();

					attrs = node.findAttribute(aType.atString, Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
					String nameAttr = ((AttributeString)attrs.get(0)).getValue();
					
					sb = new StringBuilder();
					
					sb.append(pre);
					sb.append(name);
					sb.append("\",\"");
					sb.append(nameAttr);
					sb.append("\",\"");
					sb.append(path);
					sb.append("\",\"");
					sb.append(beginLine);
					sb.append("\"\n");
					
					bw.write(sb.toString());
						
			    		
			    }
	
				c.fullFree();
			}
			
			
			
			/*
			Set<Integer> modifiedMethodsMMID = new HashSet<Integer>();
		    for(Commit c : is.getCommits().values()) {
		    	loadLimMMID(c.getSha());
		    	modifiedMethodsMMID.addAll(c.getModifiedMethodsMMID());
				c.fullFree();
			}

		    String pre = "\"" + is.getId() + "\",\"" + "https://github.com/" + project.getUser() + "/" + project.getRepo() + "/issues/" + is.getId() + "\",\"";
		    ca.setTime(is.getOpened());
		    time = ca.getTimeInMillis();
		    pre += time + "\",\"" + is.getLast().getSha() + "\",\"";
		    ca.setTime(is.getLast().getCreated());
		    time = ca.getTimeInMillis();
		    pre += time + "\",\"" ;
		    loadLim(is.getLast().getSha(), false, true);
    		Graph g = is.getLast().getGraph();
		    for(Integer id : modifiedMethodsMMID) {
		    	if(is.getLast().getLongnameNodeIdIndexMethods().containsKey(id)) {
		    		int uid = is.getLast().getLongnameNodeIdIndexMethods().get(id);
		    		Node node = g.findNode("L" + uid);

		    		List<Attribute> attrs = node.findAttribute(aType.atComposite, Metric.ATTR_POSITION, Metric.CONTEXT_ATTRIBUTE);
					AttributeComposite pos = (AttributeComposite) attrs.get(0);
					
					attrs = pos.findAttribute(aType.atString, Metric.ATTR_PATH, "");
					String path = ((AttributeString)attrs.get(0)).getValue();

					attrs = pos.findAttribute(aType.atInt, Metric.ATTR_LINE, "");
					int beginLine = ((AttributeInt)attrs.get(0)).getValue();

					attrs = node.findAttribute(aType.atString, Metric.ATTR_NAME, Metric.CONTEXT_ATTRIBUTE);
					String name = ((AttributeString)attrs.get(0)).getValue();

					attrs = node.findAttribute(aType.atString, Metric.ATTR_LONGNAME, Metric.CONTEXT_ATTRIBUTE);
					String nameAttr = ((AttributeString)attrs.get(0)).getValue();
					
					sb = new StringBuilder();
					
					sb.append(pre);
					sb.append(name);
					sb.append("\",\"");
					sb.append(nameAttr);
					sb.append("\",\"");
					sb.append(path);
					sb.append("\",\"");
					sb.append(beginLine);
					sb.append("\"\n");
					
					bw.write(sb.toString());
					
		    		
		    	}
		    }
		    is.getLast().fullFree();
*/
		}
		

		bw.flush();
		bw.close();
		
	}
	

	public void loadTestFilters(String f) {
		LOGGER.info("Loading test filters...");
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(new java.io.File(f)));
			String line;
			while ((line = br.readLine()) != null) {
				project.getTestFilters().add(line);
			}
			br.close();
		} catch (NumberFormatException e) {
			LOGGER.severe(e.getMessage());
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}

	public void loadPrevlast(String prevlast) {
		LOGGER.info("Loading prev last...");
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(new java.io.File(prevlast)));
			String line;
			while ((line = br.readLine()) != null) {
				String[] row = line.split(","); 

				project.getIssues().get(Integer.parseInt(row[0])).setPrevAndLastCommit(row[1], row[2]); 
			}
			br.close();
		} catch (NumberFormatException e) {
			LOGGER.severe(e.getMessage());
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}

	/**
	 * betolti a limek eleresi utjat
	 * @param lims
	 */
	public void setLims(List<String> lims) {
		LOGGER.info("Set lims...");
		for (String lim : lims) {
			String sha;
			sha = (String) lim.subSequence(0, lim.lastIndexOf("\\"));
			if (lim.contains("\\")) {
				sha = (String) sha.subSequence(sha.lastIndexOf("\\") + 1, sha.length());
			} else {
				//sha = (String) lim.subSequence(0,lim.lastIndexOf("."));
			}
			//sha = (String) sha.subSequence(0, sha.lastIndexOf("."));
			//LOGGER.info("Setting lim: " + sha + " file: " + lim);
			lim = lim.trim();
			if(this.lims.containsKey(sha)) {
				duplicatedLims.add(this.lims.get(sha));
				duplicatedLims.add(lim);
				//LOGGER.info("Double SM result for " + sha + " :\n" + this.lims.get(sha) + "\n" + lim);
			}
			this.lims.put(sha, lim);
		}
	}
	
	
	/** 
	 * betolti a limet a megfelelo commithoz, ha meg nincs betoltve
	 * osszegyujti a modositott forraskod elemeket
	 * @param sha
	 */
	public void loadLim(String sha, boolean connectModifiedElements, boolean buildLongNameNodeIdMap) {

		/*if(sha == null) {
			LOGGER.severe("sha = null");
			return;
		}*/

		String limFile = this.lims.get(sha);
		
		if(limFile == null) {
			missingLims.add(sha);
			LOGGER.severe("Missing lim for: " + sha);
			return;
		}
		
		Commit commit = project.getCommits().get(sha);
		if(commit == null) {
			LOGGER.severe("Commit not found: " + sha);
		} else if(commit.getRoot() != null) {
			LOGGER.info("Lim is already loaded for commit: " + sha);
		} else {
			//LimHandler handler = new LimHandler(project.getStrTable(), commit, changepathFrom, changepathTo, !connectModifiedElements);

			try{
				
				//if((connectModifiedElements && !commit.isConnected()) ||
				//		buildLongNameNodeIdMap) {

					Graph graph = new Graph();
					graph.loadBinary(limFile);
					
					commit.setGraph(graph);
					if(buildLongNameNodeIdMap) {
						commit.buildLongNameNodeIdMap();
					}
					if(connectModifiedElements) {
						commit.connectDiff();
					}
				//}
	
/*
			} catch (IOException e) {
				e.printStackTrace();
				LOGGER.severe(e.getMessage());
			} catch (SAXException e) {
				LOGGER.severe(e.getMessage());*/
			} catch (GraphlibException e) {
				e.printStackTrace();
				LOGGER.severe("Error: cannot load binary graph: " + limFile + " " + e.getMessage());
			}
		}
	}

	public void loadLimMMID(String sha) {


		String limFile = this.lims.get(sha);
		
		if(limFile == null) {
			//LOGGER.severe("Missing lim for: " + sha);
			return;
		}
		
		Commit commit = project.getCommits().get(sha);
		if(commit == null) {
			LOGGER.severe("Commit not found: " + sha);
		} else {
			
			try{

				Graph graph = new Graph();
				graph.loadBinary(limFile);
				
				commit.setGraph(graph);
				commit.connectDiffMMID();
	
			} catch (GraphlibException e) {
				e.printStackTrace();
				LOGGER.severe("Error: cannot load binary graph: " + limFile + " " + e.getMessage());
			}
		}
	}
	
	/*public void loadLim(String sha, boolean connectModifiedElements) {


		String limFile = this.lims.get(sha);
		
		if(limFile == null) {
			//LOGGER.severe("Missing lim for: " + sha);
			return;
		}
		
		Commit commit = project.getCommits().get(sha);
		if(commit == null) {
			LOGGER.severe("Commit not found: " + sha);
		} else if(commit.getRoot() != null) {
			LOGGER.info("Lim is already loaded for commit: " + sha);
		} else {
			LimHandler handler = new LimHandler(project.getStrTable(), commit, changepathFrom, changepathTo, !connectModifiedElements);

			
			//LOGGER.info("Loading lim: " + sha);

			String tempzip = ".\\tmp\\" + sha + ".xml.gz";
			String tempfile = ".\\tmp\\" + sha + ".xml";

			Process process;
			try {
				//LOGGER.info("copy "+limFile + " "+tempzip);
				Files.copy(Paths.get(limFile), Paths.get(tempzip));

				//LOGGER.info("gunzip  "+tempzip);
				process = new ProcessBuilder("gunzip", tempzip).start();
				InputStream is = process.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line;
	
				while ((line = br.readLine()) != null) {
					LOGGER.info(line);
				}
				
				process.waitFor();
				
				SAXParser parser = new SAXParser();
				parser.setContentHandler(handler);
				parser.parse(tempfile);
				
				if(connectModifiedElements) {
					commit.connectDiff();
					commit.collectModifiedElements();
				}
	

				//LOGGER.info("delete "+tempfile);
				Files.delete(Paths.get(tempfile));

			} catch (IOException e) {
				e.printStackTrace();
				LOGGER.severe(e.getMessage());
			} catch (InterruptedException e) {
				LOGGER.severe(e.getMessage());
			} catch (SAXException e) {
				LOGGER.severe(e.getMessage());
			}
		}
	}*/

	public void saveSourceElementDiffStat(String out) {
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(out)));
			bw.write("sha,deltas,S1c,S1m,S2c,S2m,S3c,S3m\n");
			for(Commit c : project.getCommits().values()) {
				if(c.isConnected()) {
					StringBuilder sb = new StringBuilder();
					sb.append(c.getSha());
					sb.append(",");
					sb.append(c.getDeltas());
					sb.append(",");
					sb.append(c.getS1c());
					sb.append(",");
					sb.append(c.getS1m());
					sb.append(",");
					sb.append(c.getS2c());
					sb.append(",");
					sb.append(c.getS2m());
					sb.append(",");
					sb.append(c.getS3c());
					sb.append(",");
					sb.append(c.getS3m());
					sb.append("\n");
					bw.write(sb.toString());
				}
			}			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}
	
	public void dump(String dumpfile) {
		LOGGER.info("Dumping project: " + dumpfile);
		Dumper.dump(project, dumpfile);
	}
	
	public void loadFirstCommits(String firstCommits) {
		LOGGER.info("Load first commits...");
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(new java.io.File(firstCommits)));
			String line;
			while ((line = br.readLine()) != null) {
				String[] row = line.split(",");
				project.getIssues().get(Integer.parseInt(row[0])).setFirstCommit(row[1]);
			}
			br.close();
		} catch (NumberFormatException e) {
			LOGGER.severe(e.getMessage());
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}

	public void loadTraditionalCommits(String commits) {
		LOGGER.info("Load traditional commits...");
		Set<String> origTraditionalCommits = new HashSet<String>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(new java.io.File(commits)));
			String line;
			while ((line = br.readLine()) != null) {
				origTraditionalCommits.add(line);
			}
			br.close();
		} catch (NumberFormatException e) {
			LOGGER.severe(e.getMessage());
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
		
		approxTraditional(origTraditionalCommits);
	}
	
	private void approxTraditional(Set<String> origTraditionalCommits) {

		try {
			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(traditionalTimestamp)));
			bw.write("traditional,traditionalTimestamp,approx,approxTimestamp\n");

			GitHubClient client = new GitHubClient();
			client.setCredentials("gypeti", "1nb8szs1");
	
			RepositoryService repoService = new RepositoryService(client);
			CommitService commitService = new CommitService(client);
		
			Repository projectRepository = repoService.getRepository(project.getUser(), project.getRepo());
			for(String sha : origTraditionalCommits) {
				Date d = null;
				RepositoryCommit origc = commitService.getCommit(projectRepository, sha);
				d = origc.getCommit().getCommitter().getDate();
				
				Commit prev = null;
				for(Commit c : project.getSortedCommits()) {
					if(c.getSha().equals(sha)) {
						// ha megtalálható a master branchben, akkor ezt használjuk továbbra is
						prev = c;
						break;
					}
					if(c.getCreated().after(d)) {
						// ez a commit már a release utáni, az elõzõ commit lesz a jó
						break;
					}
					prev = c;
				}
				if(prev == null)
					LOGGER.info("No prev commit found for release: " + sha);
				else {
					//LOGGER.info("Traditional approx: " + sha + " " + prev.getSha());
					traditionalCommits.add(prev.getSha());
					prev.setRelease(true);
					bw.write(sha + "," + dateformat.format(d) + "," + prev.getSha() + "," + dateformat.format(prev.getCreated()) + "\n");
				}
			}
			

			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		determinePrevReleaseForIssues();
	}
	
	private void determinePrevReleaseForIssues() {
		List<Commit> sortedTraditionalApprox = new ArrayList<Commit>();
		for(String sha : traditionalCommits) {
			sortedTraditionalApprox.add(project.getCommits().get(sha));
		}
		
		Collections.sort(sortedTraditionalApprox, new Comparator<Commit>() {
			public int compare(Commit o1, Commit o2) {
				if (o1.getCreated().after(o2.getCreated())) return 1;
				else if (o1.getCreated().before(o2.getCreated())) return -1;
				else return 0;
			}
		});
		
		for(Issue is : project.getIssues().values()) {
			Commit prev = null;
			for(Commit c : sortedTraditionalApprox) {
				if(c.getCreated().after(is.getOpened())) {
					break;
				}
				prev = c;
			}
			if(prev == null) {
				//LOGGER.severe("Issue does not have previous release: " + is.getId());
			}
			is.setPrevRelease(prev);
			// TODO: töröl
			//System.out.println(is.getId() + " - " + prev);
		}
		
		//writeReleaseStat();
		
	}
	
	public void writeReleaseStat() {

		try {
			
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(releasestat)));
			bw.write("sha,intersect,prev,buggy_files,buggy_classes,buggy_methods\n");
			for(String sha : traditionalCommits) {
				Commit c = project.getCommits().get(sha);
				int count = 0;
				int prevcount = 0;
				for(Issue is : project.getIssues().values()) {
					if(is.getOpened().before(c.getCreated()) && (is.getClosed()== null || is.getClosed().after(c.getCreated()))) {
						count++;
					}
					if(is.getPrevRelease() == c) {
						prevcount++;
					}
				}
				bw.write(sha + "," + count + "," + prevcount + "," +
						c.getBuggy_files() + "," + c.getBuggy_classes() + "," + c.getBuggy_methods()  + "\n");
				
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void dumpBeforeCommits(String dumpbefore) {
		LOGGER.info("Dump before commits...");
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(dumpbefore)));
			
			for(Issue is : project.getIssues().values()) {
				bw.write(Integer.toString(is.getId()));
				bw.write(",");
				Iterator<Commit> it = is.getBeforeCommits().iterator();
				while(it.hasNext()) {
					Commit c = it.next();
					bw.write(c.getSha());
					if(it.hasNext())
						bw.write(",");
				}
				bw.write("\n");
			}
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}

	
	public void dumpCommits(String dumpfile) {
		LOGGER.info("Dumping commits...");
		try {
			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(dumpfile)));
			
			for(Commit c : project.getCommits().values()) {
				bw.write(c.getSha());
				bw.write(",");
				bw.write(dateformat.format(c.getCreated()));
				bw.write("\n");
			}
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}
	public void dumpIssueCommits(String dumpfile) {
		LOGGER.info("Dumping issue commits...");
		try {
			BufferedWriter bw = new BufferedWriter( new FileWriter(new java.io.File(dumpfile)));
			Set<String> commits = new HashSet<String>();
			for(Issue is : project.getIssues().values()) {
				commits.addAll(is.getCommits().keySet());
			}

			for(String c : commits) {
				bw.write(c);
				bw.write("\n");
			}
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}

}
