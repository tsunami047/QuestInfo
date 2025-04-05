package com.killercraft.jimy.questinfo.manager;

import com.google.common.base.Objects;
import com.killercraft.jimy.questinfo.database.DatabaseClient;
import com.killercraft.jimy.questinfo.util.ConfigUtil;
import com.killercraft.jimy.questinfo.util.GermUtil;
import com.killercraft.jimy.questinfo.util.QuestUtil;
import github.saukiya.sxitem.SXItem;
import github.saukiya.sxitem.data.item.ItemManager;
import io.aoitori043.aoitorimapplugin.config.mapper.OverlayMapper;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.items.MythicItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static com.killercraft.jimy.questinfo.QuestManager.*;

public class   QuestTask implements Cloneable{

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuestTask)) return false;
        QuestTask questTask = (QuestTask) o;
        return Objects.equal(questName, questTask.questName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(questName);
    }
    @Getter
    @Setter
    private String questName;
    @Getter
    @Setter
    private List<String> questInfo;
    @Getter
    @Setter
    private Location naviLoc;
    @Getter
    @Setter
    private HashMap<Location,OffNavigation> offNavMap;
    @Getter
    @Setter
    private String hudquestName;
    @Getter
    @Setter
    private List<String> hudquestinfo;
    @Getter
    @Setter
    private boolean abandon = false;
    @Getter
    @Setter
    private List<String> needInfo = new ArrayList<>();
    @Getter
    @Setter
    private List<String> reward = new ArrayList<>();
    @Getter
    @Setter
    private String questId;
    @Getter
    private final List<ItemStack> rewardItems = new ArrayList<>();

    @Getter
    @Setter
    private String navType = "导航";

    @Getter
    @Setter
    private boolean isAutoNavigation = false;

    @Setter
    private boolean navigating = false;
    @Getter
    @Setter
    private String npcNav;
    @Getter
    @Setter
    private String rewardQuestId;


    public QuestTask(String questId,String questName, List<String> questInfo) {
        this.questId = questId;
        this.questName = questName;
        this.questInfo = questInfo;
    }


    public QuestTask clone() {
        QuestTask qt = null;
        try {
            qt = (QuestTask) super.clone();
            qt.setNeedInfo(new ArrayList<>(needInfo));
            qt.setOffNavMap(new HashMap<>(offNavMap));


        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return qt;
    }

    public void setRewardItems(List<String> rewardItems) {
        for(String itemInfo:rewardItems){
            String[] s = itemInfo.split("<->");
            if(s[0].equalsIgnoreCase("MythicMobs")){
                Optional<MythicItem> maybeItem = MythicMobs.inst().getItemManager().getItem(s[1]);
                if (maybeItem.isPresent()) {
                    MythicItem mi = maybeItem.get();
                    int limit = 1;
                    if(s.length >= 3){
                        limit = Integer.parseInt(s[2]);
                    }
                    ItemStack stack = BukkitAdapter.adapt(mi.generateItemStack(limit));
                    this.rewardItems.add(stack.clone());
                }
            }else if(s[0].equalsIgnoreCase("SxItem")){
                ItemManager im = SXItem.getItemManager();
                if(im.hasItem(s[1])){
                    int limit = 1;
                    if(s.length >= 3){
                        limit = Integer.parseInt(s[2]);
                    }
                    ItemStack stack = im.getItem(s[1],null);
                    ConfigUtil.debug("装备是否存在:"+(stack==null));
                    if(stack != null){
                        stack.setAmount(limit);
                        this.rewardItems.add(stack.clone());
                    }
                }
            }
        }
    }



    //开始导航时立刻朝着目标点进行追踪 并将其记录至 navigationQuests
    //导航结束时 立刻清理导航对应的图标  未实现
    //导航开始时 为玩家添加对应的导航图标  未实现
    //记录在 navigationQuests 中的任务们 在任务界面高亮显示
    //记录在 navigationQeests 中的人物们 在玩家每次进行跨世界传送时立刻进行一次导航追踪
    //主线任务接取后自动进入 navigationQuests 中 可以手动去除 也可以手动将其它任务添加至列表
    //周期五秒判定玩家是否距离导航终点距离5格内 为否时为玩家立刻进行此点位导航
    //任何大于
    public void navigation(Player player){
//        for(QuestTask tempQtn:navigationQuests.getOrDefault(player.getUniqueId(),new ArrayList<>())){
//            if(tempQtn.getQuestId().equals(this.questId)){
//                this.navigating = tempQtn.isNavigating();
//                break;
//            }
//        }
//        ConfigUtil.debug("任务导航状态现在是:"+navigating);
        Set<QuestTask> questTasks = navigationQuests.getOrDefault(player.getUniqueId(),new LinkedHashSet<>());

        if (!navigating){

            boolean contains = false;
            for(QuestTask qt:questTasks){
                if(qt.getQuestId().equals(this.questId)) {
                    contains = true;
                    break;
                }
            }
            ConfigUtil.debug("任务是否在导航:"+contains);
            if(!contains){
//                questTasks.add(this);
//                navigationQuests.put(player.getUniqueId(),questTasks);
                DatabaseClient.addQuestTask(player,this.getQuestId());
                //立刻为玩家引导
                if(naviLoc != null){
                    ConfigUtil.debug("导航坐标存在:"+naviLoc.toString());
                    Location playerLoc = player.getLocation();
//                    System.out.println("坐标:"+this.npcNav);
                    Location dungeonNpcLoc = QuestUtil.getDungeonNpcLoc(this.npcNav, player);
                    if (dungeonNpcLoc != null){
                        GermUtil.clearNavigating(player,this);
                        GermUtil.addNavigating(player,this);
                        GermUtil.sendNavigation(player,dungeonNpcLoc,this);
//                        System.out.println("副本NPC导航坐标:"+dungeonNpcLoc);
                        if(playerLoc.distance(dungeonNpcLoc) < 5){
                            ConfigUtil.debug("距离不足5格 提示玩家过近！");
                            player.sendMessage(langMap.get("PlayerNear"));
                        }
                    } else if(player.getWorld() != naviLoc.getWorld()){
                        String worldName = player.getWorld().getName();
                        ConfigUtil.debug("导航坐标点与玩家坐标不是同一个世界，开始尝试寻找驿站");
                        if(posts.containsKey(worldName)){
                            ConfigUtil.debug("驿站存在");
                            PostInfo pi = posts.get(worldName);
                            Location to = pi.getNavigation(player,naviLoc,true);
                            if(to != null){

                                ConfigUtil.debug("终点存在 开始导航");
                                GermUtil.clearNavigating(player,this);
                                GermUtil.addNavigating(player,this);
                                GermUtil.sendNavigation(player, to, this);

                                if(to.getWorld() != player.getWorld()) {
                                    ConfigUtil.debug("终点存在 但无驿站对应");
                                    player.sendMessage(langMap.get("NotPost"));
                                }
                            }
                        }else{
                            ConfigUtil.debug("驿站不存在！");
                        }
                    }else{

                        ConfigUtil.debug("导航坐标和玩家坐标是同一个世界，开始导航");

                        GermUtil.clearNavigating(player,this);
                        GermUtil.addNavigating(player,this);
                        GermUtil.sendNavigation(player,naviLoc,this);

                        if(playerLoc.distance(naviLoc) < 5){
                            ConfigUtil.debug("距离不足5格 提示玩家过近！");
                            player.sendMessage(langMap.get("PlayerNear"));
                        }
                    }
                }
                navigating = true;
            }else {
                navigating = false;
                DatabaseClient.removeQuestTask(player,this.getQuestId());
//                questTasks.removeIf(qt -> qt.getQuestId().equals(questId));
//                navigationQuests.put(player.getUniqueId(),questTasks);
                GermUtil.clearNavigating(player,this);
            }

        }else{
            DatabaseClient.removeQuestTask(player,this.getQuestId());
//            questTasks.removeIf(qt -> qt.getQuestId().equals(questId));
//            navigationQuests.put(player.getUniqueId(),questTasks);
            GermUtil.clearNavigating(player,this);
            navigating = false;
        }
//        System.out.println("导航状态:"+isNavigating());
        GermUtil.updataquesthud(player);
    }

    public OverlayMapper getOverlayMapper() {
        List<String> tooltip;
        if (this.needInfo != null) {
            tooltip = new ArrayList<>(this.needInfo);
        }else{
            tooltip = new ArrayList<>();
        }
        List<String> strings = QuestUtil.generateMapOverlayAppend(this);
        tooltip.addAll(strings);
        return OverlayMapper.builder()
                .index(questId)
                .enable("true")
                .world(naviLoc.getWorld().getName())
                .z(String.valueOf(naviLoc.getBlockZ()))
                .x(String.valueOf(naviLoc.getBlockX()))
                .width("23")
                .height("23")
                .labelText(this.questName)
                .tooltip(tooltip)
                .path(this.navType)
                .minZoom("0")
                .maxZoom("8")
                .labelScale("1")
                .labelColor("ffffff")
                .labelBackgroundColor("000000")
                .labelOpacity("1")
                .labelBackgroundOpacity("0.5")
                .labelFontShadow("true")
                .labelMinZoom("0")
                .labelMaxZoom("8")
                .labelOffsetX("0")
                .labelOffsetY("16")
                .build();
    }

    public boolean isNavigating() {
        return navigating;
    }
}
