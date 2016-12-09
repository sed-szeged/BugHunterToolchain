package bughunter.datacollector;

import java.text.DecimalFormat;

public class Utils {

	public static String format(Float f) {
		DecimalFormat formatter = new DecimalFormat("#.##");
		return formatter.format(f);
	}
}
