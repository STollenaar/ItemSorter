package tollenaar.stephen.ItemSorter.Events;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;

import tollenaar.stephen.ItemSorter.Core.Database;
import tollenaar.stephen.ItemSorter.Util.Book;
import tollenaar.stephen.ItemSorter.Util.Frame;

public class HopperHandler implements Listener {

	private Database database;

	public HopperHandler(Database database) {
		this.database = database;
	}

	@EventHandler
	public void onHopperInputEvent(InventoryMoveItemEvent event) {
		boolean junctionCancelled = false;
		if (event.getSource().getType() == InventoryType.HOPPER
				&& database.hasSavedHopper(event.getSource().getLocation()))

		{
			int hopperID = (int) database.getSavedHopperByLocation(event.getSource().getLocation(), "id");
			Block hopper = event.getSource().getLocation().getBlock();

			Frame frame = database.getSavedItemFrameByHopperID(hopperID, "id");
			if (frame != null) {
				Book book = Book.getBook(frame.getHopperID());
				if (book.hasRatio() && book.isJunction(hopper)) {
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
		if (event.getDestination().getType() == InventoryType.HOPPER
				&& database.hasSavedHopper(event.getDestination().getLocation())) {
			int hopperID = (int) database.getSavedHopperByLocation(event.getDestination().getLocation(), "id");

			Frame frame = database.getSavedItemFrameByHopperID(hopperID, "id");
			if (frame != null) {
				Book book = Book.getBook(frame.getHopperID());

				// checking the configuration
				if (!book.allowItem(event.getDestination(), event.getItem())) {
					event.setCancelled(true);
					if (!junctionCancelled) {
						//reversing the step of the ratio
						hopperID = (int) database.getSavedHopperByLocation(event.getSource().getLocation(), "id");

						frame = database.getSavedItemFrameByHopperID(hopperID, "id");
						if (frame != null) {
							book = Book.getBook(frame.getHopperID());
							book.reverseRatioStep();
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

	@EventHandler
	public void onHopperPickUpEvent(InventoryPickupItemEvent event) {
		if (event.getInventory().getType() == InventoryType.HOPPER
				&& database.hasSavedHopper(event.getInventory().getLocation())) {
			int hopperID = (int) database.getSavedHopperByLocation(event.getInventory().getLocation(), "id");

			Frame frame = database.getSavedItemFrameByHopperID(hopperID, "id");
			if (frame != null) {
				Book book = Book.getBook(frame.getHopperID());

				// checking the configuration
				if (book.isStrictMode()) {
					if (!book.allowItem(event.getInventory(), event.getItem().getItemStack())) {
						event.setCancelled(true);
						return;
					}
				}
			}

		}
	}
}
