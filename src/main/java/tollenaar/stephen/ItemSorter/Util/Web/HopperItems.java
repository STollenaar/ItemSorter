package tollenaar.stephen.ItemSorter.Util.Web;

import java.util.List;

public class HopperItems {
	
	private final List<String> items;
	private final List<String> enchantments;
	private final List<String> potions;
	
	private final boolean strictMode;
	private final boolean preventOverflow;
	private final Ratio ratio;
	
	public HopperItems(List<String> items,List<String> enchantments, List<String> potions, boolean strictMode, boolean preventOverflow, Ratio ratio) {
		this.items = items;
		this.enchantments = enchantments;
		this.potions = potions;
		this.strictMode = strictMode;
		this.preventOverflow = preventOverflow;
		this.ratio = ratio;
	}

	public List<String> getItems() {
		return items;
	}

	public boolean isStrictMode() {
		return strictMode;
	}

	public boolean isPreventOverflow() {
		return preventOverflow;
	}

	public Ratio getRatio() {
		return ratio;
	}

	public List<String> getEnchantments() {
		return enchantments;
	}

	public List<String> getPotions() {
		return potions;
	}
	
	

}
