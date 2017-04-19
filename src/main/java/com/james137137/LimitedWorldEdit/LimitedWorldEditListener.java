/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.james137137.LimitedWorldEdit;

import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.util.eventbus.EventHandler.Priority;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import java.util.HashSet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 *
 * @author James
 */
public class LimitedWorldEditListener implements Listener {

    private LimitedWorldEdit LimitedWorldEdit;

    boolean checkByPass = true;

    LimitedWorldEditListener(LimitedWorldEdit aThis) {
        this.LimitedWorldEdit = aThis;
    }

    @Subscribe(priority = Priority.VERY_EARLY)
    public void onEditSession(EditSessionEvent event) {
        if (event.getActor() == null || !event.getActor().isPlayer()) {
            return;
        }
        Player player = Bukkit.getPlayer(event.getActor().getUniqueId());
        if (checkByPass && player != null && player.hasPermission("LimitedWorldEdit.bypass")) {
            return;
        }
        HashSet<RegionWrapper> mask = WEManager.getMask(player);
        event.setExtent(new WEExtent(mask, event.getExtent()));

    }
}
