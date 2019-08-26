package tollenaar.stephen.ItemSorter.Events;

import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
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
		if (event.getDestination().getType() == InventoryType.HOPPER
				&& database.hasSavedHopper(event.getDestination().getLocation())) {
			int hopperID = (int) database.getSavedHopperByLocation(event.getDestination().getLocation(), "id");
			@SuppressWarnings("unchecked")
			List<Integer> frames = (List<Integer>) database.getSavedItemFrameByHopperID(hopperID, "id");
			List<Book> books = Book.getBook(frames);
			if(books.size() != 0){
				for(Book book : books){
					if(!book.hasInputConfig(event.getItem().getType())){
						event.setCancelled(true);
					}
				}
			}
			
/*			List<String> results = database.getSignDatas(event.getDestination().getLocation());
			if(!results.contains(event.getItem().getType().name().toLowerCase())){
				event.setCancelled(true);
			}*/
		}
	}

}
