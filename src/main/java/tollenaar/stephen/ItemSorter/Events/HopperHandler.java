package tollenaar.stephen.ItemSorter.Events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.InventoryHolder;

import tollenaar.stephen.ItemSorter.Core.Database;
import tollenaar.stephen.ItemSorter.Util.Server.Book;
import tollenaar.stephen.ItemSorter.Util.Server.Frame;
import tollenaar.stephen.ItemSorter.Util.Server.Hopper;

public class HopperHandler implements Listener {

	private Database database;

	public HopperHandler(Database database) {
		this.database = database;
	}

	@EventHandler
	public void onHopperInputEvent(InventoryMoveItemEvent event) {
		boolean junctionCancelled = false;

		// flattening in case of multiple items in the same hopper inventory slot
		if (event.getSource().getLocation().getBlock().getType() == Material.HOPPER) {
			Block hopper = event.getSource().getLocation().getBlock();
			if (Hopper.isJunction(hopper)
					&& !event.getDestination().getLocation().equals(hopper.getRelative(BlockFace.DOWN).getLocation())
					&& database.hasSavedHopper(hopper.getRelative(BlockFace.DOWN).getLocation())) {

				int hopperID = (int) database.getSavedHopperByLocation(hopper.getRelative(BlockFace.DOWN).getLocation(),
						"id");
				Frame frame = database.getSavedItemFrameByHopperID(hopperID, "id");
				if (frame != null) {
					Book book = Book.getBook(frame.getId());
					// checking the configuration
					if (book != null && book.allowItem(
							((InventoryHolder) hopper.getRelative(BlockFace.DOWN).getState()).getInventory(),
							event.getItem())) {
						event.setCancelled(true);
						return;
					}
				}
			}
		}

		// checking the source and seeing if it has to abide by any ratio rules
		if (event.getSource().getLocation().getBlock().getType() == Material.HOPPER
				&& database.hasSavedHopper(event.getSource().getLocation()))

		{
			int hopperID = (int) database.getSavedHopperByLocation(event.getSource().getLocation(), "id");
			Block hopper = event.getSource().getLocation().getBlock();

			Frame frame = database.getSavedItemFrameByHopperID(hopperID, "id");
			if (frame != null) {
				Book book = Book.getBook(frame.getId());
				if (book != null && book.hasRatio() && Hopper.isJunction(hopper)) {
					// checking the ratio and acting accordingly
					if (!book.allowedMove(event.getDestination(), hopper)) {
						event.setCancelled(true);
						junctionCancelled = true;
					} else {
						event.setCancelled(false);
						junctionCancelled = false;
					}
				}
			}
		}

		// standard item filtering
		if (event.getDestination().getLocation().getBlock().getType() == Material.HOPPER
				&& database.hasSavedHopper(event.getDestination().getLocation())) {
			int hopperID = (int) database.getSavedHopperByLocation(event.getDestination().getLocation(), "id");

			Frame frame = database.getSavedItemFrameByHopperID(hopperID, "id");
			if (frame != null) {
				Book book = Book.getBook(frame.getId());
				// checking the configuration
				if (book != null && !book.allowItem(event.getDestination(), event.getItem())) {
					event.setCancelled(true);
					if (!junctionCancelled) {
						// reversing the step of the ratio
						hopperID = (int) database.getSavedHopperByLocation(event.getDestination().getLocation(), "id");

						frame = database.getSavedItemFrameByHopperID(hopperID, "id");
						if (frame != null) {
							book = Book.getBook(frame.getId());
							if (book.hasRatio()) {
								book.reverseRatioStep();
							}
						}
					}
				} else if (!junctionCancelled) {
					event.setCancelled(false);
				}
			} else {
				database.deleteHopper(event.getDestination().getLocation());
			}
		}
	}

	// this checks if the hopper can pick up an item and is not in strict mode.
	@EventHandler
	public void onHopperPickUpEvent(InventoryPickupItemEvent event) {
		if (event.getInventory().getLocation().getBlock().getType() == Material.HOPPER
				&& database.hasSavedHopper(event.getInventory().getLocation())) {
			int hopperID = (int) database.getSavedHopperByLocation(event.getInventory().getLocation(), "id");

			Frame frame = database.getSavedItemFrameByHopperID(hopperID, "id");
			if (frame != null) {
				Book book = Book.getBook(frame.getId());
				// checking the configuration
				if (book != null && book.isStrictMode()) {
					if (!book.allowItem(event.getInventory(), event.getItem().getItemStack())) {
						event.setCancelled(true);
						return;
					}
				}
			}
		}
	}
}
