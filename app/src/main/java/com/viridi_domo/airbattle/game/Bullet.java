package com.viridi_domo.airbattle.game;

import android.graphics.Bitmap;

public class Bullet extends AutoSprite {

    public Bullet(Bitmap bitmap){
        super(bitmap);
        setSpeed(-10);//负数表示子弹向上飞
    }

}