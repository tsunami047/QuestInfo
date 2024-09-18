package com.killercraft.jimy.questinfo.database;

import com.killercraft.jimy.questinfo.QuestInfo;
import com.killercraft.jimy.questinfo.QuestManager;
import com.killercraft.jimy.questinfo.database.entity.QuestItem;
import com.killercraft.jimy.questinfo.database.entity.QuestPlayerData;
import com.killercraft.jimy.questinfo.manager.QuestTask;
import io.aoitori043.aoitoriproject.CanaryClientImpl;
import io.aoitori043.aoitoriproject.database.orm.SQLClient;
import io.aoitori043.aoitoriproject.script.AoitoriPlayerJoinEvent;
import io.aoitori043.aoitoriproject.script.AoitoriPlayerQuitEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.killercraft.jimy.questinfo.QuestManager.navigationQuests;

/**
 * @Author: natsumi
 * @CreateTime: 2024-08-28  10:51
 * @Description: ?
 */
public class DatabaseClient {

    public static SQLClient sqlClient = CanaryClientImpl.sqlClient;

    public static void init() {
        sqlClient.bindEntity(QuestPlayerData.class);
        sqlClient.bindEntity(QuestItem.class);
        Bukkit.getPluginManager().registerEvents(new DatabaseListener(), QuestInfo.plugin);
    }

    public static ConcurrentHashMap<String,QuestPlayerData> questCacheMap = new ConcurrentHashMap<>();

    public static QuestPlayerData getPlayerQuestData(String playerName) {
        return questCacheMap.computeIfAbsent(playerName, k -> {
            List<QuestPlayerData> query = sqlClient.query(QuestPlayerData.class, entity -> {
                entity.setPlayerName(playerName);
            });
            if (query == null || query.isEmpty()) {
                QuestPlayerData questPlayerData = new QuestPlayerData();
                questPlayerData.setPlayerName(playerName);
                questPlayerData = sqlClient.insert(questPlayerData);
                return questPlayerData;
            } else {
                return query.get(0);
            }
        });
    }

    public static Set<QuestTask> getQuestTaskList(String playerName) {
        QuestPlayerData playerQuestData = getPlayerQuestData(playerName);
        Set<QuestTask> result = new LinkedHashSet<>();
        List<String> list = playerQuestData.getQuests().values().stream().map(k -> k.getQuestName()).collect(Collectors.toList());
        for (String s : list) {
            if(QuestManager.quests.containsKey(s)){
                result.add(QuestManager.quests.get(s).clone());
            }
        }
        return result;
    }

    public static Set<QuestTask> addQuestTask(Player player,String questId){
        QuestPlayerData playerQuestData = getPlayerQuestData(player.getName());
        playerQuestData.insertQuest(questId);
        Set<QuestTask> questTasks = navigationQuests.get(player.getUniqueId());
        questTasks.add(QuestManager.quests.get(questId).clone());
        return questTasks;
    }

    public static Set<QuestTask> removeQuestTask(Player player,String questId){
        QuestPlayerData playerQuestData = getPlayerQuestData(player.getName());
        playerQuestData.removeQuest(questId);
        Set<QuestTask> questTasks = navigationQuests.get(player.getUniqueId());
        questTasks.remove(QuestManager.quests.get(questId).clone());
        return questTasks;
    }

    public static class DatabaseListener implements Listener {

        @EventHandler
        public void onAoitoriPlayerJoin(AoitoriPlayerJoinEvent event){
            Player player = event.getPlayer();
            Set<QuestTask> questTaskList = DatabaseClient.getQuestTaskList(player.getName());
            navigationQuests.put(player.getUniqueId(), questTaskList);
        }
        @EventHandler
        public void onAoitoriPlayerQuit(AoitoriPlayerQuitEvent event) {
            questCacheMap.remove(event.getPlayer().getName());
            navigationQuests.remove(event.getPlayer().getUniqueId());
        }
    }
}
