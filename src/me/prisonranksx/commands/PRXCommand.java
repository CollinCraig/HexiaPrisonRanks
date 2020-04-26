package me.prisonranksx.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.data.RankPath;
import me.prisonranksx.data.XUser;
import me.prisonranksx.events.PrestigeUpdateCause;
import me.prisonranksx.events.RankUpdateCause;
import me.prisonranksx.events.RebirthUpdateCause;
import me.prisonranksx.events.XPrestigeUpdateEvent;
import me.prisonranksx.events.XRankUpdateEvent;
import me.prisonranksx.events.XRebirthUpdateEvent;

public class PRXCommand extends BukkitCommand {
	
	private PrisonRanksX main = (PrisonRanksX)Bukkit.getPluginManager().getPlugin("PrisonRanksX");
	private String ver = "1.0";
	public PRXCommand(String commandName) {
		super(commandName);
		this.setDescription(main.getStringWithoutPAPI(main.configManager.commandsConfig.getString("commands." + commandName + ".description", "Manage ranks,prestiges,rebirths settings")));
		this.setUsage(main.getStringWithoutPAPI(main.configManager.commandsConfig.getString("commands." + commandName + ".usage", "/prx help [page]")));
		this.setPermission(main.configManager.commandsConfig.getString("commands." + commandName + ".permission", "prisonranksx.admin"));
		this.setPermissionMessage(main.getStringWithoutPAPI(main.configManager.commandsConfig.getString("commands." + commandName + ".permission-message", "&cYou don't have permission to execute this command.")));
		this.setAliases(main.configManager.commandsConfig.getStringList("commands." + commandName + ".aliases"));
		ver = main.getDescription().getVersion();
	}

	public String getRandomColor() {
		int rand = main.prxAPI.numberAPI.getRandomInteger(0, 15);
			switch (rand) {
			  case 10: return "a";
			  case 11: return "b";
			  case 12: return "c";
			  case 13: return "d";
			  case 14: return "e";
			  case 15: return "f";
			  default: return String.valueOf(rand);
			}
	}
	
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if(!sender.hasPermission(this.getPermission())) {
			sender.sendMessage(this.getPermissionMessage());
			return true;
		}
        if(args.length == 0) {
        	sender.sendMessage(main.prxAPI.c("&3[&6PrisonRanksX&3] &av" + ver));
        	sender.sendMessage(main.prxAPI.c("&7<> = required &8⎟ &7[] = optional &8⎟ &7() = optional prefix"));
            sender.sendMessage(main.prxAPI.c("&c&m                                                                      &c"));
        	sender.sendMessage(main.prxAPI.c("&c/&6prx help [page] &7⎟ &3show the available commands"));
        	sender.sendMessage(main.prxAPI.c("&c/&6prx reload &7⎟ &3reload the entire plugin"));
        	sender.sendMessage(main.prxAPI.c("&c/&6prx save &7⎟ &3save the ranks/prestiges/etc.. you created ingame"));
        	sender.sendMessage(main.prxAPI.c("&c/&6prx createrank <name> <cost> [displayname] (-path:)[pathname]"));
        	sender.sendMessage(main.prxAPI.c("&c/&6prx setrankcost <name> <cost>"));
        	sender.sendMessage(main.prxAPI.c("&c/&6prx setrankdisplay <name> <displayname>"));
        	sender.sendMessage(main.prxAPI.c("&c/&6prx setrankpath <name> <path>"));
        	sender.sendMessage(main.prxAPI.c("&c/&6prx delrank <name>"));
        	sender.sendMessage(main.prxAPI.c("&c/&6prx setdefaultrank <name> &7⎟ &3set the default rank in the default path"));
        	sender.sendMessage(main.prxAPI.c("&c/&6prx setlastrank <name> &7⎟ &3set the last rank in the default path"));
        	sender.sendMessage(main.prxAPI.c("&c/&6prx setrank <player> <rank> [pathname] &7⎟ &3set the player rank"));
        	sender.sendMessage(main.prxAPI.c("&c/&6prx resetrank <player> [pathname] &7⎟ &3reset the player rank"));
        	sender.sendMessage(main.prxAPI.c("&c/&6forcerankup <player>"));
            sender.sendMessage(main.prxAPI.c("&3[&6Page&3] &7(&f1&7/&f3)"));
            sender.sendMessage(main.prxAPI.c("&c&m                                                                      &c"));
        } else if (args.length == 1) {
        	if(args[0].equalsIgnoreCase("getprestiges")) {
        		sender.sendMessage(String.valueOf(main.prxAPI.getPlayerPrestiges((Player)sender)));
        	}
        	if(args[0].equalsIgnoreCase("dev")) {
        		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
        			try {
        				sender.sendMessage(main.prxAPI.c("&2Converting rankdata..."));
                  FileConfiguration oldRanked = YamlConfiguration.loadConfiguration(new File(main.getDataFolder() + "/ranked.yml"));
                  for(String uuids : oldRanked.getConfigurationSection("Players").getKeys(false)) {
                	  String uuid = uuids;
                	  String rank = oldRanked.getString("Players." + uuid);
                	  main.configManager.rankDataConfig.set("players." + uuid + ".rank", rank);
                	  main.configManager.rankDataConfig.set("players." + uuid + ".path", main.prxAPI.getDefaultPath());
                  }
                  main.configManager.saveRankDataConfig();
                  sender.sendMessage(main.prxAPI.c("&eRank data conversion success."));
        			} catch (Exception err) {
        				sender.sendMessage(main.prxAPI.c("&cRank data is already converted."));
        			}
        			try {
        				FileConfiguration oldPrestiged = YamlConfiguration.loadConfiguration(new File(main.getDataFolder() + "/prestiged.yml"));
        				for(String uuids : oldPrestiged.getConfigurationSection("Players").getKeys(false)) {
        					String prestige = oldPrestiged.getString("Players." + uuids);
        					main.configManager.prestigeDataConfig.set("players." + uuids, prestige);
        				}
        				main.configManager.savePrestigeDataConfig();
        				sender.sendMessage(main.prxAPI.c("&ePrestige data conversion success."));
        			} catch (Exception err) {
        				sender.sendMessage(main.prxAPI.c("&cPrestige data is already converted."));
        			}
        		});
        	}
        	else if(args[0].equalsIgnoreCase("terminate")) {
        		if(main.terminateMode) {
        		sender.sendMessage(main.prxAPI.c("&7Terminate mode &cdisabled&7."));
        		main.terminateMode = false;
        		} else {
        			sender.sendMessage(main.prxAPI.c("&7Terminate mode &aenabled&7."));
        			main.terminateMode = true;
        		}
        	}
        	else if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
            	sender.sendMessage(main.prxAPI.c("&3[&6PrisonRanksX&3] &av" + ver));
            	sender.sendMessage(main.prxAPI.c("&7<> = required &8⎟ &7[] = optional &8⎟ &7() = optional prefix"));
                sender.sendMessage(main.prxAPI.c("&c&m                                                                      &c"));
            	sender.sendMessage(main.prxAPI.c("&c/&6prx help [page] &7⎟ &3type member in the page for members help"));
            	sender.sendMessage(main.prxAPI.c("&c/&6prx reload &7⎟ &3reloads the entire plugin"));
            	sender.sendMessage(main.prxAPI.c("&c/&6prx save &7⎟ &3save the ranks/prestiges/etc.. you created ingame"));
            	sender.sendMessage(main.prxAPI.c("&c/&6prx createrank <name> <cost> [displayname] (-path:)[pathname]"));
            	sender.sendMessage(main.prxAPI.c("&c/&6prx setrankcost <name> <cost>"));
            	sender.sendMessage(main.prxAPI.c("&c/&6prx setrankdisplay <name> <displayname>"));
            	sender.sendMessage(main.prxAPI.c("&c/&6prx setrankpath <name> <path>"));
            	sender.sendMessage(main.prxAPI.c("&c/&6prx delrank <name>"));
            	sender.sendMessage(main.prxAPI.c("&c/&6prx setdefaultrank <name> &7⎟ &3set the default rank in the default path"));
            	sender.sendMessage(main.prxAPI.c("&c/&6prx setlastrank <name> &7⎟ &3set the last rank in the default path"));
            	sender.sendMessage(main.prxAPI.c("&c/&6prx setrank <player> <rank> [pathname] &7⎟ &3set the player rank"));
            	sender.sendMessage(main.prxAPI.c("&c/&6prx resetrank <player> [pathname] &7⎟ &3reset the player rank"));
                sender.sendMessage(main.prxAPI.c("&3[&6Page&3] &7(&f1&7/&f3)"));
                sender.sendMessage(main.prxAPI.c("&c&m                                                                      &c"));
        	} else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
        		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
        		sender.sendMessage(main.prxAPI.c("&eReloading..."));
        		main.manager.reload();
        		sender.sendMessage(main.prxAPI.g("reload"));
        		});
        	} else if (args[0].equalsIgnoreCase("cleartask")) {
        		main.prxAPI.taskedPlayers.clear();
        		sender.sendMessage(main.prxAPI.c("&c&k~&r &4&l&o&nTask limit cleared&r &c&k~"));
        	} else if (args[0].equalsIgnoreCase("errors")) {
        		if(main.errorInspector.getErrors().isEmpty()) {
        			sender.sendMessage(main.prxAPI.c("&" + getRandomColor() + "&l&oNo errors were found."));
        			return true;
        		}
        		main.errorInspector.getErrors().forEach(error -> {
        			sender.sendMessage(main.prxAPI.c(error));
        		});
        	} else if (args[0].equalsIgnoreCase("save")) {
        		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
        		sender.sendMessage(main.prxAPI.c("&eSaving data..."));
        		main.manager.save();
        		sender.sendMessage(main.prxAPI.g("save"));
        		});
        	} else if (args[0].equalsIgnoreCase("createrank")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx createrank &4<name> <cost> &c[displayname]"));
        	} else if (args[0].equalsIgnoreCase("createprestige")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx createprestige &4<name> <cost> &c[displayname]"));
        	} else if (args[0].equalsIgnoreCase("createrebirth")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx createrebirth &4<name> <cost> &c[displayname]"));
        	} else if (args[0].equalsIgnoreCase("setrankcost")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx setrankcost &4<name> <cost>"));
        	} else if (args[0].equalsIgnoreCase("setprestigecost")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx setprestigecost &4<name> <cost>"));
        	} else if (args[0].equalsIgnoreCase("setrebirthcost")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx setrebirthcost &4<name> <cost>"));
        	} else if (args[0].equalsIgnoreCase("setrankdisplay") || args[0].equalsIgnoreCase("setrankdisplayname")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx setrankdisplay &4<name> <displayname>"));
        	} else if (args[0].equalsIgnoreCase("setprestigedisplay") || args[0].equalsIgnoreCase("setprestigedisplayname")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx setprestigedisplay &4<name> <displayname>"));
        	} else if (args[0].equalsIgnoreCase("setrebirthdisplay") || args[0].equalsIgnoreCase("setrebirthdisplayname")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx setrebirthdisplay &4<name> <displayname>"));
        	} else if (args[0].equalsIgnoreCase("setrankpath") || args[0].equalsIgnoreCase("setrankpathname")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx setrankpath &4<name> <path>"));
        	} else if (args[0].equalsIgnoreCase("delrank") || args[0].equalsIgnoreCase("deleterank")
        			|| args[0].equalsIgnoreCase("removerank") || args[0].equalsIgnoreCase("remrank")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx " + args[0] + " &4<name>"));
        	} else if (args[0].equalsIgnoreCase("delprestige") || args[0].equalsIgnoreCase("deleteprestige")
        			|| args[0].equalsIgnoreCase("removeprestige") || args[0].equalsIgnoreCase("remprestige")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx " + args[0] + " &4<name>"));
        	} else if (args[0].equalsIgnoreCase("delrebirth") || args[0].equalsIgnoreCase("deleterebirth")
        			|| args[0].equalsIgnoreCase("removerebirth") || args[0].equalsIgnoreCase("remrebirth")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx " + args[0] + " &4<name>"));
        	} else if (args[0].equalsIgnoreCase("setdefaultrank") || args[0].equalsIgnoreCase("setfirstrank")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx " + args[0] + " &4<name>"));
        	} else if (args[0].equalsIgnoreCase("setfirstprestige") || args[0].equalsIgnoreCase("setdefaultprestige")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx " + args[0] + " &4<name>"));
        	} else if (args[0].equalsIgnoreCase("setdefaultrebirth") || args[0].equalsIgnoreCase("setfirstrebirth")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx " + args[0] + " &4<name>"));
        	} else if (args[0].equalsIgnoreCase("setlastrank") || args[0].equalsIgnoreCase("setfinalrank")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx " + args[0] + " &4<name>"));
        	} else if (args[0].equalsIgnoreCase("setlastprestige") || args[0].equalsIgnoreCase("setfinalprestige")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx " + args[0] + " &4<name>"));
        	} else if (args[0].equalsIgnoreCase("setlastrebirth") || args[0].equalsIgnoreCase("setfinalrebirth")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx " + args[0] + " &4<name>"));
        	} else if (args[0].equalsIgnoreCase("setrank")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx setrank &4<player> <rankname>"));
        	} else if (args[0].equalsIgnoreCase("setprestige")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx setprestige &4<player> <prestigename>"));
        	} else if (args[0].equalsIgnoreCase("setrebirth")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx setrebirth &4<player> <rebirthname>"));
        	} else if (args[0].equalsIgnoreCase("resetrank")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx resetrank &4<player>"));
        	} else if (args[0].equalsIgnoreCase("resetprestige")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx resetprestige &4<player>"));
        	} else if (args[0].equalsIgnoreCase("resetrebirth")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx resetrebirth &4<player>"));
        	}
        } else if(args.length == 2) {
        	if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
        		if(args[1].equalsIgnoreCase("1")) {
                	sender.sendMessage(main.prxAPI.c("&3[&6PrisonRanksX&3] &av" + ver));
                	sender.sendMessage(main.prxAPI.c("&7<> = required &8⎟ &7[] = optional &8⎟ &7() = optional prefix"));
                    sender.sendMessage(main.prxAPI.c("&c&m                                                                      &c"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx help [page] &7⎟ &3shows the available commands"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx reload &7⎟ &3reloads the entire plugin"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx save &7⎟ &3save the ranks/prestiges/etc.. you created ingame"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx createrank <name> <cost> [displayname] (-path:)[pathname]"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx setrankcost <name> <cost>"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx setrankdisplay <name> <displayname>"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx setrankpath <name> <path>"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx delrank <name>"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx setdefaultrank <name> &7⎟ &3set the default rank in the default path"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx setlastrank <name> &7⎟ &3set the last rank in the default path"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx setrank <player> <rank> [pathname] &7⎟ &3set the player rank"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx resetrank <player> [pathname] &7⎟ &3reset the player rank"));
                    sender.sendMessage(main.prxAPI.c("&3[&6Page&3] &7(&f1&7/&f3)"));
                    sender.sendMessage(main.prxAPI.c("&c&m                                                                      &c"));
        		} else if (args[1].equalsIgnoreCase("2")) {
                   	sender.sendMessage(main.prxAPI.c("&3[&6PrisonRanksX&3] &av" + ver));
                	sender.sendMessage(main.prxAPI.c("&7<> = required &8⎟ &7[] = optional"));
                    sender.sendMessage(main.prxAPI.c("&c&m                                                                      &c"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx createprestige <name> <cost> [displayname]"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx setprestigecost <name> <cost>"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx setprestigedisplay <name> <displayname>"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx delprestige <name>"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx setfirstprestige <name>"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx setlastprestige <name>"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx setprestige <player> <prestige>"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx resetprestige <player>"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx delplayerprestige <player>"));
                    sender.sendMessage(main.prxAPI.c("&3[&6Page&3] &7(&f2&7/&f3)"));
                    sender.sendMessage(main.prxAPI.c("&c&m                                                                      &c"));
        		} else if (args[1].equalsIgnoreCase("3")) {
                   	sender.sendMessage(main.prxAPI.c("&3[&6PrisonRanksX&3] &av" + ver));
                	sender.sendMessage(main.prxAPI.c("&7<> = required &8⎟ &7[] = optional"));
                    sender.sendMessage(main.prxAPI.c("&c&m                                                                      &c"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx createrebirth <name> <cost> [displayname]"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx setrebirthcost <name> <cost>"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx setrebirthdisplay <name> <displayname>"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx delrebirth <name>"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx setfirstrebirth <name>"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx setlastrebirth <name>"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx setrebirth <player> <rebirth>"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx resetrebirth <player>"));
                	sender.sendMessage(main.prxAPI.c("&c/&6prx delplayerrebirth <player>"));
                    sender.sendMessage(main.prxAPI.c("&3[&6Page&3] &7(&f3&7/&f3)"));
                    sender.sendMessage(main.prxAPI.c("&c&m                                                                      &c"));
        		} else if (args[1].equalsIgnoreCase("member")) {
        			sender.sendMessage(main.prxAPI.c("&7- &9Prison Help &7-"));
        			sender.sendMessage(main.prxAPI.c("&6/rankup"));
        			sender.sendMessage(main.prxAPI.c("&6/rankupmax"));
        			sender.sendMessage(main.prxAPI.c("&6/ranks"));
        			sender.sendMessage(main.prxAPI.c("&6/prestige"));
        			sender.sendMessage(main.prxAPI.c("&6/prestiges"));
        			sender.sendMessage(main.prxAPI.c("&6/rebirth"));
        			sender.sendMessage(main.prxAPI.c("&6/rebirths"));
        			sender.sendMessage(main.prxAPI.c("&6/autorankup"));
        			sender.sendMessage(main.prxAPI.c("&6/autoprestige"));
        		}
        	} else if (args[0].equalsIgnoreCase("createrank")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx createrank <name> &4<cost> &c[displayname]"));
        	} else if (args[0].equalsIgnoreCase("createprestige")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx createprestige <name> &4<cost> &c[displayname]"));
        	} else if (args[0].equalsIgnoreCase("createrebirth")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx createrebirth <name> &4<cost> &c[displayname]"));
        	} else if (args[0].equalsIgnoreCase("setrank")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx setrank <player> &4<rank>"));
        	} else if (args[0].equalsIgnoreCase("setprestige")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx setprestige <player> &4<prestige>"));
        	} else if (args[0].equalsIgnoreCase("setrebirth")) {
        		sender.sendMessage(main.prxAPI.c("&c/&6prx setrebirth <player> &4<rebirth>"));
        	}
        	else if(args[0].equalsIgnoreCase("delrank")) {
        		String matchedRank = main.manager.matchRank(args[1]);
        		main.manager.delRank(matchedRank);
        		sender.sendMessage(main.prxAPI.g("delrank").replace("%args1%", matchedRank));
        	} else if (args[0].equalsIgnoreCase("delprestige")) {
        		String matchedPrestige = main.manager.matchPrestige(args[1]);
        		main.manager.delPrestige(matchedPrestige);
        		sender.sendMessage(main.prxAPI.g("delprestige").replace("%args1%", matchedPrestige));
        	} else if (args[0].equalsIgnoreCase("delrebirth")) {
        		String matchedRebirth = main.manager.matchRebirth(args[1]);
        		main.manager.delRebirth(matchedRebirth);
        		sender.sendMessage(main.prxAPI.g("delrebirth").replace("%args1%", matchedRebirth));
        	} else  if (args[0].equalsIgnoreCase("resetrank")) {
        		String parsedPlayerName = args[1];
        		if(Bukkit.getPlayer(parsedPlayerName) == null) {
        			sender.sendMessage(main.prxAPI.g("playernotfound").replace("%player%", args[1]));
        			return true;
        		}
        		Player p = Bukkit.getPlayer(parsedPlayerName);
        		XRankUpdateEvent e = new XRankUpdateEvent(p, RankUpdateCause.RANKSET, main.globalStorage.getStringData("defaultrank"));
        		if(e.isCancelled()) {
        			return true;
        		}
        		main.prxAPI.resetPlayerRank(p);
        		sender.sendMessage(main.prxAPI.g("resetrank").replace("%target%", p.getName())
        				.replace("%firstrank%", main.globalStorage.getStringData("defaultrank")));
        		if(main.globalStorage.getStringListMap().get("RankOptions.rank-reset-cmds").contains("[rankpermissions]")) {
        		    Set<String> perms = main.prxAPI.allRankAddPermissions;
        		    for(String perm : perms) {
        			    main.perm.delPermission(p, perm);
        		    }
        		}
        		for(String command : main.globalStorage.getStringListMap().get("RankOptions.rank-reset-cmds")) {
        			if(!command.startsWith("[rankpermissions") && !command.startsWith("[prestigeperm") && !command.startsWith("[rebirthp")) {
        				main.executeCommand(p, command);
        			}
        		}
        		Bukkit.getServer().getPluginManager().callEvent(e);
        	} else if (args[0].equalsIgnoreCase("resetprestige")) {
        		String parsedPlayerName = args[1];
        		if(Bukkit.getPlayer(parsedPlayerName) == null) {
        			sender.sendMessage(main.prxAPI.g("playernotfound").replace("%player%", args[1]));
        			return true;
        		}
        		Player p = Bukkit.getPlayer(parsedPlayerName);
        		XRankUpdateEvent e = new XRankUpdateEvent(p, RankUpdateCause.RANKSET, main.globalStorage.getStringData("defaultrank"));
        		if(e.isCancelled()) {
        			return true;
        		}
        		main.prxAPI.setPlayerPrestige(p, main.prxAPI.getFirstPrestige());
        		sender.sendMessage(main.prxAPI.g("resetprestige").replace("%target%", p.getName())
        				.replace("%firstprestige%", main.globalStorage.getStringData("firstprestige")));
        		if(main.globalStorage.getStringListMap().get("PrestigeOptions.prestige-reset-cmds").contains("[rankpermissions]")) {
        		    Set<String> perms = main.prxAPI.allRankAddPermissions;
        		    for(String perm : perms) {
        			    main.perm.delPermission(p, perm);
        		    }
        		}
        		if(main.globalStorage.getStringListMap().get("PrestigeOptions.prestige-reset-cmds").contains("[prestigepermissions$1]")) {
        			Set<String> perms = main.prxAPI.allPrestigeAddPermissions;
        			main.prestigeStorage.getAddPermissionList(main.prxAPI.getFirstPrestige()).forEach(fperm -> {
        				perms.remove(fperm);
        			});
        			for(String perm : perms) {
        				main.perm.delPermission(p, perm);
        			}
        		}
        		for(String cmd : main.globalStorage.getStringListMap().get("PrestigeOptions.prestige-reset-cmds")) {
        			if(!cmd.endsWith("permissions] remove")) {
        				main.executeCommand(p, cmd);
        			}
        		}
        	} else if (args[0].equalsIgnoreCase("resetrebirth")) {
        		String parsedPlayerName = args[1];
        		if(Bukkit.getPlayer(parsedPlayerName) == null) {
        			sender.sendMessage(main.prxAPI.g("playernotfound").replace("%player%", args[1]));
        			return true;
        		}
        		Player p = Bukkit.getPlayer(parsedPlayerName);
        		XRebirthUpdateEvent e = new XRebirthUpdateEvent(p, RebirthUpdateCause.SETREBIRTH);
        		if(e.isCancelled()) {
        			return true;
        		}
        		main.prxAPI.setPlayerRebirth(p, main.prxAPI.getFirstRebirth());
        		sender.sendMessage(main.prxAPI.g("resetrebirth").replace("%target%", p.getName())
        				.replace("%firstrebirth%", main.globalStorage.getStringData("firstrebirth")));
        		if(main.globalStorage.getStringListMap().get("RebirthOptions.rebirth-reset-cmds").contains("[rankpermissions]")) {
        		    Set<String> perms = main.prxAPI.allRankAddPermissions;
        		    for(String perm : perms) {
        			    main.perm.delPermission(p, perm);
        		    }
        		}
        		if(main.globalStorage.getStringListMap().get("RebirthOptions.rebirth-reset-cmds").contains("[prestigepermissions]")) {
        			Set<String> perms = main.prxAPI.allPrestigeAddPermissions;
        			for(String perm : perms) {
        				main.perm.delPermission(p, perm);
        			}
        		}
        		if(main.globalStorage.getStringListMap().get("RebirthOptions.rebirth-reset-cmds").contains("[rebirthpermissions$1]")) {
        			Set<String> perms = main.prxAPI.allRebirthAddPermissions;
        			main.rebirthStorage.getAddPermissionList(main.prxAPI.getFirstRebirth()).forEach(fperm -> {
        				perms.remove(fperm);
        			});
        			for(String perm : perms) {
        				main.perm.delPermission(p, perm);
        			}
        		}
        		for(String cmd : main.globalStorage.getStringListMap().get("RebirthOptions.rebirth-reset-cmds")) {
        			if(!cmd.endsWith("permissions] remove")) {
        				main.executeCommand(p, cmd);
        			}
        		}
        		Bukkit.getServer().getPluginManager().callEvent(e);
        	} else if (args[0].equalsIgnoreCase("setdefaultrank") || args[0].equalsIgnoreCase("setfirstrank")) {
        		String rankn = main.manager.matchRank(args[1]);
        		main.manager.setDefaultRank(rankn, true);
        		sender.sendMessage(main.prxAPI.g("setdefaultrank").replace("%args1%", rankn));
        	} else if (args[0].equalsIgnoreCase("setlastrank")) {
        		String rankn = main.manager.matchRank(args[1]);
        		main.manager.setLastRank(rankn, true);
        		sender.sendMessage(main.prxAPI.g("setlastrank").replace("%args1%", rankn));
        	} else if (args[0].equalsIgnoreCase("delprestige")) {
        		String prestigen = main.manager.matchPrestige(args[1]);
        		main.manager.delPrestige(prestigen);
        		sender.sendMessage(main.prxAPI.g("delprestige").replace("%args1%", prestigen));
        	} else if (args[0].equalsIgnoreCase("setfirstprestige")) {
        		String prestigen = main.manager.matchPrestige(args[1]);
        	    main.manager.setFirstPrestige(prestigen, true);
                sender.sendMessage(main.prxAPI.g("setfirstprestige").replace("%args1%", prestigen));
        	} else if (args[0].equalsIgnoreCase("setlastprestige")) {
        		String prestigen = main.manager.matchPrestige(args[1]);
        		main.manager.setLastPrestige(prestigen, true);
        		sender.sendMessage(main.prxAPI.g("setlastprestige").replace("%args1%", prestigen));
        	} else if (args[0].equalsIgnoreCase("setfirstrebirth")) {
        		String rebirthn = main.manager.matchRebirth(args[1]);
        		main.manager.setFirstRebirth(rebirthn, true);
        		sender.sendMessage(main.prxAPI.g("setfirstrebirth").replace("%args1%", rebirthn));
        	} else if (args[0].equalsIgnoreCase("setlastrebirth")) {
        		String rebirthn = main.manager.matchRebirth(args[1]);
        		main.manager.setLastRebirth(rebirthn, true);
        		sender.sendMessage(main.prxAPI.g("setlastrebirth").replace("%args1%", rebirthn));
        	} else if (args[0].equalsIgnoreCase("delrebirth")) {
        		String rebirthn = main.manager.matchRebirth(args[1]);
        		main.manager.delRebirth(rebirthn);
        		sender.sendMessage(main.prxAPI.g("delrebirth").replace("%args1%", rebirthn));
        	} else if (args[0].equalsIgnoreCase("delplayerprestige")) {
        		if(Bukkit.getPlayer(args[1]) == null) {
        			sender.sendMessage(main.prxAPI.g("playernotfound").replace("%player%", args[1]));
        			return true;
        		}
        		Player p = Bukkit.getPlayer(args[1]);
        		XUser user = XUser.getXUser(p);
        		XPrestigeUpdateEvent e = new XPrestigeUpdateEvent(p, PrestigeUpdateCause.DELPRESTIGE);
        		if(e.isCancelled()) {
        			return true;
        		}
        		main.manager.delPlayerPrestige(user);
        		if(main.globalStorage.getStringListMap().get("PrestigeOptions.prestige-delete-cmds").contains("[rankpermissions]")) {
        		    Set<String> perms = main.prxAPI.allRankAddPermissions;
        		    for(String perm : perms) {
        			    main.perm.delPermission(p, perm);
        		    }
        		} if (main.globalStorage.getStringListMap().get("PrestigeOptions.prestige-delete-cmds").contains("[prestigepermissions]")) {
        			Set<String> perms2 = main.prxAPI.allPrestigeAddPermissions;
        			for(String perm : perms2) {
        				main.perm.delPermission(p, perm);
        			}
        		}
        		for(String cmd : main.globalStorage.getStringListMap().get("PrestigeOptions.prestige-delete-cmds")) {
        			if(!cmd.endsWith("permissions] remove")) {
        				main.executeCommand(p, cmd);
        			}
        		}
        		sender.sendMessage(main.prxAPI.g("delplayerprestige").replace("%player%", p.getName()));
        		Bukkit.getPluginManager().callEvent(e);
        	} else if (args[0].equalsIgnoreCase("delplayerrebirth")) {
        		if(Bukkit.getPlayer(args[1]) == null) {
        			sender.sendMessage(main.prxAPI.g("playernotfound").replace("%player%", args[1]));
        			return true;
        		}
        		Player p = Bukkit.getPlayer(args[1]);
        		XUser user = XUser.getXUser(p);
        		XRebirthUpdateEvent e = new XRebirthUpdateEvent(p, RebirthUpdateCause.DELREBIRTH);
        		if(e.isCancelled()) {
        			return true;
        		}
        		main.manager.delPlayerRebirth(user);
        		if(main.globalStorage.getStringListMap().get("RebirthOptions.rebirth-delete-cmds").contains("[rankpermissions]")) {
        		    Set<String> perms = main.prxAPI.allRankAddPermissions;
        		    for(String perm : perms) {
        			    main.perm.delPermission(p, perm);
        		    }
        		} if (main.globalStorage.getStringListMap().get("RebirthOptions.rebirth-delete-cmds").contains("[prestigepermissions]")) {
        			Set<String> perms2 = main.prxAPI.allPrestigeAddPermissions;
        			for(String perm : perms2) {
        				main.perm.delPermission(p, perm);
        			}
        		}
        		if(main.globalStorage.getStringListMap().get("RebirthOptions.rebirth-delete-cmds").contains("[rebirthpermissions]")) {
        			Set<String> perms = main.prxAPI.allRebirthAddPermissions;
        			for(String perm : perms) {
        				main.perm.delPermission(p, perm);
        			}
        		}
        		for(String cmd : main.globalStorage.getStringListMap().get("RebirthOptions.rebirth-delete-cmds")) {
        			if(!cmd.endsWith("permissions] remove")) {
        				main.executeCommand(p, cmd);
        			}
        		}
        		sender.sendMessage(main.prxAPI.g("delplayerrebirth").replace("%player%", p.getName()));
        		Bukkit.getPluginManager().callEvent(e);
        	}
        } else if(args.length == 3) {
        	if(args[0].equalsIgnoreCase("createrank")) {
        		double costy;
        		if(!main.prxAPI.numberAPI.isNumber(args[2])) {
        			costy = main.prxAPI.numberAPI.parseBalance(args[2]);
        		} else {
        			costy = Double.valueOf(args[2]);
        		}
        		main.manager.createRank(args[1], Double.valueOf(costy));
        		main.configManager.saveRanksConfig();
        		sender.sendMessage(main.prxAPI.g("createrank").replace("%createdrank%", args[1])
        				.replace("%rankcost%", args[2]));
        	} else if (args[0].equalsIgnoreCase("createprestige")) {
        		double costy;
        		if(!main.prxAPI.numberAPI.isNumber(args[2])) {
        			costy = main.prxAPI.numberAPI.parseBalance(args[2]);
        		} else {
        			costy = Double.valueOf(args[2]);
        		}
        		main.manager.createPrestige(args[1], Double.valueOf(costy));
        		main.configManager.savePrestigesConfig();
        		sender.sendMessage(main.prxAPI.g("createprestige").replace("%createdprestige", args[1])
        				.replace("%prestigecost%", args[2]));
        	} else if (args[0].equalsIgnoreCase("createrebirth")) {
        		double costy;
        		if(!main.prxAPI.numberAPI.isNumber(args[2])) {
        			costy = main.prxAPI.numberAPI.parseBalance(args[2]);
        		} else {
        			costy = Double.valueOf(args[2]);
        		}
        		main.manager.createRebirth(args[1], Double.valueOf(costy));
        		main.configManager.saveRebirthsConfig();
        		sender.sendMessage(main.prxAPI.g("createrebirth").replace("%createdrebirth%", args[1])
        		        .replace("%rebirthcost%", args[2]));
        	} else  if (args[0].equalsIgnoreCase("setrankcost")) {
          		double costy;
        		if(!main.prxAPI.numberAPI.isNumber(args[2])) {
        			costy = main.prxAPI.numberAPI.parseBalance(args[2]);
        		} else {
        			costy = Double.valueOf(args[2]);
        		}
        		String matchedRank = main.manager.matchRank(args[1]);
        		main.manager.setRankCost(matchedRank, costy);
        		main.configManager.saveRanksConfig();
        		sender.sendMessage(main.prxAPI.g("setrankcost").replace("%args1%", matchedRank)
        				.replace("%args2%", args[2]));
        	} else if (args[0].equalsIgnoreCase("setprestigecost")) {
        		double costy;
        		if(!main.prxAPI.numberAPI.isNumber(args[2])) {
        			costy = main.prxAPI.numberAPI.parseBalance(args[2]);
        		} else {
        			costy = Double.valueOf(args[2]);
        		}
        		String matchedPrestige = main.manager.matchPrestige(args[1]);
        		main.manager.setPrestigeCost(matchedPrestige, costy);
        		main.configManager.savePrestigesConfig();
        		sender.sendMessage(main.prxAPI.g("setprestigecost").replace("%args1%", matchedPrestige)
        				.replace("%args2%", args[2]));
        	} else if (args[0].equalsIgnoreCase("setrebirthcost")) {
        		double costy;
        		if(!main.prxAPI.numberAPI.isNumber(args[2])) {
        			costy = main.prxAPI.numberAPI.parseBalance(args[2]);
        		} else {
        			costy = Double.valueOf(args[2]);
        		}
        		String matchedRebirth = main.manager.matchRebirth(args[1]);
        		main.manager.setRebirthCost(matchedRebirth, costy);
        		main.configManager.saveRebirthsConfig();
        		sender.sendMessage(main.prxAPI.g("setrebirthcost").replace("%args1%", matchedRebirth)
        				.replace("%args2%", args[2]));
        	} else if (args[0].equalsIgnoreCase("setrankdisplay")) {
        		String matchedRank = main.manager.matchRank(args[1]);
        		String newDisplayName = main.getArgs(args, 2);
        		main.manager.setRankDisplayName(matchedRank, newDisplayName);
        		main.configManager.saveRanksConfig();
        		sender.sendMessage(main.prxAPI.g("setrankdisplay").replace("%args1%", matchedRank)
        				.replace("%args2%", newDisplayName + " §f=> " + main.getStringWithoutPAPI(newDisplayName)));
        	} else if (args[0].equalsIgnoreCase("setprestigedisplay")) {
        		String matchedPrestige = main.manager.matchPrestige(args[1]);
        		String newDisplayName = main.getArgs(args, 2);
        		main.manager.setPrestigeDisplayName(matchedPrestige, newDisplayName);
        		main.configManager.savePrestigesConfig();
        		sender.sendMessage(main.prxAPI.g("setprestigedisplay").replace("%args1%", matchedPrestige)
        				.replace("%args2%", newDisplayName + " §f=> " + main.getStringWithoutPAPI(newDisplayName)));
        	} else if (args[0].equalsIgnoreCase("setrebirthdisplay")) {
        		String matchedRebirth = main.manager.matchRebirth(args[1]);
        		String newDisplayName = main.getArgs(args, 2);
        		main.manager.setRebirthDisplayName(matchedRebirth, newDisplayName);
        		main.configManager.savePrestigesConfig();
        		sender.sendMessage(main.prxAPI.g("setrebirthdisplay").replace("%args1%", matchedRebirth)
        				.replace("%args2%", newDisplayName + " §f=> " + main.getStringWithoutPAPI(newDisplayName)));
        	} else if (args[0].equalsIgnoreCase("setrank")) {
        		if(Bukkit.getPlayer(args[1]) == null) {
            			sender.sendMessage(main.prxAPI.g("playernotfound").replace("%player%", args[1]));
        			return true;
        		}
        		Player p = Bukkit.getPlayer(args[1]);
        		String newRank = main.manager.matchRank(args[2]);
        		if(!main.prxAPI.rankExists(newRank)) {
        			sender.sendMessage(main.prxAPI.g("rank-notfound").replace("%rank%", newRank));
        			return true;
        		}
        		XRankUpdateEvent e = new XRankUpdateEvent(p, RankUpdateCause.RANKSET);
        		if(e.isCancelled()) {
        			return true;
        		}
        		main.prxAPI.setPlayerRank(p, newRank);
        		sender.sendMessage(main.prxAPI.g("setrank").replace("%target%", p.getName())
        				.replace("%settedrank%", newRank));
        		Bukkit.getPluginManager().callEvent(e);
        	} else if (args[0].equalsIgnoreCase("setpath")) {
        		if(Bukkit.getPlayer(args[1]) == null) {
        			sender.sendMessage(main.prxAPI.g("playernotfound").replace("%player%", args[1]));
    			    return true;
    		    }
    		    Player p = Bukkit.getPlayer(args[1]);
    		    String newPath = main.manager.matchPath(args[2]);
    		    if(!main.prxAPI.pathExists(newPath)) {
    		    	sender.sendMessage(main.prxAPI.g("path-notfound").replace("%path%", newPath));
    		    	return true;
    		    }
    		    main.prxAPI.setPlayerPath(p, newPath);
    		    if(!main.prxAPI.rankExists(main.prxAPI.getPlayerRank(p), newPath)) {
    		    	sender.sendMessage(main.prxAPI.c("&cUnable to find player's rank within the new path."));
    		    	return true;
    		    }
        	} else if (args[0].equalsIgnoreCase("setprestige")) {
        		if(Bukkit.getPlayer(args[1]) == null) {
        			  sender.sendMessage(main.prxAPI.g("playernotfound").replace("%player%", args[1]));
        			return true;
        		}
        		Player p = Bukkit.getPlayer(args[1]);
        		String newPrestige = main.manager.matchPrestige(args[2]);
        		if(!main.prxAPI.prestigeExists(newPrestige)) {
        			sender.sendMessage(main.prxAPI.g("prestige-notfound").replace("%prestige%", newPrestige));
        			return true;
        		}
        		XPrestigeUpdateEvent e = new XPrestigeUpdateEvent(p, PrestigeUpdateCause.SETPRESTIGE);
        		if(e.isCancelled()) {
        			return true;
        		}
        		main.prxAPI.setPlayerPrestige(p, newPrestige);
        		sender.sendMessage(main.prxAPI.g("setprestige").replace("%target%", p.getName())
        				.replace("%settedprestige%", newPrestige));
        		Bukkit.getPluginManager().callEvent(e);
        	} else if (args[0].equalsIgnoreCase("setrebirth")) {
        		if(Bukkit.getPlayer(args[1]) == null) {
      			  sender.sendMessage(main.prxAPI.g("playernotfound").replace("%player%", args[1]));
      			return true;
      		}
      		Player p = Bukkit.getPlayer(args[1]);
      		String newRebirth = main.manager.matchRebirth(args[2]);
    		if(!main.prxAPI.rebirthExists(newRebirth)) {
    			sender.sendMessage(main.prxAPI.g("rebirth-notfound").replace("%rebirth%", newRebirth));
    			return true;
    		}
    		XRebirthUpdateEvent e = new XRebirthUpdateEvent(p, RebirthUpdateCause.SETREBIRTH);
    		if(e.isCancelled()) {
    			return true;
    		}
      		main.prxAPI.setPlayerRebirth(p, newRebirth);
      		sender.sendMessage(main.prxAPI.g("setrebirth").replace("%target%", p.getName())
      				.replace("%settedrebirth%", newRebirth));
      		Bukkit.getPluginManager().callEvent(e);
        	} else if (args[0].equalsIgnoreCase("setrankpath")) {
            	String rank = main.manager.matchRank(args[1]);
            	String newPath = main.manager.matchPath(args[2]);
            	main.manager.setRankPathName(rank, newPath);
            	sender.sendMessage(main.prxAPI.g("setrankpath").replace("%args1%", rank)
            			.replace("%args2%", newPath));
            }
        } else if (args.length >= 4) {
        	if (args[0].equalsIgnoreCase("setrank")) {
        		if(Bukkit.getPlayer(args[1]) == null) {
            			sender.sendMessage(main.prxAPI.g("playernotfound").replace("%player%", args[1]));
        			return true;
        		}
        		Player p = Bukkit.getPlayer(args[1]);
        		String newRank = main.manager.matchRank(args[2]);
        		if(!main.prxAPI.rankExists(newRank)) {
        			sender.sendMessage(main.prxAPI.g("rank-notfound").replace("%rank%", newRank));
        			return true;
        		}
        		String newPath = main.manager.matchPath(args[3]);
        		RankPath rp = new RankPath(newRank, newPath);
        		if(!main.prxAPI.rankPathExists(rp)) {
        			sender.sendMessage(main.prxAPI.g("path-notfound").replace("%path%", newPath));
        			return true;
        		}
        		XRankUpdateEvent e = new XRankUpdateEvent(p, RankUpdateCause.RANKSET_BYCONVERT);
        		main.prxAPI.setPlayerRankPath(p, rp);
        		sender.sendMessage(main.prxAPI.g("setrank").replace("%target%", p.getName())
        				.replace("%settedrank%", newRank));
        		sender.sendMessage(main.prxAPI.c("&7Path: " + newPath));
        	}
        	if(args[0].equalsIgnoreCase("createrank")) {
        		double costy;
        		if(!main.prxAPI.numberAPI.isNumber(args[2])) {
        			costy = main.prxAPI.numberAPI.parseBalance(args[2]);
        		} else {
        			costy = Double.valueOf(args[2]);
        		}
        		String multiArgs = main.getArgs(args, 3);
        		String path = "";
        		if(multiArgs.contains("-path:")) {
        		for(String argument : multiArgs.split(" ")) {
        			if(argument.startsWith("-path:")) {
        				path = argument.substring(6);
        			}
        		}
        		path = main.manager.matchPath(path);
        		}
        		String displayName = multiArgs.replace("-path:" + path, "");
        		main.manager.createRank(args[1], Double.valueOf(costy), main.globalStorage.getStringData("defaultpath"), displayName);
        		main.configManager.saveRanksConfig();
        		sender.sendMessage(main.prxAPI.g("createrank").replace("%createdrank%", args[1])
        				.replace("%rankcost%", args[2]));
        		sender.sendMessage(main.prxAPI.c("&7Display: &r" + main.prxAPI.c(displayName)));
        		if(!path.equals("")) {
        			sender.sendMessage(main.prxAPI.c("&7Path: &6" + path));
        		}
        	} else if (args[0].equalsIgnoreCase("createprestige")) {
        		double costy;
        		if(!main.prxAPI.numberAPI.isNumber(args[2])) {
        			costy = main.prxAPI.numberAPI.parseBalance(args[2]);
        		} else {
        			costy = Double.valueOf(args[2]);
        		}
        		String displayName = main.getArgs(args, 3);
        		main.manager.createPrestige(args[1], Double.valueOf(costy), displayName);
        		main.configManager.savePrestigesConfig();
        		sender.sendMessage(main.prxAPI.g("createprestige").replace("%createdprestige", args[1])
        				.replace("%prestigecost%", args[2]));
        		sender.sendMessage(main.prxAPI.c("&7Display: " + main.prxAPI.c(displayName)));
        	} else if (args[0].equalsIgnoreCase("createrebirth")) {
        		double costy;
        		if(!main.prxAPI.numberAPI.isNumber(args[2])) {
        			costy = main.prxAPI.numberAPI.parseBalance(args[2]);
        		} else {
        			costy = Double.valueOf(args[2]);
        		}
        		String displayName = main.getArgs(args, 3);
        		main.manager.createRebirth(args[1], Double.valueOf(costy), displayName);
        		main.configManager.saveRebirthsConfig();
        		sender.sendMessage(main.prxAPI.g("createrebirth").replace("%createdrebirth%", args[1])
        		        .replace("%rebirthcost%", args[2]));
        		sender.sendMessage(main.prxAPI.c("&7Display: " + main.prxAPI.c(displayName)));
        	}
        }
		return true;
	}
}
