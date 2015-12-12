package com.xili7.game;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MainGameViewport extends Viewport {
    public MainGameViewport(int x, int y, int width, int height) {
        OrthographicCamera camera = new OrthographicCamera();
        setCamera(camera);
        setScreenBounds(x, y, width, height);
        setWorldSize(100, 200);
    }
}
