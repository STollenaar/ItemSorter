package tollenaar.stephen.ItemSorter.Events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.BookMeta;

import tollenaar.stephen.ItemSorter.Core.Database;

import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;

public class SignHandler implements Listener {
	private Database database;

	public SignHandler(Database database) {
		this.database = database;
	}

	// handling configuring of the signs
	@EventHandler
	public void onSignConfigEvent(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem() != null
				&& event.getItem().getType() == Material.WRITTEN_BOOK
				&& event.getClickedBlock().getType().name().toLowerCase().contains("sign")
				&& database.getSavedSign(event.getClickedBlock().getLocation())) {
			database.saveSigns(event.getClickedBlock().getLocation(),
					((BookMeta) event.getItem().getItemMeta()).getPages());
		}
	}

	// handling the sign place events
	@EventHandler
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

	// handling the block break events
	@EventHandler
	public void onSignOrHopperBreakEvent(BlockBreakEvent event) {
		if (event.getBlock().getType().name().toLowerCase().contains("sign")
				&& database.getSavedSign(event.getBlock().getLocation())) {
			database.deleteSign(event.getBlock().getLocation());
		} else if (event.getBlock().getType() == Material.HOPPER
				&& database.getSavedHopper(event.getBlock().getLocation())) {
			database.deleteHopper(event.getBlock().getLocation());
		}
	}
}
