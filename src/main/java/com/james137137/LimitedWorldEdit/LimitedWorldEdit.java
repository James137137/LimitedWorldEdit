/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.james137137.LimitedWorldEdit;

import com.james137137.LimitedWorldEdit.hooks.API;
import com.james137137.LimitedWorldEdit.hooks.WorldGaurdAPI;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author James
 */
public class LimitedWorldEdit extends JavaPlugin {

    
    static final Logger log = Logger.getLogger("Minecraft");
    public double delay; //in secounds
    WorldGaurdAPI worldGaurdAPI = null;
    public static API api;

    public static WorldEditPlugin worldEdit;

    @Override
    public void onEnable() {
        saveConfig();
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin != null) {
            api = new WorldGaurdAPI(this);
        }

        WorldEdit.getInstance().getEventBus().register(new LimitedWorldEditListener(this));

        String version = Bukkit.getServer().getPluginManager().getPlugin(this.getName()).getDescription().getVersion();
        log.log(Level.INFO, this.getName() + ":Version {0} enabled", version);
    }

    @Override
    public void onDisable() {
        log.info("LimitedWorldEdit: disabled");
    }

}
