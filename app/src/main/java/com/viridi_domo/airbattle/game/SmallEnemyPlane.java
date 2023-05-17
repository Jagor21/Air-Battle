package com.viridi_domo.airbattle.game;

import android.graphics.Bitmap;

public class SmallEnemyPlane extends EnemyPlane {

    public SmallEnemyPlane(Bitmap bitmap){
        super(bitmap);
        setPower(1);
        setValue(1000);
    }

}