package com.james137137.limitedworldedit;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.world.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

final class PlayerWorldListener implements Listener {

    private final JavaPlugin plugin;
    private final WorldGuardRegionProvider regionProvider;

    PlayerWorldListener(JavaPlugin plugin, WorldGuardRegionProvider regionProvider) {
        this.plugin = plugin;
        this.regionProvider = regionProvider;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        refresh(event.getPlayer(), false);
        plugin.getServer().getScheduler().runTask(plugin, () -> refresh(event.getPlayer(), false));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        refresh(event.getPlayer(), true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChanged(PlayerChangedWorldEvent event) {
        refresh(event.getPlayer(), true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        restore(event.getPlayer());
    }

    void refresh(Player player, boolean forcePlayerWorld) {
        if (!player.isOnline()) {
            return;
        }

        Actor actor = BukkitAdapter.adapt(player);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(actor);
        World currentOverride = session.getWorldOverride();

        if (actor.hasPermission(EditSessionListener.BYPASS_PERMISSION)) {
            restore(session, currentOverride);
            return;
        }

        World target;
        boolean restoreAsOverride;
        if (forcePlayerWorld) {
            target = BukkitAdapter.adapt(player.getWorld());
            restoreAsOverride = false;
        } else if (LimitedWorlds.isLimited(currentOverride)) {
            target = LimitedWorlds.unwrap(currentOverride);
            restoreAsOverride = LimitedWorlds.restoreValue(currentOverride) != null;
        } else if (currentOverride != null) {
            target = currentOverride;
            restoreAsOverride = true;
        } else {
            target = BukkitAdapter.adapt(player.getWorld());
            restoreAsOverride = false;
        }

        OwnedRegionMask mask = regionProvider.forPlayer(target, player.getUniqueId());
        session.setWorldOverride(LimitedWorlds.wrap(target, mask, restoreAsOverride));
    }

    void restore(Player player) {
        Actor actor = BukkitAdapter.adapt(player);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(actor);
        restore(session, session.getWorldOverride());
    }

    private static void restore(LocalSession session, World currentOverride) {
        if (LimitedWorlds.isLimited(currentOverride)) {
            session.setWorldOverride(LimitedWorlds.restoreValue(currentOverride));
        }
    }
}
