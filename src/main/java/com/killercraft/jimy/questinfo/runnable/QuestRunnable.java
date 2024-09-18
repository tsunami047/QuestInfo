package com.killercraft.jimy.questinfo.runnable;

import com.killercraft.jimy.questinfo.manager.PostInfo;
import com.killercraft.jimy.questinfo.manager.QuestTask;
import com.killercraft.jimy.questinfo.manager.Station;
import com.killercraft.jimy.questinfo.util.ConfigUtil;
import com.killercraft.jimy.questinfo.util.GermUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.killercraft.jimy.questinfo.QuestManager.*;

public class QuestRunnable implements Runnable{
    @Override
    public void run() {
        for (UUID uid:navigationQuests.keySet()){
            Player player = Bukkit.getPlayer(uid);
            if(player == null) continue;
            Set<QuestTask> qtList = navigationQuests.get(uid);
            for(QuestTask qt:qtList){
                Location loc = qt.getNaviLoc();
                if(loc != null){
                    Location playerLoc = player.getLocation();
                    String worldName = player.getWorld().getName();
                    if(player.getWorld() != loc.getWorld()){
                        if(posts.containsKey(worldName)){
                            PostInfo pi = posts.get(worldName);
                            Location to = pi.getNavigation(player,loc,false);
                            if(to != null){
                                if(GermUtil.checkNavigating(player, qt)) {
                                    GermUtil.addNavigating(player,qt);
                                    GermUtil.sendNavigation(player, to, qt);
                                }
                            }
                        }else{
                            if(GermUtil.checkNavigating(player, qt)) {
                                GermUtil.addNavigating(player,qt);
                                GermUtil.sendNavigation(player, loc, qt);
                            }
                        }
                    }else{
                        if(playerLoc.distance(loc) > 5){
                            if(GermUtil.checkNavigating(player, qt)) {
                                GermUtil.addNavigating(player,qt);
                                GermUtil.sendNavigation(player, loc, qt);
                            }
                        }

                        if(stationMap.containsKey(worldName)){
                            ConfigUtil.debug("驿站内寻找世界 "+worldName +" 存在");
                            List<Station> stations = stationMap.get(worldName);
                            for(Station st:stations){
                                ConfigUtil.debug("开始检测驿站 "+st.getStationName());
                                st.checkNavePlayer(player);
                            }
                        }
                    }
                }
            }
        }
    }
}
