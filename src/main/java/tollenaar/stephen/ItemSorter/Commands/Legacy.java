package tollenaar.stephen.ItemSorter.Commands;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
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
					meta.setAuthor(player.getName());
					meta.setLore(loreList);

					meta.setPages(book.toPages());

					BaseComponent[] editPage = new ComponentBuilder(
							"To edit the configuration click here.")
									.event(new ClickEvent(ClickEvent.Action.OPEN_URL,
											plugin.getConfig().getString("URL")
													+ plugin.getConfig().getString("editPageResponse") + "?configData="
													+ URLEncoder.encode(book.toString(),
															java.nio.charset.StandardCharsets.UTF_8.toString())))
									.create();

					meta.spigot().addPage(editPage);
					player.getInventory().getItemInMainHand().setItemMeta(meta);

				} catch (ClassNotFoundException | IOException e) {
					player.sendMessage("This is not a correct hopperconfiguration");
				}
			}else {
				player.sendMessage("This is not a correct hopperconfiguration");
			}
		}
	}

}
