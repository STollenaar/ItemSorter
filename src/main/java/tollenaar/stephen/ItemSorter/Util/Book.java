package tollenaar.stephen.ItemSorter.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Book implements Serializable {

	private static final long serialVersionUID = 4519138450923555946L;
	private static transient Map<Integer, Book> BOOKS; // mapped from frameID to
	private List<Material> inputConfig = new ArrayList<>(); // input sorting configure

	public Book(int frameID) {
		this.addSelf(frameID);
	}

	public void addSelf(int frameID) {
		if (BOOKS == null) {
			BOOKS = new HashMap<>();
		}
		BOOKS.put(frameID, this);
	}

	public boolean hasInputConfig(Material material) {
		return inputConfig.contains(material);
	}

	public List<String> toPages() {
		List<String> pages = new ArrayList<String>();

		String path = "InputConfig";
		FileConfiguration pageBuilder = new YamlConfiguration();
		// tmp list for better formatting
		List<String> tmp = new ArrayList<>();
		for (Material material : this.inputConfig) {
			tmp.add(material.name());
		}
		pageBuilder.set(path, tmp);

		List<String> inputConfigList = new ArrayList<String>(Arrays.asList(pageBuilder.saveToString().split("\n")));
		for (int i = 0; i < inputConfigList.size(); i += 14) {
			int maxSub = Math.min(inputConfigList.size() - i, 13);
			String page = inputConfigList.subList(i, i + maxSub).toString().replace("]", "").replace("[", "")
					.replace(",", "\n");
			pages.add(page);
		}

		return pages;
	}

	public void addInputConfig(Material material) {
		this.inputConfig.add(material);
	}

	public static Book getBook(int frameID) {
		if (BOOKS == null) {
			BOOKS = new HashMap<>();
		}

		return BOOKS.get(frameID);
	}

	public static List<Book> getBook(List<Integer> frameIDs) {
		List<Book> tmp = new ArrayList<>();
		for (int frameID : frameIDs) {
			if (getBook(frameID) != null) {
				tmp.add(BOOKS.get(frameID));
			}
		}
		return tmp;
	}

	public static void removeBook(int frameID) {
		if (BOOKS == null) {
			BOOKS = new HashMap<>();
		}
		BOOKS.remove(frameID);
	}

	/** Read the object from Base64 string. */
	public static Object fromString(String s) throws IOException, ClassNotFoundException {
		byte[] data = Base64.getDecoder().decode(s);
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
		Object o = ois.readObject();
		ois.close();
		return o;
	}

	/** Write the object to a Base64 string. */
	public static String toString(Serializable o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}

}
