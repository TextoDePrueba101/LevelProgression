package tdp.levelProgression.listeners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import org.bukkit.ChatColor;
import tdp.levelProgression.LevelProgression;

public class ArcherListener implements Listener {
	private FileConfiguration lang= LevelProgression.lang;
	
	public ArcherListener (LevelProgression plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler 
	public void bowLimitationsOnShoot(PlayerInteractEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;

		Player p = e.getPlayer();
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;
		
		if (isBow(p.getInventory().getItemInMainHand())) bowVerifier(p, p.getInventory().getItemInMainHand());
		if (isBow(p.getInventory().getItemInOffHand())) bowVerifier(p, p.getInventory().getItemInOffHand());	
	}
	
	@EventHandler
	public void distanceDamage(EntityDamageByEntityEvent e){
		if (!LevelProgression.testPluginActive(e.getEntity().getWorld())) return;
		
		if (e.getDamager() instanceof Arrow && ((Arrow)e.getDamager()).getShooter() instanceof Player 
				&& LevelProgression.pgetData(((Player)((Arrow)e.getDamager()).getShooter()), "ARCHER")>=150 ) {
			Arrow a = (Arrow) e.getDamager();
			Player p = (Player) a.getShooter();
			
			double fDam= e.getDamage();
			double distance = p.getLocation().distance(e.getEntity().getLocation());
			if (distance>100) distance=100;
			fDam = fDam* (1+(
					distance*distance*5/(100000)
					));

			e.setDamage(fDam);
		}
	}
	
	@EventHandler
	public void specialArrowHit(ProjectileHitEvent e) {
		if (!LevelProgression.testPluginActive(e.getEntity().getWorld())) return;
		Entity arrow = e.getEntity();
		if (arrow.getCustomName()==null) return;
		
		ProjectileSource shooter = e.getEntity().getShooter();
		
		if (arrow.getCustomName().equals("NETHERITE_INGOT BOW")) {
			Player p = (Player) shooter;
			p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20* 60,0,true));
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20* 60,0,true));
			p.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 20* 400,0,true));
			p.teleport(arrow);
			p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 5.0F, 1.0F);
		} 
		else if (arrow.getCustomName().equals("NETHERITE_INGOT CROSSBOW")) {
			if (e.getHitEntity()!= null) e.getHitEntity().getWorld().createExplosion(e.getHitEntity().getLocation(), 2,true,false);
			else e.getHitBlock().getWorld().createExplosion(e.getHitBlock().getLocation(), 4,true,true);
		} 
		else if (arrow.getCustomName().equals("DIAMOND BOW")) {			
			Collection<Entity> ent = arrow.getWorld().getNearbyEntities(arrow.getLocation(),8,8,8);
			
			for (Entity entities : ent) {
				if (entities instanceof LivingEntity) {
					LivingEntity entity = (LivingEntity) entities;
					
					entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 5,0 ,false));
				}
			}
		} 
		else if (arrow.getCustomName().equals("DIAMOND CROSSBOW")) {			
			if (e.getHitBlock()!= null) {
				Block b = e.getHitBlock();
				
				if (b.getType()!=Material.BEDROCK && b.getPistonMoveReaction()!=PistonMoveReaction.BLOCK && b.getType()!=Material.WATER && b.getType()!=Material.LAVA && b.getType()!=Material.AIR && !b.getType().isInteractable()) {
					Vector vec = arrow.getVelocity().normalize().multiply(0.6);
					vec.add(new Vector(0,0.3,0));
					FallingBlock fB = b.getWorld().spawnFallingBlock(b.getLocation().add(0.5,0,0.5), b.getBlockData());
					fB.setVelocity(vec);
					fB.setDropItem(true);
					b.setType(Material.AIR);
					arrow.remove();
				}
			}
		} 
		else if (arrow.getCustomName().equals("IRON_INGOT BOW")) {			
			if (e.getHitEntity()!= null) {
				Entity ent = e.getHitEntity();
				if (ent instanceof LivingEntity) {

					((LivingEntity) ent).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS , 20 * 4 , 0));
					((LivingEntity) ent).addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION , 20 * 8 , 3));
					ent.playEffect(EntityEffect.VILLAGER_ANGRY);
					ent.sendMessage(ChatColor.GOLD +lang.getString("archer1"));
				}
			}
		} 
		else if (arrow.getCustomName().equals("GOLD_INGOT BOW")) {			
			Collection<Entity> ent = arrow.getWorld().getNearbyEntities(arrow.getLocation(),6,6,6);
			
			for (Entity entities : ent) {
				if (entities instanceof LivingEntity) {
					LivingEntity entity = (LivingEntity) entities;
					
					entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 5,0 ,false));
				}
			}
		} 
		else if (arrow.getCustomName().equals("GOLD_INGOT CROSSBOW")) {			
			if (e.getHitEntity()!= null && e.getHitEntity() instanceof LivingEntity && !e.getHitEntity().getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "boss"), PersistentDataType.INTEGER)) {
				LivingEntity ent = (LivingEntity) e.getHitEntity();
				ItemStack item = ent.getEquipment().getItemInMainHand();
				
				if (!item.getType().isAir()) {
					ent.getWorld().dropItemNaturally(ent.getLocation(), item);
					ent.getEquipment().setItemInMainHand(null);
					ent.sendMessage(ChatColor.GOLD +lang.getString("archer2"));
				}
			}
		} 
		else if (arrow.getCustomName().equals("COPPER_INGOT")) {			
			if (e.getHitEntity() instanceof LivingEntity) {
				LivingEntity hitted = (LivingEntity) e.getHitEntity();
				if (hitted.getWorld().hasStorm()) hitted.getWorld().strikeLightning(hitted.getLocation());
			}
		} 
		else if (arrow.getCustomName().equals("LAPIS_LAZULI")) {			
			if (e.getHitBlock()!= null) {
				Block b = e.getHitBlock();
				LivingEntity target = getNearestLivingEntity(b.getLocation(),6,arrow,(Entity) shooter);
				
				if (target==null) return; //THIS CAN CAUSE PROBLEMS===========================================================================================don't do it
				
				Vector vec = target.getLocation().toVector().add(arrow.getLocation().toVector().multiply(-1));
				
				Arrow arrowClone = (Arrow) arrow.getWorld().spawnEntity(arrow.getLocation(), EntityType.ARROW);
				arrowClone.setBasePotionData(((Arrow)arrow).getBasePotionData());
				arrowClone.setDamage(((Arrow)arrow).getDamage());
				arrowClone.setVelocity(vec.normalize().multiply(3));
			}
		} 
		else if (arrow.getCustomName().equals("EMERALD")) {			
			if (e.getHitBlock()!= null) {
				Block b = e.getHitBlock();				
				
				Collection<Entity> entities = b.getWorld().getNearbyEntities(b.getLocation(),7,7,7);
				entities.remove(arrow);
				entities.remove(((Entity)shooter));
				for (Entity ent : entities) {
					if (ent instanceof LivingEntity) {
						LivingEntity target = (LivingEntity) ent;
						
						Vector vec = target.getLocation().toVector().add(arrow.getLocation().toVector().multiply(-1));						
						Arrow arrowClone = (Arrow) arrow.getWorld().spawnEntity(arrow.getLocation(), EntityType.ARROW);
						arrowClone.setBasePotionData(((Arrow)arrow).getBasePotionData());
						arrowClone.setDamage(((Arrow)arrow).getDamage());
						arrowClone.setVelocity(vec.multiply(3));
					}
				}
			}
		}
		else if (arrow.getCustomName().equals("REDSTONE")) {			
			if (e.getHitEntity() instanceof LivingEntity) {
				LivingEntity hitted = (LivingEntity) e.getHitEntity();
				
				hitted.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 20 * 15, 0, true));
			}
		} 
		else if (arrow.getCustomName().equals("COAL_BLOCK")) {			
			if (e.getHitEntity() instanceof Player) {
				Player p = (Player) e.getHitEntity();

				ItemStack[] items = p.getInventory().getContents().clone();
				List<ItemStack> lit = new ArrayList<ItemStack>();
				
				for (int n=9;n<=35;n++) {
					if (items[n]==null) items[n]=new ItemStack(Material.AIR);
					lit.add(items[n].clone());
					items[n]=null;
				}
				Collections.shuffle(lit);
				
				int i=9;
				for (ItemStack it : lit) {
					items[i]=it;
					i++;
				}
				
				p.getInventory().setContents(items);
			}
		} 		
	}
	
	@EventHandler 
	public void bowLimitationsOnShoot(EntityShootBowEvent e) {
		if (!LevelProgression.testPluginActive(e.getEntity().getWorld())) return;

		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			ItemStack oppoHand = getOppositeHand(p, e.getHand());
			ItemStack shootHand = e.getBow();
			
			for (ItemStack i : p.getEquipment().getArmorContents()) { //Mas armadura = mas da�o al arco
				if (i!=null) LevelProgression.damageItem(p, e.getBow(), 1);
			}
			
			if (shootHand.getItemMeta().getLocalizedName().equals("INFINITE_CROSSBOW") && LevelProgression.pgetData(p,"ARCHER")>=100) {
				Boolean test=true; 
				
				for (ItemStack item : p.getInventory()) if (test && item!=null && item.getType()==Material.ARROW && item.getAmount()<item.getMaxStackSize()) {
					item.setAmount((1+item.getAmount()));
					test=false;
				}
				
				if (test) p.getWorld().dropItem(p.getLocation(),new ItemStack(Material.ARROW));
				
				((Arrow) e.getProjectile()).setPickupStatus(PickupStatus.CREATIVE_ONLY);
				
			}
			
			
			if (oppoHand.getType()==Material.NETHERITE_INGOT && shootHand.getType()==Material.BOW && LevelProgression.pgetData(p,"ARCHER")>=280) {
				LevelProgression.substractAmount(oppoHand,1);
				Entity ent = e.getProjectile();
				ent.setCustomName("NETHERITE_INGOT BOW");
			}
			else if (oppoHand.getType()==Material.NETHERITE_INGOT && shootHand.getType()==Material.CROSSBOW && LevelProgression.pgetData(p,"ARCHER")>=270) {
				LevelProgression.substractAmount(oppoHand,1);
				Entity ent = e.getProjectile();
				ent.setCustomName("NETHERITE_INGOT CROSSBOW");
			}
			else if (oppoHand.getType()==Material.DIAMOND && shootHand.getType()==Material.BOW && LevelProgression.pgetData(p,"ARCHER")>=80) {
				LevelProgression.substractAmount(oppoHand,1);
				Entity ent = e.getProjectile();
				ent.setCustomName("DIAMOND BOW");
				
				Collection<Entity> ents = p.getWorld().getNearbyEntities(p.getLocation(),20,20,20);
				
				for (Entity entities : ents) {
					if (entities instanceof LivingEntity) {
						LivingEntity entity = (LivingEntity) entities;
						
						entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 5,0 ,false));
					}
				}
			}
			else if (oppoHand.getType()==Material.DIAMOND && shootHand.getType()==Material.CROSSBOW && LevelProgression.pgetData(p,"ARCHER")>=140) {
				LevelProgression.substractAmount(oppoHand,1);
				Entity ent = e.getProjectile();
				ent.setCustomName("DIAMOND CROSSBOW");
			}
			else if (oppoHand.getType()==Material.IRON_INGOT && shootHand.getType()==Material.BOW && LevelProgression.pgetData(p,"ARCHER")>=40) {
				LevelProgression.substractAmount(oppoHand,1);
				Entity ent = e.getProjectile();
				ent.setCustomName("IRON_INGOT BOW");
			}
			else if (oppoHand.getType()==Material.IRON_INGOT && shootHand.getType()==Material.CROSSBOW && LevelProgression.pgetData(p,"ARCHER")>=40) {
				LevelProgression.substractAmount(oppoHand,1);
				Entity ent = e.getProjectile();

				p.setVelocity(ent.getVelocity().normalize().multiply(-0.8));

				ent.setCustomName("IRON_INGOT BOW");
			}
			else if (oppoHand.getType()==Material.GOLD_INGOT && shootHand.getType()==Material.BOW && LevelProgression.pgetData(p,"ARCHER")>=110) {
				LevelProgression.substractAmount(oppoHand,1);
				Entity ent = e.getProjectile();
				ent.setCustomName("GOLD_INGOT BOW");
			}
			else if (oppoHand.getType()==Material.GOLD_BLOCK && shootHand.getType()==Material.CROSSBOW && LevelProgression.pgetData(p,"ARCHER")>=170) {
				LevelProgression.substractAmount(oppoHand,1);
				Entity ent = e.getProjectile();
				ent.setCustomName("GOLD_INGOT CROSSBOW");
			}
			else if (oppoHand.getType()==Material.COPPER_BLOCK && LevelProgression.pgetData(p,"ARCHER")>=240) {
				LevelProgression.substractAmount(oppoHand,1);
				Entity ent = e.getProjectile();
				ent.setCustomName("COPPER_INGOT");

			}
			else if (oppoHand.getType()==Material.COAL_BLOCK && LevelProgression.pgetData(p,"ARCHER")>=20) {
				LevelProgression.substractAmount(oppoHand,1);
				Entity ent = e.getProjectile();
				ent.setCustomName("COAL_BLOCK");
			}
			else if (oppoHand.getType()==Material.LAPIS_LAZULI) {
					LevelProgression.substractAmount(oppoHand,1);
					Entity ent = e.getProjectile();
					ent.setCustomName("LAPIS_LAZULI");
			}
			else if (oppoHand.getType()==Material.EMERALD) {
					LevelProgression.substractAmount(oppoHand,1);
					Entity ent = e.getProjectile();
					ent.setCustomName("EMERALD");
			}
			else if (oppoHand.getType()==Material.REDSTONE) {
					LevelProgression.substractAmount(oppoHand,1);
					Entity ent = e.getProjectile();
					ent.setCustomName("REDSTONE");
			}


			
		}		
	}
	
	public LivingEntity getNearestLivingEntity(Location loc,int r,Entity exeption,Entity exeption2) {
		Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc,r,r,r);
		entities.remove(exeption);
		if (exeption2!=null) entities.remove(exeption2);
		LivingEntity closest = null;
		
		for (Entity ent : entities) {
			double minD=r*1.73+1;
			if (ent instanceof LivingEntity) {
				double D = ent.getLocation().distance(loc);
				if (minD>D) {
					minD=D;
					closest=(LivingEntity) ent;
				}
			}
		}
		return closest;
	}
	
	public ItemStack getOppositeHand (Player p, EquipmentSlot slot ) {
		if (slot.equals(EquipmentSlot.HAND)) return p.getInventory().getItemInOffHand();
		else return p.getInventory().getItemInMainHand();
	}
	
	public boolean isBow(ItemStack item) {
		if (item==null) return false;
		else if (item.getType()==Material.BOW || item.getType()==Material.CROSSBOW) return true;
		else return false;
	}
	
	public void bowVerifier(Player p, ItemStack item) {
		if (item.getType()==Material.BOW && LevelProgression.pgetData(p,"ARCHER") < 50) {
			p.sendMessage(ChatColor.RED + lang.getString("archer3"));
			
			if (!LevelProgression.breakItemWhenDenied) {
				ItemStack itemCloned = item.clone();
				item.setAmount(0);
				p.getWorld().dropItemNaturally(p.getLocation(), itemCloned); 
				p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 5.0F, 1F);
				return;
			}

			item.setAmount(0);
			p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 5.0F, 1F);
		} 
		else if (item.getType()==Material.CROSSBOW && LevelProgression.pgetData(p,"ARCHER") < 10) {
			p.sendMessage(ChatColor.RED + lang.getString("archer4"));
			
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
	
	public void enchVerifier(Player p , ItemStack item) {
		if (item.getItemMeta() != null) {
			
			if (item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY)>0 && LevelProgression.pgetData(p, "ARCHER") <90) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY)>1 && LevelProgression.pgetData(p, "ARCHER") <200) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY)>2 && LevelProgression.pgetData(p, "ARCHER") <250) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.ARROW_KNOCKBACK)>0 && LevelProgression.pgetData(p, "ARCHER") <70) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.ARROW_KNOCKBACK)>1 && LevelProgression.pgetData(p, "ARCHER") <170) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.MENDING)>0 && LevelProgression.pgetData(p, "ARCHER") <260) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.ARROW_FIRE)>0 && LevelProgression.pgetData(p, "ARCHER") <180) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.ARROW_INFINITE)>0 && LevelProgression.pgetData(p, "ARCHER") <100) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.MULTISHOT)>0 && LevelProgression.pgetData(p, "ARCHER") <160) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.QUICK_CHARGE)>0 && LevelProgression.pgetData(p, "ARCHER") <20) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.QUICK_CHARGE)>1 && LevelProgression.pgetData(p, "ARCHER") <130) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.QUICK_CHARGE)>2 && LevelProgression.pgetData(p, "ARCHER") <220) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.PIERCING)>0 && LevelProgression.pgetData(p, "ARCHER") <30) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.PIERCING)>1 && LevelProgression.pgetData(p, "ARCHER") <120) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.PIERCING)>2 && LevelProgression.pgetData(p, "ARCHER") <190) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.PIERCING)>3 && LevelProgression.pgetData(p, "ARCHER") <290) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.ARROW_DAMAGE)>0 && LevelProgression.pgetData(p, "ARCHER") <60) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.ARROW_DAMAGE)>1 && LevelProgression.pgetData(p, "ARCHER") <100) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.ARROW_DAMAGE)>2 && LevelProgression.pgetData(p, "ARCHER") <150) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.ARROW_DAMAGE)>3 && LevelProgression.pgetData(p, "ARCHER") <230) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.ARROW_DAMAGE)>4 && LevelProgression.pgetData(p, "ARCHER") <300) enchDenied(p, item);
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
