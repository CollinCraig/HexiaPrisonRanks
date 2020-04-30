package me.prisonranksx.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import me.prisonranksx.PrisonRanksX;

public class RebirthRandomCommands {
	
	private String rebirthName;
	private boolean withKeys;
	private Map<String, Object> randomCommandsMap;
	private List<List<String>> commandsList;
	private List<Double> chances;
	
	private static PrisonRanksX main = (PrisonRanksX)Bukkit.getPluginManager().getPlugin("PrisonRanksX");
	
	public RebirthRandomCommands(String rebirthName, boolean withKeys) {this.rebirthName = rebirthName; this.withKeys = withKeys;
	this.commandsList = new ArrayList<>();
	this.chances = new ArrayList<>();
	this.randomCommandsMap = new HashMap<String, Object>();
	}
	
	public RebirthRandomCommands(String rebirthName, boolean withKeys, boolean loadSections) {this.rebirthName = rebirthName; this.withKeys = withKeys;
	this.commandsList = new ArrayList<>();
	this.chances = new ArrayList<>();
	this.randomCommandsMap = new HashMap<String, Object>();
	  if(loadSections) {
		  loadSections(rebirthName);
	  }
	}
	
	public void loadSections(String rebirthName) {
		Map<String, Object> randomCommandsMap = new HashMap<String, Object>();
		if(main.configManager.rebirthsConfig.getConfigurationSection("Rebirths."  + rebirthName + ".randomcmds") != null &&
				!main.configManager.rebirthsConfig.getConfigurationSection("Rebirths." + rebirthName + ".randomcmds").getKeys(false).isEmpty()) {
		randomCommandsMap = main.configManager.rebirthsConfig.getConfigurationSection("Rebirths." + rebirthName + ".randomcmds").getValues(withKeys);
		}
		List<List<String>> commandsList = new ArrayList<>();
		List<Double> chances = new ArrayList<>();
		if(!randomCommandsMap.isEmpty()) {
		  for(String section : randomCommandsMap.keySet()) {
			  commandsList.add(main.configManager.rebirthsConfig.getStringList("Rebirths." + rebirthName + ".randomcmds." + section + ".commands"));
			  chances.add(main.configManager.rebirthsConfig.getDouble("Rebirths." + rebirthName + ".randomcmds." + section + ".chance"));
		  }
		}
		setRandomCommandsMap(randomCommandsMap);
		setCommandsList(commandsList);
		setChances(chances);
	}
	//public Map<String, Object> getCommandSections() {
		//return main.configManager.ranksConfig.getConfigurationSection("Ranks." + pathName + "." + rankName + ".randomcmds").getValues(false);
	//}
	
	public void setRandomCommandsMap(Map<String, Object> randomCommandsMap) {
		this.randomCommandsMap = randomCommandsMap;
	}
	
	public void setCommandsList(List<List<String>> commandsList) {
		this.commandsList = commandsList;
	}
	
	public void setChances(List<Double> chances) {
		this.chances = chances;
	}
	
	public Map<String, Object> getRandomCommandsMap() {
		return randomCommandsMap;
	}
	
	public List<List<String>> getCommandsListCollection() {
		return commandsList;
	}
	
	public List<Double> getChancesCollection() {
		return chances;
	}
    
	public List<String> getCommands(String section) {
		if(randomCommandsMap.containsKey(section)) {
		return ((ConfigurationSection)randomCommandsMap.get(section)).getStringList("commands");
		}
		return null;
	}
	
	public Double getChance(String section) {
		return ((ConfigurationSection)randomCommandsMap.get(section)).getDouble("chance");
	}
}
