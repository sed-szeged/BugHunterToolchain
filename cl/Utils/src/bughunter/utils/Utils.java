package bughunter.utils;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import bughunter.utils.model.*;

public class Utils {
	
	private final static Logger LOGGER = Logger.getLogger(Utils.class.getName());

	public static String format(Float f) {
		DecimalFormat formatter = new DecimalFormat("#.##");
		return formatter.format(f);
	}

	public static String format(Date d) {
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		return dateformat.format(d);
	}
	
	public static Project parseGitHubXML(java.io.File xmlFileName) {

		LOGGER.info("Parsing xml: " + xmlFileName);
		
		Document dom;
		Map<Integer, User> contributors = new HashMap<Integer, User>();
		Map<Integer, Issue> issues = new HashMap<Integer, Issue>();
		Map<String, Commit> commits = new HashMap<String, Commit>();

		StringStorage strTable = new StringStorage();
		Project project = new Project(strTable);
		try {

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

				User user = new User();
				user.id = id;
				user.commits = contint;
				contributors.put(id, user);

			}

			// get commits node
			NodeList commitsNodes = rootElement.getElementsByTagName("Commits");
			Element commitsNode = (Element) commitsNodes.item(0);

			// get commit node
			NodeList commitNodes = commitsNode.getElementsByTagName("Commit");
			
			for (int a = 0; a < commitNodes.getLength(); a++) {
				Commit commit = new Commit();
				Element commitNode = (Element) commitNodes.item(a);
				commit.sha = commitNode.getAttribute("sha");
				String created_atStr = commitNode.getAttribute("date");
				commit.created = dateformat.parse(created_atStr);
				String authorIdStr = commitNode.getAttribute("author");
				int authorId = -1;
				if (!authorIdStr.equals(""))
					authorId = Integer.parseInt(authorIdStr);

				// create the commit author user
				commit.author = contributors.get(authorId);

				// get parents node
				NodeList parentsNodes = commitNode.getElementsByTagName("Parents");
				Element parentsNode = (Element) parentsNodes.item(0);

				// get parent node
				NodeList parentNodes = parentsNode.getElementsByTagName("Parent");

				for (int j = 0; j < parentNodes.getLength(); j++) {

					Element parentNode = (Element) parentNodes.item(j);
					String parentSha = parentNode.getAttribute("sha");
					commit.parents.add(parentSha);
				}
				
				// get files node
				NodeList filesNodes = commitNode.getElementsByTagName("Files");
				Element filesNode = (Element) filesNodes.item(0);

				// get file node FAJLKEZELES ->>>> DATA.XMLben MEGVAN A HELYES FAJL NEV, ITT NINCS FELDOLGOZVA..?
				// 57 sor addition, 0 deletion, 2 method added, 1 fajl listaban 1 fajl bejegyzes
				// Files/ File -> filename /patch
				NodeList fileNodes = filesNode.getElementsByTagName("File");

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
					commit.files.put(f.filename, f);

					// get patch node
					String patch = fileNode.getElementsByTagName("Patch").item(0).getTextContent();
					patch = patch.replaceAll(" @@([^\n]*)", " @@");

					// create filechanges
					FileChange fg = new FileChange();
					fg.additions = additions;
					fg.changes = changes;
					fg.deletions = deletions;
					fg.diff = patch;
					fg.modifiedFile = f;

					switch (status.toLowerCase()) {
					case "modified":
					case "renamed":
					case "changed":
						fg.status = FileChange.Status.Modified;
						break;
					case "removed":
						fg.status = FileChange.Status.Deleted;
						break;
					case "added":
						fg.status = FileChange.Status.Added;
						break;
					default:
						System.out.println(status);
						System.exit(1);
					}

					commit.changes.add(fg);
				}

				// create commits
				commit.fix = false;
				commit.project = project;
				commits.put(commit.sha, commit);
			}

			// get Closedissues node

			NodeList closedIssuesNodes = rootElement.getElementsByTagName("ClosedIssues");

			Element closedIssuesNode = (Element) closedIssuesNodes.item(0);

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
					ic.fix = true;
					issueCommits.put(sha, ic);

				}

				// create issues
				Issue issue = new Issue(number, created_at, closed_at, issueCommits);
				issue.project = project;
				issues.put(number, issue);

			}

			// get Openissues
			NodeList openIssuesNodes = rootElement.getElementsByTagName("OpenIssues");
			Element openIssuesNode = (Element) openIssuesNodes.item(0);

			// get issues
			NodeList openIssueNodes = ((Element) openIssuesNode).getElementsByTagName("Issue");

			for (int i = 0; i < openIssueNodes.getLength(); i++) {

				Element issueNode = (Element) openIssueNodes.item(i);
				String numberStr = issueNode.getAttribute("number");
				int number = Integer.parseInt(numberStr);
				String created_atStr = issueNode.getAttribute("created_at");
				Date created_at = dateformat.parse(created_atStr);

				Issue issue = new Issue();
				issue.id = number;
				issue.project = project;
				issue.opened = created_at;
				issues.put(number, issue);
			}

			project.user = projectUser;
			project.repo = projectRepo;
			project.commits = commits;
			project.contributors = contributors;
			project.issues = issues;

		} catch (ParserConfigurationException e) {
			LOGGER.severe(e.getMessage());
		} catch (SAXException e) {
			LOGGER.severe(e.getMessage());
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		} catch (ParseException e) {
			LOGGER.severe(e.getMessage());
		}
		return project;
	}
	
	public static Map.Entry<Integer, Long> pair(long a, long b) {
		return new AbstractMap.SimpleEntry<Integer, Long>((int)a, b);
	}
}
