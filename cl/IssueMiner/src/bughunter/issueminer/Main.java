package bughunter.issueminer;

import java.util.logging.Logger;

import uk.co.flamingpenguin.jewel.cli.CliFactory;

public class Main {

	private final static Logger LOGGER = Logger.getLogger(Main.class.getName());

	private static Options processArguments(String[] args) {
		Cmd cmd = null;
		Options ret = new Options();
		try {
			cmd = CliFactory.parseArguments(Cmd.class, args);

			if (cmd.getRawData().isEmpty()) {
				LOGGER.severe("No raw data file was given");
				System.exit(1);
			} else {
				ret.setRawData(cmd.getRawData());
			}

			if (cmd.isLastCommitsOutFile()) {
				ret.setLastCommitsOutFile(cmd.getLastCommitsOutFile());
			} else {
				ret.setLastCommitsOutFile("");
			}

			if (cmd.isFirstCommitsOutFile()) {
				ret.setFirstCommitsOutFile(cmd.getFirstCommitsOutFile());
			} else {
				ret.setFirstCommitsOutFile("");
			}

			if (cmd.isBeforeCommitsOutFile()) {
				ret.setBeforeCommitsOutFile(cmd.getBeforeCommitsOutFile());
			} else {
				ret.setBeforeCommitsOutFile("");
			}

			if (cmd.isReferencedCommitsOutFile()) {
				ret.setReferencedCommitsOutFile(cmd.getReferencedCommitsOutFile());
			} else {
				ret.setReferencedCommitsOutFile("");
			}

			if (cmd.isBeforeAndLastCommitsOutFile()) {
				ret.setBeforeAndLastCommitsOutFile(cmd.getBeforeAndLastCommitsOutFile());
			} else {
				ret.setBeforeAndLastCommitsOutFile("");
			}

			if (cmd.isStatOutFile()) {
				ret.setStatOutFile(cmd.getStatOutFile());
			} else {
				ret.setStatOutFile("");
			}

			if (cmd.isAllCommitFile()) {
				ret.setAllCommitFile(cmd.getAllCommitFile());
			} else {
				ret.setAllCommitFile("");
			}

			if (cmd.isWorkDir()) {
				ret.setWorkDir(cmd.getWorkDir());
			} else {
				ret.setWorkDir("");
			}

			if (cmd.isCommitsToMultipleIssuesOutFile()) {
				ret.setCommitsToMultipleIssuesOutFile(cmd.getCommitsToMultipleIssuesOutFile());
			} else {
				ret.setCommitsToMultipleIssuesOutFile("");
			}

		} catch (Exception e) {
			LOGGER.severe(e.toString());
			System.exit(1);
		}

		return ret;
	}

	public static void main(String[] args) {

		Options opts = processArguments(args);

		IssueMiner miner = new IssueMiner(opts.getRawData(), opts.getWorkDir());

		if (!opts.getLastCommitsOutFile().isEmpty())
			miner.produceLastCommits(opts.getLastCommitsOutFile());

		if (!opts.getFirstCommitsOutFile().isEmpty())
			miner.produceFirstCommits(opts.getFirstCommitsOutFile());

		if (!opts.getBeforeCommitsOutFile().isEmpty())
			miner.produceBeforeCommits(opts.getBeforeCommitsOutFile());

		if (!opts.getReferencedCommitsOutFile().isEmpty())
			miner.produceReferencedCommits(opts.getReferencedCommitsOutFile());

		if (!opts.getBeforeAndLastCommitsOutFile().isEmpty())
			miner.produceBeforeAndLastCommits(opts.getBeforeAndLastCommitsOutFile());
		
		if (!opts.getStatOutFile().isEmpty())
			miner.produceStatistics(opts.getStatOutFile());

		if (!opts.getCommitsToMultipleIssuesOutFile().isEmpty())
			miner.commit2IssuesCount(opts.getCommitsToMultipleIssuesOutFile());

		if (!opts.getAllCommitFile().isEmpty())
			miner.produceAllCommits(opts.getAllCommitFile());
		
	}

}
