package com.killercraft.jimy.questinfo.manager;

import com.killercraft.jimy.questinfo.util.ConfigUtil;
import com.killercraft.jimy.questinfo.util.GermUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.killercraft.jimy.questinfo.QuestManager.navigationQuests;
import static com.killercraft.jimy.questinfo.QuestManager.stationNavigating;
public class Station {
    private World world;
    private String stationName;
    private Location stationLoc;
    private List<String> postInfo = new ArrayList<>();

    public Station(World world,String stationName,Location stationLoc,List<String> postInfo){
        this.world = world;
        this.stationName = stationName;
        this.stationLoc = stationLoc;
        this.postInfo = postInfo;
    }

    public void checkNavePlayer(Player player){
        Location loc = player.getLocation();
        ConfigUtil.debug("玩家引导中存在玩家 "+player.getName());
        List<Station> stationNavi = stationNavigating.getOrDefault(player,new ArrayList<>());

        HashSet<String> addStationNavList = new HashSet<>();
        for(QuestTask qt:navigationQuests.getOrDefault(player.getUniqueId(),new HashSet<>())){
            String id = qt.getQuestId();
            if(postInfo.contains(id)){
                if(stationLoc.distance(loc) <= 80){
                    addStationNavList.add(id);
                    ConfigUtil.debug("添加一位id为 "+id);
                }
            }
        }

        boolean contains = false;
        for(Station st:stationNavi){
            if(st.getStationName().equals(this.stationName)){
                contains = true;
                ConfigUtil.debug("存在名为  "+stationName+" 的驿站导航");
                break;
            }
        }

        if(contains){
            boolean remove = true;
            for (String add : addStationNavList) {
                if (postInfo.contains(add)) {
                    remove = false;
                    break;
                }
            }
            if (remove) {
                //clear nav effect
                ConfigUtil.debug("对  "+stationName+" 的驿站导航执行移除");
                stationNavi.removeIf(st -> st.getStationName().equals(stationName));
                GermUtil.removeStationNavigation(player, stationName);
            }
        }else{

            boolean nav = false;
            for (String add : addStationNavList) {
                if (postInfo.contains(add)) {
                    nav = true;
                    break;
                }
            }
            if(nav){
                //send nav effect
                ConfigUtil.debug("对  "+stationName+" 的驿站导航执行添加");
                if(loc.distance(stationLoc) >= 5) {
                    if(player.getVehicle() == null) {
                        GermUtil.sendStationNavigation(player, stationLoc, stationName);
                        stationNavi.add(this);
                    }
                }
            }
        }

        if(stationNavi.isEmpty()){
            stationNavigating.remove(player);
        }else {
            stationNavigating.put(player, stationNavi);
        }
    }

    public Location getStationLoc() {
        return stationLoc;
    }

    public List<String> getPostInfo() {
        return postInfo;
    }


    public void setStationLoc(Location stationLoc) {
        this.stationLoc = stationLoc;
    }

    public void setPostInfo(List<String> postInfo) {
        this.postInfo = postInfo;
    }


    public void setWorld(World world) {
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    public void addPost(String id){
        this.postInfo.add(id);
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }
}
