package com.killercraft.jimy.questinfo.other;

import com.killercraft.jimy.questinfo.manager.QuestTask;
import com.killercraft.jimy.questinfo.util.QuestUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class QuestHook extends PlaceholderExpansion {

    public boolean canRegister() {
        return Bukkit.getPluginManager().getPlugin("QuestInfo") != null;
    }
    public String getAuthor() {
        return "Jimy_Spirits";
    }

    public String getIdentifier() {
        return "qinfo";
    }

    public String getVersion() {
        return "1.0";
    }

    public String onPlaceholderRequest(Player player, String s) {
        if(s.contains("num")){
            return QuestUtil.getQuestDefaultTasks(player).size()+"";
        }else if(s.contains("info")){
            List<QuestTask> qtList = QuestUtil.getQuestTasks(player);
            StringBuilder builder = new StringBuilder();
            for(QuestTask qt:qtList){
                builder.append(qt.getQuestName()).append("\n");
                for(String need:qt.getNeedInfo()){
                    builder.append(need).append("\n");
                }
            }
            return PlaceholderAPI.setPlaceholders(player,builder.toString()).replace("<NULL>","0").replace("NULL","0")
                    .replace("<TIMEOUT>","0").replace("null","0");
        }
        return null;
    }
}
