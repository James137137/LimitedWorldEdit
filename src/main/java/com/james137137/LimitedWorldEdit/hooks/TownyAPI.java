/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.james137137.LimitedWorldEdit.hooks;

import com.james137137.LimitedWorldEdit.LimitedWorldEdit;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.PlayerCache;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.palmergames.bukkit.towny.war.flagwar.TownyWar;
import com.palmergames.bukkit.towny.war.flagwar.TownyWarConfig;
import com.sk89q.worldedit.BlockVector;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author James
 */
public class TownyAPI {

    private final Towny towny;

    public TownyAPI(LimitedWorldEdit athis) {
        Plugin plugin = athis.getServer().getPluginManager().getPlugin("Towny");
        towny = (Towny) plugin;
    }

    public boolean CanBuildHere(Player player, BlockVector pos1, World selworld) {
        boolean myResult = true;
        try {

            Location location = new Location(selworld, pos1.getBlockX(), pos1.getBlockY(), pos1.getZ());
            Block block = location.getBlock();
            TownyWorld world = TownyUniverse.getDataSource().getWorld(selworld.getName());
            WorldCoord worldCoord = new WorldCoord(world.getName(), Coord.parseCoord(location));
            boolean bBuild = PlayerCacheUtil.getCachePermission(player, location, 1, (byte) 0, TownyPermission.ActionType.BUILD);
            if (bBuild) {
                return myResult;
            }
            PlayerCache cache = towny.getCache(player);
            PlayerCache.TownBlockStatus status = cache.getStatus();
            if ((status == PlayerCache.TownBlockStatus.ENEMY) && (TownyWarConfig.isAllowingAttacks()) && (Material.STONE == TownyWarConfig.getFlagBaseMaterial())) {
                try {
                    if (TownyWar.callAttackCellEvent(this.towny, player, block, worldCoord)) {
                        return myResult;
                    }
                } catch (TownyException e) {
                    TownyMessaging.sendErrorMsg(player, e.getMessage());
                }
                myResult = false;
            } else {
                if (status == PlayerCache.TownBlockStatus.WARZONE) {
                    if (!TownyWarConfig.isEditableMaterialInWarZone(block.getType())) {
                        myResult = false;
                        TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_warzone_cannot_edit_material"), new Object[]{"build", block.getType().toString().toLowerCase()}));

                    }
                    return myResult;
                }
                myResult = false;
            }

        } catch (NotRegisteredException ex) {
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_not_configured"));
            myResult = false;
        }
        return myResult;
    }

}
