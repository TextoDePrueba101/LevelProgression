package tdp.levelProgression.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import org.bukkit.ChatColor;
import tdp.levelProgression.LevelProgression;

public class FoodRestrictionListener implements Listener{
	private FileConfiguration lang = LevelProgression.lang;
	
	public FoodRestrictionListener(LevelProgression plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void FoodNotEat(PlayerItemConsumeEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;
		
		Player p = e.getPlayer();
		ItemStack item = e.getItem();
		Material m = item.getType();
		boolean cancell=false;
		
		if (m == Material.PUMPKIN_PIE || m == Material.BEETROOT_SOUP || m == Material.RABBIT_STEW || m == Material.POTATO || m == Material.MELON_SLICE
				||  m == Material.COOKIE || m == Material.APPLE) {
			
			if (LevelProgression.pgetData(p, "FOODLVL") <10 && LevelProgression.pgetData(p, "ADVENTURER") <30) {
				cancell=true;
			} 
		} 
		else if (m == Material.MUSHROOM_STEW || m == Material.BREAD || m == Material.PORKCHOP || m == Material.GOLDEN_APPLE || 
				m == Material.COD || m == Material.SALMON || m == Material.BEEF || m == Material.CHICKEN || 
				m == Material.CARROT || m == Material.MUTTON) {
			
			if (LevelProgression.pgetData(p, "FOODLVL") <25 && LevelProgression.pgetData(p, "ADVENTURER") <90) {
				cancell=true;
			}
		} 
		else if (m == Material.COOKED_PORKCHOP || m == Material.COOKED_COD || m == Material.COOKED_COD ||
				m == Material.COOKED_SALMON || m == Material.COOKED_BEEF || m == Material.COOKED_CHICKEN || m == Material.COOKED_MUTTON || 
				m == Material.GOLDEN_CARROT || m == Material.BAKED_POTATO) {
			
			if (LevelProgression.pgetData(p, "FOODLVL") <50 && LevelProgression.pgetData(p, "ADVENTURER") <170) {
				cancell=true;
			}
		} 
		else if (item.getItemMeta().getLocalizedName().equals("levelResetItem")) {
			//Level Reset
			
			p.playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 5.0F, 1.0F);
			Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + p.getName() + lang.getString("food1"));
			
			LevelProgression.psetData(p,"ADVENTURER",0);
			LevelProgression.psetData(p,"WIZARD",0);
			LevelProgression.psetData(p,"SOLDIER",0);
			LevelProgression.psetData(p,"ARCHER",0);
			LevelProgression.psetData(p,"SHIELDMAN",0);
			LevelProgression.psetData(p,"TRIDENTMAN",0);
			LevelProgression.psetData(p,"MINER",0);
			LevelProgression.psetData(p,"LUMBERJACK",0);
			LevelProgression.psetData(p,"FOODLVL",0);
			
			int newlvl = (LevelProgression.pgetData(p, "LEVEL") / 2);
			
			LevelProgression.psetData(p,"ABILITYPOINTS",newlvl);
			LevelProgression.psetData(p,"LEVEL",newlvl);
			
		}
		else if (item.getItemMeta().getLocalizedName().equals("XPbottle")) {
			LevelProgression.addXP(p, 500);
			p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 5.0F, 1.0F);
		}
		
		if (item.getItemMeta().getLocalizedName().equals("TP_GOLDEN_CARROT") && LevelProgression.pgetData(p,"ADVENTURER")>=100) {
			cancell=false;
			if (p.getBedSpawnLocation() == null) p.sendMessage(ChatColor.ITALIC + "" + ChatColor.GRAY + lang.getString("food2"));
			else {
				p.teleport(p.getBedSpawnLocation());
				p.getWorld().playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 5.0F, 1.0F);
			}
		}
		else if (item.getItemMeta().getLocalizedName().equals("RAIN_KELP") && LevelProgression.pgetData(p,"TRIDENTMAN")>=100) {
			p.getWorld().setStorm(true);
		}

		if (m == Material.APPLE && LevelProgression.pgetData(p, "WIZARD") >=5) cancell=false;
		else if (m == Material.BEETROOT_SOUP || m == Material.MUSHROOM_STEW || m == Material.RABBIT_STEW) if (LevelProgression.pgetData(p, "WIZARD") >=10) cancell=false;

		if (cancell) {
			e.setCancelled(true);
			p.sendMessage(ChatColor.RED + lang.getString("food3"));
		}
		
	}	
}
