package com.killercraft.jimy.questinfo.manager;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.killercraft.jimy.questinfo.QuestManager.stationMap;

public class StationTemp {
    private World world;
    private Location stationLoc;
    private List<Location> postInfo = new ArrayList<>();
    private HashSet<String> postQuests = new HashSet<>();
    private String stationName;

    public StationTemp(World world,String stationName, Location stationLoc, List<Location> postInfo) {
        this.world = world;
        this.stationLoc = stationLoc;
        this.postInfo = postInfo;
        this.stationName = stationName;
    }

    public void addId(String id){
        this.postQuests.add(id);
    }

    public boolean checkDis(Location loc){
        boolean check = false;
        for(Location location:postInfo){
            if(location.getWorld() != loc.getWorld()) continue;
            if(location.distance(loc) <= 80){
                check = true;
                break;
            }
        }
        return check;
    }

    public void addStation(){
        Station station = new Station(world,stationName,stationLoc,new ArrayList<>(postQuests));
        List<Station> stList = stationMap.getOrDefault(world.getName(),new ArrayList<>());
        stList.add(station);
        stationMap.put(world.getName(),stList);
    }
}
