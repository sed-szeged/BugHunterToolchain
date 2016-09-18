package bughunter.issuecollector;

import uk.co.flamingpenguin.jewel.cli.Option;

public interface Cmd {

	@Option(shortName = "r", description = "The repo")
	String getRepo();

	@Option(shortName = "a", description = "The repo user")
	String getRepouser();

	@Option(shortName = "l", description = "The label list file")
	String getLabellistfile();

	@Option(shortName = "o", description = "The output file name")
	String getOutputfile();

	@Option(shortName = "u", description = "The username of the github user")
	String getUsername();

	@Option(shortName = "p", description = "The password of the github user")
	String getPassword();

	@Option(helpRequest = true, shortName = "h", description = "Print help")
	boolean getHelp();
}
