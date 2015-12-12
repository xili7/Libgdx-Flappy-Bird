package com.xili7.game;

import com.badlogic.gdx.Game;

import sun.applet.Main;

/**
 * Created by liray on 12/11/2015.
 */
public class MyGdxGame extends Game {
    @Override
    public void create() {
        setScreen(new MainScreen());
    }
}
