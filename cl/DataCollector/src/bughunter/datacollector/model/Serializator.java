package bughunter.datacollector.model;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.logging.Logger;

public class Serializator {

	private final static Logger LOGGER = Logger.getLogger(Serializator.class.getName());

	public static void serialize(Project project, String path) throws IOException {

		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(path));
			out.writeObject(project);
		} catch (IOException e) {
			//e.printStackTrace();
			LOGGER.severe(e.getMessage());
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	public static Project deserialize(String path) throws IOException, ClassNotFoundException {

		Project project = null;
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(path));
			project = (Project) in.readObject();
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		} catch (ClassNotFoundException e) {
			LOGGER.severe(e.getMessage());
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return project;

	}

}
