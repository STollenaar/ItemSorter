package tollenaar.stephen.ItemSorter.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import tollenaar.stephen.ItemSorter.Core.ItemSorter;

public abstract class SubCommand {

	protected ItemSorter plugin;

	public SubCommand(ItemSorter plugin) {
		this.plugin = plugin;
	}

	public abstract void onCommand(CommandSender sender, Command cmd, String label, String[] args);

	protected String convertToInvisibleString(String s) {
		StringBuilder hidden = new StringBuilder();
		for (char c : s.toCharArray())
			hidden.append(ChatColor.COLOR_CHAR + "" + c);
		return hidden.toString();
	}
}
