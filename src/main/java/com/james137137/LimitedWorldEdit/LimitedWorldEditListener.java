/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.james137137.LimitedWorldEdit;

import com.sk89q.minecraft.util.commands.CommandException;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @author James
 */
public class LimitedWorldEditListener implements Listener {

    private List<String> worldEditCommands;
    private List<String> worldGaurdCommands;
    private LimitedWorldEdit LimitedWorldEdit;
    private static Player player;

    LimitedWorldEditListener(LimitedWorldEdit aThis) {
        this.LimitedWorldEdit = aThis;
        worldEditCommands = LimitedWorldEdit.getConfig().getStringList("worldEditCommands");
        worldGaurdCommands = LimitedWorldEdit.getConfig().getStringList("worldGaurdCommands");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        player = event.getPlayer();
        LimitedWorldEdit.SetPlayerID(player);
    }
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) throws CommandException {
         player = event.getPlayer();
        if (player.hasPermission("LimitedWorldEdit.bypass")) {
            return;
        }


        String command = event.getMessage();
        if (command.length() >= 2) {
            //String commandsub = command.substring(0, 2);
            if (isWorldEditCommand(event.getMessage()) || isWorldGaurdCommand(event.getMessage())) {
               
                    if (!(LimitedWorldEdit.CanWorldEdit(player))) {
                        event.setCancelled(true);
                    }

                

            }
        } else {
            return;
        }

    }

    private boolean isWorldEditCommand(String message) {
        String command = "";
        if (message.length() <= 2) {
            return false;
        }
        for (int i = 2; i < message.length(); i++) {
            if (message.charAt(i) == ' ') {
                break;
            } else {
                command += message.charAt(i);
            }

        }
        
        for (int i = 0; i < worldEditCommands.size(); i++) {
            if (command.equalsIgnoreCase(worldEditCommands.get(i))) {
                return true;
            }
        }

        return false;
    }
    
    private boolean isWorldGaurdCommand(String message) {
        String command = "";
        /*if (message.length() <= 2) {
            return false;
        }*/
        int count = 0;
        for (int i = 1; i < message.length(); i++) {
            if (message.charAt(i) == ' ') {
                if (count != 1)
                {
                    command += message.charAt(i);
                    count++;
                }
                else
                {
                  break;  
                }
                
            } else {
                command += message.charAt(i);
            }

        }
        for (int i = 0; i < worldGaurdCommands.size(); i++) {
            if (command.equalsIgnoreCase(worldGaurdCommands.get(i))) {
                return true;
            }
        }

        return false;
    }
}
