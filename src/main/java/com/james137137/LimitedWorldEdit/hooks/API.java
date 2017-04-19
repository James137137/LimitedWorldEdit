/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.james137137.LimitedWorldEdit.hooks;

import com.james137137.LimitedWorldEdit.RegionWrapper;
import java.util.List;
import org.bukkit.entity.Player;

/**
 *
 * @author James
 */
public interface API {
    public List<RegionWrapper> getRegions(Player player);
}
