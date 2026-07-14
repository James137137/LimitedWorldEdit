package com.james137137.limitedworldedit;

import com.sk89q.worldedit.WorldEdit;
import org.bukkit.plugin.java.JavaPlugin;

public final class LimitedWorldEditPlugin extends JavaPlugin {

    private EditSessionListener editSessionListener;

    @Override
    public void onEnable() {
        editSessionListener = new EditSessionListener(new WorldGuardRegionProvider());
        WorldEdit.getInstance().getEventBus().register(editSessionListener);
        getLogger().info("Enabled WorldEdit restrictions for player-owned WorldGuard regions.");
    }

    @Override
    public void onDisable() {
        if (editSessionListener != null) {
            WorldEdit.getInstance().getEventBus().unregister(editSessionListener);
            editSessionListener = null;
        }
    }
}
