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
import java.util.logging.Level;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.data.type.Hopper;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Book implements Serializable {

	private static final long serialVersionUID = 4519138450923555946L;
	private static transient Map<Integer, Book> BOOKS; // mapped from frameID to

	private List<Material> inputConfig = new ArrayList<>(); // input sorting configure
	private int frameID;
	private boolean strictMode;
	private boolean preventOverflow;
	private Ratio ratio;

	public Book(int frameID) {
		this.frameID = frameID;
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
		List<String> pages = new ArrayList<>();

		String path = "InputConfig";
		FileConfiguration pageBuilder = new YamlConfiguration();
		// tmp list for better formatting
		List<String> tmp = new ArrayList<>();
		for (Material material : this.inputConfig) {
			tmp.add(getDisplayName(material));
		}
		pageBuilder.set("StrictMode", isStrictMode());
		pageBuilder.set("PreventOverflow", hasPreventOverflow());
		if (hasRatio()) {
			pageBuilder.set("Ratio", ratio.getFirst() + " To " + ratio.getSecond());
		}else {
			pageBuilder.set("Ratio", "None");
		}

		pageBuilder.set(path, tmp);

		List<String> inputConfigList = new ArrayList<>(Arrays.asList(pageBuilder.saveToString().split("\n")));
		for (int i = 0; i < inputConfigList.size(); i += 14) {
			int maxSub = Math.min(inputConfigList.size() - i, 13);
			String page = inputConfigList.subList(i, i + maxSub).toString().replace("]", "").replace("[", "")
					.replace(", ", "\n");
			pages.add(page);
		}

		// preventing a book that's too big
		if (pages.size() >= 15) {
			String[] lastPage = pages.get(14).split("\n");
			lastPage[lastPage.length - 1] = "...";
			pages.set(14, String.join("\n", lastPage));
			pages = pages.subList(0, 15);
		}

		return pages;
	}

	public boolean allowItem(Inventory inventory, ItemStack item) {
		// filtering the items
		if (!hasInputConfig(item.getType())) {
			return false;
		}
		// prevents accepting an item if the next container in the system is full
		if (hasPreventOverflow()) {
			Block hopper = inventory.getLocation().getBlock();
			BlockFace facing = ((Hopper) hopper.getBlockData()).getFacing();
			Block target = hopper.getRelative(facing);
			if (target instanceof Container) {
				Container t = (Container) target;
				if (!hasRoom(t.getInventory(), item)) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}

	public boolean isJunction(Block hopper) {
		BlockFace facing = ((Hopper) hopper.getBlockData()).getFacing();
		if (facing != BlockFace.DOWN) {
			return hopper.getRelative(BlockFace.DOWN).getType() == Material.HOPPER
					|| hopper.getRelative(BlockFace.DOWN).getType() == Material.HOPPER_MINECART;
		}
		return false;
	}

	private boolean hasRoom(Inventory inventory, ItemStack item) {
		return Arrays.stream(inventory.getStorageContents())
				.anyMatch(itemStack -> itemStack == null || item.getAmount() < itemStack.getMaxStackSize());
	}

	// sorting the correct move detail
	public boolean allowedMove(Inventory destination, Block hopper) {
		BlockFace facing = ((Hopper) hopper.getBlockData()).getFacing();
		if (ratio.isFirstSelector() && hopper.getRelative(facing).getLocation().equals(destination.getLocation())) {
			ratio.addCount();
			return true;
		} else if (!ratio.isFirstSelector()
				&& hopper.getRelative(BlockFace.DOWN).getLocation().equals(destination.getLocation())) {
			ratio.addCount();
			return true;
		} else {
			return false;
		}
	}

	private String getDisplayName(Material material) {
		return WordUtils.capitalizeFully(material.name().toLowerCase().replace("_", " "));
	}

	public void addInputConfig(Material material) {
		this.inputConfig.add(material);
	}

	public void emptyInputConfig() {
		this.inputConfig = new ArrayList<>();
	}

	public List<String> toItems() {
		List<String> items = new ArrayList<>();
		for (Material material : this.inputConfig) {
			items.add(material.name().toLowerCase());
		}
		if (hasPreventOverflow()) {
			items.add("prevent_overflow");
		}
		if (isStrictMode()) {
			items.add("strict_mode");
		}
		if (hasRatio()) {
			items.add("junction_ratio");
			items.add(Integer.toString(ratio.getFirst()));
			items.add(Integer.toString(ratio.getSecond()));
		}
		System.out.println(items);
		return items;
	}

	/** Write the object to a Base64 string. */
	public String toString() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			oos.close();
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, e.toString());
		}
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}

	public int getFrameID() {
		return frameID;
	}

	public boolean isStrictMode() {
		return strictMode;
	}

	public void setStrictMode(boolean strictMode) {
		this.strictMode = strictMode;
	}

	public boolean hasPreventOverflow() {
		return preventOverflow;
	}

	public void setPreventOverflow(boolean preventOverflow) {
		this.preventOverflow = preventOverflow;
	}

	public void setRatio(int first, int second) {
		this.ratio = new Ratio(first, second);
	}

	public void emptyRatio() {
		this.ratio = null;
	}

	public boolean hasRatio() {
		return this.ratio != null;
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
	public static Book fromString(String s) throws IOException, ClassNotFoundException {
		byte[] data = Base64.getDecoder().decode(s);
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
		Object o = ois.readObject();
		ois.close();
		return (Book) o;
	}

}
