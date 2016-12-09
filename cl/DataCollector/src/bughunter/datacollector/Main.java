package bughunter.datacollector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import uk.co.flamingpenguin.jewel.cli.CliFactory;

import bughunter.datacollector.Options;

public class Main {

	private final static Logger LOGGER = Logger.getLogger(Main.class.getName());

	private static Options processArguments(String[] args) {
		Cmd cmd = null;
		Options ret = new Options();
		try {
			cmd = CliFactory.parseArguments(Cmd.class, args);

			List<String> lims = null;

			if (cmd.isLimlist()) {
				lims = cmd.getLimlist();
			}

			if (cmd.isLimlistfile()) {
				if (lims == null)
					lims = new ArrayList<String>();
				BufferedReader br = new BufferedReader(new FileReader(new File(cmd.getLimlistfile())));
				String line;

				while ((line = br.readLine()) != null) {
					lims.add(line);
				}
				br.close();
			}

			if (lims != null) {
				ret.setLimFiles(true);
				ret.setLimFiles(lims);
			} else {
				ret.setLimFiles(false);
			}
			if (cmd.isBinary()) {
				ret.setBinary(cmd.getBinary());
			} else {
				ret.setBinary("");
			}
			if(cmd.isCsvdir()) {
				ret.setCsvdir(cmd.getCsvdir());
			} else {
				ret.setCsvdir("");
			}
			if (cmd.isRawdata()) {
				ret.setRawData(cmd.getRawdata());
			} else {
				ret.setRawData("");
			}
			if (cmd.isDumpbeforefile()) {
				ret.setDumpbefore(cmd.getDumpbeforefile());
			} else {
				ret.setDumpbefore("");
			}
			if (cmd.isTestFilter()) {
				ret.setTestFilter(cmd.getTestFilter());
			} else {
				ret.setTestFilter("");
			}
			if (cmd.isDumpfile()) {
				ret.setDumpfile(cmd.getDumpfile());
			} else {
				ret.setDumpfile("");
			}
			if (cmd.isDiffstat()) {
				ret.setDiffstat(cmd.getDiffstat());
			} else {
				ret.setDiffstat("");
			}
			if(cmd.isPrevlast()) {
				ret.setPrevlast(cmd.getPrevlast());
			} else {
				ret.setPrevlast("");
			}
			if(cmd.isFirstCommits()) {
				ret.setFirstCommits(cmd.getFirstCommits());
			} else {
				ret.setFirstCommits("");
			}
			if(cmd.isDumpDataCSV()) {
				ret.setDumpDataCSV(cmd.getDumpDataCSV());
			} else {
				ret.setDumpDataCSV("");
			}
			if(cmd.isTraditionaldir()) {
				ret.setTraditionaldir(cmd.getTraditionaldir());
			} else {
				ret.setTraditionaldir("");
			}
			if(cmd.isTraditionalCommits()) {
				ret.setTraditionalCommits(cmd.getTraditionalCommits());
			} else {
				ret.setTraditionalCommits("");
			}
			if (cmd.isOutput()) {
				ret.setOutput(cmd.getOutput());
			} else {
				ret.setOutput("");
			}
			if (cmd.isNolicense()) {
				ret.setNolicense(cmd.getNolicense());
			} else {
				ret.setNolicense("");
			}
			if (cmd.isMissinglim()) {
				ret.setMissinglimg(cmd.getMissinglim());
			} else {
				ret.setMissinglimg("");
			}
			if (cmd.isDuplicatedlims()) {
				ret.setDuplicatedlims(cmd.getDuplicatedlims());
			} else {
				ret.setDuplicatedlims("");
			}
			if (cmd.isReleaseStat()) {
				ret.setReleasestat(cmd.getReleaseStat());
			} else {
				ret.setReleasestat("");
			}
			if (cmd.isTraditionalTimestamp()) {
				ret.setTraditionalTimestamp(cmd.getTraditionalTimestamp());
			} else {
				ret.setTraditionalTimestamp("");
			}
			ret.setComputeMetrics(cmd.isComputemetrics());
			if (cmd.isDumpCommits()) {
				ret.setDumpCommits(cmd.getDumpCommits());
			} else {
				ret.setDumpCommits("");
			}
			if (cmd.isDumpIssueCommits()) {
				ret.setDumpIssueCommits(cmd.getDumpIssueCommits());
			} else {
				ret.setDumpIssueCommits("");
			}

		} catch (Exception e) {
			LOGGER.severe(e.toString());
			System.exit(1);
		}

		return ret;
	}

	public static void main(String[] args) {

		try {

			Options opts = processArguments(args);

			DataCollector data = new DataCollector();

			if (!opts.getBinary().isEmpty())
				data.load(opts.getBinary());

			if (!opts.getRawData().isEmpty()){
				data.parseXML(opts.getRawData());
			}

			if(!opts.getPrevlast().isEmpty())
				data.loadPrevlast(opts.getPrevlast());
			
			if(!opts.getFirstCommits().isEmpty())
				data.loadFirstCommits(opts.getFirstCommits());

			if(!opts.getNolicense().isEmpty())
				data.setNoLicense(opts.getNolicense());
			if(!opts.getMissinglimg().isEmpty())
				data.setMissingLims(opts.getMissinglimg());
			if(!opts.getDuplicatedlims().isEmpty())
				data.setDuplicatedLims(opts.getDuplicatedlims());
			if(!opts.getTraditionalTimestamp().isEmpty())
				data.setTraditionalTimestamp(opts.getTraditionalTimestamp());
			if(!opts.getReleasestat().isEmpty())
				data.setReleasestat(opts.getReleasestat());
			
			if(!opts.getTestFilter().isEmpty())
				data.loadTestFilters(opts.getTestFilter());
			
			if (opts.isLimFiles())
				data.setLims(opts.getLimFiles());


			if(!opts.getTraditionalCommits().isEmpty()) {
				data.loadTraditionalCommits(opts.getTraditionalCommits());
			}

			if(!opts.getDumpCommits().isEmpty()) {
				data.dumpCommits(opts.getDumpCommits());
			}
			
			if(!opts.getDumpIssueCommits().isEmpty()) {
				data.dumpIssueCommits(opts.getDumpIssueCommits());
			}

			if (opts.isComputeMetrics()){

				data.collectNeededCommtis();
				
				data.collectModifiedElements();

				data.computeProcessMetrics();
				data.computeBugNumbers();
				data.computeCommitBugStat();
				if(!opts.getReleasestat().isEmpty())
					data.writeReleaseStat();
			}
			if(!opts.getCsvdir().isEmpty()) {
				data.writeDatabase(opts.getCsvdir());
			}
			if (!opts.getTraditionaldir().isEmpty()){
				data.writeTraditional(opts.getTraditionaldir());
			}
			if (!opts.getDumpDataCSV().isEmpty()){
				data.dumpData(opts.getDumpDataCSV());
			}
			
			if(!opts.getDumpbefore().isEmpty())
				data.dumpBeforeCommits(opts.getDumpbefore());

			if (!opts.getDumpfile().isEmpty())
				data.dump(opts.getDumpfile());

			if (!opts.getDiffstat().isEmpty())
				data.saveSourceElementDiffStat(opts.getDiffstat());
			
			if (!opts.getOutput().isEmpty())
				data.save(opts.getOutput());

			LOGGER.info("Done");
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.severe(e.toString());
			System.exit(1);
		}

	}

}
