package com.killercraft.jimy.questinfo;

import com.germ.germplugin.api.GermDosAPI;
import com.killercraft.jimy.questinfo.database.DatabaseClient;
import com.killercraft.jimy.questinfo.listener.QuestListener;
import com.killercraft.jimy.questinfo.other.QuestHook;
import com.killercraft.jimy.questinfo.runnable.QuestHudRunnable;
import com.killercraft.jimy.questinfo.runnable.QuestRunnable;
import com.killercraft.jimy.questinfo.util.ConfigUtil;
import com.killercraft.jimy.questinfo.util.GermUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import static com.killercraft.jimy.questinfo.QuestManager.root;


public final class QuestInfo extends JavaPlugin {

    public static QuestInfo plugin;

    @EventHandler
    public void onPluginLoad(){

    }

    @Override
    public void onEnable() {
        plugin = this;
        DatabaseClient.init();
        root = getDataFolder().getAbsolutePath();
        QuestManager.plugin = Bukkit.getPluginManager().getPlugin("QuestInfo");
        saveDefaultConfig();
        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                ConfigUtil.update();
            }
        },40);


        Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if(papi != null){
            boolean isLoadPAPI = new QuestHook().register();
            if(isLoadPAPI){
                System.out.println("[QuestInfo]Placeholder API Loaded!");
            }else{
                System.out.println("[QuestInfo]Placeholder API Unloaded!");
            }
        }

        Bukkit.getPluginManager().registerEvents(new QuestListener(),this);
        Bukkit.getScheduler().runTaskTimer(this,new QuestRunnable(),40,40);
        Bukkit.getScheduler().runTaskTimer(this,new QuestHudRunnable(),40,100);
//        MySQLSupplierManager.registerSupplier(new QuestSupplier());
        GermDosAPI.registerDos("QuestInfo");


    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()){
            return false;
        }
        if(args.length >= 1){
            switch (args[0].toLowerCase()) {
                case "reload":
                    if (sender.isOp()) {
                        ConfigUtil.update();
                        sender.sendMessage("[QuestInfo]Reload Config!");
                    }
                    break;
                case "open":
                    GermUtil.openGui(Bukkit.getPlayer(args[1]));
                    ConfigUtil.debug("send gui");
                    break;
                case "debug":
                    if (sender.isOp()) {
                        QuestManager.debug = !QuestManager.debug;
                        sender.sendMessage("debug = " + QuestManager.debug);
                    }
                    break;
            }
        }else{
            sender.sendMessage("=======[QuestInfo]=======");
            if(sender.isOp()){
                sender.sendMessage("/qinfo reload - 重载配置");
                sender.sendMessage("/qinfo debug - 开启debug模式");
            }
            sender.sendMessage("/qinfo open 玩家名 - 为指定玩家打开任务面板");
            sender.sendMessage("=======[QuestInfo]=======");
        }
        return true;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
