package bughunter.datacollector.model;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.List;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.CDATASection;


public class Dumper {
	
	public static void dump(Project project, String path) {
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();
            Element rootElement = doc.createElement("Project");
            rootElement.setAttribute("User", project.getUser());
            rootElement.setAttribute("Repo", project.getRepo());
            doc.appendChild(rootElement);
            
            Element contributorsElement = doc.createElement("Contributors");
            rootElement.appendChild(contributorsElement);
            
            Map<Integer, User> contributors = project.getContributors();
            for (Map.Entry<Integer, User> contributorEntry : contributors.entrySet()) {
            	User contributorValue = contributorEntry.getValue();
            	Element user = doc.createElement("User");
            	user.setAttribute("id", Integer.toString(contributorValue.getId()));
            	user.setAttribute("contributions", Integer.toString(contributorValue.getCommits()));
            	contributorsElement.appendChild(user);
            }
            
            Element commitsElement = doc.createElement("Commits");
            rootElement.appendChild(commitsElement);
            
            Map<String, Commit> commits = project.getCommits();
            for (Map.Entry<String, Commit> commitEntry : commits.entrySet()) {
            	
            	Commit commitValue = commitEntry.getValue();
            	Element commit = doc.createElement("Commit");
            	
            	commit.setAttribute("sha", commitValue.getSha());
            	commit.setAttribute("created", commitValue.getCreated().toString());
            	commit.setAttribute("numberOfBugs", Integer.toString(commitValue.getNumberOfBugs()));
            	commit.setAttribute("fix", Boolean.toString(commitValue.isFix()));
            	
            	if(commitValue.getAuthor() != null) {
                	Element authorElement = doc.createElement("Author");
                	authorElement.setAttribute("ref", Integer.toString(commitValue.getAuthor().getId()));
                	commit.appendChild(authorElement);
            	}
            	
            	commitsElement.appendChild(commit);
            	
            	Element filesElement = doc.createElement("Files");
            	commit.appendChild(filesElement);
            	List<File> files = commitValue.getFiles();
            	for (File fileEntry : files) {
            		Element file = doc.createElement("File");
            		file.setAttribute("filename", fileEntry.getFilename());
            		filesElement.appendChild(file);
            	}
            	
            	Element fileChangesElement = doc.createElement("FileChanges");
            	commit.appendChild(fileChangesElement);
            	List<FileChange> filechanges = commitValue.getChanges();
            	for (FileChange changeEntry : filechanges) {
            		Element change = doc.createElement("FileChange");
            		change.setAttribute("status", changeEntry.getStatus().toString());
            		change.setAttribute("additions", Integer.toString(changeEntry.getAdditions()));
            		change.setAttribute("deletions", Integer.toString(changeEntry.getDeletions()));
            		change.setAttribute("changes", Integer.toString(changeEntry.getChanges()));
            		fileChangesElement.appendChild(change);
            		
            		Element diffElement = doc.createElement("Diff");
            		CDATASection patchElement = doc.createCDATASection(changeEntry.getDiff());
            		diffElement.appendChild(patchElement);
            		change.appendChild(diffElement);
            		
            		Element modifiedFileElement = doc.createElement("ModifiedFile");
            		modifiedFileElement.setAttribute("ref", changeEntry.getModifiedFile().getFilename());
            		change.appendChild(modifiedFileElement);
            		
            		Element modifiedClassesElement = doc.createElement("ModifiedClasses");
            		change.appendChild(modifiedClassesElement);
            		/*Set<Class> classlist = changeEntry.getModifiedClasses();
            		for (Class classEntry : classlist) {
            			Element classElement = doc.createElement("Class");
            			classElement.setAttribute("ref", Integer.toString(classEntry.getId()));
            			modifiedClassesElement.appendChild(classElement);
            		}*/
            		
            		Element modifiedMethodsElement = doc.createElement("ModifiedMethods");
            		change.appendChild(modifiedMethodsElement);
            		/*Set<Method> methodlist = changeEntry.getModifiedMethods();
            		for (Method methodEntry : methodlist) {
            			Element methodElement = doc.createElement("Method");
            			methodElement.setAttribute("ref", Integer.toString(methodEntry.getId()));
            			modifiedMethodsElement.appendChild(methodElement);
            		}*/
            		
            	}
            	/*
            	Element nodesElement = doc.createElement("Nodes");
            	commit.appendChild(nodesElement);
            	Map<Integer, SourceElement> nodes = commitValue.getNodes();
                for (Map.Entry<Integer, SourceElement> nodeEntry : nodes.entrySet()) {
                	SourceElement sourceElementValue = nodeEntry.getValue();
                	Element packageElement;
                	if(sourceElementValue instanceof Package)
                		packageElement = doc.createElement("Package");
                	else if(sourceElementValue instanceof Class)
                		packageElement = doc.createElement("Class");
                	else
                		packageElement = doc.createElement("Method");
                	packageElement.setAttribute("id", Integer.toString(sourceElementValue.getId()));
                	packageElement.setAttribute("name", sourceElementValue.getName());
                	packageElement.setAttribute("longname", sourceElementValue.getLongName());
                	packageElement.setAttribute("modificationsForThisFix", Integer.toString(sourceElementValue.getModificationsForThisFix()));
                	packageElement.setAttribute("fixes", Integer.toString(sourceElementValue.getFixes()));
                	
                	if(!(sourceElementValue instanceof Package)) {
	                	Element sourceRangeElement = doc.createElement("SourceRange");
	                	Element sourceRangeBeginElement = doc.createElement("SourcePositionBegin");
	                	sourceRangeBeginElement.setAttribute("file", sourceElementValue.getRange().getBegin().getFile().getFilename());
	                	sourceRangeBeginElement.setAttribute("line", Integer.toString(sourceElementValue.getRange().getBegin().getLine()));
	                	sourceRangeBeginElement.setAttribute("column", Integer.toString(sourceElementValue.getRange().getBegin().getColumn()));
	                	sourceRangeElement.appendChild(sourceRangeBeginElement);
	                	Element sourceRangeEndElement = doc.createElement("SourcePositionEnd");
	                	sourceRangeEndElement.setAttribute("file", sourceElementValue.getRange().getEnd().getFile().getFilename());
	                	sourceRangeEndElement.setAttribute("line", Integer.toString(sourceElementValue.getRange().getEnd().getLine()));
	                	sourceRangeEndElement.setAttribute("column", Integer.toString(sourceElementValue.getRange().getEnd().getColumn()));
	                	sourceRangeElement.appendChild(sourceRangeEndElement);
	                	packageElement.appendChild(sourceRangeElement);
                	}
                	
                	nodesElement.appendChild(packageElement);
                	
                	Element membersElement = doc.createElement("Members");
                	packageElement.appendChild(membersElement);
                	Set<SourceElement> sourceElementList = sourceElementValue.getMembers();
            		for (SourceElement sourceElementEntry : sourceElementList) {
            			Element memberElement = doc.createElement("Member");
            			memberElement.setAttribute("ref", Integer.toString(sourceElementEntry.getId()));
            			membersElement.appendChild(memberElement);
            		}
                }*/
            	
            }
            
            Element issuesElement = doc.createElement("Issues");
            rootElement.appendChild(issuesElement);
            
            Map<Integer, Issue> issues = project.getIssues();
            for (Map.Entry<Integer, Issue> issueEntry : issues.entrySet()) {
            	Issue issueValue = issueEntry.getValue();
            	Element issue = doc.createElement("Issue");
        		issue.setAttribute("id", Integer.toString(issueValue.getId()));
        		issue.setAttribute("prev", issueValue.getPrev()==null?"-":issueValue.getPrev().getSha());
        		issue.setAttribute("last", issueValue.getLast()==null?"-":issueValue.getLast().getSha());
        		issue.setAttribute("opened", issueValue.getOpened().toString());
        		issue.setAttribute("closed", issueValue.getClosed()==null?"-":issueValue.getClosed().toString());
        		issuesElement.appendChild(issue);
        		
        		Element issueCommitsElement = doc.createElement("Commits");
        		issue.appendChild(issueCommitsElement);
            	Map<String, Commit> issueCommits = issueValue.getCommits();
                for (Map.Entry<String, Commit> issueCommitEntry : issueCommits.entrySet()) {
                   	Commit issueCommitValue = issueCommitEntry.getValue();
                   	Element issueCommit = doc.createElement("Commit");
                   	issueCommit.setAttribute("sha", issueCommitValue.getSha());
                   	issueCommitsElement.appendChild(issueCommit);  
            	} 
            		
            }
          
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            

			OutputFormat format = new OutputFormat(doc);
			format.setIndenting(true);

			XMLSerializer serializer = new XMLSerializer(new FileOutputStream(new java.io.File(path)), format);

			serializer.serialize(doc);

        } catch (Exception e) {
            e.printStackTrace();
        }

		
	}

}
