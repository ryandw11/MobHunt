package me.ryandw11.mobhunt.listners.mobkill;

import java.util.List;

import me.ryandw11.mobhunt.core.Main;
import me.ryandw11.mobhunt.core.Mobs;
import net.milkbowl.vault.economy.EconomyResponse;
import net.minecraft.server.v1_11_R1.IChatBaseComponent;
import net.minecraft.server.v1_11_R1.PacketPlayOutChat;
import net.minecraft.server.v1_11_R1.IChatBaseComponent.ChatSerializer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDeathEvent;

public class MobKill_1_11_R1 implements Listener{
	private int am; // used for stuff
	private String amount; // used for the global amount in the methods!
	
	private Main plugin;
	public MobKill_1_11_R1(Main plugin){
		this.plugin = plugin;
	}
	

	
	@EventHandler
	public void OnDeath(EntityDeathEvent event){
		Entity e = event.getEntity();
		if(e instanceof Player) 
			return;    
		
			Entity monsterEnt = (Entity) e;
			String monstertype = e.getType().toString().toLowerCase();
			
			if(!(((LivingEntity) monsterEnt).getKiller() instanceof Player)) 
				return;
			
			Player p = ((LivingEntity) monsterEnt).getKiller();
				
			if(plugin.getConfig().getBoolean("GameMode_Limit.Enabled") == true) 
				if(p.getGameMode() == GameMode.CREATIVE) return;
				
				Boolean check = checkWorld(p); //Checks to see if world is disabled.
				
				//If mob has meta data return
				if(grabMetaData(monsterEnt)) 
					return;
				//if world is disabled.
				if(check) 
					return;
			

						if(p.hasPermission("mobhunt.pay")){//perm
							//==========================================Zombie and Skeleton mob check==============================

								EconomyResponse er = null;
								//If it is a string / has a range
								if(plugin.getConfig().isString(monstertype + ".Money")){// new area
									if(Mobs.splitFirst(plugin.getConfig().getString(monstertype + ".Money")) == -1){
										p.sendMessage("[MobHunt] �cError: �6Mob.Money �a- String is not vailed. Contact an admin!");
									}
									am = Mobs.genRandom(//Grabs a random number
										Mobs.splitFirst(plugin.getConfig().getString(monstertype + ".Money")), //Get's number 1
										Mobs.splitSecond(plugin.getConfig().getString(monstertype + ".Money")) // Get's number 2
										);
									amount = String.valueOf(am);
									er = Main.econ.depositPlayer(p, am);
									vault(er, p, monstertype);
								}
								else if(plugin.getConfig().isInt(monstertype + ".Money")){ //else if it is an int
									if(plugin.getConfig().getInt(monstertype + ".Money") == 0) return;
									amount = String.valueOf(plugin.getConfig().getInt(monstertype + ".Money"));
									int mon = plugin.getConfig().getInt(monstertype + ".Money");
									er = Main.econ.depositPlayer(p, mon);
									vault(er, p, monstertype);
								}
								else{ //if it is none of the above
									plugin.logger.warning("[MobHunt] Error: An error has occured in the config! Please check " + monstertype + ".Money!");
									return;
								}//-----------Vault----------------
								
							
							
							
							//==================================Sounds==========================
									if(!(plugin.getConfig().getString(monstertype + ".Sound").equalsIgnoreCase("none"))){

										playerSound(p, plugin.getConfig().getString(monstertype + ".Sound"));
									}
							//===============================Efects=============================
									if(!(plugin.getConfig().getString(monstertype + ".Particle").equalsIgnoreCase("none"))){
										int Effnum = plugin.getConfig().getInt("Particle.Effect_Number");
										Effect eff = EffectOut(plugin.getConfig().getString(monstertype + ".Particle"));
										Location loc = e.getLocation();
										e.getWorld().playEffect(loc, eff, Effnum);
									}
							
							//----------------------------------------------------
							//---------------Announcements------------------------
							//----------------------------------------------------
							
							if(plugin.getConfig().contains("Announcements." + e.getType().toString().toLowerCase())){
								Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Prefix") + plugin.getConfig().getString("Announcements." + e.getType().toString().toLowerCase()).replace("{player}", p.getDisplayName())));
							}
							
							int kills = plugin.kill.getInt(p.getUniqueId().toString() + "kills");
							kills += 1;
							plugin.kill.set(p.getUniqueId() + "kills", kills);
							plugin.saveFile();
							
							//---------------------------------------Rewards---------------------------------------------
							if(p.hasPermission("mobhunt.rewards")){
								EconomyResponse moneykill = null;
								int i = 1;
								int numberRewards = plugin.getConfig().getInt("Rewards.Number");
								
								do{
									if(plugin.kill.getInt(p.getUniqueId().toString() + "kills") == plugin.getConfig().getInt("Rewards." + i + ".Kills")){
										moneykill = Main.econ.depositPlayer(p, plugin.getConfig().getInt("Rewards." + i +".Money"));
										if(moneykill.transactionSuccess()) {//----------Vault-------------
											p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Prefix")) + ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Rewards." + i +".Message")));
										} else {
											p.sendMessage(String.format("An error occured: %s", moneykill.errorMessage));
											plugin.logger.warning("[MobHunt] Error- The config: config.yml. Is not defined correctly.");
										}
									}
									i++;
								}while(i <= numberRewards);
								
								
								}//perm
								//-------------------------------------------[Title]-----------------------------------
								
								
							}
							//-------------------------------------------[Title]------------------------------

							

						
					}// end

					
				
				
				
			//------------{End of Death Event}------------------------------
	
	public void actionBar(Player player, String message){
		CraftPlayer p = (CraftPlayer) player;
		IChatBaseComponent cbc = ChatSerializer.a("{\"text\": \"" + message + "\"}");
		PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte) 2);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(ppoc);
	}
	
	public void playerSound(Player p, String sound){
		p.playSound(p.getLocation(), soundEffect(sound), 3, 0);
	}
	
	public Sound soundEffect(String sounds){
		Sound sound = null;
		switch(sounds){
		case "Bat_Death":
			sound = Sound.ENTITY_BAT_DEATH;
			break;
		case "Blaze_Death":
			sound = Sound.ENTITY_BLAZE_DEATH;
			break;
		case "Blaze_Shoot":
			sound = Sound.ENTITY_BLAZE_SHOOT;
			break;
		case "Cat_Death":
			sound = Sound.ENTITY_CAT_DEATH;
			break;
		case "Cat_Purr":
			sound = Sound.ENTITY_CAT_PURR;
			break;
		case "Cat_Hiss":
			sound = Sound.ENTITY_CAT_HISS;
			break;
		case "Chicken_Death":
			sound = Sound.ENTITY_CHICKEN_DEATH;
			break;
		case "Chicken_Hurt":
			sound = Sound.ENTITY_CHICKEN_HURT;
			break;
		case "Cow_Death":
			sound = Sound.ENTITY_COW_DEATH;
			break;
		case "Cow_Hurt":
			sound = Sound.ENTITY_COW_HURT;
			break;
		case "Creeper_Death":
			sound = Sound.ENTITY_CREEPER_DEATH;
			break;
		case "Creeper_Hurt":
			sound = Sound.ENTITY_CREEPER_HURT;
			break;
		case "Elder_Guardian_Curse":
			sound = Sound.ENTITY_ELDER_GUARDIAN_CURSE;
			break;
		case "Elder_Guardian_Death":
			sound = Sound.ENTITY_ELDER_GUARDIAN_DEATH;
			break;
		case "Ender_Guardian_Hurt":
			sound = Sound.ENTITY_ELDER_GUARDIAN_HURT;
			break;
		case "Enderdragon_Growl":
			sound = Sound.ENTITY_ENDERDRAGON_GROWL;
			break;
		case "Enderdragon_Flap":
			sound = Sound.ENTITY_ENDERDRAGON_FLAP;
			break;
		case "Enderman_Death":
			sound = Sound.ENTITY_ENDERMEN_DEATH;
			break;
		case "Enderman_Hurt":
			sound = Sound.ENTITY_ENDERMEN_HURT;
			break;
		case "Explosion":
			sound = Sound.ENTITY_GENERIC_EXPLODE;
			break;
		case "Ghast_Death":
			sound = Sound.ENTITY_GHAST_DEATH;
			break;
		case "Ghast_Hurt":
			sound = Sound.ENTITY_GHAST_HURT;
			break;
		case "Ghast_Scream":
			sound = Sound.ENTITY_GHAST_SCREAM;
			break;
		case "Ghast_Warn":
			sound = Sound.ENTITY_GHAST_WARN;
			break;
		case "Horse_Hurt":
			sound = Sound.ENTITY_HORSE_HURT;
			break;
		case "Horse_Death":
			sound = Sound.ENTITY_HORSE_DEATH;
			break;
		case "IronGolem_Death":
			sound = Sound.ENTITY_IRONGOLEM_DEATH;
			break;
		case "Pig_Death":
			sound = Sound.ENTITY_PIG_DEATH;
			break;
		case "Rabbit_Death":
			sound = Sound.ENTITY_RABBIT_DEATH;
			break;
		case "Shulker_Death":
			sound = Sound.ENTITY_SHULKER_DEATH;
			break;
		case "Slime_Death":
			sound = Sound.ENTITY_SLIME_DEATH;
			break;
		case "Slime_Hurt":
			sound = Sound.ENTITY_SLIME_HURT;
			break;
		case "Snowman_Death":
			sound = Sound.ENTITY_SNOWMAN_DEATH;
			break;
		case "Villager_Death":
			sound = Sound.ENTITY_VILLAGER_DEATH;
			break;
		case "Wolf_Death":
			sound = Sound.ENTITY_WOLF_DEATH;
			break;
		case "Zombie_Hurt":
			sound = Sound.ENTITY_ZOMBIE_HURT;
			break;
		case "Husk":
			sound = Sound.ENTITY_HUSK_AMBIENT;
			break;
		case "Husk_Step":
			sound = Sound.ENTITY_HUSK_STEP;
			break;
		case "Stray":
			sound = Sound.ENTITY_STRAY_AMBIENT;
			break;
		case "Stray_hurt":
			sound = Sound.ENTITY_STRAY_HURT;
			break;
		case "polarbear":
			sound = Sound.ENTITY_POLAR_BEAR_AMBIENT;
			break;
		case "polarbear_warning":
			sound = Sound.ENTITY_POLAR_BEAR_WARNING;
			break;
		case "polarbear_death":
			sound = Sound.ENTITY_POLAR_BEAR_DEATH;
			break;
		}
		return sound;
	}
	
	public Effect EffectOut(String particle){
		Effect eff = null;
		switch(particle){
		
		case "bow_fire":
			eff = Effect.BOW_FIRE;
			break;
		case "mobspawner_flames":
			eff = Effect.MOBSPAWNER_FLAMES;
			break;
		case "cloud":
			eff = Effect.CLOUD;
			break;
		case "colored_dust":
			eff = Effect.COLOURED_DUST;
			break;
		case "crit":
			eff = Effect.CRIT;
			break;
		case "ender_signal":
			eff = Effect.ENDER_SIGNAL;
			break;
		case "explosion":
			eff = Effect.EXPLOSION;
			break;
		case "explosion_huge":
			eff = Effect.EXPLOSION_HUGE;
			break;
		case "explosion_large":
			eff = Effect.EXPLOSION_LARGE;
			break;
		case "firework_spark":
			eff = Effect.FIREWORKS_SPARK;
			break;
		case "flame":
			eff = Effect.FLAME;
			break;
		case "footstep":
			eff = Effect.FOOTSTEP;
			break;
		case "happy_villager":
			eff = Effect.HAPPY_VILLAGER;
			break;
		case "heart":
			eff = Effect.HEART;
			break;
		case "large_smoke":
			eff = Effect.LARGE_SMOKE;
			break;
		case "lava_pop":
			eff = Effect.LAVA_POP;
			break;
		case "magic_crit":
			eff = Effect.MAGIC_CRIT;
			break;
		case "note":
			eff = Effect.NOTE;
			break;
		case "particle_smoke":
			eff = Effect.PARTICLE_SMOKE;
			break;
		case "small_smoke":
			eff = Effect.SMALL_SMOKE;
			break;
		case "smoke":
			eff = Effect.SMOKE;
			break;
		case "thunder_cloud":
			eff = Effect.VILLAGER_THUNDERCLOUD;
			break;
		case "spell":
			eff = Effect.SPELL;
			break;
		case "dust":
			eff = Effect.TILE_DUST;
			break;
		case "none":
			eff = null;
			break;
		}
		return eff;
	}
	
	private boolean checkWorld(Player p){
		World w = p.getWorld();
		List<String> worlds = plugin.getConfig().getStringList("Worlds_Not_Allowed");
		for(String s : worlds){
			if(w.getName().equals(s)){
				return true;
			}
		}
		return false;
	}
	private void vault(EconomyResponse er, Player p, String monstertype){
		if(er.transactionSuccess()) {//----------Vault-------------
			if(plugin.getConfig().getString("Messages.Mode").equalsIgnoreCase("Chat")){
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Prefix")) + ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Mob_Kill_Message").replace("{mob}", monstertype).replace("{money}", amount)));
			}
			else if(plugin.getConfig().getString("Messages.Mode").equalsIgnoreCase("Title")){
				if(plugin.getConfig().getInt(monstertype + ".Money") != 0){
					String message = plugin.getConfig().getString("Title.Message").replace("{mob}", monstertype).replace("{money}", amount).replace("&", "�");
					actionBar(p, message);
				}
			}
			else{
				plugin.logger.warning("[MobHunt] Error: An error has occured in the config! Please check Messages.Mode!");
				p.sendMessage("[MobHunt] �cError: �aAn error has occured. Please contact the server admin!");
			}
		} else {
			p.sendMessage(String.format("An error occured: %s", er.errorMessage));
		}
	}
	
	private boolean grabMetaData(Entity e){
		if(e.hasMetadata("SR")){
			return true;
		}
		else{
			return false;
		}
	}


}
