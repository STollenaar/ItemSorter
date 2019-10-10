package tollenaar.stephen.ItemSorter.Util.Server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Set;
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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import tollenaar.stephen.ItemSorter.Util.Web.HopperItems;
import tollenaar.stephen.ItemSorter.Util.Web.Ratio;

public class Book implements Serializable {

	private static final long serialVersionUID = 4519138450923555946L;
	private static transient BiMap<Integer, Book> BOOKS = HashBiMap.create(); // mapped from frameID to

	private List<Material> inputConfig = new ArrayList<>(); // input sorting configure
	private List<String> enchantments = new ArrayList<>();
	private List<String> potions = new ArrayList<>();
	private transient int frameID = -1;

	private boolean strictMode;
	private boolean preventOverflow;
	private Ratio ratio;

	public Book(int frameID) {
		this.frameID = frameID;
		this.addSelf(frameID);
	}

	public void addSelf(int frameID) {
		this.frameID = frameID;
		BOOKS.put(frameID, this);
	}

	public boolean hasInputConfig(Material material) {
		return inputConfig.contains(material);
	}

	public boolean hasEnchantment(Set<Enchantment> enchantment) {
		for (Enchantment ent : enchantment) {
			if (enchantments.contains(ent.getKey().getKey())) {
				return true;
			}
		}
		return false;
	}

	public boolean hasPotion(List<PotionEffect> potion) {
		for (PotionEffect pot : potion) {
			if (potions.contains(pot.getType().getName())) {
				return true;
			}
		}
		return false;
	}

	public List<String> toPages() {
		List<String> pages = new ArrayList<>();

		// Material filtering
		String path = "Materials";
		FileConfiguration pageBuilder = new YamlConfiguration();
		// tmp list for better formatting
		List<String> tmp = new ArrayList<>();
		for (Material material : this.inputConfig) {
			tmp.add(getDisplayName(material));
		}

		// Advance options
		pageBuilder.set("StrictMode", isStrictMode());
		pageBuilder.set("PreventOverflow", hasPreventOverflow());
		if (hasRatio()) {
			pageBuilder.set("Ratio", ratio.getFirst() + " To " + ratio.getSecond());
		} else {
			pageBuilder.set("Ratio", "None");
		}

		// Enchantment filtering
		pageBuilder.set(path, tmp);
		tmp.clear();
		path = "Enchantments";
		for (String ent : enchantments) {
			tmp.add(WordUtils.capitalizeFully(ent.replace("_", " ")));
		}

		// Potion filtering
		pageBuilder.set(path, tmp);
		tmp.clear();
		path = "Potions";
		for (String pot : potions) {
			tmp.add(WordUtils.capitalizeFully(pot.replace("_", " ")));
		}
		pageBuilder.set(path, tmp);

		List<String> inputConfigList = new ArrayList<>(Arrays.asList(pageBuilder.saveToString().split("\n")));
		for (int i = 0; i < inputConfigList.size(); i += 14) {
			int maxSub = Math.min(inputConfigList.size() - i, 13);
			String page = inputConfigList.subList(i, i + maxSub).toString().replace("]", "").replace("[", "")
					.replace(", ", "\n");
			pages.add(page);
		}

		// Preventing a book that's too big
		if (pages.size() >= 50) {
			String[] lastPage = pages.get(49).split("\n");
			lastPage[lastPage.length - 1] = "...";
			pages.set(14, String.join("\n", lastPage));
			pages = pages.subList(0, 50);
		}

		return pages;
	}

	public boolean allowItem(Inventory inventory, ItemStack item) {
		// filtering the items
		if (!hasInputConfig(item.getType())) {
			return false;
		} else if (item.getItemMeta().hasEnchants() && enchantments.size() != 0
				&& !hasEnchantment(item.getItemMeta().getEnchants().keySet())) {
			return false;
		} else if (item.getItemMeta() instanceof PotionMeta && ((PotionMeta) item.getItemMeta()).hasCustomEffects()
				&& potions.size() != 0 && !hasPotion(((PotionMeta) item.getItemMeta()).getCustomEffects())) {
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

	public void addEnchantment(Enchantment enchantment) {
		this.enchantments.add(enchantment.getKey().getKey());
	}

	public void addPotion(PotionEffectType potion) {
		this.potions.add(potion.getName());
	}

	public void emptyInputConfig() {
		this.inputConfig.clear();
		this.enchantments.clear();
		this.potions.clear();
	}

	public HopperItems toItems() {
		List<String> items = new ArrayList<>();
		for (Material material : this.inputConfig) {
			items.add(material.name().toLowerCase());
		}
		
		HopperItems hopperItems = new HopperItems(items, enchantments, potions, isStrictMode(), hasPreventOverflow(), ratio);

		return hopperItems;
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

	public void reverseRatioStep() {
		this.ratio.reverseCount();
	}

	public static Book getBook(int frameID) {
		return BOOKS.get(frameID);
	}

	public static Book getBook(String bookvalue) {
		System.out.println(BOOKS + " " + bookvalue);
		for (Book book : BOOKS.values()) {
			if (book.toString().equals(bookvalue)) {
				return book;
			}
		}
		return null;
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
