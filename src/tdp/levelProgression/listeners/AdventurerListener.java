package tdp.levelProgression.listeners;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


import tdp.levelProgression.LevelProgression;

public class AdventurerListener implements Listener{
	private FileConfiguration lang = LevelProgression.lang;
	
	public AdventurerListener(LevelProgression plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void boatLimitations(VehicleMoveEvent e) {
		if (!LevelProgression.testPluginActive(e.getVehicle().getWorld())) return;

		Vehicle boat = e.getVehicle();
		if (boat.getType().equals(EntityType.BOAT) || boat.getType().equals(EntityType.CHEST_BOAT)) {
			List<Entity> entities = boat.getPassengers();
			
			if (entities.size() >0 && entities.get(0) instanceof Player) {
				Player p = (Player) entities.get(0);
					
				if (LevelProgression.pgetData(p, "ADVENTURER") <25) {
						
					boat.eject();
						
					p.sendMessage(ChatColor.RED +lang.getString("adventurer1"));
						
				}

			}
			
		}
	}
	
	
	@EventHandler
	public void adventureInteractonLimitations(PlayerInteractEntityEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;

		if (e.getRightClicked().getType() == EntityType.VILLAGER && LevelProgression.pgetData(e.getPlayer(), "ADVENTURER") <150) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + lang.getString("adventurer2"));
		}
		
		if (e.getRightClicked().getType() == EntityType.STRIDER && LevelProgression.pgetData(e.getPlayer(), "ADVENTURER") <75) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + lang.getString("adventurer3"));
		}

		if (e.getRightClicked().getType() == EntityType.HORSE && LevelProgression.pgetData(e.getPlayer(), "ADVENTURER") <50) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + lang.getString("adventurer4"));
		}
		
		
	
	}
	
	@EventHandler
	private void entityCCed(EntityDamageByEntityEvent e) { // CC DE AVENTURERO
		if (!LevelProgression.testPluginActive(e.getEntity().getWorld())) return;
		
		if (e.getCause() == DamageCause.ENTITY_ATTACK) {
			if (e.getDamager().getType() == EntityType.PLAYER) {
				Player p = (Player) e.getDamager();
				if (LevelProgression.pgetData( p, "ADVENTURER") >=130 && p.getInventory().getItemInMainHand().getType() == Material.SPYGLASS 
					&& LevelProgression.pgetData(p, "CC1") == 0 && e.getEntity() instanceof LivingEntity) {
					LevelProgression.psetData(p, "CC1", 3);
					LivingEntity ent = (LivingEntity) e.getEntity();
					if (ent instanceof Player) {
						ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 6, false));
						ent.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 128, false));
					} else {
						ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 5, 6, false));
					}
				}
			}
			if (e.getEntity() instanceof Player && LevelProgression.pgetData((Player)e.getEntity(), "ADVENTURER")>=200 && ((Math.random() * 1000 + 1)<=85)) {
				e.setCancelled(true);
				((Player)e.getEntity()).playSound(e.getEntity(), Sound.ENTITY_PLAYER_ATTACK_NODAMAGE, 5.0F, 1.0F);
			}
		}
	}
	
	@EventHandler
	public void PlayerRespawnPersistent(PlayerRespawnEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;
		Player p = e.getPlayer();
		
		if (!e.isBedSpawn() && hasPspawn(p)) {
			p.sendMessage(ChatColor.ITALIC + "Persistent respawn point used");
			
			e.setRespawnLocation(getPspawn(p).add(0.5,0.1,0.5));
			
			removePspawn(p);
		}
	}
	
	@EventHandler
	public void mapHoldingRestrictionAndSecondHability (PlayerInteractEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;
		Player p = e.getPlayer();
		mapAllow(p);
			
			if (e.getClickedBlock()!=null && e.getAction() == Action.LEFT_CLICK_BLOCK  
			&& LevelProgression.pgetData(p, "ADVENTURER")>= 95) {
				if (e.getClickedBlock().getType() == Material.CAMPFIRE || e.getClickedBlock().getType() == Material.SOUL_CAMPFIRE) {
					
					Location l = e.getClickedBlock().getLocation();
					l.add(0, 1, 0);
					setPspawn(p,l);
					
					p.sendMessage(ChatColor.ITALIC + "Persistent respawn point set");
				}
			}
			
			if (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.ENDER_CHEST && LevelProgression.pgetData(p, "ADVENTURER") >=150) {
				Block b = e.getClickedBlock();
				b.setType(Material.AIR);
				b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.ENDER_CHEST));
			}
			
			if (e.getClickedBlock()!=null && e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.SHULKER_BOX && LevelProgression.pgetData(p, "ADVENTURER") < 250 ) {
				p.sendMessage(ChatColor.RED + lang.getString("adventurer5"));
				e.setCancelled(true);
			}
		
			if (e.getHand()== EquipmentSlot.OFF_HAND && e.getAction()!= Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK) {
				//======================================tp
				if (p.getInventory().getItemInOffHand() != null && e.getAction()==Action.RIGHT_CLICK_AIR
				&& p.getInventory().getItemInOffHand().getType() == Material.SPYGLASS 
				&& LevelProgression.pgetData(p, "ADVENTURER") >=300 && p.getPose() == Pose.SNEAKING) 
				{
					if (p.getLevel() < 2) {
						p.sendMessage(ChatColor.RED + lang.getString("adventurer6"));
					} else {
						Location tpDestination = p.getTargetBlock(null, 30).getLocation();
						p.teleport(tpDestination);
						p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 5.0F, 1.0F);
						
						int n = p.getLevel() - 2;
						p.setLevel(n);
					}
				}
				//======================================escape
				if (p.getInventory().getItemInOffHand() != null && p.getInventory().getItemInOffHand().getType() == Material.COMPASS 
				&& LevelProgression.pgetData(p, "ADVENTURER") >=230 && p.getPose() == Pose.SNEAKING) {
					if (p.getLevel() < 10) {
						p.sendMessage(ChatColor.RED +lang.getString("adventurer7"));
					} else {
						p.playSound(p, Sound.BLOCK_BREWING_STAND_BREW, 5.0F, 1.0F);
						p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000, 0, true));
						p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 1000, 0, true));
						p.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + lang.getString("adventurer8"));
						if (LevelProgression.pgetData(p, "ADVENTURER") >=270) {
							p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000, 0, true));
							p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 1000, 1, true));
							p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 1000, 0, true));
							p.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 1000, 0, true));
						}
						
						int n = p.getLevel() - 10;
						p.setLevel(n);
					}
				}
		}
	}
	
	public Location getPspawn(Player p) {
		int[] bLoc = p.getPersistentDataContainer().get(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "spawnLoation"), PersistentDataType.INTEGER_ARRAY);
		Location loc = new Location (Bukkit.getWorld(LevelProgression.worldsName.get(bLoc[3])),bLoc[0],bLoc[1],bLoc[2]); 
		
		return loc;
	}
	
	public void removePspawn(Player p) {
		p.getPersistentDataContainer().remove(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "spawnLoation"));
	}
	
	public boolean hasPspawn(Player p) {
		if (p.getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "spawnLoation"), PersistentDataType.INTEGER_ARRAY))
			return true;
		else return false;
	}
	
	public void setPspawn(Player p,Location loc) {
		int worldIndex = LevelProgression.worldsName.indexOf(loc.getWorld().getName());
		int[] larray= {loc.getBlockX(),loc.getBlockY(),loc.getBlockZ(),worldIndex};
		p.getPersistentDataContainer().set(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "spawnLoation"), PersistentDataType.INTEGER_ARRAY, larray);
	}
	
	public void mapAllow(Player p) {
		if (p.getInventory().getItemInMainHand().getType() == Material.MAP || p.getInventory().getItemInMainHand().getType() == Material.FILLED_MAP ||
		p.getInventory().getItemInOffHand().getType() == Material.MAP || p.getInventory().getItemInOffHand().getType() == Material.FILLED_MAP) {
			if (LevelProgression.pgetData(p, "ADVENTURER") < 100) {
				ItemStack mainHand = p.getInventory().getItemInMainHand();
				ItemStack offHand = p.getInventory().getItemInOffHand();
				ItemStack itemCloned = mainHand.clone();

				if (mainHand.getType() == Material.MAP || mainHand.getType() == Material.FILLED_MAP) {
					itemCloned = mainHand.clone();
					mainHand.setAmount(0);
				}
				if (offHand.getType() == Material.MAP || offHand.getType() == Material.FILLED_MAP) {
					itemCloned = offHand.clone();
					offHand.setAmount(0);
				}
				
				p.getWorld().dropItemNaturally(p.getLocation(), itemCloned); 
				p.sendMessage(ChatColor.RED + "A�n no sabes leer mapas");
				p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 5.0F, 1F);
			}
		}

	}

}