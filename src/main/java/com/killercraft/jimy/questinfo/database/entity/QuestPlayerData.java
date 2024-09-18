package com.killercraft.jimy.questinfo.database.entity;

import io.aoitori043.aoitoriproject.database.orm.cache.EmbeddedHashMap;
import io.aoitori043.aoitoriproject.database.orm.sign.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: natsumi
 * @CreateTime: 2024-09-04  19:16
 * @Description: ?
 */
@Data
@NoArgsConstructor
@Cache(cacheType = Cache.CacheType.ONLY_MYSQL)
@Entity(tableName = "qi_quest_player")
public class QuestPlayerData {

    @AggregateRoot
    public Long id;
    @Key
    @PlayerName
    public String playerName;

    @OneToMany(mapFieldName = "questName", mapEntity = QuestItem.class)
    public EmbeddedHashMap<String, QuestItem> quests;

    public void insertQuest(String questName){
        if(quests.containsKey(questName)) return;
        QuestItem questItem = new QuestItem(questName);
        quests.put(questName, questItem);
    }

    public void removeQuest(String questName){
        quests.remove(questName);
    }

}
