package tollenaar.stephen.ItemSorter.Events;

import java.io.IOException;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
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

import io.netty.buffer.Unpooled;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.MinecraftKey;
import net.minecraft.server.v1_14_R1.PacketDataSerializer;
import net.minecraft.server.v1_14_R1.PacketPlayOutCustomPayload;
import tollenaar.stephen.ItemSorter.Core.Database;
import tollenaar.stephen.ItemSorter.Core.ItemSorter;
import tollenaar.stephen.ItemSorter.Util.Book;
import tollenaar.stephen.ItemSorter.Util.Hologram;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;

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
			if (frame.isEmpty()) {
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

								// String name = b.toItems().toString().replace("[", "").replace("]", "");
								// new Hologram(event.getRightClicked(), name);
							} catch (ClassNotFoundException | IOException e) {
								// TODO handle/explain exception
							}
						}
					}
				}
				// forcing the book to open to the player
			} else if (frame.getItem().getType() == Material.WRITTEN_BOOK) {
				BookMeta meta = (BookMeta) frame.getItem().getItemMeta();
				System.out.println(meta.hasLore());
				if (meta.hasLore()) {
					String bookValue = meta.getLore().get(0).replace("§", "");
					try {
						Book b = (Book) Book.fromString(bookValue);
						// opening the book
						Player player = event.getPlayer();
						int slot = player.getInventory().getHeldItemSlot();
						ItemStack old = player.getInventory().getItem(slot);
						player.getInventory().setItem(slot, frame.getItem());
						PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload(
							MinecraftKey.a("minecraft:open_book"),
							new PacketDataSerializer(Unpooled.buffer()).a(EnumHand.MAIN_HAND));
						((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
						player.getInventory().setItem(slot, old);
						event.setCancelled(true);
					} catch (ClassNotFoundException | IOException e) {
						e.printStackTrace();
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
			if (Hologram.hologramExistsAtLocation(event.getEntity().getLocation())) {
				Hologram.removeHologramAtLocation(event.getEntity().getLocation());
			}
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
			if (Hologram.hologramExistsAtLocation(event.getEntity().getLocation())) {
				Hologram.removeHologramAtLocation(event.getEntity().getLocation());
			}
		}
	}

	@EventHandler
	public void onHopperEditEvent(PlayerInteractEvent event) {
		if (event.getItem() != null && event.getItem().getType() == Material.WRITTEN_BOOK
			&& event.getItem().getItemMeta().getLore().size() == 1) {
			try {
				Book book = (Book) Book.fromString(event.getItem().getItemMeta().getLore().get(0).replaceAll("§", ""));
				database.savePlayer(event.getPlayer().getUniqueId(), book.toString());
			} catch (ClassNotFoundException | IOException e) {
			}
		}
	}
}
