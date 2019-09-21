package tollenaar.stephen.ItemSorter.Events;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;

import tollenaar.stephen.ItemSorter.Core.Database;
import tollenaar.stephen.ItemSorter.Util.Book;

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
			@SuppressWarnings("unchecked")
			List<Integer> frames = (List<Integer>) database.getSavedItemFrameByHopperID(hopperID, "id");
			if (!frames.isEmpty()) {
				List<Book> books = Book.getBook(frames);
				if (!books.isEmpty()) {
					for (Book book : books) {
						if (book.hasRatio() && book.isJunction(hopper)) {
							// checking the ratio and acting accordingly
							if (!book.allowedMove(event.getDestination(), hopper)) {
								event.setCancelled(true);
								junctionCancelled = true;
							} else {
								event.setCancelled(false);
								junctionCancelled = false;
								break;
							}
						}
					}
				}
			}
		}
		if (event.getDestination().getType() == InventoryType.HOPPER
				&& database.hasSavedHopper(event.getDestination().getLocation())) {
			int hopperID = (int) database.getSavedHopperByLocation(event.getDestination().getLocation(), "id");

			@SuppressWarnings("unchecked")
			List<Integer> frames = (List<Integer>) database.getSavedItemFrameByHopperID(hopperID, "id");
			if (!frames.isEmpty()) {
				List<Book> books = Book.getBook(frames);
				if (!books.isEmpty()) {
					for (Book book : books) {

						// checking the configuration
						if (!book.allowItem(event.getDestination(), event.getItem())) {
							event.setCancelled(true);
						} else if (!junctionCancelled) {
							event.setCancelled(false);
							return;
						}
					}

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

			@SuppressWarnings("unchecked")
			List<Integer> frames = (List<Integer>) database.getSavedItemFrameByHopperID(hopperID, "id");
			if (!frames.isEmpty()) {
				List<Book> books = Book.getBook(frames);
				if (!books.isEmpty()) {
					for (Book book : books) {

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
	}
}
