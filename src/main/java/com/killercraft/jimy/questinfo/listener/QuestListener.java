package com.killercraft.jimy.questinfo.listener;

import com.germ.germplugin.api.dynamic.DynamicBase;
import com.germ.germplugin.api.dynamic.gui.*;
import com.germ.germplugin.api.event.GermNavigateFinishEvent;
import com.germ.germplugin.api.event.GermReceiveDosEvent;
import com.germ.germplugin.api.event.GermReloadEvent;
import com.germ.germplugin.api.event.gui.GermGuiButtonEvent;
import com.killercraft.jimy.questinfo.QuestManager;
import com.killercraft.jimy.questinfo.database.DatabaseClient;
import com.killercraft.jimy.questinfo.manager.QuestTask;
import com.killercraft.jimy.questinfo.manager.Station;
import com.killercraft.jimy.questinfo.util.ConfigUtil;
import com.killercraft.jimy.questinfo.util.GermUtil;
import com.killercraft.jimy.questinfo.util.QuestUtil;
import ink.ptms.chemdah.api.ChemdahAPI;
import ink.ptms.chemdah.api.event.collect.QuestEvents;
import ink.ptms.chemdah.core.PlayerProfile;
import ink.ptms.chemdah.core.quest.Quest;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static com.killercraft.jimy.questinfo.QuestManager.*;


public class QuestListener implements Listener {


    @EventHandler
    public void onNavigationComplete(GermNavigateFinishEvent event){
        String index = event.getIndexName();
        Player player = event.getPlayer();
        List<QuestTask> qtList = navigating.getOrDefault(player,new ArrayList<>());
        qtList.removeIf(qt -> qt.getQuestName().equals(index));
        if(qtList.isEmpty()){
            navigating.remove(player);
        }else {
            navigating.put(player, qtList);
        }

        List<Station> stList = stationNavigating.getOrDefault(player,new ArrayList<>());
        stList.removeIf(st -> st.getStationName().equals(index));
        if(!stList.isEmpty()){
            stationNavigating.put(player,stList);
        }else{
            stationNavigating.remove(player);
        }

    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        navigating.remove(player);
        stationNavigating.remove(player);
    }

    @EventHandler
    public void onDeath(PlayerRespawnEvent event){
        Player player = event.getPlayer();
        navigating.remove(player);
        stationNavigating.remove(player);
    }
    @EventHandler
    public void onTeleport(PlayerTeleportEvent event){
        Location from = event.getFrom();
        Location to = event.getTo();
        if(from.getWorld() != to.getWorld()){
            navigating.remove(event.getPlayer());
            stationNavigating.remove(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            GermUtil.updataquesthud(event.getPlayer());
        },40L);
    }

    @EventHandler
    public void onQuestAccept(QuestEvents.Accept.Post event) {

        Quest quest = event.getQuest();
        Player player = event.getPlayerProfile().getPlayer();

        List<QuestTask> questList = QuestUtil.getQuestDefaultTasks(player);
        for(QuestTask qt:questList){
            if(qt.isAutoNavigation()) {
                if (qt.getQuestId().equals(quest.getId())) {
//                    List<QuestTask> qtList = navigationQuests.getOrDefault(player.getUniqueId(),new ArrayList<>());
//                    qt.navigation(player);
//                    qtList.add(qt.clone());
//
//                    navigationQuests.put(player.getUniqueId(), qtList);
                    qt.navigation(player);
                    DatabaseClient.addQuestTask(player,qt.getQuestId());
                    GermUtil.addFaceToNavigation(player, qt);
                    break;
                }
            }
        }
        GermUtil.updataquesthud(player);
    }

    @EventHandler
    public void onQuestComplete(QuestEvents.Complete.Post event) {
        Quest quest = event.getQuest();
        Player player = event.getPlayerProfile().getPlayer();
        Set<QuestTask> qtList = navigationQuests.getOrDefault(player.getUniqueId(),new LinkedHashSet<>());
        Iterator<QuestTask> it = qtList.iterator();
        while (it.hasNext()){
            QuestTask qt = it.next();
            if(qt.getQuestId().equals(quest.getId())){
                GermUtil.removeNavigation(player,qt);
                it.remove();
            }
        }
        DatabaseClient.removeQuestTask(player,quest.getId());
        GermUtil.updataquesthud(player);
    }

    @EventHandler
    public void onQuestFail(QuestEvents.Fail.Post event) {
        Quest quest = event.getQuest();
        Player player = event.getPlayerProfile().getPlayer();
        Set<QuestTask> qtList = navigationQuests.getOrDefault(player.getUniqueId(),new LinkedHashSet<>());
        Iterator<QuestTask> it = qtList.iterator();
        while (it.hasNext()){
            QuestTask qt = it.next();
            if(qt.getQuestId().equals(quest.getId())){
                GermUtil.clearNavigating(player,qt);
                it.remove();
            }
        }
        DatabaseClient.removeQuestTask(player,quest.getId());
        GermUtil.updataquesthud(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onGermReceiveDos(GermReceiveDosEvent event) {
        if (!event.getDosId().equals("QuestInfo")) {
            return;
        }
        Player player = event.getPlayer();
        String[] split = event.getDosContent().split(" ");
        if (split[0].equals("navigation")){
//            List<QuestTask> questTasks = QuestUtil.getQuestTasks(player);
//            QuestTask questTask = questTasks.get(Integer.parseInt(split[1]));

            QuestTask questTask = QuestUtil.atNameGetTask(player,split[1]);

            for(QuestTask qta:navigationQuests.getOrDefault(player.getUniqueId(),new LinkedHashSet<>())){
                if(qta.getQuestId().equals(questTask.getQuestId())){
                    questTask = qta;
                }
            }

            questTask.navigation(player);
            if (questTask.isNavigating()){
                GermUtil.addFaceToNavigation(player, questTask);
            }

            GermUtil.updataquesthud(player);
        }
    }

//    @EventHandler(ignoreCancelled = true)
//    public void onGermGuiButton(GermGuiButtonEvent event) {
//        if (!event.getGermGuiScreen().getGuiName().equals("任务面板HUD")) {
//            return;
//        }
//        GermGuiButton.EventType et = event.getEventType();
//        if(et != GermGuiButton.EventType.LEFT_CLICK){
//            return;
//        }
//        Player player = event.getPlayer();
//        GermGuiButton germGuiButton = event.getGermGuiButton();
//        String[] split = germGuiButton.getIndexName().split("_");
//        String index = split[2];
//        List<QuestTask> questTasks = QuestUtil.getQuestTasks(player);
//        QuestTask questTask = questTasks.get(Integer.parseInt(index));
//
//        for(QuestTask qta:navigationQuests.getOrDefault(player.getUniqueId(),new ArrayList<>())){
//            if(qta.getQuestId().equals(questTask.getQuestId())){
//                questTask = qta;
//            }
//        }
//        questTask.navigation(player);
//    }

    @EventHandler(ignoreCancelled = true)
    public void onGermReload(GermReloadEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                GermUtil.updataquesthud(onlinePlayer);
            }
        },40L);
    }

    @EventHandler
    public void onGermButton(GermGuiButtonEvent event){
        GermGuiButton.EventType et = event.getEventType();
        if(et == GermGuiButton.EventType.LEFT_CLICK){
            Player player = event.getPlayer();
            GermGuiButton ggb = event.getGermGuiButton();
            String index = ggb.getIndexName();
            GermGuiScreen ggs = event.getGermGuiScreen();
            if(index.startsWith("quest_button_")){
                ConfigUtil.debug("点击按钮");
                String questId = index.substring(13);
                ConfigUtil.debug("任务id = "+questId);
                if(QuestManager.quests.containsKey(questId)){
                    ConfigUtil.debug("ID存在 开始获取Task");
                    QuestTask qt = QuestManager.quests.get(questId).clone();

                    GermGuiLabel questNameLabel = (GermGuiLabel) ggs.getGuiPart("任务名_name");
                    questNameLabel.setText(qt.getQuestName());
                    ConfigUtil.debug("任务名已设定");

                    GermGuiScroll gScroll = (GermGuiScroll) ggs.getGuiPart("任务内容_scroll");
                    gScroll.setScrolledV("0");
                    GermGuiLabel ggl = (GermGuiLabel) gScroll.getGuiPart("任务内容");
                    if(ggl != null) {
                        ConfigUtil.debug("任务内容存在 开始设定内容");
                        List<String> allInfo = new ArrayList<>();
//                        List<String> questInfo = qt.getQuestInfo();
//                        if(!questInfo.isEmpty()){
//                            allInfo.add(langMap.get("QuestInfoTitle"));
//                            allInfo.addAll(questInfo);
//                        }

                        List<String> questNeed = qt.getNeedInfo();
                        if(!questNeed.isEmpty()){
                            allInfo.add(langMap.get("QuestNeedTitle"));
                            questNeed.replaceAll(s -> PlaceholderAPI.setPlaceholders(player,s).replace("NULL","0"));
                            allInfo.addAll(questNeed);
                        }

                        List<String> questReward = qt.getReward();
                        List<String> chemQuestRewardText = QuestUtil.getChemQuestRewardText(qt.getQuestId());
                        allInfo.add(langMap.get("QuestRewardTitle"));
                        if (questReward.isEmpty() && chemQuestRewardText.isEmpty()){
                            allInfo.add(langMap.get("NoReward"));
                        }else{
                            allInfo.addAll(questReward);
                            allInfo.addAll(chemQuestRewardText);
                        }
                        ggl.setTexts(allInfo);
                    }
                    List<ItemStack> questItems = qt.getRewardItems();
                    int itemcount = 0;
                    ConfigUtil.debug("任务奖励渲染存在 开始设定物品");
                    for(int i = 1;i<=6;i++) {
                        GermGuiSlot gSlot = (GermGuiSlot) ggs.getGuiPart("奖励展示" + i);
                        if (i > questItems.size()) {
                            gSlot.setItemStack(new ItemStack(Material.AIR));
                            continue;
                        }
                        gSlot.setItemStack(questItems.get(i - 1));
                        itemcount++;
                    }
                    if (itemcount < 6){
                        List<ItemStack> chemQuestRewardItemStack = QuestUtil.getChemQuestRewardItemStack(qt.getQuestId());
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


                    GermGuiButton failQuestButton = (GermGuiButton) ggs.getGuiPart("放弃任务_button");
                    failQuestButton.setEnable(qt.isAbandon());

                }
            }else if(index.equals("任务导航_button")){
                GermGuiLabel questNameLabel = (GermGuiLabel) ggs.getGuiPart("任务名_name");
                List<String> questTexts = questNameLabel.getTexts();
                if(!questTexts.isEmpty()){
                    String questText = questTexts.get(0);
                    QuestTask qt = QuestUtil.atNameGetTask(player,questText);
                    ConfigUtil.debug("任务是否等于null? = " + (qt != null));



                    if(qt != null){

                        for(QuestTask qta:navigationQuests.getOrDefault(player.getUniqueId(),new LinkedHashSet<>())){
                            if(qta.getQuestId().equals(qt.getQuestId())){
                                qt = qta;
                            }
                        }

                        qt.navigation(player);
                        GermGuiScroll questScroll = (GermGuiScroll) ggs.getGuiPart("任务列表_scroll");
                        if(qt.isNavigating()){
                            GermUtil.addFaceToNavigation(player, qt);
                            ConfigUtil.debug("任务已开始导航");
                            ggb.setDefaultPath(guiquestsettings.get("navigation-off-path1"));
                            for(GermGuiPart<? extends DynamicBase> ggp:questScroll.getGuiParts()){
                                if(ggp.getIndexName().equals("quest_button_"+qt.getQuestId())){
                                    GermGuiButton gButton = (GermGuiButton) ggp;
                                    gButton.setDefaultPath(hoverPath);
                                }
                            }
                        }else{
                            ConfigUtil.debug("任务已关闭导航");
                            ggb.setDefaultPath(guiquestsettings.get("navigation-on-path1"));
                            for(GermGuiPart<? extends DynamicBase> ggp:questScroll.getGuiParts()){
                                if(ggp.getIndexName().equals("quest_button_"+qt.getQuestId())){
                                    GermGuiButton gButton = (GermGuiButton) ggp;
                                    gButton.setDefaultPath(defaultPath);

                                }
                            }
                        }
                    }
                }
            }else if(index.equals("放弃任务_button")){
                GermGuiLabel questNameLabel = (GermGuiLabel) ggs.getGuiPart("任务名_name");
                List<String> questTexts = questNameLabel.getTexts();
                if(!questTexts.isEmpty()){
                    String questText = questTexts.get(0);
                    QuestTask qt = QuestUtil.atNameGetTask(player,questText);
                    if(qt != null){
                        if(qt.isAbandon()){
                            PlayerProfile playerProfile = ChemdahAPI.INSTANCE.getPlayerProfile().get(player.getName());
                            String questId = qt.getQuestId();
                            List<Quest> quests = playerProfile.getQuests(false);
                            for(Quest quest:quests){
                                if(quest.getId().equals(questId)){
                                    quest.failQuestFuture();
                                }
                            }
                        }
                    }
                }
            }else if(index.startsWith("筛选_")){
                String type = index.replace("筛选_","");

                GermGuiScroll gScroll = (GermGuiScroll) ggs.getGuiPart("任务列表_scroll");
                GermGuiButton questButton = (GermGuiButton) ggs.getGuiPart("任务模板");
                if(questButton != null){
                    for(GermGuiPart<? extends DynamicBase> part:gScroll.getGuiParts()){
                        gScroll.removeGuiPart(part);
                    }
                    ConfigUtil.debug("已清理所有轴中组件");

                    List<QuestTask> questTasks = QuestUtil.getQuestTasks(player,type);

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
//                            List<String> questInfo = mainQuest.getQuestInfo();
//                            if(!questInfo.isEmpty()){
//                                allInfo.add(langMap.get("QuestInfoTitle"));
//                                allInfo.addAll(questInfo);
//                            }

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

                        GermGuiButton failQuestButton = (GermGuiButton) ggs.getGuiPart("放弃任务_button");
                        failQuestButton.setEnable(mainQuest.isAbandon());
                    }
                }
            }
        }
    }
}
