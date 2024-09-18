package com.killercraft.jimy.questinfo.database.entity;

import io.aoitori043.aoitoriproject.database.orm.sign.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: natsumi
 * @CreateTime: 2024-09-04  19:17
 * @Description: ?
 */
@Data
@NoArgsConstructor
@Cache(cacheType = Cache.CacheType.ONLY_MYSQL)
@Entity(tableName = "qi_quest_item")
public class QuestItem {

    @AggregateRoot
    public Long id;
    @ForeignAggregateRoot(mapEntity = QuestPlayerData.class)
    public Long playerId;

    public String questName;
    public Date insertDate;



    public QuestItem(String questName) {
        this.questName = questName;
        this.insertDate = new Date();
    }
}
