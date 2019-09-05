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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import tollenaar.stephen.ItemSorter.Util.Book;
import tollenaar.stephen.ItemSorter.Util.Frame;

public class HopperConfiguring {
	private ItemSorter plugin;

	public HopperConfiguring(ItemSorter plugin) {
		this.plugin = plugin;
	}

	public void configureHopper(int frameID, UUID player, Map<String, List<String>> formParams) throws UnsupportedEncodingException {
		Frame frame = Frame.getFRAME(frameID);
		Location frameLoc = new Location(
			Bukkit.getWorld(frame.getWorld()), frame.getX(), frame.getY(), frame.getZ(),
			frame.getYaw(), frame.getPitch()
		);

		for (Entity ent : frameLoc.getChunk().getEntities()) {
			// getting the right item frame
			if (ent instanceof ItemFrame && ent.getLocation().equals(frameLoc)) {
				ItemFrame fr = (ItemFrame) ent;

				// loading the book materials
				Book book = new Book(frameID);

				for (String key : formParams.keySet()) {
					String value = formParams.get(key).get(0);
					if (key.contains("input_")) {
						if (Material.matchMaterial(value) != null) {
							book.addInputConfig(Material.matchMaterial(value));
						}
					}
				}
				// serializing the book and saving as lore
				String bookValue = book.toString();
				List<String> loreList = new ArrayList<String>();
				// hidden value
				loreList.add(convertToInvisibleString(bookValue));
				ItemStack replaceItem = new ItemStack(Material.WRITTEN_BOOK);
				BookMeta meta = (BookMeta) replaceItem.getItemMeta();
				meta.setTitle("HopperConfiguration");
				meta.setAuthor(Bukkit.getPlayer(player).getName());
				meta.setLore(loreList);

				meta.setPages(book.toPages());

				BaseComponent[] editPage = new ComponentBuilder("To edit the configuration click here.")
					.event(new ClickEvent(ClickEvent.Action.OPEN_URL, plugin.getConfig().getString("URL")
						+ plugin.getConfig().getString("editPageResponse") + "?configData=" + URLEncoder.encode(bookValue, "UTF-8")))
					.create();

				meta.spigot().addPage(editPage);

				// changing the item frame item
				replaceItem.setItemMeta(meta);
				fr.setItem(replaceItem);

				break;
			}
		}

	}

	public void editConfigureHopper(UUID player,String bookValue, Map<String, List<String>> formParams) throws ClassNotFoundException, IOException {
		if(Bukkit.getPlayer(player).getInventory().getItemInMainHand() == null || Bukkit.getPlayer(player).getInventory().getItemInMainHand().getType() != Material.WRITTEN_BOOK ||
			!Bukkit.getPlayer(player).getInventory().getItemInMainHand().getItemMeta().getLore().get(0).replaceAll("§", "").equals(bookValue)){
			throw new NullPointerException("Not found correct item in hand");
		}

		// loading the book materials
		Book book = (Book) Book.fromString(bookValue);
		book.emptyInputConfig();

		for (String key : formParams.keySet()) {
			String value = formParams.get(key).get(0);
			if (key.contains("input_")) {
				if (Material.matchMaterial(value) != null) {
					book.addInputConfig(Material.matchMaterial(value));
				}
			}
		}
		// serializing the book and saving as lore
		String bookValueNew = book.toString();
		List<String> loreList = new ArrayList<String>();
		// hidden value
		loreList.add(convertToInvisibleString(bookValueNew));
		ItemStack replaceItem = Bukkit.getPlayer(player).getInventory().getItemInMainHand();
		BookMeta meta = (BookMeta) replaceItem.getItemMeta();
		meta.setLore(loreList);

		meta.setPages(book.toPages());

		@SuppressWarnings("deprecation")
		BaseComponent[] editPage = new ComponentBuilder("To edit the configuration click here.")
			.event(new ClickEvent(ClickEvent.Action.OPEN_URL, plugin.getConfig().getString("URL")
				+ plugin.getConfig().getString("editPageResponse") + "?configData=" + URLEncoder.encode(bookValue)))
			.create();

		meta.spigot().addPage(editPage);

		// changing the item frame item
		replaceItem.setItemMeta(meta);

	}

	private static String convertToInvisibleString(String s) {
		String hidden = "";
		for (char c : s.toCharArray())
			hidden += ChatColor.COLOR_CHAR + "" + c;
		return hidden;
	}
}
