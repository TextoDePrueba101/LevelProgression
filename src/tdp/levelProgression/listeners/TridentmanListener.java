package tdp.levelProgression.listeners;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import org.bukkit.ChatColor;
import tdp.levelProgression.LevelProgression;

public class TridentmanListener implements Listener {
	private boolean isRaining=false;
	private FileConfiguration lang= LevelProgression.lang;
	
	public TridentmanListener (LevelProgression plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void tridentInteract(PlayerInteractEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;

		if (isTrident(e.getItem())) tridentVerifier(e.getPlayer(),e.getItem());
	}
	
	@EventHandler
	public void porcentualDamage(EntityDamageByEntityEvent e) {
		if (!LevelProgression.testPluginActive(e.getEntity().getWorld()) || !(e.getEntity() instanceof LivingEntity)) return;
		LivingEntity ent = (LivingEntity) e.getEntity();

		if (e.getDamager() instanceof Trident && ((Trident)e.getDamager()).getShooter() instanceof Player) { 
			Trident tr = (Trident) e.getDamager();
			Player p = (Player) tr.getShooter();
			double dam = e.getDamage();
			int lvl = LevelProgression.pgetData(p, "TRIDENTMAN");
			
			dam = dam+dam*((double)lvl/600);
			if (isRaining) dam = dam + ent.getHealth() * 0.02;
			
			e.setDamage(dam);
		}
		
		if ((e.getDamager() instanceof Player) && ((Player)e.getDamager()).getInventory().getItemInMainHand().getType().equals(Material.TRIDENT)) {
			Player p = (Player) e.getDamager();
			double dam = e.getDamage();
			int lvl = LevelProgression.pgetData(p, "TRIDENTMAN");
			
			dam = dam+dam*((double)lvl/1200);
			if (isRaining) dam = dam + ent.getHealth() * 0.04;
			
			e.setDamage(dam);
		}
	}
	
	@EventHandler
	public void noDrowning(EntityDamageEvent e) {
		if (!LevelProgression.testPluginActive(e.getEntity().getWorld())) return;

		if (e.getCause().equals(DamageCause.DROWNING) && e.getEntity().getType().equals(EntityType.PLAYER) 
		&& LevelProgression.pgetData(((Player)e.getEntity()), "TRIDENTMAN") >=300) {
			e.setCancelled(true);
			Player p = (Player) e.getEntity();
			if ((p.getHealth()+1)<=p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) p.setHealth(p.getHealth() + 1);
		}
		else if (e.getCause().equals(DamageCause.LIGHTNING) && e.getEntity().getType().equals(EntityType.PLAYER) 
		&& LevelProgression.pgetData(((Player)e.getEntity()), "TRIDENTMAN") >=60) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler 
	public void fishRestriction(PlayerFishEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;
		ItemStack mainHand = e.getPlayer().getInventory().getItemInMainHand();
		
		if (mainHand!=null && mainHand.getType().equals(Material.FISHING_ROD)) rodVerifier(e.getPlayer(),mainHand);
		else {
			ItemStack oppoHand = e.getPlayer().getInventory().getItemInOffHand();
			if (oppoHand!=null && oppoHand.getType().equals(Material.FISHING_ROD)) rodVerifier(e.getPlayer(),oppoHand);
		}
	}
	
	@EventHandler
	public void riptideLevitation(PlayerRiptideEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;

		Player p = e.getPlayer();
		if (p.getWorld().hasStorm() && LevelProgression.pgetData(p, "TRIDENTMAN")>=150) {
			if (p.getInventory().getItemInMainHand() != null && p.getInventory().getItemInOffHand() != null
			&& p.getInventory().getItemInMainHand().getType()==Material.TRIDENT && p.getInventory().getItemInOffHand().getType()==Material.TRIDENT) {
				
				p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING,20*3,0,true));
			}
		}
	}
	
	@EventHandler
	public void specialTridentHit(ProjectileHitEvent e) {
		if (!LevelProgression.testPluginActive(e.getEntity().getWorld())) return;
	
		Entity trident = e.getEntity();
		if (trident.getCustomName()==null) return;
				
		ProjectileSource shooter = e.getEntity().getShooter();
		
		if (trident.getCustomName().equals("NETHERITE_INGOT TRIDENT")) {
			Player p = (Player) shooter;
			Location destination = trident.getLocation().clone();
			destination.setDirection(destination.getDirection().multiply(-1));
			p.teleport(destination);
			p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 5.0F, 1.0F);

			Location loc = new Location(p.getWorld(), p.getLocation().getBlockX(),p.getLocation().getBlockY(),p.getLocation().getBlockZ());

			Location clone = loc.clone();
			clone.add(0,-1,0);
			
			for (int y=-1;y<=0;y++) {
				for (int x=-2;x<=2;x++) {
					for (int z=-2;z<=2;z++) {
						Block B = p.getWorld().getBlockAt(
								(int) loc.getX() + x,
								(int) loc.getY() + y,
								(int) loc.getZ() + z);
						
						if (B.getType().isSolid() && !B.getType().isInteractable() && !B.getType().equals(Material.BEDROCK) 
						&& !B.getType().isInteractable() && !B.getPistonMoveReaction().equals(PistonMoveReaction.BLOCK) && !B.getLocation().equals(clone)) {
						
							BlockData Bdata = B.getBlockData();
							B.setType(Material.AIR);
							FallingBlock fB = B.getWorld().spawnFallingBlock(B.getLocation().add(0.5,0,0.5), Bdata);
							
							Vector vecBb = B.getLocation().add(p.getLocation().multiply(-1)).toVector().normalize(); //de p con sentido a B
							vecBb.setY(0);
							vecBb.multiply(0.6);
							
							Vector vec = new Vector(0,0.4,0);
							vec.add(vecBb); 
							
							fB.setVelocity(vec);
							fB.setHurtEntities(false);
							fB.setDropItem(false);
						}
					}
				}
			}
			
			if (isRaining) {
				int n = 2;
				p.getWorld().strikeLightning(p.getLocation().add(n,0,n));
				p.getWorld().strikeLightning(p.getLocation().add(-n,0,n));
				p.getWorld().strikeLightning(p.getLocation().add(n,0,-n));
				p.getWorld().strikeLightning(p.getLocation().add(-n,0,-n));
			}
			
			trident.setCustomName("");
		} 
		else if (trident.getCustomName().equals("GOLD_BLOCK TRIDENT")) {
			int r = 3;
			
			Collection<Entity> ent = trident.getWorld().getNearbyEntities(trident.getLocation(),r,r,r);
			
			for (Entity entities : ent) {
				if (entities instanceof Item) entities.setVelocity(trident.getVelocity().multiply(-1));
				
				if (isRaining && (entities instanceof LivingEntity)) {
					LivingEntity ent1 = (LivingEntity) entities;
					Location loc = ent1.getLocation();
					double jumpY=0.7;
					
					Block b = ent1.getWorld().getBlockAt(loc.getBlockX(),loc.getBlockY()- 1,loc.getBlockZ());
					
					if (b.getType().isSolid() && b.getType().isOccluding()) {
						BlockData B=b.getBlockData();
						b.setType(Material.AIR);
	
						FallingBlock fB = b.getWorld().spawnFallingBlock(b.getLocation().add(0.5,0,0.5), B);
						
						fB.setVelocity(new Vector(0,jumpY,0));
						fB.setHurtEntities(false);
						fB.setDropItem(false);
						
						ent1.setVelocity(ent1.getVelocity().add(new Vector(0,jumpY*1.7,0)));
					}
				}
			}
		} 
		else if (trident.getCustomName().equals("LAPIS_LAZULI TRIDENT")) {			
			if (e.getHitEntity()!=null && (e.getHitEntity() instanceof LivingEntity)) {
				LivingEntity ent = (LivingEntity) e.getHitEntity();
				
				ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,20*3,0,true));
				ent.setVelocity(ent.getVelocity().add(trident.getVelocity().normalize().multiply(-1.9)));
				
				if (isRaining) {
					Location loc = ent.getLocation();
					Block b = ent.getWorld().getBlockAt(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ());
					
					if (!b.getWorld().getEnvironment().equals(Environment.NETHER)) b.setType(Material.WATER);
					//if (b.getType().isAir()) b.setType(Material.WATER);
				}
			}
		} 
		else if (trident.getCustomName().equals("DIAMOND TRIDENT")) {			
			trident.setCustomName("");
			trident.getWorld().createExplosion(trident.getLocation(), 2 ,true,true);
			
			Location loc=trident.getLocation();
			
			Block B = trident.getWorld().getBlockAt(
				loc.getBlockX(),
				loc.getBlockY(),
				loc.getBlockZ());
						
			if (B.getType().isAir() && !B.getWorld().getEnvironment().equals(Environment.NETHER)) B.setType(Material.WATER);
					
			if (isRaining) {
				int d=6;
				Collection<Entity> ent = trident.getWorld().getNearbyEntities(trident.getLocation(),d,d,d);
				
				for (Entity entities : ent) if (entities instanceof LivingEntity) entities.getWorld().strikeLightning(entities.getLocation());
			}
		} 
		else if (trident.getCustomName().equals("COPPER_BLOCK TRIDENT")) {			
			AbstractArrow trid = (AbstractArrow) trident;
			double r = 7;
			
			ItemStack it = ((ThrowableProjectile)trident).getItem();
			LevelProgression.damageItem(it,10);
			((ThrowableProjectile)trident).setItem(it);
			
			Collection<Entity> entities = trident.getNearbyEntities(0, 0, 0);
			entities.clear();
			if (isRaining) entities = trid.getNearbyEntities(r,r,r);
			if (e.getHitEntity() != null) entities.add(e.getHitEntity());
			
			if (entities != null) for (Entity possible : entities) {
				if (possible instanceof LivingEntity && ((LivingEntity) possible).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
					LivingEntity target = (LivingEntity) possible;
					target.attack(target);
					target.sendMessage(ChatColor.GOLD + lang.getString("trident1"));				
				}
			}
		} 
		else if (trident.getCustomName().equals("EMERALD TRIDENT")) {			
			if (e.getHitEntity() != null && e.getHitEntity() instanceof LivingEntity) {
				Player p = (Player) shooter;
				LivingEntity ent = (LivingEntity) e.getHitEntity();
				
				Location loc = ent.getLocation();
				loc.setDirection(ent.getEyeLocation().getDirection());
				loc.add(ent.getEyeLocation().getDirection().normalize().multiply(-1.1));
				
				p.teleport(loc);
				
				if (isRaining) p.getWorld().strikeLightning(p.getLocation());
			}
			
		} 
		else if (trident.getCustomName().equals("PUFFERFISH TRIDENT")) {			
			trident.getWorld().spawnEntity(trident.getLocation(), EntityType.PUFFERFISH);
		} 
		else if (trident.getCustomName().equals("CREEPER_HEAD TRIDENT")) {			
			trident.getWorld().spawnEntity(trident.getLocation(), EntityType.CREEPER);
		} 
		else if (trident.getCustomName().equals("ZOMBIE_HEAD TRIDENT")) {			
			trident.getWorld().spawnEntity(trident.getLocation(), EntityType.DROWNED);
		} 
		else if (trident.getCustomName().equals("SKELETON_SKULL TRIDENT")) {			
			trident.getWorld().spawnEntity(trident.getLocation(), EntityType.SKELETON);
		} 
		else if (trident.getCustomName().equals("WITHER_SKELETON_SKULL TRIDENT")) {			
			trident.getWorld().spawnEntity(trident.getLocation(), EntityType.WITHER_SKELETON);
		} 
	}

	@EventHandler 
	public void tridentLimitationsOnShoot(ProjectileLaunchEvent e) {
		if (!LevelProgression.testPluginActive(e.getEntity().getWorld())) return;
		
		if (e.getEntity() instanceof Trident) {
			Trident t = (Trident) e.getEntity();
			if (!(t.getShooter() instanceof Player)) return;
			
			Player p = (Player) t.getShooter();
			ItemStack oppoHand=null;
			//ItemStack shootHand=null;
			isRaining=p.getWorld().hasStorm();

			if (p.getInventory().getItemInMainHand() != null 
			&& p.getInventory().getItemInMainHand().getType()==Material.TRIDENT) { //el evento sucede antes de que el jugador pierda el tridente, por lo cual para este punto a�n lo tiene en mano
				oppoHand = getOppositeHand(p, EquipmentSlot.HAND);
			} 
			else {
				oppoHand = getOppositeHand(p, EquipmentSlot.OFF_HAND);
				if (oppoHand==null) return;
			}
			
			
			if (oppoHand.getType()==Material.NETHERITE_INGOT && LevelProgression.pgetData(p,"TRIDENTMAN")>=280) {
				LevelProgression.substractAmount(oppoHand,1);
				Entity ent = e.getEntity();
				ent.setCustomName("NETHERITE_INGOT TRIDENT");
				
				potionEffectsOnStorm(p);
			}
			else if (oppoHand.getType()==Material.GOLD_BLOCK && LevelProgression.pgetData(p,"TRIDENTMAN")>=230) {
				LevelProgression.substractAmount(oppoHand,1);
				Entity ent = e.getEntity();
				ent.setCustomName("GOLD_BLOCK TRIDENT");
				
				potionEffectsOnStorm(p);
			}
			else if (oppoHand.getType()==Material.IRON_INGOT && LevelProgression.pgetData(p,"TRIDENTMAN")>=120) {
				LevelProgression.substractAmount(oppoHand,1);
				Entity ent = e.getEntity();

				p.setVelocity(ent.getVelocity().normalize().multiply(1.5));
				ent.setVelocity(ent.getVelocity().multiply(1.5));
				if (isRaining) {
					p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,20*5,1,true));
					p.getWorld().strikeLightning(p.getLocation());
				}
				
				potionEffectsOnStorm(p);
			}
			else if (oppoHand.getType()==Material.LAPIS_LAZULI && LevelProgression.pgetData(p,"TRIDENTMAN")>=90) {
				LevelProgression.substractAmount(oppoHand,1);
				Entity ent = e.getEntity();
				ent.setCustomName("LAPIS_LAZULI TRIDENT");
				
				potionEffectsOnStorm(p);
			}
			else if (oppoHand.getType()==Material.DIAMOND && LevelProgression.pgetData(p,"TRIDENTMAN")>=240) {
				LevelProgression.substractAmount(oppoHand,1);
				Entity ent = e.getEntity();
				ent.setCustomName("DIAMOND TRIDENT");
				
				potionEffectsOnStorm(p);
			}
			else if (oppoHand.getType()==Material.COPPER_BLOCK && LevelProgression.pgetData(p,"TRIDENTMAN")>=195) {
				LevelProgression.substractAmount(oppoHand,1);
				Entity ent = e.getEntity();
				ent.setCustomName("COPPER_BLOCK TRIDENT");
				
				potionEffectsOnStorm(p);
			}
			else if (oppoHand.getType()==Material.EMERALD && LevelProgression.pgetData(p,"TRIDENTMAN")>=225) {
				LevelProgression.substractAmount(oppoHand,1);
				Entity ent = e.getEntity();
				ent.setCustomName("EMERALD TRIDENT");
				
				potionEffectsOnStorm(p);
			}
			else if (oppoHand.getType()==Material.PUFFERFISH) {
				LevelProgression.substractAmount(oppoHand,1);
				Entity ent = e.getEntity();
				ent.setCustomName("PUFFERFISH TRIDENT");
				
				potionEffectsOnStorm(p);
			}
			else if (oppoHand.getType()==Material.CREEPER_HEAD) {
				LevelProgression.substractAmount(oppoHand,1);
				Entity ent = e.getEntity();
				ent.setCustomName("CREEPER_HEAD TRIDENT");
				
				potionEffectsOnStorm(p);
			}
			else if (oppoHand.getType()==Material.ZOMBIE_HEAD) {
				LevelProgression.substractAmount(oppoHand,1);
				Entity ent = e.getEntity();
				ent.setCustomName("ZOMBIE_HEAD TRIDENT");
				
				potionEffectsOnStorm(p);
			}
			else if (oppoHand.getType()==Material.SKELETON_SKULL) {
				LevelProgression.substractAmount(oppoHand,1);
				Entity ent = e.getEntity();
				ent.setCustomName("SKELETON_SKULL TRIDENT");
				
				potionEffectsOnStorm(p);
			}
			else if (oppoHand.getType()==Material.WITHER_SKELETON_SKULL) {
				LevelProgression.substractAmount(oppoHand,1);
				Entity ent = e.getEntity();
				ent.setCustomName("WITHER_SKELETON_SKULL TRIDENT");
				
				potionEffectsOnStorm(p);
			}

			
			
			
		}		
	}
	
	public void potionEffectsOnStorm(Player p) {
		int t=6;
		if (isRaining && LevelProgression.pgetData(p, "TRIDENTMAN")>=150) {
			p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,20*t,0,true));
			p.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER,20*t,0,true));
			p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,20*t,0,true));
		}
	}
	
	//COMPROBATIONS==========================================
	public ItemStack getOppositeHand (Player p, EquipmentSlot slot ) {
		if (slot.equals(EquipmentSlot.HAND)) return p.getInventory().getItemInOffHand();
		else return p.getInventory().getItemInMainHand();
	}
	
	public boolean isTrident(ItemStack item) {
		if (item==null) return false;
		else if (item.getType()==Material.TRIDENT) return true;
		else return false;
	}
	
	public void tridentVerifier(Player p, ItemStack item) {
		if (item.getType()==Material.TRIDENT && LevelProgression.pgetData(p,"TRIDENTMAN") < 25) {
			p.sendMessage(ChatColor.RED +lang.getString("trident2"));
			if (!LevelProgression.breakItemWhenDenied) {
				ItemStack itemCloned = item.clone();
				item.setAmount(0);
				p.getWorld().dropItemNaturally(p.getLocation(), itemCloned); 
				p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 5.0F, 1F);
				return;
			}
			item.setAmount(0);
			p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 5.0F, 1F);
		} else {
			enchVerifier(p,item);
		}
	}
	
	public void rodVerifier(Player p, ItemStack item) {
		if (item.getType()==Material.FISHING_ROD && LevelProgression.pgetData(p,"TRIDENTMAN") < 1) {
			p.sendMessage(ChatColor.RED +lang.getString("trident3"));
			if (!LevelProgression.breakItemWhenDenied) {
				ItemStack itemCloned = item.clone();
				item.setAmount(0);
				p.getWorld().dropItemNaturally(p.getLocation(), itemCloned); 
				p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 5.0F, 1F);
				return;
			}
			item.setAmount(0);
			p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 5.0F, 1F);
		} else {
			enchVerifierRod(p,item);
		}
	}
	
	public void enchVerifierRod(Player p , ItemStack item) {
		if (item.getItemMeta() != null) {
			
			if (item.getItemMeta().getEnchantLevel(Enchantment.MENDING)>0 && LevelProgression.pgetData(p, "TRIDENTMAN") <5) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.LURE)>0 && LevelProgression.pgetData(p, "TRIDENTMAN") <2) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.LURE)>1 && LevelProgression.pgetData(p, "TRIDENTMAN") <6) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.LURE)>2 && LevelProgression.pgetData(p, "TRIDENTMAN") <9) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.LUCK)>0 && LevelProgression.pgetData(p, "TRIDENTMAN") <3) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.LUCK)>1 && LevelProgression.pgetData(p, "TRIDENTMAN") <7) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.LUCK)>2 && LevelProgression.pgetData(p, "TRIDENTMAN") <10) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY)>0 && LevelProgression.pgetData(p, "TRIDENTMAN") <4) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY)>1 && LevelProgression.pgetData(p, "TRIDENTMAN") <8) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY)>2 && LevelProgression.pgetData(p, "TRIDENTMAN") <10) enchDenied(p, item);
		}
	}

	public void enchVerifier(Player p , ItemStack item) {
		if (item.getItemMeta() != null) {
			
			if (item.getItemMeta().getEnchantLevel(Enchantment.CHANNELING)>0 && LevelProgression.pgetData(p, "TRIDENTMAN") <100) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY)>0 && LevelProgression.pgetData(p, "TRIDENTMAN") <70) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY)>1 && LevelProgression.pgetData(p, "TRIDENTMAN") <170) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY)>2 && LevelProgression.pgetData(p, "TRIDENTMAN") <270) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.LOYALTY)>0 && LevelProgression.pgetData(p, "TRIDENTMAN") <80) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.LOYALTY)>1 && LevelProgression.pgetData(p, "TRIDENTMAN") <140) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.LOYALTY)>2 && LevelProgression.pgetData(p, "TRIDENTMAN") <220) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.RIPTIDE)>0 && LevelProgression.pgetData(p, "TRIDENTMAN") <110) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.RIPTIDE)>1 && LevelProgression.pgetData(p, "TRIDENTMAN") <180) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.RIPTIDE)>2 && LevelProgression.pgetData(p, "TRIDENTMAN") <260) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.MENDING)>0 && LevelProgression.pgetData(p, "TRIDENTMAN") <250) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.IMPALING)>0 && LevelProgression.pgetData(p, "TRIDENTMAN") <30) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.IMPALING)>1 && LevelProgression.pgetData(p, "TRIDENTMAN") <90) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.IMPALING)>2 && LevelProgression.pgetData(p, "TRIDENTMAN") <160) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.IMPALING)>3 && LevelProgression.pgetData(p, "TRIDENTMAN") <210) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.IMPALING)>4 && LevelProgression.pgetData(p, "TRIDENTMAN") <290) enchDenied(p, item);
		}
	}
	
	public void enchDenied(Player p , ItemStack item) {
		ItemStack itemCloned = item.clone();
		item.setAmount(0);
		p.getWorld().dropItemNaturally(p.getLocation(), itemCloned); 
		p.sendMessage(ChatColor.RED + lang.getString("enchDenied"));
		p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 5.0F, 1F);
	}
	
}
