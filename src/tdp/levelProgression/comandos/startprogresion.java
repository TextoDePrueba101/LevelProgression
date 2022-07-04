package tdp.levelProgression.comandos;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import tdp.levelProgression.LevelProgression;

public class startprogresion implements CommandExecutor{
	private FileConfiguration lang = LevelProgression.lang;
	
	public startprogresion(LevelProgression main) {
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] args) {
		if (!arg0.isOp()) {
			arg0.sendMessage(ChatColor.RED + lang.getString("CommandDenied"));
			return false;
		}
		if (args.length<1) return false;
		
		Player p = Bukkit.getPlayer(args[0]);
		
		p.sendMessage(ChatColor.ITALIC +lang.getString("startProg1")+args[0]+lang.getString("startProg2"));
		LevelProgression.psetData(p,"ADVENTURER",0);
		LevelProgression.psetData(p,"WIZARD",0);
		LevelProgression.psetData(p,"SOLDIER",0);
		LevelProgression.psetData(p,"ARCHER",0);
		LevelProgression.psetData(p,"SHIELDMAN",0);
		LevelProgression.psetData(p,"TRIDENTMAN",0);
		LevelProgression.psetData(p,"MINER",0);
		LevelProgression.psetData(p,"LUMBERJACK",0);
		LevelProgression.psetData(p,"FOODLVL",0);
		LevelProgression.psetData(p,"XP",0);
		LevelProgression.psetData(p,"ABILITYPOINTS",0);
		LevelProgression.psetData(p,"LEVEL",0);
		LevelProgression.psetData(p,"CC1",0);
		
		return true;
	}
}
