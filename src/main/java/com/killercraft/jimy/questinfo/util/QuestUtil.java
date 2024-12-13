package com.killercraft.jimy.questinfo.util;

import com.germ.germplugin.api.util.ItemUtil;
import com.killercraft.jimy.questinfo.QuestManager;
import com.killercraft.jimy.questinfo.manager.QuestTask;
import ink.ptms.adyeshach.core.entity.EntityInstance;
import ink.ptms.chemdah.api.ChemdahAPI;
import ink.ptms.chemdah.core.PlayerProfile;
import ink.ptms.chemdah.core.quest.Agent;
import ink.ptms.chemdah.core.quest.AgentType;
import ink.ptms.chemdah.core.quest.Quest;
import ink.ptms.chemdah.core.quest.Template;
import io.aoitori043.tintensifyplus.core.TIntensifyAPI;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.killercraft.jimy.questinfo.QuestManager.chemQuestReward;
import static com.killercraft.jimy.questinfo.QuestManager.debug;

public class QuestUtil {
    public static List<Quest> getPlayerQuests(Player player){
        PlayerProfile playerProfile = ChemdahAPI.INSTANCE.getPlayerProfile().get(player.getName());

        if (playerProfile == null){
            return new ArrayList<>();
        }

        //过滤配置中不存在的任务们
        List<Quest> quests = playerProfile.getQuests(false);
        Iterator<Quest> it = quests.iterator();
        while (it.hasNext()){
            Quest quest = it.next();
            String questId = quest.getId();
            if(!QuestManager.quests.containsKey(questId)){
                it.remove();
            }
        }

        return quests;
    }

    public static List<String> getChemQuestRewardText(QuestTask qt){

        String id = qt.getRewardQuestId();
        if (id == null){
            id = qt.getQuestId();
        }

        List<String> list = new ArrayList<>();
        Template questTemplate = ChemdahAPI.INSTANCE.getQuestTemplate(id);
        if (questTemplate != null) {
            for (Agent agent : questTemplate.getAgentList()) {
                if (agent.getType() == AgentType.QUEST_COMPLETE || agent.getType() == AgentType.QUEST_COMPLETED) {
                    for (String s : agent.getAction()) {
                        String[] split = s.split("\n");
                        for (String line : split) {
                            if (line.contains("ll addexp {{sender}}") || line.contains("ll addexp {{ sender }}")){
                                String[] parts = line.split("}}");
                                if (parts.length > 1) {
                                    // 获取分割后的第二部分并去掉前后的空格
                                    String afterBrace = parts[1].trim();
                                    // 按空格分割，提取出第一个数字
                                    String[] subParts = afterBrace.split(" ");
                                    if (subParts.length > 0) {
                                        String number = subParts[0].replace("\"","");
                                        list.add(chemQuestReward.getString("text.经验").replace("%exp%",number));
                                    } else {
                                        System.out.println("任务: "+id +" 奖励出错 读取经验信息时出错");
                                    }
                                } else {
                                    System.out.println("任务: "+id +" 奖励出错 读取经验信息时出错");
                                }
                            }else if (line.contains("te addm {{sender}}") || line.contains("te addm {{ sender }}")){
                                String[] parts = line.split("}}");
                                if (parts.length > 1) {
                                    // 获取分割后的第二部分并去掉前后的空格
                                    String afterBrace = parts[1].trim();
                                    // 按空格分割，提取出第一个数字
                                    String[] subParts = afterBrace.split(" ");
                                    if (subParts.length > 0) {
                                        String number = subParts[0].replace("\"","");
                                        list.add(chemQuestReward.getString("text.绑定金币").replace("%money%",number));
                                    } else {
                                        System.out.println("任务: "+id +" 奖励出错 读取绑定金币信息时出错");
                                    }
                                } else {
                                    System.out.println("任务: "+id +" 奖励出错 读取绑定金币信息时出错");
                                }
                            }else if (line.contains("eco give {{sender}}") || line.contains("eco give {{ sender }}")){
                                String[] parts = line.split("}}");
                                if (parts.length > 1) {
                                    // 获取分割后的第二部分并去掉前后的空格
                                    String afterBrace = parts[1].trim();
                                    // 按空格分割，提取出第一个数字
                                    String[] subParts = afterBrace.split(" ");
                                    if (subParts.length > 0) {
                                        String number = subParts[0].replace("\"","");
                                        list.add(chemQuestReward.getString("text.金币").replace("%money%",number));
                                    } else {
                                        System.out.println("任务: "+id +" 奖励出错 读取金币信息时出错");
                                    }
                                } else {
                                    System.out.println("任务: "+id +" 奖励出错 读取金币信息时出错");
                                }
                            }
                        }
                    }
                }
            }
        }
        return list;
    }
    public static List<ItemStack> getChemQuestRewardItemStack(QuestTask qt){

        String id = qt.getRewardQuestId();
        if (id == null){
            id = qt.getQuestId();
        }

        List<ItemStack> list = new ArrayList<>();
        Template questTemplate = ChemdahAPI.INSTANCE.getQuestTemplate(id);
        if (questTemplate != null) {
            for (Agent agent : questTemplate.getAgentList()) {
                if (agent.getType() == AgentType.QUEST_COMPLETE || agent.getType() == AgentType.QUEST_COMPLETED) {
                    for (String s : agent.getAction()) {
                        String[] split = s.split("\n");
                        for (String line : split) {
                            if (line.contains("ti supergive {{ sender }}") || line.contains("ti supergive {{sender}}")){
                                int endIndex = line.indexOf("}}");
                                if (endIndex != -1) {
                                    // 从 }} 之后的部分开始截取
                                    String substring = line.substring(endIndex + 2).trim();
                                    // 按空格分割字符串
                                    String[] parts = substring.split(" ");
                                    // 提取出美味午餐1级
                                    if (parts.length > 0) {
                                        String itemName = parts[0];
                                        ItemStack allItem = TIntensifyAPI.getAllItem(itemName);
                                        allItem.setAmount(Integer.parseInt(parts[2].replace("\"","")));
                                        if (!ItemUtil.isEmpty(allItem)){
                                            list.add(allItem);
                                        }
                                    } else {
                                        System.out.println("任务: "+id+ "错误ti supergive 无法获取物品");
                                    }
                                } else {
                                    System.out.println("任务: "+id+ "错误ti supergive 无法获取物品");
                                }
                            }
                        }
                    }
                }
            }
        }
        return list;
    }



    public static List<QuestTask> getQuestTasks(Player player,String type){
        List<Quest> quests = getPlayerQuests(player);

        List<QuestTask> allTasks = new ArrayList<>();

        if (type.equalsIgnoreCase("all")){
            for (Quest quest : quests) {
                String questId = quest.getId();
                if (QuestManager.quests.containsKey(questId)) {
                    QuestTask qt = QuestManager.quests.get(quest.getId()).clone();
                    allTasks.add(qt);
                }
            }
        }else if(type.equalsIgnoreCase("other")){
            for (Quest quest : quests) {
                String questId = quest.getId();
                if (QuestManager.quests.containsKey(questId)) {
                    QuestTask qt = QuestManager.quests.get(quest.getId()).clone();
                    if (!qt.getQuestName().contains("支线") && !qt.getQuestName().contains("主线")) {
                        allTasks.add(qt);
                    }
                }
            }
        }else {
            for (Quest quest : quests) {
                String questId = quest.getId();
                if (QuestManager.quests.containsKey(questId)) {
                    QuestTask qt = QuestManager.quests.get(quest.getId()).clone();
                    if (qt.getQuestName().contains(type)) {
                        allTasks.add(qt);
                    }
                }
            }
        }
        return allTasks;
    }

    public static List<QuestTask> getQuestTasks(Player player){
        List<Quest> quests = getPlayerQuests(player);

        //为任务优先级排序后 return
        List<QuestTask> mainTasks = new ArrayList<>();
        List<QuestTask> offTasks = new ArrayList<>();
        List<QuestTask> otherTasks = new ArrayList<>();
        for(Quest quest:quests){
            String questId = quest.getId();
            if(QuestManager.quests.containsKey(questId)){
                QuestTask qt = QuestManager.quests.get(quest.getId()).clone();
                if(qt.getQuestName().contains("主线")){
                    mainTasks.add(qt);
                }else if(qt.getQuestName().contains("支线")){
                    offTasks.add(qt);
                }else{
                    otherTasks.add(qt);
                }
            }
        }
        List<QuestTask> allTasks = new ArrayList<>();
        allTasks.addAll(otherTasks);
        allTasks.addAll(mainTasks);
        allTasks.addAll(offTasks);
        return allTasks;
    }


    //不排序的List
    public static List<QuestTask> getQuestDefaultTasks(Player player){
        List<Quest> quests = getPlayerQuests(player);

        List<QuestTask> tasks = new ArrayList<>();
        for(Quest quest:quests){
            String questId = quest.getId();
            if(QuestManager.quests.containsKey(questId)){
                tasks.add(QuestManager.quests.get(quest.getId()).clone());
            }
        }
        return tasks;
    }

    public static QuestTask atNameGetTask(Player player,String questName){
        for(QuestTask qt:getQuestDefaultTasks(player)){
            if(qt.getQuestName().equals(questName)){
                return qt.clone();
            }
        }
        return null;
    }

    public static Location getNpcLoc(String npcName){
        ConfigUtil.debug("NPC导航坐标获取开始,NPC名称:"+npcName);
        Location location = null;
        try {
            List<EntityInstance> ei = QuestManager.npcManager.getEntityById(npcName);
            ConfigUtil.debug("查询到的NPC名称的NPC们有 "+ei.size()+" 个");
            if(ei.size() >= 1) {
                location = ei.get(0).getLocation();
            }
        }catch (Throwable e){
            ConfigUtil.debug("获取坐标报错");
            if(debug){
                e.printStackTrace();
            }
            return null;
        }

        ConfigUtil.debug("坐标存在，开始反馈");
        return location;
    }

    //获取当前NPC有多少个
    public static Location getDungeonNpcLoc(String npcName,Player player){
        List<EntityInstance> ei = QuestManager.npcTempManager.getEntityById(npcName);
        if (ei.isEmpty()){
//            System.out.println("未找到副本NPC坐标");
            return null;
        }
        for (EntityInstance entityInstance : ei) {
//            System.out.println(entityInstance.getLocation());
            Location entityInstanceLocation = entityInstance.getLocation();
            if (entityInstanceLocation.getWorld().getName().equals(player.getWorld().getName())) {
//                System.out.println("找到NPC坐标 "+entityInstanceLocation.getWorld().getName());
                return entityInstanceLocation;
            }
        }
        return null;
    }






}
