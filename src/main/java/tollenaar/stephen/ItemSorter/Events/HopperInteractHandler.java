package tollenaar.stephen.ItemSorter.Events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.BookMeta;

import tollenaar.stephen.ItemSorter.Core.Database;
import tollenaar.stephen.ItemSorter.Core.ItemSorter;

import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
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
		System.out.println(event.getPlayer().getItemInHand().getType());
		if (event.getPlayer().getItemInHand() != null
				&& event.getPlayer().getItemInHand().getType() == Material.WRITABLE_BOOK
				&& event.getRightClicked().getType() == EntityType.ITEM_FRAME
				&& ((ItemFrame) event.getRightClicked()).isEmpty()
				&& database.getSavedItemFrame(event.getRightClicked().getLocation())) {
			database.saveFrames(event.getRightClicked().getLocation());

			event.getPlayer().sendMessage("Go to " + plugin.getConfig().getString("URL") + ":"
					+ plugin.getConfig().getInt("port") + "/" + plugin.getConfig().getString("initialPageResponse")
					+ "?userCode=" + event.getPlayer().getUniqueId().toString() + " to configure the hopper sorting");
		}
	}

	// handling item frame place event
	@EventHandler
	public void onItemFramePlaceEvent(HangingPlaceEvent event) {
		System.out.println(event.getEntity());
		System.out.println(event.getBlock());
		if (event.getEntity().getType() == EntityType.ITEM_FRAME) {

			// filtering to correct one
			if (event.getBlock().getType() == Material.HOPPER) {
				database.saveHoppers(event.getBlock().getLocation(), event.getEntity().getLocation());
			}

		}
	}

	// handling the sign place events
	public void onSignPlaceEvent(SignChangeEvent event) {

		if (!(event.getBlock().getState().getBlockData() instanceof WallSign)) {
			return;
		}

		Sign sign = (Sign) event.getBlock().getState();
		WallSign w = (WallSign) event.getBlock().getState().getBlockData();

		Block attachedBlock = event.getBlock().getRelative(w.getFacing().getOppositeFace());

		// filtering to correct one
		if (attachedBlock.getType() == Material.HOPPER) {
			database.saveHoppers(attachedBlock.getLocation(), sign.getLocation());
		}
	}

	// handling the hopper break
	@EventHandler
	public void onHopperBreakEvent(BlockBreakEvent event) {
		if (event.getBlock().getType() == Material.HOPPER && database.getSavedHopper(event.getBlock().getLocation())) {
			database.deleteHopper(event.getBlock().getLocation());
		}
	}

	// handling the item frame break event

	@EventHandler
	public void onFrameBreakEvent(HangingBreakEvent event) {
		if (database.getSavedItemFrame(event.getEntity().getLocation())) {
			database.deleteFrame(event.getEntity().getLocation());
		}
	}

	// handling for when the player removes an item out of the item frame
	@EventHandler
	public void onItemRemoveEvent(EntityDamageByEntityEvent event) {
		if (event.getDamager().getType() == EntityType.PLAYER && event.getEntityType() == EntityType.ITEM_FRAME
				&& database.getSavedItemFrame(event.getEntity().getLocation())) {
			database.deleteFrame(event.getEntity().getLocation());
		}
	}
}
