package tollenaar.stephen.ItemSorter.Commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import tollenaar.stephen.ItemSorter.Core.ItemSorter;
import tollenaar.stephen.ItemSorter.Util.Book;

public class Legacy extends SubCommand {

	public Legacy(ItemSorter plugin) {
		super(plugin);
	}

	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;

			if (player.getInventory().getItemInMainHand().getType() == Material.WRITTEN_BOOK
					&& player.getInventory().getItemInMainHand().getItemMeta().hasLore()) {
				ItemStack item = player.getInventory().getItemInMainHand();
				BookMeta meta = (BookMeta) item.getItemMeta();
				try {
					Book book = Book.fromString(meta.getLore().get(0).replace("§", ""));
					List<String> loreList = new ArrayList<>();
					// hidden value
					loreList.add(convertToInvisibleString(book.toString()));

					meta.setTitle("HopperConfiguration");
					meta.setAuthor("ItemSorter");
					meta.setLore(loreList);

					meta.setPages(book.toPages());

					player.getInventory().getItemInMainHand().setItemMeta(meta);

				} catch (ClassNotFoundException | IOException e) {
					player.sendMessage("This is not a correct hopperconfiguration");
				}
			} else {
				player.sendMessage("This is not a correct hopperconfiguration");
			}
		}
	}

}
