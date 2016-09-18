package bughunter.issuecollector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import uk.co.flamingpenguin.jewel.cli.CliFactory;

public class Main {

	private final static Logger LOGGER = Logger.getLogger(Main.class.getName());

	private static Options processArguments(String[] args) {
		Cmd cmd = null;
		Options ret = new Options();
		try {
			cmd = CliFactory.parseArguments(Cmd.class, args);

			if (cmd.getLabellistfile().isEmpty()) {
				LOGGER.severe("No label list file was given");
				System.exit(1);
			} else {
				BufferedReader br = new BufferedReader(new FileReader(new File(cmd.getLabellistfile())));
				String line;
				List<String> labels = new ArrayList<String>();
				while ((line = br.readLine()) != null) {
					labels.add(line);
				}
				ret.setLabels(labels);
				br.close();
			}

			if (cmd.getRepouser().isEmpty()) {
				LOGGER.severe("No repo user was given");
				System.exit(1);
			} else {
				ret.setRepouser(cmd.getRepouser());
			}

			if (cmd.getRepo().isEmpty()) {
				LOGGER.severe("No repo was given");
				System.exit(1);
			} else {
				ret.setRepo(cmd.getRepo());
			}

			if (cmd.getUsername().isEmpty()) {
				LOGGER.severe("No username was given");
				System.exit(1);
			} else {
				ret.setUsername(cmd.getUsername());
			}

			if (cmd.getPassword().isEmpty()) {
				LOGGER.severe("No password was given");
				System.exit(1);
			} else {
				ret.setPassword(cmd.getPassword());
			}

			if (cmd.getOutputfile().isEmpty()) {
				LOGGER.severe("No output file was given");
				System.exit(1);
			} else {
				ret.setOut(cmd.getOutputfile());
			}

		} catch (Exception e) {
			LOGGER.severe(e.toString());
			System.exit(1);
		}

		return ret;
	}

	public static void main(String[] args) {

		Options opts = processArguments(args);

		IssueCollector ic = new IssueCollector(opts.getUsername(), opts.getPassword());
		ic.collect(opts.getRepouser(), opts.getRepo(), opts.getLabels());
		ic.saveXML(opts.getOut());
	}

}
