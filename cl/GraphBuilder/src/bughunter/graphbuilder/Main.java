package bughunter.graphbuilder;

import java.util.logging.Logger;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import bughunter.graphbuilder.Options;

public class Main {

	private final static Logger LOGGER = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) {
		Options options = new Options();
		CmdLineParser parser = new CmdLineParser(options);

		try {

			parser.parseArgument(args);

			new GraphBuilder(options).run();

			LOGGER.info("Done");
			
		} catch( CmdLineException e ) {
			//e.printStackTrace();
			LOGGER.severe(e.getMessage());
            parser.printUsage(System.err);

        } catch (Exception e) {
			e.printStackTrace();
			LOGGER.severe(e.toString());
			System.exit(1);
		}

	}

}
