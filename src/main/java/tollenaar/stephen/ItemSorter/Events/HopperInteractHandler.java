package tollenaar.stephen.ItemSorter.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import tollenaar.stephen.ItemSorter.Core.Database;
import tollenaar.stephen.ItemSorter.Core.ItemSorter;
import tollenaar.stephen.ItemSorter.Util.Book;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;

public class HopperInteractHandler implements Listener {
	private Database database;
	private ItemSorter plugin;

	public HopperInteractHandler(ItemSorter plugin) {
		this.plugin = plugin;
		this.database = this.plugin.getDatabase();
	}

	// handling configuring of the frames
	@EventHandler
	public void onHopperInteractEvent(PlayerInteractEntityEvent event) {

		if (event.getRightClicked().getType() == EntityType.ITEM_FRAME
				&& database.hasSavedItemFrame(event.getRightClicked().getLocation())) {

			ItemFrame frame = (ItemFrame) event.getRightClicked();
			int frameID = (int) database.getSavedItemFrameByLocation(event.getRightClicked().getLocation(), "id");

			if (frame.getItem().getType() == Material.AIR) {
				// getting to configure
				if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.WRITABLE_BOOK) {
					database.savePlayer(event.getPlayer().getUniqueId(), frameID);

					String url = plugin.getConfig().getString("URL")
							+ plugin.getConfig().getString("initialPageResponse") + "?userCode="
							+ event.getPlayer().getUniqueId().toString() + "&frameID=" + frameID;

					TextComponent text = new TextComponent("Click here to configure the hopper sorting");
					text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
					event.getPlayer().sendMessage(text);

					// when replacing a configured book into an item frame
				} else if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.WRITTEN_BOOK) {
					BookMeta meta = (BookMeta) event.getPlayer().getInventory().getItemInMainHand().getItemMeta();
					if (meta.hasLore()) {
						String bookValue = meta.getLore().get(0).replace("§", "");
						try {
							Book b = Book.fromString(bookValue);
							b.addSelf(frameID);

						} catch (ClassNotFoundException | IOException e) {
							// no need for logging, if the item is a written
							// book and has lore, but is not
							// from this plugin it can throw this error.
						}
					}
				}
				// forcing the book to open to the player
			} else if (frame.getItem().getType() == Material.WRITTEN_BOOK && frame.getItem().getItemMeta().hasLore()) {
				try {
					ItemStack item = frame.getItem();
					Player player = event.getPlayer();
					Book book = Book.fromString(item.getItemMeta().getLore().get(0).replace("§", ""));

					ItemStack replaceItem = new ItemStack(Material.WRITTEN_BOOK);
					BookMeta meta = (BookMeta) replaceItem.getItemMeta();
					List<String> loreList = new ArrayList<>();
					// hidden value
					loreList.add(convertToInvisibleString(book.toString()));
					meta.setTitle("HopperConfiguration");
					meta.setAuthor("ItemSorter");
					meta.setLore(loreList);

					meta.setPages(book.toPages());

					BaseComponent[] editPage = new ComponentBuilder("To edit the configuration click here.")
							.event(new ClickEvent(ClickEvent.Action.OPEN_URL,
									plugin.getConfig().getString("URL")
											+ plugin.getConfig().getString("editPageResponse") + "?configData="
											+ event.getPlayer().getUniqueId().toString()))
							.create();
					meta.spigot().addPage(editPage);
					replaceItem.setItemMeta(meta);
					player.openBook(replaceItem);
					
					database.savePlayer(player.getUniqueId(), book.toString(), true, null);
					event.setCancelled(true);
				} catch (ClassNotFoundException | IOException e) {
					// no need for logging, if the item is a written book
					// and has lore, but is not
					// from this plugin it can throw this error.
				}
			}
		}
	}

	private static String convertToInvisibleString(String s) {
		StringBuilder hidden = new StringBuilder();
		for (char c : s.toCharArray())
			hidden.append(ChatColor.COLOR_CHAR + "" + c);
		return hidden.toString();
	}

	// handling item frame place event
	@EventHandler
	public void onItemFramePlaceEvent(HangingPlaceEvent event) {
		if (event.getEntity().getType() == EntityType.ITEM_FRAME && event.getBlock().getType() == Material.HOPPER) {
			database.saveHoppers(event.getBlock().getLocation(), event.getEntity().getLocation());
		}
	}

	// handling the hopper break
	@EventHandler
	public void onHopperBreakEvent(BlockBreakEvent event) {
		if (event.getBlock().getType() == Material.HOPPER && database.hasSavedHopper(event.getBlock().getLocation())) {
			database.deleteHopper(event.getBlock().getLocation());
		}
	}

	// handling the item frame break event

	@EventHandler
	public void onFrameBreakEvent(HangingBreakEvent event) {
		if (database.hasSavedItemFrame(event.getEntity().getLocation())) {
			database.deleteFrame(event.getEntity().getLocation());
		}
	}

	// handling for when the player removes an item out of the item frame
	@EventHandler
	public void onItemRemoveEvent(EntityDamageByEntityEvent event) {

		// filtering player and item frame
		if (event.getDamager().getType() == EntityType.PLAYER && event.getEntityType() == EntityType.ITEM_FRAME
				&& database.hasSavedItemFrame(event.getEntity().getLocation())) {

			if (database.hasSavedPlayerWithItemFrame(event.getDamager().getUniqueId(),
					(int) database.getSavedItemFrameByLocation(event.getEntity().getLocation(), "id"))) {
				database.deletePlayerWithFrame(event.getDamager().getUniqueId(),
						(int) database.getSavedItemFrameByLocation(event.getEntity().getLocation(), "id"));
			}

			Book.removeBook((int) database.getSavedItemFrameByLocation(event.getEntity().getLocation(), "id"));
		}
	}

	@EventHandler
	public void onHopperEditEvent(PlayerInteractEvent event) {
		if (event.getItem().getType() == Material.WRITTEN_BOOK && event.getItem().getItemMeta().hasLore()) {
			try {
				ItemStack item = event.getItem();
				Player player = event.getPlayer();
				Book book = Book.fromString(item.getItemMeta().getLore().get(0).replace("§", ""));
				BookMeta meta = (BookMeta) item.getItemMeta();
				meta.setPages(book.toPages());

				BaseComponent[] editPage = new ComponentBuilder("To edit the configuration click here.")
						.event(new ClickEvent(ClickEvent.Action.OPEN_URL,
								plugin.getConfig().getString("URL") + plugin.getConfig().getString("editPageResponse")
										+ "?configData=" + player.getUniqueId().toString()))
						.create();

				meta.spigot().addPage(editPage);
				item.setItemMeta(meta);
				player.getInventory().setItem(event.getHand(), item);
				database.savePlayer(event.getPlayer().getUniqueId(), book.toString(), false, event.getHand());
			} catch (ClassNotFoundException | IOException e) {
				// no need for logging, if the item is a written book and has
				// lore, but is not
				// from this plugin it can throw this error.
			}
		}
	}
}
