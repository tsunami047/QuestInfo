package com.killercraft.jimy.questinfo.manager;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class PostInfo {
    private final HashMap<String,String> postStation;

    public PostInfo(HashMap<String,String> postStation){
        this.postStation = postStation;
    }

    public Location getNavigation(Player player, Location to, boolean message){
        World world = player.getWorld();
        String worldName = world.getName();
        if(to != null && to.getWorld() != null){
            String toWorldName = to.getWorld().getName();
            if(!worldName.equals(toWorldName)){
                if(postStation.containsKey(toWorldName)){
                    String info = postStation.get(toWorldName);
                    if(info.startsWith("loc:")){
                        String[] s = info.substring(4).split(",");
                        double x = Double.parseDouble(s[0]);
                        double y = Double.parseDouble(s[1]);
                        double z = Double.parseDouble(s[2]);
                        return new Location(world,x,y,z);
                    }else if(info.startsWith("msg:")){
                        if(message) {
                            String msg = info.substring(4);
                            player.sendMessage(msg);
                        }
                    }
                }
            }
        }
        return to;
    }
}
