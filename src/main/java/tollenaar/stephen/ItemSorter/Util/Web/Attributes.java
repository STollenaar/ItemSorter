package tollenaar.stephen.ItemSorter.Util.Web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

public class Attributes {

    private final List<Item> items;
    private final List<Image> containers;
    private final List<Item> enchantments;
    private final List<Item> potions;

    public Attributes(List<Item> items, List<Image> containers) {
        Collections.sort(items, (a, b) -> {
            return a.getName().compareTo(b.getName());
        });
        this.items = items;
        this.containers = containers;

        this.enchantments = new ArrayList<>();
        for (Enchantment ent : Enchantment.values()) {
            enchantments.add(new Item(ent.hashCode(), 1, ent.getKey().getKey(),
                    WordUtils.capitalizeFully(ent.getKey().getKey().replace("_", " "))));
        }

        this.potions = new ArrayList<>();
        for (PotionEffectType pot : PotionEffectType.values()) {
            potions.add(new Item(pot.hashCode(), 1, pot.getName(),
                    WordUtils.capitalizeFully(pot.getName().replace("_", " "))));
        }
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
