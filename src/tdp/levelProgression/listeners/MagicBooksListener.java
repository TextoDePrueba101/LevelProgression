package tdp.levelProgression.listeners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import tdp.levelProgression.LevelProgression;

public class MagicBooksListener implements Listener{
	private boolean dobleClickProblem=false;
	private FileConfiguration lang=LevelProgression.lang;
	
	public MagicBooksListener(LevelProgression plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void trowEnchPower (PlayerInteractEvent e) {
		if (dobleClickProblem) {
			dobleClickProblem=!dobleClickProblem;
			return;
		}
		
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;

		if (e.getHand()== EquipmentSlot.OFF_HAND && e.getAction()!= Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK) {
			//El if anterior es verdadero cuando el jugador con el objeto en la OffHand da click derecho en general
			
			Player p = e.getPlayer();
		
			if (p != null && p.getInventory().getItemInOffHand().getType()==Material.ENCHANTED_BOOK && LevelProgression.pgetData(p,"CC1")==0) {
				int xpCost = getXpCost(p.getInventory().getItemInOffHand());
				LevelProgression.psetData(p, "CC1", 2);
				
				boolean test=true;
				for (ItemStack i : p.getInventory().getContents()) if (test && i!=null && i.getItemMeta().getLocalizedName().equals("MAGIC_XP_BOTTLE") && LevelProgression.pgetData(p, "WIZARD")>=100) {
					xpCost=(xpCost/4);
					i.setAmount(i.getAmount()-1);
					test=false;
				}
				
				if (!enchPowerVerifier(p, p.getInventory().getItemInOffHand())) {
					p.sendMessage(ChatColor.RED + lang.getString("magic1"));
				} else if (p.getLevel() < xpCost) {
					p.sendMessage(ChatColor.RED + lang.getString("magic2") + xpCost + lang.getString("magic3"));
				} else {
					p.setLevel(p.getLevel() - xpCost);
					p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 5.0F, 1F);
					runPowers(p,p.getInventory().getItemInOffHand(),e);
				}
			}
		}
		
		if (e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.ENCHANTING_TABLE && LevelProgression.pgetData(e.getPlayer(), "WIZARD") < 50) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + lang.getString("magic4"));
		}
		
		if (e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.BREWING_STAND && LevelProgression.pgetData(e.getPlayer(), "WIZARD") < 100) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED +lang.getString("magic5"));
		}
	
	}
	
	@EventHandler
	public void breakBlockXP(BlockBreakEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;

		Player p = e.getPlayer();
		Block block = e.getBlock();
		
		//silktouch-----------------------------------------------------------------------------------------------
		if (p.getInventory().getItemInMainHand().getType() == Material.ENCHANTED_BOOK) {

			
			EnchantmentStorageMeta enchMeta = (EnchantmentStorageMeta) p.getInventory().getItemInMainHand().getItemMeta();
			
				if (LevelProgression.pgetData(p,"WIZARD") >= 100 && enchMeta.hasStoredEnchant(Enchantment.SILK_TOUCH) && p.getLevel() >= 5) {
					int ExpLvl = p.getLevel() - 5;
					p.setLevel(ExpLvl);
					p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 5.0F, 1F);

					e.setCancelled(true);
					ItemStack dropItem = new ItemStack(block.getType(), 1);
					block.setType(Material.AIR);
					block.getWorld().dropItemNaturally(block.getLocation(), dropItem);
				}
		}
		//end silktouch--------------------------------------------------------------------------------------------
	
	}
	
	@EventHandler
	public void enchBookRename(InventoryClickEvent e) {
		if (!LevelProgression.testPluginActive(e.getWhoClicked().getWorld())) return;

		//Testea si el item clickeado o el item sostenido es un libro de encantamiento y lo renombra a su costo
		if (e.getInventory().equals(LevelProgression.gui)) return;
		if (e.getInventory().getItem(0) != null && e.getInventory().getItem(0).getItemMeta().getLocalizedName().equals("BACKPANE")) return;
		
		if (e.getCurrentItem() == null || e.getCursor() == null) {	
				return;
		}else if (e.getCursor().getType()==Material.ENCHANTED_BOOK) {
			ItemMeta meta = e.getCursor().getItemMeta();
			meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "" + getXpCost(e.getCursor()) + lang.getString("magicCost"));
			e.getCursor().setItemMeta(meta);
		} else if (e.getCurrentItem().getType()==Material.ENCHANTED_BOOK) {
			ItemMeta meta = e.getCurrentItem().getItemMeta();
			meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "" + getXpCost(e.getCurrentItem()) + lang.getString("magicCost"));
			e.getCurrentItem().setItemMeta(meta);
		}
		
	}
	
	@EventHandler
	public void specialPotionsCreation(PlayerItemConsumeEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;

		Player p = e.getPlayer();
		ItemStack item = e.getItem();
		
		if (item.getType() == Material.POTION) {
			PotionMeta potion = (PotionMeta) item.getItemMeta();
		
			if (potion.getBasePotionData().getType() == PotionType.WATER && LevelProgression.pgetData(p,"WIZARD") >= 20 && p.getLevel()>=3) {
				e.setItem(new ItemStack(Material.AIR));
				p.getWorld().dropItem(p.getLocation(), new ItemStack(Material.EXPERIENCE_BOTTLE,2));
				p.setLevel(p.getLevel()-3);
				
				p.playSound(p.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 5.0F, 1F);
				
			}
			
			if (potion.getBasePotionData().getType() == PotionType.MUNDANE && LevelProgression.pgetData(p,"WIZARD") >= 90 && p.getLevel()>=8) {
				e.setItem(new ItemStack(Material.AIR));
				p.getWorld().dropItem(p.getLocation(), new ItemStack(Material.DRAGON_BREATH,1));
				p.setLevel(p.getLevel()-8);
				
				p.playSound(p.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 5.0F, 1F);
				
			}
		
			
		} 		
	}

	public int getXpCost(ItemStack book) { //Cada nivel de Encantamiento tiene un costo establecido �nicamente en este m�todo en la candidad sumada
		int totalCost=0;
		EnchantmentStorageMeta enchMeta = (EnchantmentStorageMeta) book.getItemMeta();

		if (enchMeta.getStoredEnchantLevel(Enchantment.SILK_TOUCH) == 1) totalCost=totalCost+5;
		if (enchMeta.getStoredEnchantLevel(Enchantment.DIG_SPEED) == 1) totalCost=totalCost+1;
		if (enchMeta.getStoredEnchantLevel(Enchantment.DIG_SPEED) == 2) totalCost=totalCost+2;
		if (enchMeta.getStoredEnchantLevel(Enchantment.DIG_SPEED) == 3) totalCost=totalCost+3;
		if (enchMeta.getStoredEnchantLevel(Enchantment.DIG_SPEED) == 4) totalCost=totalCost+4;
		if (enchMeta.getStoredEnchantLevel(Enchantment.DIG_SPEED) == 5) totalCost=totalCost+5;
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_ENVIRONMENTAL) == 1) totalCost=totalCost+2;
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_ENVIRONMENTAL) == 2) totalCost=totalCost+5;
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_ENVIRONMENTAL) == 3) totalCost=totalCost+8;
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_ENVIRONMENTAL) == 4) totalCost=totalCost+10;
		if (enchMeta.getStoredEnchantLevel(Enchantment.FROST_WALKER) == 1) totalCost=totalCost+10;
		if (enchMeta.getStoredEnchantLevel(Enchantment.FROST_WALKER) == 2) totalCost=totalCost+15;
		if (enchMeta.getStoredEnchantLevel(Enchantment.DURABILITY) == 1) totalCost=totalCost+15;
		if (enchMeta.getStoredEnchantLevel(Enchantment.DURABILITY) == 2) totalCost=totalCost+21;
		if (enchMeta.getStoredEnchantLevel(Enchantment.DURABILITY) == 3) totalCost=totalCost+27;
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_FIRE) == 1) totalCost=totalCost+1;
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_FIRE) == 2) totalCost=totalCost+3;
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_FIRE) == 3) totalCost=totalCost+7;
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_FIRE) == 4) totalCost=totalCost+10;
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_FALL) == 1) totalCost=totalCost+4;
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_FALL) == 2) totalCost=totalCost+6;
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_FALL) == 3) totalCost=totalCost+6;
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_FALL) == 4) totalCost=totalCost+10;
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_EXPLOSIONS) == 1) totalCost=totalCost+2;
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_EXPLOSIONS) == 2) totalCost=totalCost+8;
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_EXPLOSIONS) == 3) totalCost=totalCost+4;
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_EXPLOSIONS) == 4) totalCost=totalCost+5;
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_PROJECTILE) == 1) totalCost=totalCost+1;
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_PROJECTILE) == 2) totalCost=totalCost+1;
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_PROJECTILE) == 3) totalCost=totalCost+4;
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_PROJECTILE) == 4) totalCost=totalCost+7;
		if (enchMeta.getStoredEnchantLevel(Enchantment.MENDING) == 1) totalCost=totalCost+40;
		if (enchMeta.getStoredEnchantLevel(Enchantment.LUCK) == 1) totalCost=totalCost+4;
		if (enchMeta.getStoredEnchantLevel(Enchantment.LUCK) == 2) totalCost=totalCost+3;
		if (enchMeta.getStoredEnchantLevel(Enchantment.LUCK) == 3) totalCost=totalCost+8;
		if (enchMeta.getStoredEnchantLevel(Enchantment.LURE) == 1) totalCost=totalCost+4;
		if (enchMeta.getStoredEnchantLevel(Enchantment.LURE) == 2) totalCost=totalCost+5;
		if (enchMeta.getStoredEnchantLevel(Enchantment.LURE) == 3) totalCost=totalCost+75;
		if (enchMeta.getStoredEnchantLevel(Enchantment.CHANNELING) == 1) totalCost=totalCost+8;
		if (enchMeta.getStoredEnchantLevel(Enchantment.SOUL_SPEED) == 1) totalCost=totalCost+4;
		if (enchMeta.getStoredEnchantLevel(Enchantment.SOUL_SPEED) == 2) totalCost=totalCost+10;
		if (enchMeta.getStoredEnchantLevel(Enchantment.SOUL_SPEED) == 3) totalCost=totalCost+20;
		if (enchMeta.getStoredEnchantLevel(Enchantment.DEPTH_STRIDER) == 1) totalCost=totalCost+3;
		if (enchMeta.getStoredEnchantLevel(Enchantment.DEPTH_STRIDER) == 2) totalCost=totalCost+15;
		if (enchMeta.getStoredEnchantLevel(Enchantment.DEPTH_STRIDER) == 3) totalCost=totalCost+30;
		if (enchMeta.getStoredEnchantLevel(Enchantment.THORNS) == 1) totalCost=totalCost+1;
		if (enchMeta.getStoredEnchantLevel(Enchantment.THORNS) == 2) totalCost=totalCost+4;
		if (enchMeta.getStoredEnchantLevel(Enchantment.THORNS) == 3) totalCost=totalCost+0;
		if (enchMeta.getStoredEnchantLevel(Enchantment.WATER_WORKER) == 1) totalCost=totalCost+5;
		
		//if (enchMeta.getStoredEnchantLevel(Enchantment) == ) totalCost=totalCost+;
		return totalCost;
	}
	
	public boolean enchPowerVerifier(Player p, ItemStack book) { //return true if the player can use all the enchants in the book
	
		EnchantmentStorageMeta enchMeta = (EnchantmentStorageMeta) book.getItemMeta();
		
		if (enchMeta!=null) {
			
			if (enchMeta.getStoredEnchantLevel(Enchantment.SILK_TOUCH) == 1 && LevelProgression.pgetData(p, "WIZARD") < 200 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.DIG_SPEED) == 1 && LevelProgression.pgetData(p, "WIZARD") < 1 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.DIG_SPEED) == 2 && LevelProgression.pgetData(p, "WIZARD") < 10 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.DIG_SPEED) == 3 && LevelProgression.pgetData(p, "WIZARD") < 30 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.DIG_SPEED) == 4 && LevelProgression.pgetData(p, "WIZARD") < 50 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.DIG_SPEED) == 5 && LevelProgression.pgetData(p, "WIZARD") < 75 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_ENVIRONMENTAL) ==1  && LevelProgression.pgetData(p, "WIZARD") < 15 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_ENVIRONMENTAL) ==2  && LevelProgression.pgetData(p, "WIZARD") < 55 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_ENVIRONMENTAL) ==3  && LevelProgression.pgetData(p, "WIZARD") < 175 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_ENVIRONMENTAL) ==4  && LevelProgression.pgetData(p, "WIZARD") < 295 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.FROST_WALKER) == 1 && LevelProgression.pgetData(p, "WIZARD") < 60 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.FROST_WALKER) == 2 && LevelProgression.pgetData(p, "WIZARD") < 170 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.DURABILITY) == 1 && LevelProgression.pgetData(p, "WIZARD") < 35 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.DURABILITY) == 2 && LevelProgression.pgetData(p, "WIZARD") < 90 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.DURABILITY) == 3 && LevelProgression.pgetData(p, "WIZARD") < 120 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_FIRE) == 1 && LevelProgression.pgetData(p, "WIZARD") < 67 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_FIRE) == 2 && LevelProgression.pgetData(p, "WIZARD") < 87 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_FIRE) == 3 && LevelProgression.pgetData(p, "WIZARD") < 165 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_FIRE) == 4 && LevelProgression.pgetData(p, "WIZARD") < 235 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_FALL) == 1 && LevelProgression.pgetData(p, "WIZARD") < 30 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_FALL) == 2 && LevelProgression.pgetData(p, "WIZARD") < 52 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_FALL) == 3 && LevelProgression.pgetData(p, "WIZARD") < 214 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_FALL) == 4 && LevelProgression.pgetData(p, "WIZARD") < 268 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_EXPLOSIONS) == 1 && LevelProgression.pgetData(p, "WIZARD") < 67 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_EXPLOSIONS) == 2 && LevelProgression.pgetData(p, "WIZARD") < 128 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_EXPLOSIONS) == 3 && LevelProgression.pgetData(p, "WIZARD") < 187 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_EXPLOSIONS) == 4 && LevelProgression.pgetData(p, "WIZARD") < 278 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_PROJECTILE) == 1 && LevelProgression.pgetData(p, "WIZARD") < 109 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_PROJECTILE) == 2 && LevelProgression.pgetData(p, "WIZARD") < 126 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_PROJECTILE) == 3 && LevelProgression.pgetData(p, "WIZARD") < 148 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_PROJECTILE) == 4 && LevelProgression.pgetData(p, "WIZARD") < 166 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.MENDING) == 1 && LevelProgression.pgetData(p, "WIZARD") < 280 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.LUCK) == 1 && LevelProgression.pgetData(p, "WIZARD") < 22 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.LUCK) == 2 && LevelProgression.pgetData(p, "WIZARD") < 37 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.LUCK) == 3 && LevelProgression.pgetData(p, "WIZARD") < 53 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.LURE) == 1 && LevelProgression.pgetData(p, "WIZARD") < 101 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.LURE) == 2 && LevelProgression.pgetData(p, "WIZARD") < 210 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.LURE) == 3 && LevelProgression.pgetData(p, "WIZARD") < 279 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.CHANNELING) == 1 && LevelProgression.pgetData(p, "WIZARD") < 230 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.CHANNELING) == 2 && LevelProgression.pgetData(p, "WIZARD") < 230 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.SOUL_SPEED) == 1 && LevelProgression.pgetData(p, "WIZARD") < 72 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.SOUL_SPEED) == 2 && LevelProgression.pgetData(p, "WIZARD") < 107 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.SOUL_SPEED) == 3 && LevelProgression.pgetData(p, "WIZARD") < 207 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.DEPTH_STRIDER) == 1 && LevelProgression.pgetData(p, "WIZARD") < 189 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.DEPTH_STRIDER) == 2 && LevelProgression.pgetData(p, "WIZARD") < 263 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.DEPTH_STRIDER) == 3 && LevelProgression.pgetData(p, "WIZARD") < 175 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.THORNS) == 1 && LevelProgression.pgetData(p, "WIZARD") < 0 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.THORNS) == 2 && LevelProgression.pgetData(p, "WIZARD") < 54 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.THORNS) == 3 && LevelProgression.pgetData(p, "WIZARD") < 100 ) return false;
			else if (enchMeta.getStoredEnchantLevel(Enchantment.WATER_WORKER) == 1 && LevelProgression.pgetData(p, "WIZARD") < 59 ) return false;

			return true;
		} else return false; 
		
	}
	
	
	public void runPowers (Player p, ItemStack book, PlayerInteractEvent originalEvent) {
		EnchantmentStorageMeta enchMeta = (EnchantmentStorageMeta) book.getItemMeta();
		boolean changeGamerule=false;
		
		if (p.getWorld().getGameRuleValue(GameRule.SEND_COMMAND_FEEDBACK)) {
			p.getWorld().setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);
			changeGamerule =true;
		}
		
		p.swingOffHand();
		
		//DIG SPEED================================================
		if (enchMeta.getStoredEnchantLevel(Enchantment.DIG_SPEED) == 1) {
			Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(p.getLocation(), 10, 10, 10);
			
			for (Entity entities : ent) {
				if (entities instanceof LivingEntity) {
					LivingEntity entity = (LivingEntity) entities;
					if (entity!=p) entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 200, 0, false));
				}
			}
		}
			
		if (enchMeta.getStoredEnchantLevel(Enchantment.DIG_SPEED) == 2) {
			Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(p.getLocation(), 10, 10, 10);
			
			for (Entity entities : ent) {
				if (entities instanceof LivingEntity) {
					LivingEntity entity = (LivingEntity) entities;
					if (entity!=p) entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 400, 1, false));
				}
			}
		}

		if (enchMeta.getStoredEnchantLevel(Enchantment.DIG_SPEED) == 3) {
			Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(p.getLocation(), 10, 10, 10);
			
			for (Entity entities : ent) {
				if (entities instanceof LivingEntity) {
					LivingEntity entity = (LivingEntity) entities;
					if (entity!=p) entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 600, 1, false));
				}
			}
		}

		if (enchMeta.getStoredEnchantLevel(Enchantment.DIG_SPEED) == 4) {
			Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(p.getLocation(), 10, 10, 10);
			
			for (Entity entities : ent) {
				if (entities instanceof LivingEntity) {
					LivingEntity entity = (LivingEntity) entities;
					if (entity!=p) entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 800, 2, false));
				}
			}
		}
		
		if (enchMeta.getStoredEnchantLevel(Enchantment.DIG_SPEED) == 5) {
			Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(p.getLocation(), 10, 10, 10);
			
			for (Entity entities : ent) {
				if (entities instanceof LivingEntity) {
					LivingEntity entity = (LivingEntity) entities;
					if (entity!=p) entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 1000, 3, false));
				}
			}
		}
		//END DIG SPEED==================================================	
		
		//PROTECTION
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_ENVIRONMENTAL) == 1) {

			Location loc = p.getLocation();
			
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~1 ~2 ~1 ~-1 ~-1 ~-1 minecraft:glass replace #minecraft:replaceable_plants");
			
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~1 ~2 ~1 ~-1 ~-1 ~-1 minecraft:glass replace minecraft:air");

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " + loc.getZ() 
			+ " run fill ~ ~1 ~ ~ ~ ~ minecraft:air replace minecraft:glass");
		}
		
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_ENVIRONMENTAL) == 2) {

			Location loc = p.getLocation();
			
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~1 ~2 ~1 ~-1 ~-1 ~-1 minecraft:stone replace #minecraft:replaceable_plants");
			
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~1 ~2 ~1 ~-1 ~-1 ~-1 minecraft:stone replace minecraft:air");

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " + loc.getZ() 
			+ " run fill ~ ~1 ~ ~ ~ ~ minecraft:air replace minecraft:stone");
		}
		
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_ENVIRONMENTAL) == 3) {
		
			Location loc = p.getLocation();
			loc.add(0, 4, 0);
			
			for (int i=0;i<4;i++) {
				p.getWorld().spawnEntity(loc, EntityType.SNOWMAN);
			}
			
			loc = p.getLocation();		
			
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~1 ~2 ~1 ~-1 ~-1 ~-1 minecraft:obsidian replace #minecraft:replaceable_plants");
			
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~1 ~2 ~1 ~-1 ~-1 ~-1 minecraft:obsidian replace minecraft:air");

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " + loc.getZ() 
			+ " run fill ~ ~1 ~ ~ ~ ~ minecraft:air replace minecraft:obsidian");
		}
		
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_ENVIRONMENTAL) == 4) {
			Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(p.getLocation(), 10, 10, 10);
			
			for (Entity entities : ent) {
				if (entities instanceof LivingEntity) {
					LivingEntity entity = (LivingEntity) entities;
					if (entity!=p) {
						entity.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 0, true));
						entity.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 400, 0, true));
					}
				}
			}
			Location loc = p.getLocation();
			loc.add(0, 4, 0);
			p.getWorld().spawnEntity(loc, EntityType.IRON_GOLEM);
			
			loc = p.getLocation();		
			
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~1 ~2 ~1 ~-1 ~-1 ~-1 minecraft:crying_obsidian replace #minecraft:replaceable_plants");
			
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~1 ~2 ~1 ~-1 ~-1 ~-1 minecraft:crying_obsidian replace minecraft:air");

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " + loc.getZ() 
			+ " run fill ~ ~1 ~ ~ ~ ~ minecraft:air replace minecraft:crying_obsidian");
		}
		//END PROTECTION
		
		//FROST WALKER
		if (enchMeta.getStoredEnchantLevel(Enchantment.FROST_WALKER) == 1) {
			Location loc = p.getLocation();

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~4 ~4 ~4 ~-4 ~-4 ~-4 minecraft:powder_snow replace #minecraft:dirt");
			
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~4 ~4 ~4 ~-4 ~-4 ~-4 minecraft:ice replace minecraft:water");

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " + loc.getZ() 
			+ " run fill ~4 ~4 ~4 ~-4 ~-4 ~-4 minecraft:spruce_log replace #minecraft:logs");

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " + loc.getZ() 
			+ " run fill ~4 ~4 ~4 ~-4 ~-4 ~-4 minecraft:spruce_leaves replace #minecraft:leaves");
		
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " + loc.getZ() 
			+ " run fill ~4 ~4 ~4 ~-4 ~-4 ~-4 minecraft:cobblestone replace minecraft:lava");
			
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " + loc.getZ() 
			+ " run fill ~4 ~4 ~4 ~-4 ~-4 ~-4 minecraft:snow_block replace #minecraft:base_stone_overworld");
		}

		if (enchMeta.getStoredEnchantLevel(Enchantment.FROST_WALKER) == 2) {
			Location loc = p.getLocation();

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~10 ~10 ~10 ~-10 ~-10 ~-10 minecraft:powder_snow replace #minecraft:dirt");
			
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~10 ~10 ~10 ~-10 ~-10 ~-10 minecraft:packed_ice replace minecraft:water");

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " + loc.getZ() 
			+ " run fill ~10 ~10 ~10 ~-10 ~-10 ~-10 minecraft:blue_ice replace #minecraft:logs");

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " + loc.getZ() 
			+ " run fill ~10 ~10 ~10 ~-10 ~-10 ~-10 minecraft:ice replace #minecraft:leaves");
		
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " + loc.getZ() 
			+ " run fill ~10 ~10 ~10 ~-10 ~-10 ~-10 minecraft:obsidian replace minecraft:lava");
			
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " + loc.getZ() 
			+ " run fill ~10 ~10 ~10 ~-10 ~-10 ~-10 minecraft:snow_block replace #minecraft:base_stone_overworld");
		}
		//END FROST WALKER
		
		//DURABILITY
		if (enchMeta.getStoredEnchantLevel(Enchantment.DURABILITY) == 1) {

			ItemStack[] armour = p.getInventory().getContents();
			
			for (ItemStack piece : armour) {
				if (piece!=null && piece.getItemMeta() instanceof Damageable) {
					Damageable dam = (Damageable) piece.getItemMeta();
				
					if (dam.getDamage() >= 90) {
						dam.setDamage(dam.getDamage() - 90);
					} else {
						dam.setDamage(0);
					}
					
					piece.setItemMeta((ItemMeta) dam);
				}
			}
		}
		
		if (enchMeta.getStoredEnchantLevel(Enchantment.DURABILITY) == 2) {

			ItemStack[] armour = p.getInventory().getContents();
			
			for (ItemStack piece : armour) {
				if (piece!=null && piece.getItemMeta() instanceof Damageable) {
					Damageable dam = (Damageable) piece.getItemMeta();
				
					if (dam.getDamage() >= 325) {
						dam.setDamage(dam.getDamage() - 325);
					} else {
						dam.setDamage(0);
					}
					
					piece.setItemMeta((ItemMeta) dam);
				}
			}
		}

		if (enchMeta.getStoredEnchantLevel(Enchantment.DURABILITY) == 3) {

			ItemStack[] armour = p.getInventory().getContents();
			
			for (ItemStack piece : armour) {
				if (piece!=null && piece.getItemMeta() instanceof Damageable) {
					Damageable dam = (Damageable) piece.getItemMeta();
				
					if (dam.getDamage() >= 700) {
						dam.setDamage(dam.getDamage() - 700);
					} else {
						dam.setDamage(0);
					}
					
					piece.setItemMeta((ItemMeta) dam);
				}
			}
		}
		//END DURABILITY
		
		//PROTECTION FIRE
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_FIRE) == 1) {
			Block b = LevelProgression.getPosiblePlacedBlock(p,200);
			if (b!=null) {
				Location bBelowLoc = b.getLocation();
				bBelowLoc.add(0, -1, 0);
				//if (b.getType() == Material.AIR && b.getWorld().getBlockAt(bBelowLoc).getType().isSolid()) b.setType(Material.FIRE);
				if (!b.getType().isSolid()) b.setType(Material.FIRE);
			} else p.getTargetBlock(null, 200).setType(Material.FIRE);
			
			if (originalEvent.getAction()==Action.RIGHT_CLICK_BLOCK) dobleClickProblem=true;
		}
		
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_FIRE) == 2) {
			Location loc = p.getLocation();

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " + loc.getZ() 
			+ " run fill ~2 ~2 ~2 ~-2 ~-2 ~-2 minecraft:fire replace #minecraft:replaceable_plants");
			
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " + loc.getZ() 
			+ " run fill ~2 ~2 ~2 ~-2 ~-2 ~-2 minecraft:fire keep");

		}
		
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_FIRE) == 3) {
			Block b = p.getTargetBlock(null, 200);
			
			if (!b.getType().isAir() && !b.getType().isInteractable() && !b.getType().equals(Material.BEDROCK) 
					&& !b.getType().isInteractable() && !b.getPistonMoveReaction().equals(PistonMoveReaction.BLOCK) ) b.setType(Material.LAVA);

			
			if (originalEvent.getAction()==Action.RIGHT_CLICK_BLOCK) dobleClickProblem=true;
		}
		
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_FIRE) == 4) {
			List<Block> blocks = p.getLastTwoTargetBlocks(null, 200); //index 1 == last block | index 0 == previus block
			
			Block b = blocks.get(0); 
			
			if (!b.getType().isSolid()) {
				
				Location spawnLocation = b.getLocation().add(0.5, 0, 0.5);
				
				for (int i=0; i<3;i++) p.getWorld().spawnEntity(spawnLocation, EntityType.BLAZE);
			}
		}
		//END PROTECTION FIRE
		
		//PROTECTION FALL
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_FALL) == 1) {
			Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(p.getTargetBlock(null, 200).getLocation(), 5, 5, 5);
			
			for (Entity entities : ent) {
				if (entities instanceof LivingEntity) {
					LivingEntity entity = (LivingEntity) entities;
					entity.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 200, 0, false));
				}
			}
		}

		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_FALL) == 2) {
			Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(p.getTargetBlock(null, 200).getLocation(), 5, 5, 5);
			
			for (Entity entities : ent) {
				if (entities instanceof LivingEntity) {
					LivingEntity entity = (LivingEntity) entities;
					entity.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 400, 0, false));
				}
			}
		}

		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_FALL) == 3) {
			Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(p.getTargetBlock(null, 200).getLocation(), 5, 5, 5);
			
			for (Entity entities : ent) {
				if (entities instanceof LivingEntity) {
					LivingEntity entity = (LivingEntity) entities;
					entity.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 200, 1, false));
				}
			}
		}

		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_FALL) == 4) {
			Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(p.getTargetBlock(null, 200).getLocation(), 5, 5, 5);
			
			for (Entity entities : ent) {
				if (entities instanceof LivingEntity) {
					LivingEntity entity = (LivingEntity) entities;
					entity.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 60, false));
				}
			}
		}
		//END PROTECTION FALL
		
		//PROTECTION EXPLOTIONS
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_EXPLOSIONS) == 1) {
			Location loc = p.getLocation();

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " + loc.getZ() 
			+ " run summon minecraft:fireball ~ ~1 ~");
		}

		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_EXPLOSIONS) == 2) {
			Block b = p.getTargetBlock(null, 200);
			Location loc = b.getLocation().add(0.5, 0, 0.5);

			
			DragonFireball df = (DragonFireball) b.getWorld().spawnEntity(loc, EntityType.DRAGON_FIREBALL);
			df.setDirection(new Vector(0,-1,0));
			
			p.playSound(loc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 5.0F, 1.0F);
		}
		
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_EXPLOSIONS) == 3) {
			Block b = p.getTargetBlock(null, 20);
			
			b.getWorld().createExplosion(b.getLocation(), 2);

		}
		
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_EXPLOSIONS) == 4) {
			Block b = p.getWorld().getBlockAt(p.getLocation()); 
			
			if (!b.getType().isSolid()) {
				
				Location spawnLocation = b.getLocation().add(0.5, 0, 0.5);
				
				Creeper creeper = (Creeper) p.getWorld().spawnEntity(spawnLocation, EntityType.CREEPER);
				creeper.setPowered(true);
				
			}
		}
		//END PROTECTION EXPLOTION
		
		//PROTECTION PROJECTILE
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_PROJECTILE) == 1) {
			Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(p.getLocation(), 20, 20, 20);
			
			for (Entity entities : ent) {
				if (entities instanceof LivingEntity) {
					LivingEntity entity = (LivingEntity) entities;
					
					if (entity!=p) entity.getWorld().spawnEntity(entity.getLocation().add(0, 3, 0), EntityType.ARROW);

				}
			}
		}

		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_PROJECTILE) == 2) {
			Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(p.getTargetBlock(null, 70).getLocation(), 6, 8, 6);
			
			for (Entity entities : ent) {
				if (entities instanceof LivingEntity) {
					LivingEntity entity = (LivingEntity) entities;
					
					entity.getWorld().spawnEntity(entity.getLocation().add(0, 3, 0), EntityType.ARROW);
				}
			}
		}
		
		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_PROJECTILE) == 3) {
			Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(p.getTargetBlock(null, 100).getLocation(), 10, 200, 10);
			
			for (Entity entities : ent) {
				if (entities instanceof LivingEntity) {
					LivingEntity entity = (LivingEntity) entities;
					
					entity.getWorld().spawnEntity(entity.getLocation().add(0, 3, 0), EntityType.TRIDENT);
				}
			}
		}

		if (enchMeta.getStoredEnchantLevel(Enchantment.PROTECTION_PROJECTILE) == 4) {
			Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(p.getLocation(), 100, 200, 100);
			
			for (Entity entities : ent) {
				if (entities instanceof Player) {
					LivingEntity entity = (LivingEntity) entities;
					Block b = entity.getWorld().getBlockAt(entity.getLocation().add(0,30,0));
					
					if (b!=null && !b.getType().isSolid()) {
						b.setType(Material.ANVIL);
					}
				}
			}
		}
		//END PROTECTION PROJECTILE
		
		//MENDING
		if (enchMeta.getStoredEnchantLevel(Enchantment.MENDING) == 1) {
			Entity unlukyEntity = LevelProgression.getTargetEntity(p);
			if (unlukyEntity instanceof LivingEntity) {
				LivingEntity ent1 = (LivingEntity) unlukyEntity;
				ent1.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
				ent1.getEquipment().setItemInOffHand(new ItemStack(Material.AIR));
				ent1.sendMessage(ChatColor.GOLD + "Por arte de magia has perdido lo que sosten�as");
				p.playSound(ent1.getLocation(), Sound.ITEM_SHIELD_BREAK, 5.0F, 1.0F);
				if (ent1 instanceof Player) {
					Player p1 = (Player) ent1;
					p1.playSound(p1.getLocation(), Sound.ITEM_SHIELD_BREAK, 5.0F, 1.0F);
				}
			} 
		}
		//END MENDING
		
		//LUCK
		if (enchMeta.getStoredEnchantLevel(Enchantment.LUCK) == 1) {
			List<Block> blocks = p.getLastTwoTargetBlocks(null, 150); //index 1 == last block | index 0 == previus block
			
			Block b = blocks.get(0); 
			
			if (!b.getType().isSolid()) {
				
				Location spawnLocation = b.getLocation().add(0.5, 0, 0.5);
				
				for (int i=0; i<2;i++) p.getWorld().spawnEntity(spawnLocation, EntityType.DROWNED);
			}
		}
		
		if (enchMeta.getStoredEnchantLevel(Enchantment.LUCK) == 2) {
			Location loc = p.getTargetBlock(null, 50).getLocation(); 
				
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~1 ~1 ~1 ~-1 ~ ~-1 minecraft:water keep");
		}

		if (enchMeta.getStoredEnchantLevel(Enchantment.LUCK) == 3) {
			List<Block> blocks = p.getLastTwoTargetBlocks(null, 150); //index 1 == last block | index 0 == previus block
			
			Block b = blocks.get(0); 
			
			if (!b.getType().isSolid()) {
				
				Location spawnLocation = b.getLocation().add(0.5, 0, 0.5);
				
				p.getWorld().spawnEntity(spawnLocation, EntityType.ELDER_GUARDIAN);
			}
		}
		//END LUCK 
		
		//LURE
		if (enchMeta.getStoredEnchantLevel(Enchantment.LURE) == 1) {
			Entity ent = LevelProgression.getTargetEntity(p);
			if (ent instanceof Player);
			else if (ent instanceof LivingEntity) {
				LivingEntity ent1 = (LivingEntity) ent;
				if (ent1.getLocation().distance(p.getLocation()) < 50) {
					ent1.teleport(p.getLocation());
				}
			} 
		}
		
		if (enchMeta.getStoredEnchantLevel(Enchantment.LURE) == 2) {
			Entity ent = LevelProgression.getTargetEntity(p);
			if (ent instanceof LivingEntity) {
				LivingEntity ent1 = (LivingEntity) ent;
				if (ent1.getLocation().distance(p.getLocation()) < 150) {
					Location locationEnt1 = ent1.getLocation();
					Location locationP=p.getLocation();
					
					ent1.teleport(locationP);
					p.teleport(locationEnt1);
				}
			} 
		}
		
		if (enchMeta.getStoredEnchantLevel(Enchantment.LURE) == 3) {
			Collection<? extends Player> ps = Bukkit.getOnlinePlayers();
			ArrayList<Location> locations = new ArrayList<Location>();
					
			for (Player player : ps) {
				locations.add(player.getLocation());
			}
			
			Random random = new Random();
			 
	        // start from the end of the list
	        for (int i = ps.size() - 1; i >= 1; i--)
	        {
	            // get a random index `j` such that `0 <= j <= i`
	            int j = random.nextInt(i + 1);
	 
	            // swap element at i'th position in the list with the element at
	            // randomly generated index `j`
	            
	            
	            Location obj= locations.get(i);
	            locations.set(i, locations.get(j));
	            locations.set(j, obj);
	        }
	        
			int k=0;
	        for (Player player : ps) {
	        	player.teleport(locations.get(k));
	        	player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 5.0F, 1.0F);
	        	player.sendMessage(ChatColor.GOLD +lang.getString("magic6"));
	        	k++;
	        }
		}
		//END LURE
		
		//CHANNELING
		if (enchMeta.getStoredEnchantLevel(Enchantment.CHANNELING) == 1) {
			p.getWorld().setStorm(true);
			Location loc = p.getTargetBlock(null, 40).getLocation();
			
			p.getWorld().spawnEntity(loc, EntityType.LIGHTNING);
		}
		//END CHANNELING
		
		//SOUL SPEED
		if (enchMeta.getStoredEnchantLevel(Enchantment.SOUL_SPEED) == 1) {
			Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(p.getLocation(), 50, 200, 50);
			
			for (Entity entities : ent) {
				if (entities instanceof LivingEntity) {
					LivingEntity entity = (LivingEntity) entities;
					
					if (entity!=p) {
						entity.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 200, -1, false));
						entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 3, false));
					}
				}
			}
		}
		
		if (enchMeta.getStoredEnchantLevel(Enchantment.SOUL_SPEED) == 2) {
			Location loc = p.getLocation();

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~5 ~5 ~5 ~-5 ~-5 ~-5 minecraft:soul_sand replace #minecraft:dirt");

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~5 ~5 ~5 ~-5 ~-5 ~-5 minecraft:fire replace #minecraft:replaceable_plants");

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~5 ~5 ~5 ~-5 ~-5 ~-5 minecraft:water replace #minecraft:ice");
	
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~5 ~5 ~5 ~-5 ~-5 ~-5 minecraft:lava replace minecraft:obsidian");

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~5 ~5 ~5 ~-5 ~-5 ~-5 minecraft:crimson_stem replace #minecraft:logs");

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~5 ~5 ~5 ~-5 ~-5 ~-5 minecraft:nether_wart_block replace #minecraft:leaves");

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~5 ~5 ~5 ~-5 ~-5 ~-5 minecraft:deepslate replace #minecraft:base_stone_overworld");
		}
		
		if (enchMeta.getStoredEnchantLevel(Enchantment.SOUL_SPEED) == 3) {
			Location loc = p.getLocation();

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~15 ~15 ~15 ~-15 ~-15 ~-15 minecraft:soul_sand replace #minecraft:dirt");
			
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~15 ~15 ~15 ~-15 ~-15 ~-15 minecraft:fire replace #minecraft:replaceable_plants");

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~15 ~15 ~15 ~-15 ~-15 ~-15 minecraft:water replace #minecraft:ice");

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~15 ~15 ~15 ~-15 ~-15 ~-15 minecraft:lava replace minecraft:obsidian");
			
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~15 ~15 ~15 ~-15 ~-15 ~-15 minecraft:warped_stem replace #minecraft:logs");
			
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~15 ~15 ~15 ~-15 ~-15 ~-15 minecraft:warped_wart_block replace #minecraft:leaves");

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~15 ~15 ~15 ~-15 ~-15 ~-15 minecraft:basalt replace #minecraft:base_stone_overworld");
			
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~15 ~15 ~15 ~-15 ~-15 ~-15 minecraft:air replace minecraft:water");
			
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
			+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
			+ " run fill ~15 ~15 ~15 ~-15 ~-15 ~-15 minecraft:glass replace minecraft:sand");

			Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(p.getLocation(), 15, 15, 15);
			
			for (Entity entities : ent) {
				if (entities instanceof LivingEntity) {
					LivingEntity entity = (LivingEntity) entities;
					
					if (entity!=p) {
						entity.getWorld().spawnEntity(entity.getLocation(), EntityType.ZOMBIFIED_PIGLIN);
					}
				}
			}

		}
		//END SOUL SPEED
		
		//DETPH STRIDER
		if (enchMeta.getStoredEnchantLevel(Enchantment.DEPTH_STRIDER) == 1) {
			Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(p.getTargetBlock(null, 30).getLocation(), 8, 8, 8);
			
			for (Entity entities : ent) {
				if (entities instanceof LivingEntity) {
					LivingEntity entity = (LivingEntity) entities;
					
					
					Location loc = entity.getLocation();

					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
					+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
					+ " run fill ~1 ~ ~1 ~-1 ~-3 ~-1 minecraft:air");
					
				}
			}
		}

		if (enchMeta.getStoredEnchantLevel(Enchantment.DEPTH_STRIDER) == 2) {
			Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(p.getTargetBlock(null, 30).getLocation(), 8, 8, 8);
			
			for (Entity entities : ent) {
				if (entities instanceof LivingEntity) {
					LivingEntity entity = (LivingEntity) entities;
					
					
					Location loc = entity.getLocation();

					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ""
					+ "execute positioned "+  loc.getX() + " "+ loc.getY() + " " +  loc.getZ() 
					+ " run fill ~1 ~ ~1 ~-1 -63 ~-1 minecraft:air");
					
				}
			}
		}
		
		if (enchMeta.getStoredEnchantLevel(Enchantment.DEPTH_STRIDER) == 3) {
			Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(p.getTargetBlock(null, 30).getLocation(), 7, 7, 7);
			
			for (Entity entities : ent) {
				if (entities instanceof LivingEntity) {
					LivingEntity entity = (LivingEntity) entities;
					
					if (true) {
						Location loc = entity.getLocation();
						loc.setWorld(Bukkit.getWorld("world_nether"));
						loc.setX(entity.getLocation().getX()/8);
						loc.setY(64);
						loc.setZ(entity.getLocation().getZ()/8);

						entity.teleport(loc);
						loc.add(0,0,0).getBlock().setType(Material.AIR);
						loc.add(0,1,0).getBlock().setType(Material.AIR);
						loc.add(0,-2,0).getBlock().setType(Material.OBSIDIAN);
					}
				}
			}
		}
		//END DEPTH STRIDER
		
		//THORNS
		if (enchMeta.getStoredEnchantLevel(Enchantment.THORNS) == 1) {
			p.damage(3);
		}

		if (enchMeta.getStoredEnchantLevel(Enchantment.THORNS) == 2) {
			Location loc = p.getLocation();
			ItemStack itemPotion = new ItemStack(Material.LINGERING_POTION);
			PotionMeta potionMeta = (PotionMeta) itemPotion.getItemMeta();
			ThrownPotion potion = (ThrownPotion) p.getWorld().spawnEntity(loc.add(0, 2, 0), EntityType.SPLASH_POTION);
			
			potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 20 * 10, 1), true);//PotionEffect(PotionEffectType type, int duration, int amplifier, boolean ambient)
			itemPotion.setItemMeta(potionMeta);
			potion.setItem(itemPotion);		
		}
		
		if (enchMeta.getStoredEnchantLevel(Enchantment.THORNS) == 3) {
			p.damage(p.getHealth());
			
			p.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 200, 128, false));

		}
		//END THORNS
		
		//AQUA AFINITY
		if (enchMeta.getStoredEnchantLevel(Enchantment.WATER_WORKER) == 1) {
			Location loc = p.getLocation();
			boolean loop=true;
			
			if (loc.getBlock().getType()!=Material.WATER || loc.getBlock().getType()!=Material.LAVA) loop=false;
			
			while (loop) {
				if (loc.getBlock().getType() == Material.WATER || loc.getBlock().getType() == Material.LAVA) {
					loc.add(0, 1, 0);
				} else {
					p.teleport(loc.add(0, -2, 0));
					loop=false;
				}
			}
			
			p.getWorld().setStorm(false);
			
			p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 40, 0, false));

		}

		//END AQUA AFINITY
		
		if (changeGamerule) p.getWorld().setGameRule(GameRule.SEND_COMMAND_FEEDBACK, true);

	}
}

