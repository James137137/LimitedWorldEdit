package com.james137137.limitedworldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.eventbus.EventHandler.Priority;
import com.sk89q.worldedit.util.eventbus.Subscribe;

final class EditSessionListener {

    static final String BYPASS_PERMISSION = "limitedworldedit.bypass";

    private final WorldGuardRegionProvider regionProvider;

    EditSessionListener(WorldGuardRegionProvider regionProvider) {
        this.regionProvider = regionProvider;
    }

    @Subscribe(priority = Priority.VERY_EARLY)
    public void onEditSession(EditSessionEvent event) {
        Actor actor = event.getActor();
        if (actor == null || !shouldWrap(actor.isPlayer(), actor.hasPermission(BYPASS_PERMISSION), event.getStage())) {
            return;
        }

        OwnedRegionMask mask = regionProvider.forPlayer(event.getWorld(), actor.getUniqueId());
        event.setExtent(new LimitedExtent(mask, event.getExtent()));
    }

    static boolean shouldWrap(boolean player, boolean bypass, EditSession.Stage stage) {
        return player
                && !bypass
                && (stage == EditSession.Stage.BEFORE_HISTORY || stage == EditSession.Stage.BEFORE_CHANGE);
    }
}
