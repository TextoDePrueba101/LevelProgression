package tdp.levelProgression;

import org.bukkit.configuration.ConfigurationSection;

public class WorldLevelConfig {
	private String name;
	private boolean active;
	private boolean disableScoreboard;
	private boolean evilSpiritSpawnAtEgg;
	private boolean NoXpBlockReward;
	private int xPmultiplier;
	private double bossHealtMultiplier;
	private int healthPerReward;
	private int evilSpiritProbability;
	private int ChunkTestDelay;
	private int ChunkProbability;
	
	public void setConfig(ConfigurationSection config) {
		//setup variables
		name = config.getName();
		
		active = config.getBoolean("active");
		disableScoreboard = config.getBoolean("disableScoreboard");
		evilSpiritSpawnAtEgg = config.getBoolean("evilSpiritSpawnAtEgg");
		
		xPmultiplier = config.getInt("xPmultiplier");
		bossHealtMultiplier = config.getDouble("bossHealtMultiplier");
		healthPerReward = config.getInt("healthPerReward");
		evilSpiritProbability = config.getInt("evilSpiritProbability");
		ChunkTestDelay = config.getInt("ChunkTestDelay");
		ChunkProbability = config.getInt("ChunkProbability");
		NoXpBlockReward = config.getBoolean("NoXpBlockReward");
	}
	
	public boolean getNoXpBlockReward() {
		return NoXpBlockReward;
	}
	
	public int getChunkProbability() {
		return ChunkProbability;
	}
	
	public int getChunkTestDelay() {
		return ChunkTestDelay;
	}
		
	public int getEvilSpiritProbability() {
		return evilSpiritProbability;
	}
	
	public int getHealthPerReward() {
		return healthPerReward;
	}
	
	public double getBossHealtMultiplier() {
		return bossHealtMultiplier;
	}
	
	public int getXPmultiplier() {
		return xPmultiplier;
	}

	public boolean getEvilSpiritSpawnAtEgg(){
		return evilSpiritSpawnAtEgg;
	}
	
	public boolean getActive() {
		return active;
	}
	
	public boolean getDisableScoreboard() {
		return disableScoreboard;
	}
	
	public String getName() {
		return name;
	}
}
