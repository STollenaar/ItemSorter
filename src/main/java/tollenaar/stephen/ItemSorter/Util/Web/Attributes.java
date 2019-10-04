package tollenaar.stephen.ItemSorter.Util.Web;

import java.util.List;

public class Attributes {

	private final List<Item> items;
	private final List<Image> containers;
	private final List<Item> enchantments;
	private final List<Item> potions;

	public Attributes(List<Item> items, List<Image> containers, List<Item> enchantments, List<Item> potions) {
		this.items = items;
		this.containers = containers;
		this.enchantments = enchantments;
		this.potions = potions;

	}

	public List<Item> getItems() {
		return items;
	}

	public List<Image> getContainers() {
		return containers;
	}

	public List<Item> getEnchantments() {
		return enchantments;
	}

	public List<Item> getPotions() {
		return potions;
	}

}
