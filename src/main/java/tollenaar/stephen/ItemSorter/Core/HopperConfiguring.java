package tollenaar.stephen.ItemSorter.Core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import tollenaar.stephen.ItemSorter.Util.Book;
import tollenaar.stephen.ItemSorter.Util.Frame;

public class HopperConfiguring {
	private ItemSorter plugin;
	private Database database;

	public HopperConfiguring(ItemSorter plugin) {
		this.plugin = plugin;
		this.database = plugin.getDatabase();
	}

	public void configureHopper(int frameID, UUID player, Map<String, List<String>> formParams)
			throws UnsupportedEncodingException {
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
		// serializing the book and saving as lore
		String bookValue = book.toString();
		List<String> loreList = new ArrayList<>();
		// hidden value
		loreList.add(convertToInvisibleString(bookValue));
		ItemStack replaceItem = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) replaceItem.getItemMeta();
		meta.setTitle("HopperConfiguration");
		meta.setAuthor(Bukkit.getPlayer(player).getName());
		meta.setLore(loreList);

		meta.setPages(book.toPages());

		BaseComponent[] editPage = new ComponentBuilder("To edit the configuration click here.")
				.event(new ClickEvent(ClickEvent.Action.OPEN_URL,
						plugin.getConfig().getString("URL") + plugin.getConfig().getString("editPageResponse")
								+ "?configData=" + URLEncoder.encode(bookValue, "UTF-8")))
				.create();

		meta.spigot().addPage(editPage);

		// changing the item frame item
		replaceItem.setItemMeta(meta);
		fr.setItem(replaceItem);
	}

	public void editConfigureHopper(UUID player, String bookValue, Map<String, List<String>> formParams)
			throws ClassNotFoundException, IOException {

		// loading the book materials
		Player p = Bukkit.getPlayer(player);
		Book book = Book.fromString(bookValue);
		book.emptyInputConfig();

		for (String key : formParams.keySet()) {
			String value = formParams.get(key).get(0);
			if (key.contains("input_") && Material.matchMaterial(value) != null) {
				book.addInputConfig(Material.matchMaterial(value));
			}
		}
		// serializing the book and saving as lore
		String bookValueNew = book.toString();
		List<String> loreList = new ArrayList<>();
		// hidden value
		loreList.add(convertToInvisibleString(bookValueNew));
		ItemStack replaceItem = new ItemStack(Material.WRITTEN_BOOK);

		BookMeta meta = (BookMeta) replaceItem.getItemMeta();
		meta.setTitle("HopperConfiguration");
		meta.setAuthor(Bukkit.getPlayer(player).getName());
		meta.setLore(loreList);

		meta.setPages(book.toPages());

		BaseComponent[] editPage = new ComponentBuilder("To edit the configuration click here.").event(new ClickEvent(
				ClickEvent.Action.OPEN_URL,
				plugin.getConfig().getString("URL") + plugin.getConfig().getString("editPageResponse") + "?configData="
						+ URLEncoder.encode(book.toString(), java.nio.charset.StandardCharsets.UTF_8.toString())))
				.create();

		meta.spigot().addPage(editPage);

		// changing the item frame item
		replaceItem.setItemMeta(meta);
		if (p.getInventory().getItemInMainHand().getType() == Material.WRITTEN_BOOK
				&& p.getInventory().getItemInMainHand().getItemMeta().hasLore() && p.getInventory().getItemInMainHand()
						.getItemMeta().getLore().get(0).replace("§", "").equals(bookValue)) {
			p.getInventory().setItemInMainHand(replaceItem);
		} else if (Frame.getFRAME(book.getFrameID()) != null
				&& Frame.getFRAME(book.getFrameID()).getEntityFrame() != null) {
			Frame.getFRAME(book.getFrameID()).getEntityFrame().setItem(replaceItem);
		} else {
			throw new NullPointerException("Error trying to find the edited config item");
		}
		book.addSelf(book.getFrameID());
		database.deleteEditHopper(player, bookValue);
	}

	private static String convertToInvisibleString(String s) {
		StringBuilder hidden = new StringBuilder();
		for (char c : s.toCharArray())
			hidden.append(ChatColor.COLOR_CHAR + "" + c);
		return hidden.toString();
	}

}
