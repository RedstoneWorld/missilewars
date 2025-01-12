/*
 * This file is part of MissileWars (https://github.com/Butzlabben/missilewars).
 * Copyright (c) 2018-2021 Daniel Nägele.
 *
 * MissileWars is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MissileWars is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MissileWars.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.butzlabben.missilewars.configuration;

import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.game.GameManager;
import de.butzlabben.missilewars.util.SetupUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Material.JUKEBOX;
import static org.bukkit.Material.valueOf;

/**
 * @author Butzlabben
 * @since 01.01.2018
 */
public class Config {

    private static final File DIR = MissileWars.getInstance().getDataFolder();
    private static final File FILE = new File(DIR, "config.yml");
    private static YamlConfiguration cfg;
    private static boolean isNewConfig = false;

    public static void load() {

        // check if the directory and the file exists or create it new
        isNewConfig = SetupUtil.isNewConfig(DIR, FILE);

        // try to load the config
        cfg = SetupUtil.getLoadedConfig(FILE);

        // copy the config input
        cfg.options().copyDefaults(true);

        // validate the config options
        addDefaults();

        // re-save the config with only validated options
        SetupUtil.safeFile(FILE, cfg);
        cfg = SetupUtil.getLoadedConfig(FILE);

    }

    private static void addDefaults() {
        cfg.addDefault("debug", false);
        if (debug()) {
            Logger.DEBUG.log("Debug enabled");
        }

        cfg.addDefault("setup_mode", false);

        cfg.addDefault("contact_auth_server", true);
        cfg.addDefault("prefetch_players", true);

        cfg.addDefault("restart_after_fights", -1);

        cfg.addDefault("arenas.folder", "plugins/MissileWars/arenas");

        cfg.addDefault("lobbies.multiple_lobbies", false);
        cfg.addDefault("lobbies.folder", "plugins/MissileWars/lobbies");
        cfg.addDefault("lobbies.default_lobby", "lobby0.yml");

        cfg.addDefault("missiles.folder", "plugins/MissileWars/missiles");
        cfg.addDefault("shields.folder", "plugins/MissileWars/shields");

        cfg.addDefault("replace.material", JUKEBOX.name());
        cfg.addDefault("replace.after_ticks", 2);
        cfg.addDefault("replace.radius", 15);

        cfg.addDefault("motd.enable", true);
        cfg.addDefault("motd.lobby", "&6•&e● MissileWars &7| &eLobby");
        cfg.addDefault("motd.ingame", "&6•&e● MissileWars &7| &bIngame");
        cfg.addDefault("motd.ended", "&6•&e● MissileWars &7| &cRestarting...");

        cfg.addDefault("fightstats.enable", false);
        cfg.addDefault("fightstats.show_real_skins", true);

        Location spawnLocation = Bukkit.getWorlds().get(0).getSpawnLocation().add(25, 0, 25);
        cfg.addDefault("fallback_spawn.world", spawnLocation.getWorld().getName());
        cfg.addDefault("fallback_spawn.x", spawnLocation.getX());
        cfg.addDefault("fallback_spawn.y", spawnLocation.getY());
        cfg.addDefault("fallback_spawn.z", spawnLocation.getZ());
        cfg.addDefault("fallback_spawn.yaw", spawnLocation.getYaw());
        cfg.addDefault("fallback_spawn.pitch", spawnLocation.getPitch());

        cfg.addDefault("mysql.host", "localhost");
        cfg.addDefault("mysql.database", "db");
        cfg.addDefault("mysql.port", "3306");
        cfg.addDefault("mysql.user", "root");
        cfg.addDefault("mysql.password", "");
        cfg.addDefault("mysql.fights_table", "mw_fights");
        cfg.addDefault("mysql.fightmember_table", "mw_fightmember");

        cfg.addDefault("sidebar.title", "&eInfo ●&6•");
        cfg.addDefault("sidebar.member_list_style", "%team_color%%playername%");
        cfg.addDefault("sidebar.member_list_max", 4);

        if (isNewConfig) {
            List<String> sidebarList = new ArrayList<>();

            sidebarList.add("&7Time left:");
            sidebarList.add("&e» %time%m");
            sidebarList.add("");
            sidebarList.add("%team1% &7» %team1_color%%team1_amount%");
            sidebarList.add("");
            sidebarList.add("%team2% &7» %team2_color%%team2_amount%");

            cfg.set("sidebar.entries", sidebarList);
        }
    }

    public static List<String> getScoreboardEntries() {
        return cfg.getStringList("sidebar.entries");
    }

    /**
     * This method gets the minecraft material type of the block to start missiles.
     */
    public static Material getStartReplace() {
        String name = cfg.getString("replace.material").toUpperCase();
        try {
            return valueOf(name);
        } catch (Exception e) {
            Logger.WARN.log("Could not use " + name + " as start material!");
        }
        return null;
    }

    public static Location getFallbackSpawn() {
        ConfigurationSection cfg = Config.cfg.getConfigurationSection("fallback_spawn");
        World world = Bukkit.getWorld(cfg.getString("world"));
        if (world == null) {
            Logger.WARN.log("The world configured at \"fallback_location.world\" couldn't be found. Using the default one");
            world = Bukkit.getWorlds().get(0);
        }
        Location location = new Location(world,
                cfg.getDouble("x"),
                cfg.getDouble("y"),
                cfg.getDouble("z"),
                (float) cfg.getDouble("yaw"),
                (float) cfg.getDouble("pitch"));
        if (GameManager.getInstance().getGame(location) != null) {
            Logger.WARN.log("Your fallback spawn is inside a game area. This plugins functionality can no longer be guaranteed");
        }

        return location;
    }

    public static void setFallbackSpawn(Location spawnLocation) {
        cfg.set("fallback_spawn.world", spawnLocation.getWorld().getName());
        cfg.set("fallback_spawn.x", spawnLocation.getX());
        cfg.set("fallback_spawn.y", spawnLocation.getY());
        cfg.set("fallback_spawn.z", spawnLocation.getZ());
        cfg.set("fallback_spawn.yaw", spawnLocation.getYaw());
        cfg.set("fallback_spawn.pitch", spawnLocation.getPitch());

        // re-save the config with only validated options
        SetupUtil.safeFile(FILE, cfg);
    }

    public static YamlConfiguration getConfig() {
        return cfg;
    }

    public static boolean debug() {
        return cfg.getBoolean("debug");
    }

    public static boolean isSetup() {
        return cfg.getBoolean("setup_mode");
    }

    public static boolean isContactAuth() {
        return cfg.getBoolean("contact_auth_server");
    }

    public static boolean isPrefetchPlayers() {
        return cfg.getBoolean("prefetch_players");
    }

    public static int getFightRestart() {
        return cfg.getInt("restart_after_fights");
    }

    public static String getArenasFolder() {
        return cfg.getString("arenas.folder");
    }

    public static boolean isMultipleLobbies() {
        return cfg.getBoolean("lobbies.multiple_lobbies");
    }

    public static String getLobbiesFolder() {
        return cfg.getString("lobbies.folder");
    }

    public static String getDefaultLobby() {
        return cfg.getString("lobbies.default_lobby");
    }

    public static String getMissilesFolder() {
        return cfg.getString("missiles.folder");
    }

    public static String getShieldsFolder() {
        return cfg.getString("shields.folder");
    }

    public static int getReplaceTicks() {
        return cfg.getInt("replace.after_ticks");
    }

    public static int getReplaceRadius() {
        return cfg.getInt("replace.radius");
    }

    public static String motdEnded() {
        return cfg.getString("motd.ended");
    }

    public static String motdGame() {
        return cfg.getString("motd.ingame");
    }

    public static String motdLobby() {
        return cfg.getString("motd.lobby");
    }

    public static boolean motdEnabled() {
        return cfg.getBoolean("motd.enable");
    }

    public static boolean isFightStatsEnabled() {
        return cfg.getBoolean("fightstats.enable");
    }

    public static boolean isShowRealSkins() {
        return cfg.getBoolean("fightstats.show_real_skins");
    }

    public static String getHost() {
        return cfg.getString("mysql.host");
    }

    public static String getDatabase() {
        return cfg.getString("mysql.database");
    }

    public static String getPort() {
        return cfg.getString("mysql.port");
    }

    public static String getUser() {
        return cfg.getString("mysql.user");
    }

    public static String getPassword() {
        return cfg.getString("mysql.password");
    }

    public static String getFightsTable() {
        return cfg.getString("mysql.fights_table");
    }

    public static String getFightMembersTable() {
        return cfg.getString("mysql.fightmember_table");
    }

    public static String getScoreboardTitle() {
        return cfg.getString("sidebar.title");
    }

    public static String getScoreboardMembersStyle() {
        return cfg.getString("sidebar.member_list_style");
    }

    public static int getScoreboardMembersMax() {
        return cfg.getInt("sidebar.member_list_max");
    }

}
