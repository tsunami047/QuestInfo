package com.killercraft.jimy.questinfo.util;

import com.germ.germplugin.api.GermPacketAPI;
import com.germ.germplugin.api.dynamic.gui.*;
import com.killercraft.jimy.questinfo.database.DatabaseClient;
import com.killercraft.jimy.questinfo.database.entity.QuestItem;
import com.killercraft.jimy.questinfo.database.entity.QuestPlayerData;
import com.killercraft.jimy.questinfo.manager.OffNavigation;
import com.killercraft.jimy.questinfo.manager.QuestTask;
import io.aoitori043.aoitoriproject.database.orm.cache.EmbeddedHashMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import static com.killercraft.jimy.questinfo.QuestManager.*;

public class GermUtil {
    public static void openGui(Player player){
        YamlConfiguration yc = YamlConfiguration.loadConfiguration(new File(root, "任务面板Gui.yml"));
        ConfigUtil.debug("文件存在");
        GermGuiScreen ggs = GermGuiScreen.getGermGuiScreen("任务面板", yc);
        ConfigUtil.debug("界面存在");

        GermGuiScroll gScroll = (GermGuiScroll) ggs.getGuiPart("任务列表_scroll");
        GermGuiButton questButton = (GermGuiButton) ggs.getGuiPart("任务模板");
        if(questButton != null){
            ConfigUtil.debug("按钮存在");
            gScroll.removeGuiPart(questButton);
            List<QuestTask> questTasks = QuestUtil.getQuestTasks(player);

            List<String> naviQuestIds = new ArrayList<>();
            for(QuestTask qt:navigationQuests.getOrDefault(player.getUniqueId(),new LinkedHashSet<>())){
                naviQuestIds.add(qt.getQuestId());
            }

            for(QuestTask questTask:questTasks){
                GermGuiButton questButtonClone = questButton.clone();
                questButtonClone.setEnable(true);
                questButtonClone.setEnable("true");
                questButtonClone.setText(questTask.getQuestName());
                questButtonClone.setIndexName("quest_button_"+questTask.getQuestId());
                gScroll.addGuiPart(questButtonClone);
                if(naviQuestIds.contains(questTask.getQuestId())){
                    questButtonClone.setDefaultPath(questButton.getHoverPath());
                }
                ConfigUtil.debug("添加组件");
            }

            //为刚打开的界面默认设定第一个任务的详细信息
            if(!questTasks.isEmpty()){
                QuestTask mainQuest = questTasks.get(0);

                GermGuiLabel questNameLabel = (GermGuiLabel) ggs.getGuiPart("任务名_name");
                questNameLabel.setText(mainQuest.getQuestName());
                ConfigUtil.debug("任务名已设定");

                GermGuiScroll aScroll = (GermGuiScroll) ggs.getGuiPart("任务内容_scroll");
                GermGuiLabel ggl = (GermGuiLabel) aScroll.getGuiPart("任务内容");
                if(ggl != null) {
                    ConfigUtil.debug("任务内容存在 开始设定内容");
                    List<String> allInfo = new ArrayList<>();
//                    List<String> questInfo = mainQuest.getQuestInfo();
//                    if(!questInfo.isEmpty()){
//                        allInfo.add(langMap.get("QuestInfoTitle"));
//                        allInfo.addAll(questInfo);
//                    }

                    List<String> questNeed = mainQuest.getNeedInfo();
                    if(!questNeed.isEmpty()){
                        allInfo.add(langMap.get("QuestNeedTitle"));
                        questNeed.replaceAll(s -> s.replace("NULL","0"));
                        allInfo.addAll(questNeed);
                    }

                    List<String> questReward = mainQuest.getReward();
                    List<String> chemQuestRewardText = QuestUtil.getChemQuestRewardText(mainQuest.getQuestId());
                    allInfo.add(langMap.get("QuestRewardTitle"));
                    if (questReward.isEmpty() && chemQuestRewardText.isEmpty()){
                        allInfo.add(langMap.get("NoReward"));
                    }else{
                        allInfo.addAll(questReward);
                        allInfo.addAll(chemQuestRewardText);
                    };

                    ggl.setTexts(allInfo);
                }
                List<ItemStack> questItems = mainQuest.getRewardItems();
                int itemcount = 0;
                if(!questItems.isEmpty()){
                    ConfigUtil.debug("任务奖励渲染存在 开始设定物品");
                    for(int i = 1;i<=6;i++){
                        GermGuiSlot gSlot = (GermGuiSlot) ggs.getGuiPart("奖励展示"+i);
                        if(i > questItems.size()){
                            gSlot.setItemStack(new ItemStack(Material.AIR));
                            continue;
                        }
                        gSlot.setItemStack(questItems.get(i-1));
                        itemcount ++;
                    }
                }
                if (itemcount < 6){
                    List<ItemStack> chemQuestRewardItemStack = QuestUtil.getChemQuestRewardItemStack(mainQuest.getQuestId());
                    if (!chemQuestRewardItemStack.isEmpty()){
                        for (ItemStack itemStack : chemQuestRewardItemStack) {
                            if (itemcount < 6) {
                                GermGuiSlot gSlot = (GermGuiSlot) ggs.getGuiPart("奖励展示" + (itemcount + 1));
                                gSlot.setItemStack(itemStack);
                                itemcount++;
                            }
                        }
                    }
                }
                GermGuiButton button = ggs.getGuiPart("任务导航_button", GermGuiButton.class);
                if(mainQuest.isNavigating()){
                    ConfigUtil.debug("任务已开始导航");
                    button.setDefaultPath(guiquestsettings.get("navigation-off-path1"));
                }else{
                    ConfigUtil.debug("任务已关闭导航");
                    button.setDefaultPath(guiquestsettings.get("navigation-on-path1"));
                }
                GermGuiButton failQuestButton = (GermGuiButton) ggs.getGuiPart("放弃任务_button");
                failQuestButton.setEnable(mainQuest.isAbandon());
            }
        }
        ggs.openGui(player);

    }

    public static void sendNavigation(Player player, Location location,QuestTask qt){
        if(player.getLocation().getWorld() == location.getWorld()) GermPacketAPI.sendNavigate(player,qt.getNavType(),qt.getQuestName(),location.getX(),location.getY(),location.getZ());
        ConfigUtil.debug("开始执行附属导航");
        HashMap<Location, OffNavigation> offNavMap = qt.getOffNavMap();
        for(Location loc:offNavMap.keySet()){
            OffNavigation of = offNavMap.get(loc);
            ConfigUtil.debug("附属导航开始 导航名为"+of.getIndex());
            if(of.getType().equals("dungeon")){
                World world = player.getWorld();
                String worldName = world.getName();
                ConfigUtil.debug("当前玩家所在世界"+worldName);
                ConfigUtil.debug("当前玩家应该导航的世界"+of.getDungeon());
                if(worldName.contains("dungeon") && worldName.contains(of.getDungeon())){
                    Location dungeonLoc = new Location(world,loc.getX(), loc.getY(), loc.getZ());
                    if(player.getLocation().getWorld() == dungeonLoc.getWorld()) GermPacketAPI.sendNavigate(player, of.getNavType(), of.getIndex(), dungeonLoc.getX(),dungeonLoc.getY(),dungeonLoc.getZ());
                }
            }else {
                if(player.getLocation().getWorld() == loc.getWorld()) GermPacketAPI.sendNavigate(player, of.getNavType(), of.getIndex(), loc.getX(), loc.getY(), loc.getZ());
            }
        }
    }

    public static void removeNavigation(Player player,QuestTask qt){
        GermPacketAPI.removeEffect(player,qt.getQuestName());
        for(OffNavigation of:qt.getOffNavMap().values()){
            GermPacketAPI.removeEffect(player,of.getIndex());
        }
    }



    public static void sendStationNavigation(Player player, Location location ,String naviName){
        GermPacketAPI.sendNavigate(player,"摆渡鸟导航",naviName,location.getX(),location.getY(),location.getZ());
    }

    public static void removeStationNavigation(Player player,String naviName){
        GermPacketAPI.removeEffect(player,naviName);
    }

    public static boolean checkNavigating(Player player,QuestTask qt){
        List<QuestTask> qtList = navigating.getOrDefault(player,new ArrayList<>());
        for(QuestTask tqt:qtList){
            if(tqt.getQuestId().equals(qt.getQuestId())) return false;
        }
        return true;
    }

    public static void addNavigating(Player player,QuestTask qt){
        List<QuestTask> qtList = navigating.getOrDefault(player,new ArrayList<>());
        qtList.add(qt);
        navigating.put(player,qtList);
    }

    public static  void addFaceToNavigation(Player player,QuestTask qt){
        Location naviLoc = qt.getNaviLoc();
        if(naviLoc == null){
            return;
        }
        GermPacketAPI.sendNavigate(player,"任务转动视角导航","任务转动视角导航",naviLoc.getX(),naviLoc.getY(),naviLoc.getZ());
    }

    public static void clearNavigating(Player player,QuestTask qt){
        List<QuestTask> qtList = navigating.getOrDefault(player,new ArrayList<>());
        qtList.removeIf(tqt -> tqt.getQuestId().equals(qt.getQuestId()));
        if(qtList.isEmpty()){
            navigating.remove(player);
        }else{
            navigating.put(player,qtList);
        }
        GermUtil.removeNavigation(player,qt);
    }

    public static void updataquesthud(Player player){
        List<QuestTask> qtList = QuestUtil.getQuestTasks(player);
        QuestPlayerData playerQuestData = DatabaseClient.getPlayerQuestData(player.getName());
        EmbeddedHashMap<String, QuestItem> quests1 = playerQuestData.getQuests();

        HashMap<String,String> map = new HashMap<>();
        map.put("quest_hud_size", String.valueOf(qtList.size()));
        for (int i = 0; i < hudquestmax; i++) {
            if (i >= qtList.size()) {
                break;
            }
            QuestTask questTask = qtList.get(i);


            map.put("quest_hud_idname_" + i, questTask.getQuestName());

            if (questTask.getHudquestName().equals("null")) {
                String questName = questTask.getQuestName();
                map.put("quest_hud_name_" + i, questTask.getQuestName());
            }else {
                map.put("quest_hud_name_" + i, questTask.getHudquestName());
            }
            String name = map.get("quest_hud_name_" + i);
            for (String key : questtypechecklist.keySet()) {
                List<String> list = questtypechecklist.get(key);
                for (String s : list) {
                    if (name.contains(s)){
                        map.put("quest_hud_typepath_" + i, questtypesettings.get(key).get("path"));
                        map.put("quest_hud_typetip_" + i, questtypesettings.get(key).get("typetooltip"));
                        map.put("quest_hud_typebgpath_" + i, questtypesettings.get(key).get("bgpath"));
                        break;
                    }
                }
            }

            for (String key : questNameReplace) {
                name = name.replace(key, "");
            }
            map.put("quest_hud_name_" + i, name);

            if (questTask.getHudquestinfo().isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder();
                for (String s : questTask.getNeedInfo()) {
                    stringBuilder.append(s).append("\n");
                }
                map.put("quest_hud_lore_" + i, String.valueOf(stringBuilder));
            }else {
                StringBuilder stringBuilder = new StringBuilder();
                for (String s : questTask.getHudquestinfo()) {
                    stringBuilder.append(s).append("\n");
                }
                map.put("quest_hud_lore_" + i, stringBuilder.toString());
            }
            for(QuestTask qta:navigationQuests.getOrDefault(player.getUniqueId(),new LinkedHashSet<>())){
                if(qta.getQuestId().equals(questTask.getQuestId())){
                    questTask = qta;
                }
            }
            if (questTask.getNaviLoc() == null){
                map.put("quest_hud_navigatingenable_" + i,"false");
            }else{
                map.put("quest_hud_navigatingenable_" + i,"true");
                if (quests1.containsKey(questTask.getQuestId())) {
                    map.put("quest_hud_navigatingpath_" + i, hudquestsettings.get("navigation-on-path1"));
                }else {
                    map.put("quest_hud_navigatingpath_" + i, hudquestsettings.get("navigation-off-path1"));
                }
            }
        }
        for (String s : map.keySet()) {
            GermPacketAPI.sendPlaceholder(player, s,map.get(s));
        }
    }
    public static void updatanavhud(Player player){
        List<QuestTask> qtList = QuestUtil.getQuestTasks(player);
        HashMap<String,String> map = new HashMap<>();
        for (int i = 0; i < hudquestmax; i++) {
            if (i >= qtList.size()) {
                break;
            }
            QuestTask questTask = qtList.get(i);
            for(QuestTask qta:navigationQuests.getOrDefault(player.getUniqueId(),new LinkedHashSet<>())){
                if(qta.getQuestId().equals(questTask.getQuestId())){
                    questTask = qta;
                }
            }
            System.out.println(questTask.isNavigating());
            if (questTask.isNavigating()) {
                map.put("quest_hud_navigatingpath_" + i, hudquestsettings.get("navigation-on-path1"));
            }else {
                map.put("quest_hud_navigatingpath_" + i, hudquestsettings.get("navigation-off-path1"));
            }
        }
        for (String s : map.keySet()) {
            GermPacketAPI.sendPlaceholder(player, s,map.get(s));
        }
    }

}
