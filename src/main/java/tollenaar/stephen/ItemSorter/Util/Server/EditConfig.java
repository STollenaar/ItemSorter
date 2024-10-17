package tollenaar.stephen.ItemSorter.Util.Server;

import org.bukkit.inventory.EquipmentSlot;

public class EditConfig {

	private final String bookValue;
	private final boolean hopper;
	private final EquipmentSlot slot;

	public EditConfig(String bookValue, boolean hopper, EquipmentSlot slot) {
		this.bookValue = bookValue;
		this.hopper = hopper;
		this.slot = slot;
	}

	public boolean isHopper() {
		return hopper;
	}

	public String getBookValue() {
		return bookValue;
	}

	public EquipmentSlot getSlot() {
		return slot;
	}

    @Override
	public String toString() {
		return bookValue + " " + hopper + " " + slot;
	}
}
