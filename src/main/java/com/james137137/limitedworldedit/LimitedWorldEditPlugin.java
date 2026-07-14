package com.james137137.limitedworldedit;

import com.sk89q.worldedit.WorldEdit;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class LimitedWorldEditPlugin extends JavaPlugin {

    private EditSessionListener editSessionListener;
    private PlayerWorldListener playerWorldListener;

    @Override
    public void onEnable() {
        WorldGuardRegionProvider regionProvider = new WorldGuardRegionProvider();
        editSessionListener = new EditSessionListener(regionProvider);
        WorldEdit.getInstance().getEventBus().register(editSessionListener);
        playerWorldListener = new PlayerWorldListener(this, regionProvider);
        getServer().getPluginManager().registerEvents(playerWorldListener, this);
        Bukkit.getOnlinePlayers().forEach(player -> playerWorldListener.refresh(player, true));
        getLogger().info("Enabled WorldEdit restrictions for player-owned WorldGuard regions.");
    }

    @Override
    public void onDisable() {
        if (playerWorldListener != null) {
            Bukkit.getOnlinePlayers().forEach(playerWorldListener::restore);
            playerWorldListener = null;
        }
        if (editSessionListener != null) {
            WorldEdit.getInstance().getEventBus().unregister(editSessionListener);
            editSessionListener = null;
        }
    }
}
