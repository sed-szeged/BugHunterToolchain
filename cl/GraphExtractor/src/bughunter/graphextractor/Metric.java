package bughunter.graphextractor;


public abstract class Metric {
	
	protected String name;
	public static final long DAY = 86400000;
	
	public Metric(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getFilename() {
		char c[] = name.replace(" ", "").toCharArray();
	    c[0] = Character.toLowerCase(c[0]);
		return new String(c); 
	}

	public String getFileQuery(String sha) {
		return null;
	}

	public String getMethodQuery(String sha) {
		return null;
	}

	public String getClassQuery(String sha) {
		return null;
	}
}
