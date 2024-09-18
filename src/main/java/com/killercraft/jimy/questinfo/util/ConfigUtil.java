package com.killercraft.jimy.questinfo.util;

import com.germ.germplugin.api.dynamic.gui.GermGuiButton;
import com.germ.germplugin.api.dynamic.gui.GermGuiScreen;
import com.germ.germplugin.api.dynamic.gui.GermGuiScroll;
import com.killercraft.jimy.questinfo.QuestManager;
import com.killercraft.jimy.questinfo.manager.OffNavigation;
import com.killercraft.jimy.questinfo.manager.PostInfo;
import com.killercraft.jimy.questinfo.manager.QuestTask;
import com.killercraft.jimy.questinfo.manager.StationTemp;
import io.aoitori043.dailyquest.config.ConfigHandler;
import io.aoitori043.dailyquest.config.mapper.ChemdahQuestMapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.killercraft.jimy.questinfo.QuestManager.*;

public class  ConfigUtil {

    public static void update(){

        File configFile = new File(root, "config.yml");
        FileConfiguration config = load(configFile);
//        plugin.saveResource("任务面板HUD.yml", false);


        langMap = new HashMap<>();
        ConfigurationSection langSec = config.getConfigurationSection("Lang");
        for(String temp:langSec.getKeys(false)){
            langMap.put(temp,langSec.getString(temp).replace('&', ChatColor.COLOR_CHAR));
        }
        hudquestmax = config.getInt("hud-quest-max");
        for (String key : config.getConfigurationSection("hud-settings").getKeys(false)) {
            hudquestsettings.put(key,config.getString("hud-settings."+key));
        }
        for (String key : config.getConfigurationSection("gui-settings").getKeys(false)) {
            guiquestsettings.put(key,config.getString("gui-settings."+key));
        }
        for (String key : config.getConfigurationSection("quest-type").getKeys(false)) {
            questtypechecklist.put(key,config.getStringList("quest-type."+key+".list"));
            HashMap<String, String> stringStringHashMap = new HashMap<>();
            stringStringHashMap.put("path",config.getString("quest-type."+key+".path"));
            stringStringHashMap.put("bgpath",config.getString("quest-type."+key+".bgpath"));
            stringStringHashMap.put("typetooltip",config.getString("quest-type."+key+".typetooltip"));
            questtypesettings.put(key,stringStringHashMap);

        }
        chemQuestReward = config.getConfigurationSection("chem-reward");

        posts = new HashMap<>();
        ConfigurationSection postSec = config.getConfigurationSection("Post");
        for(String temp:postSec.getKeys(false)){
            HashMap<String,String> map = new HashMap<>();
            ConfigurationSection sec2 = postSec.getConfigurationSection(temp);
            for(String temp2:sec2.getKeys(false)){
                map.put(temp2,sec2.getString(temp2).replace('&',ChatColor.COLOR_CHAR));
            }
            PostInfo pi = new PostInfo(map);
            posts.put(temp,pi);
        }

        List<StationTemp> stList = new ArrayList<>();
        ConfigurationSection stationSec = config.getConfigurationSection("Station");
        for(String temp:stationSec.getKeys(false)){
            World world = Bukkit.getWorld(temp);
            ConfigurationSection sec2 = stationSec.getConfigurationSection(temp);
            for(String temp2:sec2.getKeys(false)){
                String[] locInfo = temp2.split(",");
                double x = Double.parseDouble(locInfo[0]);
                double y = Double.parseDouble(locInfo[1]);
                double z = Double.parseDouble(locInfo[2]);
                String stationName = locInfo[3];
                Location loc = new Location(world,x,y,z);
                List<String> targetLoc = sec2.getStringList(temp2);
                List<Location> postInfo = new ArrayList<>();
                for(String target:targetLoc){
                    String[] targetLocInfo = target.split(",");
                    x = Double.parseDouble(targetLocInfo[0]);
                    y = Double.parseDouble(targetLocInfo[1]);
                    z = Double.parseDouble(targetLocInfo[2]);
                    postInfo.add(new Location(world,x,y,z));
                }
                StationTemp st = new StationTemp(world,stationName,loc,postInfo);
                stList.add(st);
            }
        }


        QuestManager.quests = new HashMap<>();

        for (Map.Entry<String, ChemdahQuestMapper> entry : ConfigHandler.chemdahQuests.entrySet()) {
        }

        File file = new File(root+"/quests");

        if(file.isDirectory()){
            for(File f: Objects.requireNonNull(file.listFiles())){
                FileConfiguration questFile = load(f);
                for(String temp:questFile.getKeys(false)){
                    String questName = questFile.getString(temp+".名字").replace('&', ChatColor.COLOR_CHAR);
                    List<String> questInfo = questFile.getStringList(temp+".内容");
                    questInfo.replaceAll(s -> s.replace('&',ChatColor.COLOR_CHAR));
                    String npcNav = questFile.getString(temp+".NPC导航");
                    if(npcNav == null) npcNav = "none";

                    String navType = questFile.getString(temp+".导航类型");
                    if(navType == null) navType = "导航";

                    boolean abandon = questFile.getBoolean(temp+".是否允许放弃",false);
                    List<String> questNeed = questFile.getStringList(temp+".需求");
                    if(questNeed == null) questNeed = new ArrayList<>();
                    questNeed.replaceAll(s -> s.replace('&',ChatColor.COLOR_CHAR));
                    List<String> questReward = questFile.getStringList(temp+".奖励描述");
                    if(questReward == null) questReward = new ArrayList<>();
                    questReward.replaceAll(s -> s.replace('&',ChatColor.COLOR_CHAR));

                    boolean autoNav = questFile.getBoolean(temp+".自动导航",false);

                    List<String> rewardItems = questFile.getStringList(temp+".奖励渲染");
                    if(rewardItems == null) rewardItems = new ArrayList<>();

                    //ID是配置的key
                    QuestTask qt = new QuestTask(temp,questName,questInfo);

                    if (questFile.contains(temp+".HUD名字")){
                        qt.setHudquestName(questFile.getString(temp+".HUD名字"));
                    }else {
                        qt.setHudquestName("null");
                    }
                    if (questFile.contains(temp+".HUD内容")){
                        qt.setHudquestinfo(questFile.getStringList(temp+".HUD内容"));
                    }else {
                        qt.setHudquestinfo(new ArrayList<>());
                    }

                    qt.setAbandon(abandon);
                    qt.setNeedInfo(questNeed);
                    qt.setReward(questReward);
                    qt.setNavType(navType);
                    qt.setRewardItems(rewardItems);
                    qt.setAutoNavigation(autoNav);
                    if(!npcNav.equals("none")){
                        ConfigUtil.debug("NPC坐标不为none，开始获取");
                        if(npcNav.startsWith("loc:")){
                            String[] s = npcNav.substring(4).split(",");
                            World world = Bukkit.getWorld(s[0]);
                            if(world != null) {
                                double x = Double.parseDouble(s[1]);
                                double y = Double.parseDouble(s[2]);
                                double z = Double.parseDouble(s[3]);
                                Location loc = new Location(world,x,y,z);
                                qt.setNaviLoc(loc);
                                for(StationTemp st:stList){
                                    if(st.checkDis(loc)){
                                        st.addId(qt.getQuestId());
                                    }
                                }
                            }else{
                                ConfigUtil.debug("未得到世界，坐标暂定为null");
                                qt.setNaviLoc(null);
                            }
                        }else {
                            Location loc = QuestUtil.getNpcLoc(npcNav);
                            qt.setNaviLoc(loc);
                            if(loc != null) {
                                for (StationTemp st : stList) {
                                    if (st.checkDis(loc)) {
                                        st.addId(qt.getQuestId());
                                    }
                                }
                            }
                        }
                    }else{
                        ConfigUtil.debug("NPC坐标为无。");
                        qt.setNaviLoc(null);
                    }

                    List<String> offNavInfo = questFile.getStringList(temp+".额外导航");
                    qt.setOffNavMap(new HashMap<>());
                    if(offNavInfo != null && !offNavInfo.isEmpty()){
                        HashMap<Location, OffNavigation> offNavMap = new HashMap<>();
                        for(String info:offNavInfo){
                            String[] s = info.split(":");
                            if(s[0].equals("npc")){
                                Location loc = QuestUtil.getNpcLoc(s[1]);
                                if(loc != null){
                                    OffNavigation of = new OffNavigation(s[2],s[3],s[0]);
                                    offNavMap.put(loc,of);
                                }
                            }else if(s[0].equals("loc")){
                                String[] e = s[1].split(",");
                                World world = Bukkit.getWorld(e[0]);
                                if(world != null) {
                                    double x = Double.parseDouble(e[1]);
                                    double y = Double.parseDouble(e[2]);
                                    double z = Double.parseDouble(e[3]);
                                    Location loc = new Location(world,x,y,z);
                                    OffNavigation of = new OffNavigation(s[2],s[3],s[0]);
                                    offNavMap.put(loc,of);
                                }
                            }else if(s[0].equals("dungeon")){
                                String[] e = s[1].split(",");
                                World world = Bukkit.getWorld("world");
                                if(world != null) {
                                    double x = Double.parseDouble(e[1]);
                                    double y = Double.parseDouble(e[2]);
                                    double z = Double.parseDouble(e[3]);
                                    Location loc = new Location(world,x,y,z);
                                    OffNavigation of = new OffNavigation(s[2],s[3],s[0]);
                                    of.setDungeon(s[4]);
                                    offNavMap.put(loc,of);
                                }
                            }
                        }
                        qt.setOffNavMap(offNavMap);
                    }

                    QuestManager.quests.put(temp,qt);
                }
            }
        }

        stationMap = new HashMap<>();
        for(StationTemp st:stList){
            st.addStation();
        }


        YamlConfiguration yc = YamlConfiguration.loadConfiguration(new File(root, "任务面板Gui.yml"));
        GermGuiScreen ggs = GermGuiScreen.getGermGuiScreen("任务面板", yc);
        GermGuiScroll gScroll = (GermGuiScroll) ggs.getGuiPart("任务列表_scroll");
        GermGuiButton questButton = (GermGuiButton) ggs.getGuiPart("任务模板");
        if(questButton != null){
            defaultPath = questButton.getDefaultPath();
            hoverPath = questButton.getHoverPath();
        }
        questNameReplace = config.getStringList("hud-name-replace");
//
//        YamlConfiguration hudyc = YamlConfiguration.loadConfiguration(new File(root, "任务面板HUD.yml"));
//        GermGuiScreen hudscreen = GermGuiScreen.getGermGuiScreen("任务面板HUD", hudyc);
//        GermGuiCanvas mobanPart = hudscreen.getGuiPart("模板", GermGuiCanvas.class);
//        GermGuiScroll scroll = hudscreen.getGuiPart("任务hud列表_scroll", GermGuiScroll.class);
//        for (int i = 0; i < hudquestmax; i++) {
//            GermGuiCanvas clone = mobanPart.clone();
//            clone.setEnable("%quest_hud_size%>"+i);
//            clone.setIndexName("hud_quest_"+i+"_canvas");
//            GermGuiTexture typetexture = clone.getGuiPart("任务类型", GermGuiTexture.class);
//            typetexture.setPath("%quest_hud_typepath_"+i+"%");
//            typetexture.setTooltip(Collections.singletonList("%quest_hud_typetip_" + i + "%"));
//            GermGuiButton germGuiButton = clone.getGuiPart("导航按钮", GermGuiButton.class);
//            germGuiButton.setIndexName("hud_quest_"+i+"_navigation_button");
//            germGuiButton.setDefaultPath("%quest_hud_navigatingpath_"+i+"%");
//            GermGuiLabel guiLabel = clone.getGuiPart("任务名", GermGuiLabel.class);
//            guiLabel.setText("%quest_hud_name_"+i+"%");
//            GermGuiLabel germGuiLabel = clone.getGuiPart("任务内容", GermGuiLabel.class);
//            germGuiLabel.setText("%quest_hud_lore_"+i+"%");
//            GermGuiTexture bgpart = clone.getGuiPart("背景", GermGuiTexture.class);
//            bgpart.setPath("%quest_hud_typebgpath_"+i+"%");
//            scroll.addGuiPart(clone);
//        }
//        GermHudAPI.removeHUD(hudscreen);
//        GermHudAPI.registerHUD(hudscreen);
//        GermHudAPI.sendRegisteredHUDToAllPlayer();
    }




    public static FileConfiguration load(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public static void debug(String msg){
        if(QuestManager.debug){
            System.out.println("[QuestInfoDebug]:"+msg);
        }
    }
}
