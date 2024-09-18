package com.killercraft.jimy.questinfo.runnable;

import com.killercraft.jimy.questinfo.util.GermUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class QuestHudRunnable implements Runnable{
    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            GermUtil.updataquesthud(player);
        }
    }
}
