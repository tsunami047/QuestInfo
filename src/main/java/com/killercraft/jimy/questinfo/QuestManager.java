package com.killercraft.jimy.questinfo;

import com.killercraft.jimy.questinfo.manager.PostInfo;
import com.killercraft.jimy.questinfo.manager.QuestTask;
import com.killercraft.jimy.questinfo.manager.Station;
import ink.ptms.adyeshach.core.Adyeshach;
import ink.ptms.adyeshach.core.entity.manager.Manager;
import ink.ptms.adyeshach.core.entity.manager.ManagerType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class QuestManager {
    public static Plugin plugin;
    public static String root;
    public static boolean debug = false;
    public static HashMap<String, QuestTask> quests = new HashMap<>();
    public static HashMap<UUID, Set<QuestTask>> navigationQuests = new HashMap<>();
    public static HashMap<Player, List<QuestTask>> navigating = new HashMap<>();
    public static HashMap<Player, List<Station>> stationNavigating = new HashMap<>();
    public static HashMap<String, List<Station>> stationMap = new HashMap<>();
    public static HashMap<String , PostInfo> posts = new HashMap<>();
    public static HashMap<String, String> langMap = new HashMap<>();
    public static Manager npcManager = Adyeshach.INSTANCE.api().getPublicEntityManager(ManagerType.PERSISTENT);
    public static String defaultPath;
    public static String hoverPath;
    public static int hudquestmax;
    public static HashMap<String, String> hudquestsettings = new HashMap<>();
    public static HashMap<String, String> guiquestsettings = new HashMap<>();

    public static ConfigurationSection chemQuestReward;
    public static HashMap<String, List<String>> questtypechecklist = new HashMap<>();
    public static HashMap<String,HashMap<String,String>> questtypesettings = new HashMap<>();

    public static List<String> questNameReplace = new ArrayList<>();
}
