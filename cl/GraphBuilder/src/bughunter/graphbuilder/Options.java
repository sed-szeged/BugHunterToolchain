package bughunter.graphbuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class Options {

	@Option(name = "-x", usage = "The github xml file", required=true)
	public File xml;
	
	@Option(name = "-l", usage = "The list of lims", required=true)
	public File lims;

	@Option(name = "-o", usage = "The output directory", required=true)
	public String out;

	@Option(name = "-f", usage = "The test filters")
	public File testfilter;

	@Argument
    private List<String> arguments = new ArrayList<String>();

}
