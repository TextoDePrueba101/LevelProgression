package tdp.levelProgression.listeners;

import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import org.bukkit.ChatColor;
import tdp.levelProgression.LevelProgression;

public class ArmorListener implements Listener{
	private FileConfiguration lang = LevelProgression.lang;
	
	public ArmorListener(LevelProgression plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void ArmourHealth(PlayerInteractEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;
		Player p = e.getPlayer();
		
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) 
			IncreaseLife((Player) e.getPlayer(), e.getPlayer().getInventory().getArmorContents());
		
	
		if (e.getItem()!=null && e.getItem().getType() == Material.SHIELD) {
			if (LevelProgression.pgetData(e.getPlayer(), "SHIELDMAN") <10) {
				e.getPlayer().sendMessage(ChatColor.RED +lang.getString("shield1"));
				
				if (!LevelProgression.breakItemWhenDenied) {
					ItemStack itemCloned = e.getItem().clone();
					e.getItem().setAmount(0);
					p.getWorld().dropItemNaturally(p.getLocation(), itemCloned); 
					p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 5.0F, 1F);
				}
				else {
					e.getItem().setAmount(0);
					e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ITEM_BREAK, 5.0F, 1F);								
				}
			} else {
				enchVerifier(e.getPlayer(),e.getItem());
			}
		}
		
		if (e.getItem() != null && e.getItem().getType() == Material.SCUTE && e.getAction() == Action.RIGHT_CLICK_BLOCK 
		&& LevelProgression.pgetData(p, "SHIELDMAN")>220 && p.getPose() == Pose.SNEAKING) { //Cegera a todos 
			int minExp=6;	
			if (p.getLevel() >= minExp) {
					
					Collection<Entity> ent = Bukkit.getWorld(p.getWorld().getName()).getNearbyEntities(p.getLocation(), 7, 7, 7);
					
					for (Entity entities : ent) {
						if (entities instanceof LivingEntity) {
							LivingEntity entity = (LivingEntity) entities;
							entity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 400, 0, false));
							entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 400, 1, false));
						}
					}
					p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 400, 0, false));
					p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 400, 4, false));
					p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 400, 1, false));
					p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 400, 3, false));
					p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 400, 0, false));
					p.playSound(p.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 5.0F, 1F);
					int n = p.getLevel() - minExp;
					p.setLevel(n);
					
				} else {
					p.sendMessage(ChatColor.RED +lang.getString("shield2") + minExp + lang.getString("shield22"));
				}
			}
			
				
	}
	
	@EventHandler
	public void IsProtected (EntityDamageEvent e) {
		if (!LevelProgression.testPluginActive(e.getEntity().getWorld())) return;
		if (!(e.getEntity() instanceof LivingEntity)) return;
		
		LivingEntity damaged = (LivingEntity) e.getEntity();
		
		//===
		if (damaged.getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "protectedBy"), PersistentDataType.STRING)) {
			
			if (getEntity(damaged.getPersistentDataContainer().get(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "protectedBy"), PersistentDataType.STRING)) != null) {
				Player protector = (Player) getEntity(damaged.getPersistentDataContainer().get(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "protectedBy"), PersistentDataType.STRING));
				
				try {
					protector.damage(e.getFinalDamage());					
				}
				catch (IllegalStateException ex) {
					try {
						protector.damage(e.getDamage());
					}
					catch (IllegalStateException ex2) {
						
					}
				}
				
				e.setCancelled(true);		

				if (damaged instanceof Player) {
					Player p = (Player) damaged;
					p.playSound(damaged.getLocation(), Sound.ENTITY_PLAYER_HURT, 5.0F, 1F);
					p.resetTitle();
					ChatColor color = ChatColor.RED;
					double lifePorcentage=(protector.getHealth()/protector.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
					
					if (lifePorcentage >= 0.8) color=ChatColor.GREEN;
					else if (lifePorcentage >= 0.5) color=ChatColor.YELLOW;
					else if (lifePorcentage >= 0.2) color=ChatColor.GOLD;
					else if (lifePorcentage >= 0.1) color=ChatColor.RED;
					else color=ChatColor.DARK_RED;
					
					p.sendTitle(color + " ", color + "-" + (int) e.getDamage() + "-", 1, 14, 1);
				}
			} 
			else {
				damaged.getPersistentDataContainer().remove(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "protectedBy"));
				damaged.sendMessage(ChatColor.GOLD +lang.getString("shield3"));
			}
		
		}
		//===

	}
	
	@EventHandler
	public void IsProtected (EntityDamageByEntityEvent e) {
		if (!LevelProgression.testPluginActive(e.getEntity().getWorld())) return;
		if (!(e.getEntity() instanceof LivingEntity)) return;
			
		LivingEntity shieldMan = (LivingEntity) e.getEntity();
		ItemStack shield = new ItemStack(Material.AIR);
		
		if (shieldMan.getEquipment().getItemInMainHand() != null && shieldMan.getEquipment().getItemInMainHand().getType()==Material.SHIELD) shield = shieldMan.getEquipment().getItemInMainHand();
		else if (shieldMan.getEquipment().getItemInOffHand() != null && shieldMan.getEquipment().getItemInOffHand().getType()==Material.SHIELD) shield = shieldMan.getEquipment().getItemInOffHand();
		
		if (shield.getType()==Material.SHIELD && shield.getItemMeta().getLocalizedName().equals("THRONS_SHIELD") && !e.getEntity().equals(e.getDamager()) 
		&& e.getEntity() instanceof Player && !((Player)e.getEntity()).isBlocking()){
	    	Player p = (Player) e.getEntity();
	    	
			if (e.getDamager() instanceof Player) {
				((Player)e.getDamager()).damage(e.getDamage()*0.51,p);
				LevelProgression.damageItem(shield,1);
			}
			else if (e.getDamager() instanceof LivingEntity) {
				((LivingEntity)e.getDamager()).damage(e.getDamage()*2.5,p);	
				LevelProgression.damageItem(shield,1);
			}		
		}
	}
	
	private boolean state = true;
	
	@EventHandler
	public void ProtectAbility (PlayerInteractEntityEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;

		state=!state;
		if (state==true) {
			if (!(e.getRightClicked() instanceof LivingEntity)) return;
			
			Player p = e.getPlayer();
			LivingEntity ent = (LivingEntity) e.getRightClicked();
			
			
			//===
			if (LevelProgression.pgetData(p, "SHIELDMAN") >=140 && p.getInventory().getItemInMainHand().getType() == Material.SHIELD && p.getPose() == Pose.SNEAKING) {
				
				StartProtection(p,ent);
				
			}
			//===
			
		}
		
	}
	
	@EventHandler
	public void ArmourHealthAfterDeath(PlayerDeathEvent e) {
		if (!LevelProgression.testPluginActive(e.getEntity().getWorld())) return;

		IncreaseLife(e.getEntity(),e.getEntity().getInventory().getArmorContents());
		if (e.getEntity().getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isProtecting"), PersistentDataType.STRING)) {
			e.getEntity().getPersistentDataContainer().remove(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isProtecting"));
		}
		hasConection(e.getEntity());
	}
	
	public void ConectionEndAfterRespawn (PlayerRespawnEvent e) {
		if (!LevelProgression.testPluginActive(e.getPlayer().getWorld())) return;

		ConectionEndedMessage(e.getPlayer());
		hasConection(e.getPlayer());
	}
	
	@EventHandler
	public void ArmourHealth(InventoryCloseEvent e) {
		
		IncreaseLife((Player) e.getPlayer(), e.getPlayer().getInventory().getArmorContents());
	}
	
	
	public void IncreaseLife(Player p, ItemStack[] itemarray){
		double baselife = 20;

		for (ItemStack item : itemarray) {
			if (item != null) {
				Material m = item.getType();

				if (m == Material.CHAINMAIL_BOOTS || m==Material.CHAINMAIL_CHESTPLATE ||m==Material.CHAINMAIL_HELMET || m==Material.CHAINMAIL_LEGGINGS ) {
						baselife = baselife + 5 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.1166));
				}
				else if (m == Material.LEATHER_HELMET) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 1 || LevelProgression.pgetData(p,"ADVENTURER") >= 1) {
						baselife = baselife + 3 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.1233));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldHelmet"));
						
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
				}	
				else if (m == Material.LEATHER_BOOTS) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 2 || LevelProgression.pgetData(p,"ADVENTURER") >= 2) {
						baselife = baselife + 3 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.1066));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldBoots"));

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
				}
				else if (m == Material.LEATHER_LEGGINGS) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 3 || LevelProgression.pgetData(p,"ADVENTURER") >= 3) {
						baselife = baselife + 3 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.1066));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldPants"));

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
				}
				else if (m == Material.LEATHER_CHESTPLATE) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 4 || LevelProgression.pgetData(p,"ADVENTURER") >= 4) {
						baselife = baselife + 3 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.0899));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldChest"));

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
				}
				else if (m == Material.GOLDEN_HELMET) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 6 || LevelProgression.pgetData(p,"ADVENTURER") >= 6) {
						baselife = baselife + 3 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.1233));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldHelmet"));

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
				}	
				else if (m == Material.GOLDEN_BOOTS) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 7 || LevelProgression.pgetData(p,"ADVENTURER") >= 7) {
						baselife = baselife + 4 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.1033));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldBoots"));

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
				}
				if (m == Material.GOLDEN_LEGGINGS) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 8 || LevelProgression.pgetData(p,"ADVENTURER") >= 8) {
						baselife = baselife + 5 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.0833));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldPants"));

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
				}
				else if (m == Material.GOLDEN_CHESTPLATE) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 9 || LevelProgression.pgetData(p,"ADVENTURER") >= 9) {
						baselife = baselife + 5 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.066));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldChest"));
						
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
				}
				else if (m == Material.IRON_HELMET) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 28) {
						baselife = baselife + 3 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.0566));//
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldHelmet"));

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
				}	
				else if (m == Material.IRON_BOOTS) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 32) {
						baselife = baselife + 3 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.0566));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldBoots"));

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
				}
				else if (m == Material.IRON_LEGGINGS) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 40) {
						baselife = baselife + 3 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.0566));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldPants"));

						if (!LevelProgression.breakItemWhenDenied) {
							ItemStack itemCloned = item.clone();
							item.setAmount(0);
							p.getWorld().dropItemNaturally(p.getLocation(), itemCloned); 
							p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 5.0F, 1F);
							return;
						}

						p.getInventory().setLeggings(new ItemStack(Material.AIR,1));
						p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 5.0F, 1F);
					}
				}
				else if (m == Material.IRON_CHESTPLATE) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 50) {
						baselife = baselife + 4  + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.0533));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldChest"));

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
				}
				else if (m == Material.DIAMOND_BOOTS) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 80) {
						baselife = baselife + 3 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.0566));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldBoots"));

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
				}
				else if (m == Material.DIAMOND_HELMET) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 70) {
						baselife = baselife + 4 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.0533));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldHelmet"));

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
				}	
				else if (m == Material.DIAMOND_LEGGINGS) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 90) {
						baselife = baselife + 4 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.02));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldPants"));

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
				}
				else if (m == Material.DIAMOND_CHESTPLATE) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 100) {
						baselife = baselife + 4 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.02));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldChest"));

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
				}
				else if (m == Material.NETHERITE_BOOTS) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 150) {
						baselife = baselife + 4 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.0366));//
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldBoots"));

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
				}
				else if (m == Material.NETHERITE_HELMET) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 125) {
						baselife = baselife + 4 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.0366));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldHelmet"));

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
				}	
				else if (m == Material.NETHERITE_CHESTPLATE) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 200) {
						baselife = baselife + 4 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.0033));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldChest"));

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
				}
				else if (m == Material.NETHERITE_LEGGINGS) {
					if (LevelProgression.pgetData(p,"SHIELDMAN") >= 175) {
						baselife = baselife + 4 + Math.round((LevelProgression.pgetData(p,"SHIELDMAN")*0.0033));
					} else {
						p.sendMessage(ChatColor.RED +lang.getString("shieldPants"));
						
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
				} 
				else if (m == Material.ELYTRA) {
					if (LevelProgression.pgetData(p,"ADVENTURER") < 300) {
						p.sendMessage(ChatColor.RED +lang.getString("shield4"));
						
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
				} 
				else if (m == Material.TURTLE_HELMET) {
					if (LevelProgression.pgetData(p,"TRIDENTMAN") < 50) {
						p.sendMessage(ChatColor.RED +lang.getString("shield5"));
						
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
						baselife = baselife + 20;
					}
				} 

				//=====================================================================

				enchVerifier(p,item);
			}
		}
		
		p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(baselife);
		if (p.getHealth() > p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) {
			p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
		}
	}
	
	public void enchVerifier(Player p , ItemStack item) {
		LevelProgression.enchVerifier(p, item);
	}
	
	public void enchDenied (Player p, ItemStack item) {
		LevelProgression.enchDenied(p, item);
	}
	
	public void StartProtection (Player protector, LivingEntity protectedEntity) {
		hasConection(protector);
		hasConection(protectedEntity);

		
		if (protectedEntity.getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isProtecting"), PersistentDataType.STRING)) {
			protector.sendMessage(ChatColor.RED +lang.getString("shield6"));
			return;

		} else if (protectedEntity.getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "protectedBy"), PersistentDataType.STRING)) {
			
			protectedEntity.getPersistentDataContainer().remove(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "protectedBy"));
			hasConection(protectedEntity);
			protector.sendMessage(ChatColor.GOLD + lang.getString("shield7") + protectedEntity.getName());
			protectedEntity.sendMessage(ChatColor.GOLD + lang.getString("shield8"));

			//protector.sendMessage("StartProtection if 2");
		} else if (protector.getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "protectedBy"), PersistentDataType.STRING)) {
		
			protector.sendMessage(ChatColor.RED + lang.getString("shield9"));
			
			//protector.sendMessage("StartProtection if 3");
		
		} else if (protector.getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isProtecting"), PersistentDataType.STRING)) {

			LivingEntity oldProtectedEntity = getEntity(protector.getPersistentDataContainer().get(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isProtecting"), PersistentDataType.STRING));
			protector.getPersistentDataContainer().set(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isProtecting"), PersistentDataType.STRING, protectedEntity.getUniqueId().toString());
			protectedEntity.getPersistentDataContainer().set(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "protectedBy"), PersistentDataType.STRING, protector.getUniqueId().toString());
			
			//protector.sendMessage("StartProtection if 4 previo hasconection");

			if (oldProtectedEntity != null) { 
				hasConection(oldProtectedEntity);
				//protector.sendMessage(ChatColor.GOLD + "Ya nadie protege a " + oldProtectedEntity.getName());
			}
			
			//protector.sendMessage("StartProtection if 4 post hasconection");
			
			protector.sendMessage(ChatColor.GREEN + lang.getString("shield10") + protectedEntity.getName());
			protectedEntity.sendMessage(ChatColor.GREEN + lang.getString("shield11"));

			
		} else {
			protector.getPersistentDataContainer().set(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isProtecting"), PersistentDataType.STRING, protectedEntity.getUniqueId().toString());
			protectedEntity.getPersistentDataContainer().set(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "protectedBy"), PersistentDataType.STRING, protector.getUniqueId().toString());
			protector.sendMessage(ChatColor.GREEN + lang.getString("shield10") + protectedEntity.getName());
			protectedEntity.sendMessage(ChatColor.GREEN + lang.getString("shield11"));
			
			//protector.sendMessage("StartProtection if 5");

		}
		
		/*
		.getPersistentDataContainer().set(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "protectedBy"), PersistentDataType.STRING, "")
		.getPersistentDataContainer().get(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "protectedBy"), PersistentDataType.STRING)
		.getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "protectedBy"), PersistentDataType.STRING)
		.getPersistentDataContainer().remove(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "protectedBy"))
	
		.getPersistentDataContainer().set(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isProtecting"), PersistentDataType.STRING, "")
		.getPersistentDataContainer().get(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isProtecting"), PersistentDataType.STRING)
		.getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isProtecting"), PersistentDataType.STRING)
		.getPersistentDataContainer().remove(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isProtecting"))
		*/
	}
	
	public boolean hasConection(LivingEntity entity) {
		//Bukkit.broadcastMessage("has conection inicio :"+ entity.getName());
		
		
		//Si es protector (entity == protector)
		if (entity.getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isProtecting"), PersistentDataType.STRING)) {
			//Bukkit.broadcastMessage("has conection if 1");

			String protectedUUID = entity.getPersistentDataContainer().get(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isProtecting"), PersistentDataType.STRING);
			LivingEntity protectedEntity = getEntity(protectedUUID);
			LivingEntity protectorEntity = (LivingEntity) entity;
			
			
			if (protectedEntity != null && protectedEntity.getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "protectedBy"), PersistentDataType.STRING)) {
				//Bukkit.broadcastMessage("has conection if 2");
				
				String protectorStringUUID =protectorEntity.getUniqueId().toString();
				String protectedStringUUID =protectedEntity.getPersistentDataContainer().get(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "protectedBy"), PersistentDataType.STRING);

				
				if (protectorStringUUID.compareTo(protectedStringUUID) == 0) {
					//Bukkit.broadcastMessage("has conection if 3");
					return true;
				}else {
					//Bukkit.broadcastMessage("has conection else if 3");
					ConectionEndedMessage(entity);
					return false;
				}
			}else {
				//Bukkit.broadcastMessage("has conection else if 2");
				ConectionEndedMessage(entity);
				return false;
			}
		} else
		
		//Si es protegido (entity == protected)
			if (entity.getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "protectedBy"), PersistentDataType.STRING)) {
				String protectorUUID = entity.getPersistentDataContainer().get(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "protectedBy"), PersistentDataType.STRING);
				LivingEntity protectorEntity = getEntity(protectorUUID);
				LivingEntity protectedEntity = null;
				
				//Bukkit.broadcastMessage("has conection if 1 a");

				if (entity instanceof LivingEntity) protectedEntity = (LivingEntity) entity;
				else return false;
				
				if (protectorEntity != null && protectorEntity.getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isProtecting"), PersistentDataType.STRING)) {
					//Bukkit.broadcastMessage("has conection if 2 a");

					String protectedStringUUID =protectedEntity.getUniqueId().toString();
					String protectorStringUUID =protectorEntity.getPersistentDataContainer().get(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isProtecting"), PersistentDataType.STRING);
					
					if (protectedStringUUID.compareTo(protectorStringUUID) == 0) { // EL ERROR ES COMPARAR STRINGS COMO INTS, DEBE USARSE COMPARE
						//Bukkit.broadcastMessage("has conection if 3 a");

						return true;
					}else {
						//Bukkit.broadcastMessage("has conection else if 3 a");
						ConectionEndedMessage(entity);
						return false;
					}
				} else {
					//Bukkit.broadcastMessage("has conection else if 2 a");
					ConectionEndedMessage(entity);
					return false;
				}
			} else {
				//Bukkit.broadcastMessage("has conection else if 1 a");

				ConectionEndedMessage(entity);
				return false;
			}
	}
	
	public void ConectionEndedMessage (LivingEntity entity) {
		if (entity.getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "protectedBy"), PersistentDataType.STRING)){
			entity.sendMessage(ChatColor.GOLD + lang.getString("shield8"));
			entity.getPersistentDataContainer().remove(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "protectedBy"));
		} 
		else if (entity.getPersistentDataContainer().has(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isProtecting"), PersistentDataType.STRING)){
			if (getEntity(entity.getPersistentDataContainer().get(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isProtecting"), PersistentDataType.STRING)) != null) {
				String protectedName = getEntity(entity.getPersistentDataContainer().get(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isProtecting"), PersistentDataType.STRING)).getName();
				entity.sendMessage(ChatColor.GOLD + lang.getString("shield7") + protectedName);
			} else {
				entity.sendMessage(ChatColor.GOLD + lang.getString("shield12"));
			}
			entity.getPersistentDataContainer().remove(new NamespacedKey(LevelProgression.getPlugin(LevelProgression.class), "isProtecting"));
		}

	}
	
	public LivingEntity getEntity(String uuid) {
		return (LivingEntity) Bukkit.getEntity(UUID.fromString(uuid));
	}
}
