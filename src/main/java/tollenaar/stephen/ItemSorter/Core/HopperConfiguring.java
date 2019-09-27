package tollenaar.stephen.ItemSorter.Core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import tollenaar.stephen.ItemSorter.Util.Book;
import tollenaar.stephen.ItemSorter.Util.EditConfig;
import tollenaar.stephen.ItemSorter.Util.Frame;

public class HopperConfiguring {
	private Database database;

	public HopperConfiguring(ItemSorter plugin) {
		this.database = plugin.getDatabase();
	}

	public void configureHopper(int frameID, UUID player, Map<String, List<String>> formParams)
			throws UnsupportedEncodingException, IllegalArgumentException {
		Frame frame = Frame.getFRAME(frameID);
		ItemFrame fr = frame.getEntityFrame();

		if (fr == null) {
			throw new NullPointerException("Error trying to find the edited config item");
		}

		// loading the book materials
		Book book = new Book(frameID);

		for (String key : formParams.keySet()) {
			String value = formParams.get(key).get(0);
			if (key.contains("input_") && Material.matchMaterial(value) != null) {
				book.addInputConfig(Material.matchMaterial(value));
			}
		}
		if (formParams.keySet().contains("strict_mode")) {
			book.setStrictMode(true);
		} else {
			book.setStrictMode(false);
		}
		if (formParams.keySet().contains("prevent_overflow")) {
			book.setPreventOverflow(true);
		} else {
			book.setPreventOverflow(false);
		}
		if (formParams.keySet().contains("junction_ratio")) {
			int first = Integer.parseInt(formParams.get("firstRatio").get(0));
			int second = Integer.parseInt(formParams.get("secondRatio").get(0));

			if (first <= 0 || second <= 0) {
				throw new IllegalArgumentException("Can't have a 0 or negative ratio");
			}

			book.setRatio(first, second);
		} else {
			book.emptyRatio();
		}

		// serializing the book and saving as lore
		String bookValue = book.toString();
		List<String> loreList = new ArrayList<>();
		// hidden value
		loreList.add(convertToInvisibleString(bookValue));
		ItemStack replaceItem = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) replaceItem.getItemMeta();
		meta.setTitle("HopperConfiguration");
		meta.setAuthor("ItemSorter");
		meta.setLore(loreList);

		meta.setPages(book.toPages());

		// changing the item frame item
		replaceItem.setItemMeta(meta);
		fr.setItem(replaceItem);
	}

	public void editConfigureHopper(UUID player, String bookValue, Map<String, List<String>> formParams)
			throws ClassNotFoundException, IOException, NullPointerException, NumberFormatException,
			IllegalArgumentException {

		// loading the book materials
		Player p = Bukkit.getPlayer(player);
		EditConfig editConfig = database.getSavedEdit(player);
		Book book = Book.getBook(bookValue);
		if (book == null) {
			book = Book.fromString(bookValue);
		}

		book.emptyInputConfig();
		for (String key : formParams.keySet()) {
			String value = formParams.get(key).get(0);
			if (key.contains("input_") && Material.matchMaterial(value) != null) {
				book.addInputConfig(Material.matchMaterial(value));
			}
		}
		if (formParams.keySet().contains("strict_mode")) {
			book.setStrictMode(true);
		} else {
			book.setStrictMode(false);
		}
		if (formParams.keySet().contains("prevent_overflow")) {
			book.setPreventOverflow(true);
		} else {
			book.setPreventOverflow(false);
		}
		if (formParams.keySet().contains("junction_ratio")) {
			int first = Integer.parseInt(formParams.get("firstRatio").get(0));
			int second = Integer.parseInt(formParams.get("secondRatio").get(0));

			if (first <= 0 || second <= 0) {
				throw new IllegalArgumentException("Can't have a 0 or negative ratio");
			}

			book.setRatio(first, second);
		} else {
			book.emptyRatio();
		}

		// serializing the book and saving as lore
		String bookValueNew = book.toString();
		List<String> loreList = new ArrayList<>();
		// hidden value
		loreList.add(convertToInvisibleString(bookValueNew));
		ItemStack replaceItem = new ItemStack(Material.WRITTEN_BOOK);

		BookMeta meta = (BookMeta) replaceItem.getItemMeta();
		meta.setTitle("HopperConfiguration");
		meta.setAuthor("ItemSorter");
		meta.setLore(loreList);

		meta.setPages(book.toPages());

		// changing the item frame item
		replaceItem.setItemMeta(meta);
		if (!editConfig.isHopper() && p.getInventory().getItem(editConfig.getSlot()).getType() == Material.WRITTEN_BOOK
				&& p.getInventory().getItem(editConfig.getSlot()).getItemMeta().hasLore()
				&& p.getInventory().getItem(editConfig.getSlot()).getItemMeta().getLore().get(0).replace("§", "")
						.equals(bookValue)) {
			p.getInventory().getItem(editConfig.getSlot()).setItemMeta(meta);
		} else if (editConfig.isHopper() && Frame.getFRAME(book.getFrameID()) != null
				&& Frame.getFRAME(book.getFrameID()).getEntityFrame() != null) {
			Frame.getFRAME(book.getFrameID()).getEntityFrame().setItem(replaceItem);
		} else {
			throw new NullPointerException("Error trying to find the edited config item");
		}
		book.addSelf(book.getFrameID());
		database.deleteEditHopper(player);
	}

	private static String convertToInvisibleString(String s) {
		StringBuilder hidden = new StringBuilder();
		for (char c : s.toCharArray())
			hidden.append(ChatColor.COLOR_CHAR + "" + c);
		return hidden.toString();
	}
}
