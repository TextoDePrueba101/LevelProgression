package tdp.levelProgression.listeners;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.util.Vector;

import tdp.levelProgression.LevelProgression;

public class SoldierListener implements Listener{
	private FileConfiguration lang = LevelProgression.lang;
	
	public SoldierListener(LevelProgression plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		if (!LevelProgression.testPluginActive(e.getEntity().getWorld())) return;

		if (!(e.getEntity() instanceof LivingEntity));
		else if (e.getDamager() instanceof Player) {
			Player p = (Player) e.getDamager();
			ItemStack offHand = p.getInventory().getItemInOffHand();
			
			if (isSword(p, p.getInventory().getItemInMainHand())) swordVerifier(p, p.getInventory().getItemInMainHand());
			if (isSword(p, offHand) && p.getAttackCooldown()==1) {
				swordVerifier(p, p.getInventory().getItemInOffHand());
				
				double damm=e.getDamage();
				double abss=p.getAbsorptionAmount();
				
				p.swingOffHand();
				if (offHand.getType()== Material.NETHERITE_SWORD && LevelProgression.pgetData(p, "SOLDIER") >= 250) {				
					damm=damm+4;
					if (abss<10) abss += 1;
						
					damageItem(p, offHand, 1);
				}
				else if (offHand.getType()== Material.DIAMOND_SWORD && LevelProgression.pgetData(p, "SOLDIER") >= 200) {				
					damm=damm+3;
					if (abss<8) abss += 1;

					damageItem(p, offHand, 1);
				}
				else if (offHand.getType()== Material.IRON_SWORD && LevelProgression.pgetData(p, "SOLDIER") >= 100) {				
					damm=damm+2;
					if (abss<6) abss += 1;
				
					damageItem(p, offHand, 1);
				}
				else if (offHand.getType()== Material.STONE_SWORD && LevelProgression.pgetData(p, "SOLDIER") >= 50) {				
					damm=damm+1;
					if (abss<4) abss += 1;
				
					damageItem(p, offHand, 1);
				}
				
				p.setAbsorptionAmount(abss);
				e.setDamage(damm);
				//Bukkit.broadcastMessage("dam "+damm);
			}
		}
		
	}
		
	@EventHandler
	public void interactLimitations(PlayerInteractEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;

		Player p = e.getPlayer();
		ItemStack mainHand=p.getInventory().getItemInMainHand().clone();
		ItemStack offHand=p.getInventory().getItemInOffHand().clone();
				
		boolean bothSwords = true;
		
		if (isSword(p, p.getInventory().getItemInMainHand())) swordVerifier(p, p.getInventory().getItemInMainHand()); 
		if (!isSword(p, p.getInventory().getItemInMainHand())) bothSwords=false;
		
		if (isSword(p, p.getInventory().getItemInOffHand())) swordVerifier(p, p.getInventory().getItemInOffHand());
		if (!isSword(p, p.getInventory().getItemInOffHand())) bothSwords=false;

		
		//Swap hands
		if (bothSwords && p.getCooldown(mainHand.getType())==0 && p.getCooldown(offHand.getType())==0
				&& e.getHand()!=null && e.getHand().equals(EquipmentSlot.HAND) && !e.getAction().equals(Action.LEFT_CLICK_AIR) && !e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
			
			swapItemInHands(p,mainHand,offHand);
			p.swingOffHand();
			p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_NODAMAGE, 5.0F, 1.0F);
			
			p.setCooldown(mainHand.getType(), 10);
			p.setCooldown(offHand.getType(), 10);
		}
	}

	@EventHandler
	public void abilityAtSwaped(PlayerSwapHandItemsEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;

		Player p = e.getPlayer();
		ItemStack mainHand = p.getInventory().getItemInMainHand();
		
		if (isSword(p, mainHand)) swordVerifier(p, mainHand);
		
		if (isSword(p, mainHand) && !p.getPose().equals(Pose.SNEAKING)) {
			e.setCancelled(true);
			
			if (LevelProgression.pgetData(p, "CC1")==0) {
			
				if (LevelProgression.pgetData(p,"SOLDIER") >= 50 && mainHand.getType() == Material.WOODEN_SWORD) {
					LevelProgression.psetData(p,"CC1", 12);
					Entity ent = LevelProgression.getTargetEntity(p);
					if (ent instanceof LivingEntity) {
						LivingEntity ent1 = (LivingEntity) ent;
						if (ent1.getLocation().distance(p.getLocation()) < 10) {
							Location loc2=p.getLocation();
							Location loc1=ent1.getLocation();
							Vector launchVector = loc2.toVector().subtract(loc1.toVector());
							launchVector.multiply(0.4);
							launchVector.setY(Math.abs(launchVector.getY()));
							
							ent1.setVelocity(launchVector);
							damageItem(p, mainHand,1);
							
						} else if (ent1.getLocation().distance(p.getLocation()) < 7) {
							Location loc2=p.getLocation();
							Location loc1=ent1.getLocation();
							Vector launchVector = loc2.toVector().subtract(loc1.toVector());
							launchVector.multiply(0.2);
							
							ent1.setVelocity(launchVector);
							damageItem(p, mainHand,1);
						}
					}
				}
			
				else if (LevelProgression.pgetData(p,"SOLDIER") >= 65 && mainHand.getType() == Material.STONE_SWORD) {
					LevelProgression.psetData(p,"CC1", 14);
					
					Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(p.getLocation(), 7, 2, 7);
					
					for (Entity entities : ent) {
						if (entities instanceof LivingEntity) {
							LivingEntity entity = (LivingEntity) entities;
							if (entity.getLocation().distance(p.getLocation()) < 5 && entity!=p) {
								entity.setVelocity(new Vector(0,0.8,0));
							}
							if (entity instanceof Player) {
								Player p1 = (Player) entity;
								p1.playSound(p1.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 5.0F, 1.0F);
							}
							if (mainHand.getType()!=Material.AIR) damageItem(p, mainHand, 1);
						}
					}
					
				}
				
				else if (LevelProgression.pgetData(p,"SOLDIER") >= 65 && mainHand.getType() == Material.GOLDEN_SWORD) {
					LevelProgression.psetData(p,"CC1", 14);
					
					Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(p.getLocation(), 10, 3, 10);
					
					p.getWorld().playEffect(p.getLocation(), Effect.END_GATEWAY_SPAWN, 0);
					
					double damm= p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
					
					for (Entity entities : ent) {
						if (entities instanceof LivingEntity) {
							LivingEntity entity = (LivingEntity) entities;
							
							if (entity.getLocation().distance(p.getLocation()) < 7 && entity!=p) {
								
								entity.damage(damm, p);
								damageItem(p,mainHand,1);
								
								Location loc1=p.getLocation();
								Location loc2=entity.getLocation();
								Vector launchVector = loc2.toVector().subtract(loc1.toVector());
								launchVector.normalize().multiply(1.2).setY(0.6);
								
								entity.setVelocity(launchVector);
							}						
							//damageItem(p, p.getInventory().getItemInOffHand(), 1);
						}
						
					}
				}
	
				else if (LevelProgression.pgetData(p,"SOLDIER") >= 150 && mainHand.getType() == Material.IRON_SWORD) {
					LevelProgression.psetData(p,"CC1", 7);
					Vector launchVector = p.getEyeLocation().getDirection();
					launchVector.normalize().multiply(1.2);
					
					p.setVelocity(launchVector);
					p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_2, 5.0F, 1.0F);
					damageItem(p, mainHand, 6);
					
					Entity ent = LevelProgression.getTargetEntity(p);
					if (ent instanceof LivingEntity) {
						LivingEntity ent1 = (LivingEntity) ent;
						if (ent1.getLocation().distance(p.getLocation()) < 10) {
							p.attack(ent1);
						}
					}
	
				}
	
				else if (LevelProgression.pgetData(p,"SOLDIER") >= 230 && mainHand.getType() == Material.DIAMOND_SWORD) {
					LevelProgression.psetData(p,"CC1", 16);
					Location loc = p.getEyeLocation().add(p.getEyeLocation().getDirection().normalize().multiply(3.2));
					
					p.getWorld().playEffect(loc, Effect.EXTINGUISH, 0);
					
					Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(loc, 6, 3, 6);
					
					for (Entity entities : ent) {
						if (entities instanceof LivingEntity) {
							LivingEntity entity = (LivingEntity) entities;
							
							if (entity.getLocation().distance(p.getLocation()) < 7 && entity!=p) {
								
								Location loc2=loc;
								Location loc1=entity.getLocation();
								Vector launchVector = loc2.toVector().subtract(loc1.toVector());
								launchVector.normalize().multiply(0.6);
								
								entity.setVelocity(launchVector);
								damageItem(p, mainHand, 1);
							}
						}
					}
					
				}
	
				else if (LevelProgression.pgetData(p,"SOLDIER") >= 270 && mainHand.getType() == Material.NETHERITE_SWORD) {
					LevelProgression.psetData(p,"CC1", 18);
					Location loc = p.getEyeLocation().add(p.getEyeLocation().getDirection().normalize().multiply(4));
					
					p.getWorld().playEffect(loc, Effect.ANVIL_LAND, 1);
					
					Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(loc, 4, 4, 4);
									
					for (Entity entities : ent) {
						if (entities instanceof LivingEntity) {
							LivingEntity entity = (LivingEntity) entities;
							
							if (entity.getLocation().distance(p.getLocation()) < 6 && entity!=p) {
								p.attack(entity);
								entity.setVelocity(new Vector(0,0,0));
							}
							damageItem(p, mainHand, 4);
						}
					}
					
				}				
			
			}
		}
	}
	
	public void swapItemInHands(Player p, ItemStack mainHand, ItemStack offHand) {
		if (mainHand.getType()==Material.AIR || offHand.getType() == Material.AIR) return;

		p.getInventory().setItemInOffHand(mainHand);
		p.getInventory().setItemInMainHand(offHand);
		
	}
	
	public boolean isSword(Player p, ItemStack item) {
		if (item==null) return false;
		else if (item.getType()==Material.DIAMOND_SWORD || item.getType()==Material.GOLDEN_SWORD || item.getType()==Material.IRON_SWORD 
		|| item.getType()==Material.NETHERITE_SWORD || item.getType()==Material.STONE_SWORD || item.getType()==Material.WOODEN_SWORD) return true;
		else return false;
	}
	
	public void swordVerifier(Player p, ItemStack item) {
		if (item.getType()==Material.STONE_SWORD && LevelProgression.pgetData(p,"SOLDIER") < 50) {
			p.sendMessage(ChatColor.RED + lang.getString("soldier1"));
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
		else if (item.getType()==Material.IRON_SWORD && LevelProgression.pgetData(p,"SOLDIER") < 100) {
			p.sendMessage(ChatColor.RED + lang.getString("soldier1"));
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
		else if (item.getType()==Material.DIAMOND_SWORD && LevelProgression.pgetData(p,"SOLDIER") < 200) {
			p.sendMessage(ChatColor.RED + lang.getString("soldier1"));
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
		else if (item.getType()==Material.NETHERITE_SWORD && LevelProgression.pgetData(p,"SOLDIER") < 250) {
			p.sendMessage(ChatColor.RED + lang.getString("soldier1"));
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
			
			if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ALL)>0 && LevelProgression.pgetData(p, "SOLDIER") <10) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ALL)>1 && LevelProgression.pgetData(p, "SOLDIER") <90) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ALL)>2 && LevelProgression.pgetData(p, "SOLDIER") <125) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ALL)>3 && LevelProgression.pgetData(p, "SOLDIER") <285) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ALL)>4 && LevelProgression.pgetData(p, "SOLDIER") <300) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_UNDEAD)>0 && LevelProgression.pgetData(p, "SOLDIER") <20) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_UNDEAD)>1 && LevelProgression.pgetData(p, "SOLDIER") <85) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_UNDEAD)>2 && LevelProgression.pgetData(p, "SOLDIER") <190) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_UNDEAD)>3 && LevelProgression.pgetData(p, "SOLDIER") <290) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_UNDEAD)>4 && LevelProgression.pgetData(p, "SOLDIER") <300) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ARTHROPODS)>0 && LevelProgression.pgetData(p, "SOLDIER") <1) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ARTHROPODS)>1 && LevelProgression.pgetData(p, "SOLDIER") <5) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ARTHROPODS)>2 && LevelProgression.pgetData(p, "SOLDIER") <15) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ARTHROPODS)>3 && LevelProgression.pgetData(p, "SOLDIER") <20) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ARTHROPODS)>4 && LevelProgression.pgetData(p, "SOLDIER") <25) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.KNOCKBACK)>0 && LevelProgression.pgetData(p, "SOLDIER") <105) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.KNOCKBACK)>1 && LevelProgression.pgetData(p, "SOLDIER") <215) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.FIRE_ASPECT)>0 && LevelProgression.pgetData(p, "SOLDIER") <110) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.FIRE_ASPECT)>1 && LevelProgression.pgetData(p, "SOLDIER") <220) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.SWEEPING_EDGE)>0 && LevelProgression.pgetData(p, "SOLDIER") <35) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.SWEEPING_EDGE)>1 && LevelProgression.pgetData(p, "SOLDIER") <140) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.SWEEPING_EDGE)>2 && LevelProgression.pgetData(p, "SOLDIER") <205) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.MENDING)>0 && LevelProgression.pgetData(p, "SOLDIER") <225) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_MOBS)>0 && LevelProgression.pgetData(p, "SOLDIER") <30) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_MOBS)>1 && LevelProgression.pgetData(p, "SOLDIER") <165) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_MOBS)>2 && LevelProgression.pgetData(p, "SOLDIER") <275) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY)>0 && LevelProgression.pgetData(p, "SOLDIER") <40) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY)>1 && LevelProgression.pgetData(p, "SOLDIER") <130) enchDenied(p, item);
			else if (item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY)>2 && LevelProgression.pgetData(p, "SOLDIER") <240) enchDenied(p, item);
		}
	}
	
	public void enchDenied(Player p , ItemStack item) {
		ItemStack itemCloned = item.clone();
		item.setAmount(0);
		p.getWorld().dropItemNaturally(p.getLocation(), itemCloned); 
		p.sendMessage(ChatColor.RED + lang.getString("enchDenied"));
		p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 5.0F, 1F);
	}
	
	public void damageItem(Player p, ItemStack item, int damage) {
		Damageable dam = (Damageable) item.getItemMeta();
		if (dam==null) return;
		
		int damageAvoid=0;
		
		for (int i=0; i<damage; i++) for (ItemStack it : p.getInventory().getContents()) {
			if (it != null && it.getItemMeta().getLocalizedName().equals("DURABILITY_BRICK") && LevelProgression.pgetData(p,"SOLDIER")>=100) {
				damageAvoid++;
				it.setAmount(it.getAmount() - 1);
			}
		}
		
		damage=damage-damageAvoid;
		
		LevelProgression.damageItem(p, item, damage);
	}
	
}