package me.plasmarob.legendcraft;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class LegendCraftTabCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player)sender;

			//alias /lca for /lc add 
			if (commandLabel.equals("lca")) {
				String[] argsOld = args.clone();
				args = new String[argsOld.length+1];
				args[0] = "add";
				for (int i=0; i < argsOld.length; i++) {
					args[i+1] = argsOld[i];
				}
			}
			//alias /lce to /lc edit 
			if (commandLabel.equals("lce")) {
				String[] argsOld = args.clone();
				args = new String[argsOld.length+1];
				args[0] = "edit";
				for (int i=0; i < argsOld.length; i++) {
					args[i+1] = argsOld[i];
				}
			}
			
			
			
			
			
			List<String> autoCompleteList = new ArrayList<String>();
			
			if (Dungeon.selectedDungeons.containsKey(p)) {
				
			}
			
			
			
			return autoCompleteList;
		}
		return null;
	}

}
