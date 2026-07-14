package com.james137137.limitedworldedit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import org.junit.jupiter.api.Test;

class EditSessionListenerTest {

    @Test
    void wrapsPlayerEditsAtBothRecommendedInterceptionStages() {
        assertTrue(EditSessionListener.shouldWrap(true, false, EditSession.Stage.BEFORE_HISTORY));
        assertTrue(EditSessionListener.shouldWrap(true, false, EditSession.Stage.BEFORE_CHANGE));
    }

    @Test
    void ignoresReorderStageNonPlayersAndBypassPermission() {
        assertFalse(EditSessionListener.shouldWrap(true, false, EditSession.Stage.BEFORE_REORDER));
        assertFalse(EditSessionListener.shouldWrap(false, false, EditSession.Stage.BEFORE_HISTORY));
        assertFalse(EditSessionListener.shouldWrap(true, true, EditSession.Stage.BEFORE_HISTORY));
    }

    @Test
    void safelyIgnoresAnEditWithoutAnActor() {
        WorldGuardRegionProvider provider = mock(WorldGuardRegionProvider.class);
        EditSessionEvent event = mock(EditSessionEvent.class);

        new EditSessionListener(provider).onEditSession(event);

        verifyNoInteractions(provider);
        verify(event, never()).setExtent(any());
    }

    @Test
    void safelyIgnoresAPlayerWithBypassPermission() {
        WorldGuardRegionProvider provider = mock(WorldGuardRegionProvider.class);
        EditSessionEvent event = mock(EditSessionEvent.class);
        Actor actor = mock(Actor.class);
        when(event.getActor()).thenReturn(actor);
        when(event.getStage()).thenReturn(EditSession.Stage.BEFORE_HISTORY);
        when(actor.isPlayer()).thenReturn(true);
        when(actor.hasPermission(EditSessionListener.BYPASS_PERMISSION)).thenReturn(true);

        new EditSessionListener(provider).onEditSession(event);

        verifyNoInteractions(provider);
        verify(event, never()).setExtent(any());
    }
}
