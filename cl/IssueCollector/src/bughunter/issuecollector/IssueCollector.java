package bughunter.issuecollector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.Contributor;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.IssueEvent;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.NoSuchPageException;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

public class IssueCollector {

	private Document dom;

	private GitHubClient client;
	private RepositoryService repoService;
	private IssueService issueService;
	private CommitService commitService;
	private UserService userService;

	private final static Logger LOGGER = Logger.getLogger(IssueCollector.class.getName());

	public IssueCollector(String username, String pswd) {

		// github client init
		this.client = new GitHubClient();
		client.setCredentials(username, pswd);

		// service init
		this.repoService = new RepositoryService(client);
		this.issueService = new IssueService(client);
		this.commitService = new CommitService(client);
		this.userService = new UserService(client);

	}

	private void checkLimit() throws IOException {
		if (client.getRemainingRequests() == -1) {
			userService.getUser();
		}
		try {
			int waiting = 0;
			while (client.getRemainingRequests() < 200) {

				if (waiting >= 8) {
					LOGGER.severe("Waiting take too much time! Aborting...");
					System.exit(1);
				}
				++waiting;
				LOGGER.info("Limit almost reached. Sleeping for 10 minutes...");
				Thread.sleep(10 * 60 * 1000);
				LOGGER.info("Good morning!");
				userService.getUser();
			}
			if (waiting > 0) {
				LOGGER.info("Let's continue the job!");
			}
		} catch (InterruptedException e) {
			LOGGER.severe(e.toString());
			System.exit(1);
		}

	}

	private void createDocument() {

		try {

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			dom = db.newDocument();

		} catch (ParserConfigurationException e) {
			LOGGER.severe(e.toString());
			System.exit(1);
		}
	}

	protected void saveXML(String output) {

		try {

			OutputFormat format = new OutputFormat(dom);
			format.setIndenting(true);

			// XML-be írás
			XMLSerializer serializer = new XMLSerializer(new FileOutputStream(new File(output)), format);

			serializer.serialize(dom);
		} catch (IOException e) {
			LOGGER.severe(e.toString());
			System.exit(1);
		}
	}

	private void removeDuplicates(List<Issue> l) {

		Set<Issue> s = new TreeSet<Issue>(new Comparator<Issue>() {

			@Override
			public int compare(Issue i1, Issue i2) {
				if (i1.getNumber() == i2.getNumber()) {
					return 0;
				} else if (i1.getNumber() > i2.getNumber()) {
					return 1;
				} else {
					return -1;
				}
			}
		});
		s.addAll(l);
		l.clear();
		l.addAll(s);
	}

	public String stripNonValidXMLCharacters(String in) {
		StringBuffer out = new StringBuffer();
		char current;

		if (in == null || ("".equals(in)))
			return "";
		for (int i = 0; i < in.length(); i++) {
			current = in.charAt(i);
			if ((current == 0x9) || (current == 0xA) || (current == 0xD) || ((current >= 0x20) && (current <= 0xD7FF))
					|| ((current >= 0xE000) && (current <= 0xFFFD)) || ((current >= 0x10000) && (current <= 0x10FFFF)))
				out.append(current);
		}
		return out.toString();

	}

	public void collect(String repouser, String repository, List<String> labels) {

		try {
			checkLimit();

			// repo lekérése
			Repository projectRepository = repoService.getRepository(repouser, repository);

			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

			Map<String, RepositoryCommit> commits = new HashMap<String, RepositoryCommit>();

			// XML kiírás kezdete
			createDocument();
			Element rootElement = dom.createElement("Project");
			rootElement.setAttribute("user", repouser);
			rootElement.setAttribute("repo", repository);
			dom.appendChild(rootElement);

			checkLimit();
			List<Contributor> contributors = repoService.getContributors(projectRepository, false);

			Element contributorsElement = dom.createElement("Contributors");
			rootElement.appendChild(contributorsElement);

			for (Contributor cont : contributors) {

				Element userElement = dom.createElement("User");
				userElement.setAttribute("id", Integer.toString(cont.getId()));
				userElement.setAttribute("contributions", Integer.toString(cont.getContributions()));
				contributorsElement.appendChild(userElement);

			}

			// /////////////////////////////////////////// commits

			checkLimit();
			List<RepositoryCommit> repositoryCommits = commitService.getCommits(projectRepository);
			LOGGER.info("Number of commits: " + repositoryCommits.size());

			Element commitsElement = dom.createElement("Commits");

			for (RepositoryCommit repositoryCommit : repositoryCommits) {

				Element commitElement = dom.createElement("Commit");

				commitsElement.appendChild(commitElement);

				// store commits by sha
				commits.put(repositoryCommit.getSha(), repositoryCommit);

				commitElement.setAttribute("sha", repositoryCommit.getSha());

				if (repositoryCommit.getCommit().getCommitter() != null) {
					commitElement.setAttribute("date", dateformat.format(repositoryCommit.getCommit().getCommitter().getDate()));
				} else {
					commitElement.setAttribute("date", "");

					LOGGER.info("Commit does not contains commiter: " + repositoryCommit.getSha());
				}

				if (repositoryCommit.getAuthor() != null) {
					commitElement.setAttribute("author", Integer.toString(repositoryCommit.getAuthor().getId()));
				} else {
					commitElement.setAttribute("author", "");

					LOGGER.info("Commit does not contains author: " + repositoryCommit.getSha());
				}
				commitElement.setAttribute("tree", repositoryCommit.getCommit().getTree().getSha());

				Element parentsElement = dom.createElement("Parents");
				commitElement.appendChild(parentsElement);

				// commit õsei
				for (Commit parent : repositoryCommit.getParents()) {

					Element parentElement = dom.createElement("Parent");

					parentElement.setAttribute("sha", parent.getSha());

					parentsElement.appendChild(parentElement);
				}

				Element filesElement = dom.createElement("Files");
				commitElement.appendChild(filesElement);

				// get commit with files
				checkLimit();
				repositoryCommit = commitService.getCommit(projectRepository, repositoryCommit.getSha());

				if (repositoryCommit.getFiles() == null) {
					LOGGER.info("Commit does not contains files: " + repositoryCommit.getSha());
				} else {
					for (CommitFile file : repositoryCommit.getFiles()) {

						Element fileElement = dom.createElement("File");

						fileElement.setAttribute("filename", file.getFilename());
						fileElement.setAttribute("status", file.getStatus());
						fileElement.setAttribute("changes", Integer.toString(file.getChanges()));
						fileElement.setAttribute("additions", Integer.toString(file.getAdditions()));
						fileElement.setAttribute("deletions", Integer.toString(file.getDeletions()));

						Element patchElement = dom.createElement("Patch");
						CDATASection patchData = dom.createCDATASection(stripNonValidXMLCharacters(file.getPatch()));
						patchElement.appendChild(patchData);

						fileElement.appendChild(patchElement);

						filesElement.appendChild(fileElement);
					}
				}
			}

			// /////////////////////////////////////////// closed issues

			Element closedIssuesElement = dom.createElement("ClosedIssues");
			rootElement.appendChild(closedIssuesElement);

			// filter, hogy tudjuk szûrni a lekérdezéseket
			Map<String, String> filter = new HashMap<String, String>();
			filter.put("state", "closed");

			// repohoz tartozó zárt issue-k lekérése
			List<Issue> closedIssues = new ArrayList<Issue>();
			for (String label : labels) {
				filter.put("labels", label);
				checkLimit();
				closedIssues.addAll(issueService.getIssues(projectRepository, filter));
			}
			removeDuplicates(closedIssues);

			LOGGER.info("Number of closed issues: " + closedIssues.size());
			closedIssuesElement.setAttribute("count", Integer.toString(closedIssues.size()));

			for (Issue issue : closedIssues) {
				// issue event-ek lekérése

				checkLimit();
				// /(lapozást itt manuálisan kell lekezelni)
				PageIterator<IssueEvent> eventIt = issueService.pageIssueEvents(repouser, repository, issue.getNumber());
				List<IssueEvent> events = new ArrayList<IssueEvent>();
				try {
					while (eventIt.hasNext())
						events.addAll(eventIt.next());
				} catch (NoSuchPageException pageException) {
					throw pageException.getCause();
				}
				// /

				Element issueElement = dom.createElement("Issue");
				issueElement.setAttribute("number", Integer.toString(issue.getNumber()));

				issueElement.setAttribute("created_at", dateformat.format(issue.getCreatedAt()));
				issueElement.setAttribute("closed_at", dateformat.format(issue.getClosedAt()));

				closedIssuesElement.appendChild(issueElement);

				for (IssueEvent event : events) {
					// egyelõre a referenced és closed típusú event-ek
					// érdekelnek minket
					if (event.getEvent().equals("referenced") || event.getEvent().equals("closed")) {

						if (event.getCommitId() == null) {
							continue;
						}
						RepositoryCommit rc = commits.get(event.getCommitId());

						if (rc != null) {

							Element issueCommitElement = dom.createElement("Commit");

							issueCommitElement.setAttribute("sha", event.getCommitId());
							issueCommitElement.setAttribute("event", event.getEvent());
							issueCommitElement.setAttribute("created_at", dateformat.format(event.getCreatedAt()));

							issueElement.appendChild(issueCommitElement);
						} else {
							LOGGER.info("Commit does not exists: (" + issue.getNumber() + ") " + event.getCommitId());
						}
					}
				}
			}

			// /////////////////////////////////////////// open issues

			Element openIssuesElement = dom.createElement("OpenIssues");
			rootElement.appendChild(openIssuesElement);

			// filter, hogy tudjuk szûrni a lekérdezéseket
			filter.clear();
			filter.put("state", "open");

			// repohoz tartozó nyitott issue-k lekérése
			List<Issue> openIssues = new ArrayList<Issue>();
			for (String label : labels) {
				filter.put("labels", label);
				checkLimit();
				openIssues.addAll(issueService.getIssues(projectRepository, filter));
			}
			removeDuplicates(openIssues);

			LOGGER.info("Number of open issues: " + openIssues.size());
			openIssuesElement.setAttribute("count", Integer.toString(openIssues.size()));

			for (Issue issue : openIssues) {

				Element issueElement = dom.createElement("Issue");
				issueElement.setAttribute("number", Integer.toString(issue.getNumber()));

				issueElement.setAttribute("created_at", dateformat.format(issue.getCreatedAt()));

				openIssuesElement.appendChild(issueElement);

			}

			// store commits
			rootElement.appendChild(commitsElement);

		} catch (IOException e) {
			LOGGER.severe(e.toString());
			System.exit(1);
		}

	}
}
