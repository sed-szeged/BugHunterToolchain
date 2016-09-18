package bughunter.graphextractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class Options {
	
	@Option(name = "-l", usage = "The list of release versions", required=true)
	public File releases;

	@Option(name = "-o", usage = "The output directory", required=true)
	public String out;

	@Option(name = "-p", usage = "The project")
	public String project;

	@Argument
    private List<String> arguments = new ArrayList<String>();

}
