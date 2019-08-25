package tollenaar.stephen.ItemSorter.Events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;

import tollenaar.stephen.ItemSorter.Core.Database;

public class HopperHandler implements Listener {

	private Database database;

	public HopperHandler(Database database) {
		this.database = database;
	}

	@EventHandler
	public void onHopperInputEvent(InventoryMoveItemEvent event) {
		if (event.getDestination().getType() == InventoryType.HOPPER
				&& database.hasSavedHopper(event.getDestination().getLocation())) {
/*			List<String> results = database.getSignDatas(event.getDestination().getLocation());
			if(!results.contains(event.getItem().getType().name().toLowerCase())){
				event.setCancelled(true);
			}*/
		}
	}

}
