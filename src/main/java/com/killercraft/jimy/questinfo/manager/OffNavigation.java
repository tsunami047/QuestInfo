package com.killercraft.jimy.questinfo.manager;

import lombok.Getter;
import lombok.Setter;

public class OffNavigation {
    @Getter
    @Setter
    private String index;
    @Getter
    @Setter
    private String navType;
    @Getter
    @Setter
    private String type;
    @Getter
    @Setter
    private String dungeon;

    public OffNavigation(String index,String navType,String type){
        this.index = index;
        this.navType = navType;
        this.type = type;
    }
}
