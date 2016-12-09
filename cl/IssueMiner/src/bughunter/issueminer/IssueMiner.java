package bughunter.issueminer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class IssueMiner {

	private final static Logger LOGGER = Logger.getLogger(IssueMiner.class
			.getName());

	private Document dom;
	
	String workDir;

	public IssueMiner(String xmlFileName, String workDir) {
		this.workDir = workDir;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			dom = db.parse(xmlFileName);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean checkCommitExist(String sha){

		ProcessBuilder pb = new ProcessBuilder("CheckCommit.bat", workDir, sha);
		pb.redirectErrorStream(true);
		Process process;
		try {
			process = pb.start();
			BufferedReader inStreamReader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));

			String line = inStreamReader.readLine();
			if ("commit".equals(line)) {
				return true;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private Map<String, String> collectLastCommits() throws ParseException {

		Map<String, String> lastCommits = new HashMap<String, String>();

		Element rootelement = dom.getDocumentElement();

		SimpleDateFormat dateformat = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss'Z'");

		NodeList closedissues = rootelement
				.getElementsByTagName("ClosedIssues");
		Element ci = (Element) closedissues.item(0);
		NodeList closedissuess = ci.getElementsByTagName("Issue");
		for (int i = 0; i < closedissuess.getLength(); ++i) {
			Element issue = (Element) closedissuess.item(i);

			String number = issue.getAttribute("number");

			NodeList commits = issue.getElementsByTagName("Commit");

			Map<Date, String> issueCommits = new HashMap<Date, String>();
			/*
			 * String shaR = ""; String shaC = ""; String string = "1990-01-01";
			 * Date dateR = new SimpleDateFormat("yyyy-MM-dd").parse(string);
			 * Date dateC = new SimpleDateFormat("yyyy-MM-dd").parse(string);
			 */
			for (int j = 0; j < commits.getLength(); ++j) {
				Element commit = (Element) commits.item(j);

				Date created = dateformat.parse(commit
						.getAttribute("created_at"));
				/*
				 * String event = commit.getAttribute("event");
				 * 
				 * if (event.equals("referenced") && dateR.compareTo(created) <
				 * 0) { dateR = created; shaR = commit.getAttribute("sha"); }
				 * else if (event.equals("closed") && dateC.compareTo(created) <
				 * 0) { dateC = created; shaC = commit.getAttribute("sha"); }
				 */
				issueCommits.put(created, commit.getAttribute("sha"));

			}
			String lcommit = "";
			
			NavigableMap<Date, String> map = new TreeMap<Date, String>(issueCommits);
			for (Date key : map.descendingKeySet()) {
				if(checkCommitExist(map.get(key))){
					lcommit = map.get(key);
					break;
				}else{
					System.out.println("Commit not exist: " + map.get(key));
				}
			}
			if(!lcommit.equals("")){
				lastCommits.put(number, lcommit);
			}else{
				lastCommits.put(number, "-");
			}
			/*
			 * if (!shaC.equals("")) { lastCommits.put(number, shaC); } else if
			 * (!shaR.equals("")) { lastCommits.put(number, shaR); }
			 */
		}
		return lastCommits;
	}

	private Map<String, String> collectFirstCommits() throws ParseException {

		Map<String, String> firstCommits = new HashMap<String, String>();

		Element rootelement = dom.getDocumentElement();

		SimpleDateFormat dateformat = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss'Z'");

		NodeList closedissues = rootelement
				.getElementsByTagName("ClosedIssues");
		Element ci = (Element) closedissues.item(0);
		NodeList closedissuess = ci.getElementsByTagName("Issue");
		for (int i = 0; i < closedissuess.getLength(); ++i) {
			Element issue = (Element) closedissuess.item(i);

			String number = issue.getAttribute("number");

			NodeList commits = issue.getElementsByTagName("Commit");

			String sha = "";
			String string = "2020-01-01";
			Date date = new SimpleDateFormat("yyyy-MM-dd").parse(string);

			for (int j = 0; j < commits.getLength(); ++j) {
				Element commit = (Element) commits.item(j);

				Date created = dateformat.parse(commit
						.getAttribute("created_at"));

				if (date.compareTo(created) > 0) {
					date = created;
					sha = commit.getAttribute("sha");
				}

			}
			if (!sha.isEmpty()) {
				firstCommits.put(number, sha);
			}
		}

		return firstCommits;
	}

	private Map<String, String> collectBeforeCommits(
			Map<String, String> firstCommits) throws ParseException {

		Element rootelement = dom.getDocumentElement();

		NodeList commitsNodes = rootelement.getElementsByTagName("Commits");
		Element commitsNode = (Element) commitsNodes.item(0);
		NodeList commitNodes = commitsNode.getElementsByTagName("Commit");

		Map<String, String> beforeCommits = new HashMap<String, String>();

		for (int i = 0; i < commitNodes.getLength(); ++i) {
			Element commit = (Element) commitNodes.item(i);
			String sha = commit.getAttribute("sha");
			NodeList parentsNodes = commit.getElementsByTagName("Parents");
			Element parentsNode = (Element) parentsNodes.item(0);
			NodeList parentNodes = parentsNode.getElementsByTagName("Parent");
			if (parentNodes.getLength() > 0) {
				Element parent = (Element) parentNodes.item(0);
				beforeCommits.put(sha, parent.getAttribute("sha"));
			}
		}

		Map<String, String> beforeFirstCommits = new HashMap<String, String>();

		for (String n : firstCommits.keySet()) {
			String firstSha = firstCommits.get(n);
			String beforeFirstSha = beforeCommits.get(firstSha);
			beforeFirstCommits.put(n, beforeFirstSha);
		}
		return beforeFirstCommits;
	}

	private Set<String> collectReferencedCommits() throws ParseException {

		Set<String> referencedCommits = new HashSet<String>();

		Element rootelement = dom.getDocumentElement();

		NodeList closedissues = rootelement
				.getElementsByTagName("ClosedIssues");
		Element ci = (Element) closedissues.item(0);
		NodeList closedissuess = ci.getElementsByTagName("Issue");
		for (int i = 0; i < closedissuess.getLength(); ++i) {
			Element issue = (Element) closedissuess.item(i);

			NodeList commits = issue.getElementsByTagName("Commit");

			for (int j = 0; j < commits.getLength(); ++j) {
				Element commit = (Element) commits.item(j);
				if(checkCommitExist(commit.getAttribute("sha"))){
					referencedCommits.add(commit.getAttribute("sha"));
				}else{
					System.out.println("Commit not exist: " + commit.getAttribute("sha"));
				}

			}

		}
		return referencedCommits;
	}
	

	public void produceLastCommits(String outFile) {

		try {
			Map<String, String> lastCommits = collectLastCommits();

			PrintWriter pw = new PrintWriter(outFile);
			for (String n : lastCommits.keySet()) {
				pw.println(n + "," + lastCommits.get(n));
			}
			pw.close();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}

	public void produceFirstCommits(String outFile) {

		try {
			Map<String, String> firstCommits = collectFirstCommits();

			PrintWriter pw = new PrintWriter(outFile);
			for (String n : firstCommits.keySet()) {
				pw.println(n + "," + firstCommits.get(n));
			}
			pw.close();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}

	public void produceBeforeAndLastCommits(String outFile) {

		try {

			Map<String, String> firstCommits = collectFirstCommits();

			Map<String, String> beforeFirstCommits = collectBeforeCommits(firstCommits);

			Map<String, String> lastCommits = collectLastCommits();

			PrintWriter pw = new PrintWriter(outFile);
			for (String n : beforeFirstCommits.keySet()) {
				if (lastCommits.get(n) == null) {
					LOGGER.warning("Issue " + n + "does not have last commit!");
				} else {
					pw.println(n + "," + beforeFirstCommits.get(n) + ","
							+ lastCommits.get(n));
				}
			}
			pw.close();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void produceAllCommits(String outFile) {

		try {

			Map<String, String> firstCommits = collectFirstCommits();

			Map<String, String> beforeFirstCommits = collectBeforeCommits(firstCommits);

			Set<String> referencedCommits = collectReferencedCommits();
			
			Set<String> allCommits = new HashSet<String>();
			allCommits.addAll(referencedCommits);
			allCommits.addAll(beforeFirstCommits.values());

			PrintWriter pw = new PrintWriter(outFile);
			for (String commitSha : allCommits) {
				pw.println(commitSha);
			}
			pw.close();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void produceBeforeCommits(String outFile) {

		try {

			Map<String, String> firstCommits = collectFirstCommits();
			Map<String, String> beforeFirstCommits = collectBeforeCommits(firstCommits);
			PrintWriter pw = new PrintWriter(outFile);
			for (String n : beforeFirstCommits.keySet()) {
				pw.println(n + "," + beforeFirstCommits.get(n));
			}
			pw.close();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void produceReferencedCommits(String outFile) {

		try {

			Set<String> referencedCommits = collectReferencedCommits();

			PrintWriter pw = new PrintWriter(outFile);
			for (String commitSha : referencedCommits) {
				pw.println(commitSha);
			}
			pw.close();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void produceStatistics(String outFile) {
		try {

			// STATS
			int NO_AllCommits = 0;
			int NO_Commits = 0;
			int NO_ClosingCommits = 0;
			int NO_Issues = 0;
			int NO_OpenIssues = 0;
			int NO_ClosedIssues = 0;
			int NO_IssuesWithClosingCommit = 0;
			int NO_IssuesWithoutClosingCommit = 0;
			float AVG_NO_CommitsPerIssue = 0;
			int MIN_NO_CommitsPerIssue = Integer.MAX_VALUE;
			int MAX_NO_CommitsPerIssue = 0;
			Map<Integer, Integer> NO_CommitPerIssue = new HashMap<Integer, Integer>();

			Map<Integer, Integer> NO_ParentPerCommit = new HashMap<Integer, Integer>();

			int NO_NotOneParentPerFirstCommit = 0;
			int NO_OneParentPerFirstCommit = 0;

			// int commit_count = 0;

			for (int i = 0; i <= 40; ++i) {
				NO_CommitPerIssue.put(new Integer(i), new Integer(0));
			}

			for (int i = 1; i <= 20; ++i) {
				NO_ParentPerCommit.put(new Integer(i), new Integer(0));
			}

			Element rootelement = dom.getDocumentElement();

			NodeList commitsNodes = rootelement.getElementsByTagName("Commits");
			Element commitsNode = (Element) commitsNodes.item(0);
			NodeList commitNodes = commitsNode.getElementsByTagName("Commit");

			Map<String, Integer> NO_ParentByCommit = new HashMap<String, Integer>();

			Set<String> referencedCommits = new HashSet<String>();

			for (int i = 0; i < commitNodes.getLength(); ++i) {
				Element commit = (Element) commitNodes.item(i);
				NodeList parentsNodes = commit.getElementsByTagName("Parents");
				Element parentsNode = (Element) parentsNodes.item(0);
				NodeList parentNodes = parentsNode
						.getElementsByTagName("Parent");

				Integer key = new Integer(parentNodes.getLength());

				if (NO_ParentPerCommit.containsKey(key)) {
					Integer count = NO_ParentPerCommit.get(key);
					++count;
					NO_ParentPerCommit.put(key, count);
				} else {
					Integer count = 1;
					NO_ParentPerCommit.put(key, count);
				}
				NO_ParentByCommit.put(commit.getAttribute("sha"), key);
			}

			NodeList closedIssuesNodes = rootelement
					.getElementsByTagName("ClosedIssues");
			Element closedIssuesNode = (Element) closedIssuesNodes.item(0);
			NodeList closedIssueNodes = closedIssuesNode
					.getElementsByTagName("Issue");
			for (int i = 0; i < closedIssueNodes.getLength(); ++i) {
				Element issue = (Element) closedIssueNodes.item(i);
				NodeList IssueCommitNodes = issue
						.getElementsByTagName("Commit");

				// commit_count += IssueCommitNodes.getLength();

				if (MIN_NO_CommitsPerIssue > IssueCommitNodes.getLength()) {
					MIN_NO_CommitsPerIssue = IssueCommitNodes.getLength();
				}
				if (MAX_NO_CommitsPerIssue < IssueCommitNodes.getLength()) {
					MAX_NO_CommitsPerIssue = IssueCommitNodes.getLength();
				}
				Integer key = new Integer(IssueCommitNodes.getLength());

				if (NO_CommitPerIssue.containsKey(key)) {
					Integer count = NO_CommitPerIssue.get(key);
					++count;
					NO_CommitPerIssue.put(key, count);
				} else {
					Integer count = 1;
					NO_CommitPerIssue.put(key, count);
				}

				boolean withClosingCommit = false;

				for (int j = 0; j < IssueCommitNodes.getLength(); ++j) {
					Element commitNode = (Element) IssueCommitNodes.item(j);
					referencedCommits.add(commitNode.getAttribute("sha"));
					String event = commitNode.getAttribute("event");
					if (event.equals("closed")) {
						NO_ClosingCommits++;
						withClosingCommit = true;
					}

					if (j == 0) {
						if (NO_ParentByCommit.get(commitNode
								.getAttribute("sha")) > 1) {
							++NO_NotOneParentPerFirstCommit;
						} else {
							++NO_OneParentPerFirstCommit;
						}
					}
				}

				if (withClosingCommit) {
					NO_IssuesWithClosingCommit++;
				} else {
					NO_IssuesWithoutClosingCommit++;
				}
			}
			NodeList openIssuesNodes = rootelement
					.getElementsByTagName("OpenIssues");
			Element openIssuesNode = (Element) openIssuesNodes.item(0);
			NodeList openIssueNodes = openIssuesNode
					.getElementsByTagName("Issue");

			NO_AllCommits = commitNodes.getLength();
			NO_Commits = referencedCommits.size();

			AVG_NO_CommitsPerIssue = NO_Commits
					/ (float) closedIssueNodes.getLength();

			NO_ClosedIssues = closedIssueNodes.getLength();
			NO_OpenIssues = openIssueNodes.getLength();
			NO_Issues = NO_ClosedIssues + NO_OpenIssues;

			PrintWriter pw = new PrintWriter(outFile);

			pw.print("number of all commits;");
			pw.print("number of commits;");
			pw.print("number of closing commits;");
			pw.print("number of issues;");
			pw.print("number of open issues;");
			pw.print("number of closed issues;");
			pw.print("number of issues with closing commits;");
			pw.print("number of issues without closing commits;");
			pw.print("avg number of commit per issue;");
			pw.print("min number of commit per issue;");
			pw.print("max number of commit per issue;");

			for (Integer key : NO_CommitPerIssue.keySet()) {
				pw.print("number of issues with " + key + " commit;");
			}

			for (Integer key : NO_ParentPerCommit.keySet()) {
				pw.print("number of commit with " + key + " parent;");
			}

			pw.print("number of first commits with 1 parent;");
			pw.print("number of first commits with not 1 parent;");

			pw.println();

			pw.print(NO_AllCommits);
			pw.print(";");
			pw.print(NO_Commits);
			pw.print(";");
			pw.print(NO_ClosingCommits);
			pw.print(";");
			pw.print(NO_Issues);
			pw.print(";");
			pw.print(NO_OpenIssues);
			pw.print(";");
			pw.print(NO_ClosedIssues);
			pw.print(";");
			pw.print(NO_IssuesWithClosingCommit);
			pw.print(";");
			pw.print(NO_IssuesWithoutClosingCommit);
			pw.print(";");
			pw.print(AVG_NO_CommitsPerIssue);
			pw.print(";");
			pw.print(MIN_NO_CommitsPerIssue);
			pw.print(";");
			pw.print(MAX_NO_CommitsPerIssue);
			pw.print(";");

			for (Integer key : NO_CommitPerIssue.keySet()) {
				pw.print(NO_CommitPerIssue.get(key));
				pw.print(";");
			}

			for (Integer key : NO_ParentPerCommit.keySet()) {
				pw.print(NO_CommitPerIssue.get(key));
				pw.print(";");
			}

			pw.print(NO_OneParentPerFirstCommit);
			pw.print(";");

			pw.print(NO_NotOneParentPerFirstCommit);
			pw.print(";");

			pw.println();

			pw.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void commit2IssuesCount(String outFile) {

		try {

			Map<String, Set<String>> commit2Issues = new HashMap<String, Set<String>>();

			Element rootelement = dom.getDocumentElement();

			NodeList closedissues = rootelement
					.getElementsByTagName("ClosedIssues");
			Element ci = (Element) closedissues.item(0);
			NodeList closedissuess = ci.getElementsByTagName("Issue");
			for (int i = 0; i < closedissuess.getLength(); ++i) {
				Element issue = (Element) closedissuess.item(i);

				NodeList commits = issue.getElementsByTagName("Commit");

				for (int j = 0; j < commits.getLength(); ++j) {
					Element commit = (Element) commits.item(j);

					if (!commit2Issues.containsKey(commit.getAttribute("sha")))
						commit2Issues.put(commit.getAttribute("sha"),
								new HashSet<String>());
					commit2Issues.get(commit.getAttribute("sha")).add(
							issue.getAttribute("number"));
				}

			}

			PrintWriter pw = new PrintWriter(outFile);
			for (String commitSha : commit2Issues.keySet()) {
				if (commit2Issues.get(commitSha).size() > 1) {
					pw.println(commitSha);
				}
			}
			pw.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
