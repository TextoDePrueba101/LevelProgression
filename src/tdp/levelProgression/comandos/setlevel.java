package tdp.levelProgression.comandos;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import tdp.levelProgression.LevelProgression;

public class setlevel implements CommandExecutor{
	private FileConfiguration lang = LevelProgression.lang;
	
	public setlevel(LevelProgression main) {
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] args) {
		if (!arg0.isOp()) {
			arg0.sendMessage(ChatColor.RED +lang.getString("CommandDenied"));
			return false;
		}
		if (args.length!=3 || !(args[0] instanceof String) || !(args[1] instanceof String)) return false;
		
		try {
			Integer.parseInt(args[2]);
		}
		catch (Exception ex){
			arg0.sendMessage(ChatColor.RED + "Invalid number");
			return true;
		}
		if (Integer.parseInt(args[2])>100000) {
			arg0.sendMessage(ChatColor.RED + "Invalid number");
			return true;
		}
		
		if (Bukkit.getPlayer(args[0]) ==null) {
			arg0.sendMessage(ChatColor.RED + "Couldn't find the player");
			return true;
		}
		
		String t = args[1].toUpperCase();
		if (!t.equals("ADVENTURER") && !t.equals("WIZARD") && !t.equals("SOLDIER") && !t.equals("ARCHER") && !t.equals("SHIELDMAN") && !t.equals("TRIDENTMAN") && !t.equals("MINER") &&
				!t.equals("LUMBERJACK") && !t.equals("FOODLVL") && !t.equals("LEVEL") && !t.equals("XP") && !t.equals("ABILITYPOINTS") && !t.equals("CC1") && !t.equals("ALL")) {
			arg0.sendMessage(ChatColor.RED + "Not such class: '"+t+"'");
			return true;
		}
		
		if (t.equals("ALL")) {
			LevelProgression.psetData(Bukkit.getPlayer(args[0]),"ADVENTURER",Integer.parseInt(args[2]));
			LevelProgression.psetData(Bukkit.getPlayer(args[0]),"WIZARD",Integer.parseInt(args[2]));
			LevelProgression.psetData(Bukkit.getPlayer(args[0]),"SOLDIER",Integer.parseInt(args[2]));
			LevelProgression.psetData(Bukkit.getPlayer(args[0]),"ARCHER",Integer.parseInt(args[2]));
			LevelProgression.psetData(Bukkit.getPlayer(args[0]),"SHIELDMAN",Integer.parseInt(args[2]));
			LevelProgression.psetData(Bukkit.getPlayer(args[0]),"TRIDENTMAN",Integer.parseInt(args[2]));
			LevelProgression.psetData(Bukkit.getPlayer(args[0]),"MINER",Integer.parseInt(args[2]));
			LevelProgression.psetData(Bukkit.getPlayer(args[0]),"LUMBERJACK",Integer.parseInt(args[2]));
			LevelProgression.psetData(Bukkit.getPlayer(args[0]),"FOODLVL",Integer.parseInt(args[2]));
			
			arg0.sendMessage(ChatColor.ITALIC + lang.getString("setlvl1") + t +lang.getString("setlvl2")+ args[0] +lang.getString("setlvl3")+ args[2]);
		}
		else {
			try {
				LevelProgression.psetData(Bukkit.getPlayer(args[0]),t,Integer.parseInt(args[2]));
				arg0.sendMessage(ChatColor.ITALIC + lang.getString("setlvl1") + t +lang.getString("setlvl2")+ args[0] +lang.getString("setlvl3")+ LevelProgression.pgetData(Bukkit.getPlayer(args[0]), t));
			}
			catch (IllegalArgumentException ex)	{
				arg0.sendMessage(""+ChatColor.RED+"Invalid argument");
			}
		}

		return true;
	}
}
