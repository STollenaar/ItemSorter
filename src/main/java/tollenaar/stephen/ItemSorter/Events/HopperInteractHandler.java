package tollenaar.stephen.ItemSorter.Events;

import java.io.IOException;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.BookMeta;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import tollenaar.stephen.ItemSorter.Core.Database;
import tollenaar.stephen.ItemSorter.Core.ItemSorter;
import tollenaar.stephen.ItemSorter.Util.Book;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;

public class HopperInteractHandler implements Listener {
	private Database database;
	private ItemSorter plugin;

	public HopperInteractHandler(ItemSorter plugin) {
		this.plugin = plugin;
		this.database = this.plugin.database;
	}

	// handling configuring of the frames
	@EventHandler
	public void onHopperConfigEvent(PlayerInteractEntityEvent event) {
		if (event.getRightClicked().getType() == EntityType.ITEM_FRAME
				&& database.hasSavedItemFrame(event.getRightClicked().getLocation())) {

			ItemFrame frame = (ItemFrame) event.getRightClicked();
			int frameID = (int) database.getSavedItemFrameByLocation(event.getRightClicked().getLocation(), "id");
			if (frame.getItem().getType() == Material.AIR) {
				if (event.getPlayer().getInventory().getItemInMainHand() != null) {
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
					} else if (event.getPlayer().getInventory().getItemInMainHand()
							.getType() == Material.WRITTEN_BOOK) {
						BookMeta meta = (BookMeta) event.getPlayer().getInventory().getItemInMainHand().getItemMeta();
						if (meta.hasLore()) {
							String bookValue = meta.getLore().get(0).replace("§", "");
							try {
								Book b = (Book) Book.fromString(bookValue);
								b.addSelf(frameID);

							} catch (ClassNotFoundException | IOException e) {
								// no need for logging, if the item is a written book and has lore, but is not
								// from this plugin it can throw this error.
								return;
							}
						}
					}
				}
				// forcing the book to open to the player
			} else if (frame.getItem().getType() == Material.WRITTEN_BOOK) {
				BookMeta meta = (BookMeta) frame.getItem().getItemMeta();
				if (meta.hasLore()) {
					String bookValue = meta.getLore().get(0).replace("§", "");
					try {
						Book.fromString(bookValue);
						// opening the book
						event.getPlayer().openBook(frame.getItem());
						database.savePlayer(event.getPlayer().getUniqueId(), bookValue);
						event.setCancelled(true);
					} catch (ClassNotFoundException | IOException e) {
						// no need for logging, if the item is a written book and has lore, but is not
						// from this plugin it can throw this error.
						return;
					}
				}
			}
		}
	}

	// handling item frame place event
	@EventHandler
	public void onItemFramePlaceEvent(HangingPlaceEvent event) {
		if (event.getEntity().getType() == EntityType.ITEM_FRAME) {

			// filtering to correct one
			if (event.getBlock().getType() == Material.HOPPER) {
				database.saveHoppers(event.getBlock().getLocation(), event.getEntity().getLocation());
			}
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
		if (event.getItem() != null && event.getItem().getType() == Material.WRITTEN_BOOK
				&& event.getItem().getItemMeta().hasLore()) {
			try {
				Book book = (Book) Book.fromString(event.getItem().getItemMeta().getLore().get(0).replace("§", ""));
				database.savePlayer(event.getPlayer().getUniqueId(), book.toString());
			} catch (ClassNotFoundException | IOException e) {
				// no need for logging, if the item is a written book and has lore, but is not
				// from this plugin it can throw this error.
				return;
			}
		}
	}
}
