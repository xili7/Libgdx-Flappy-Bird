package com.xili7.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class MainScreen implements Screen {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Sprite skyBackground;
    private Sprite ground;
    private Sprite startGameButton;
    private Sprite logo;
    private Sprite birdSprites[];
    private Vector2 logoPosition;

    private float groundOffset;
    private int direction = -1;
    private float timer;
    private int counter;

    private final int WORLD_HEIGHT = 200;
    private final int WORLD_WIDTH = 100;

    private Rectangle startGameRect;

    private void checkStartGame() {
        if (null == startGameRect) {
            startGameRect = new Rectangle((int) (0.15 * Gdx.graphics.getWidth()), 4 * Gdx.graphics.getHeight() / 5 - Gdx.graphics.getHeight() / 15, Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 15);
        }

        if (Gdx.input.isTouched() && startGameRect.contains(Gdx.input.getX(), Gdx.input.getY())) {
            startGameButton.setY(0.185f * WORLD_HEIGHT);
            ((Game)Gdx.app.getApplicationListener()).setScreen(new GameScreen());
        } else {
            startGameButton.setY(0.2f * WORLD_HEIGHT);
        }
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera(100, 200);

        skyBackground = new Sprite(new Texture("png/stage_sky.png"));
        //leave 1/10 of the screen in the bottom for the stage ground.
        skyBackground.setBounds(0, 0.15f * WORLD_HEIGHT, WORLD_WIDTH, 0.85f * WORLD_HEIGHT);

        ground = new Sprite(new Texture(("png/stage_ground.png")));

        startGameButton = new Sprite(new Texture("png/start.png"));
        startGameButton.setBounds(0.15f * WORLD_WIDTH, 0.2f * WORLD_HEIGHT, WORLD_WIDTH / 4f, WORLD_HEIGHT / 15f);

        logo = new Sprite(new Texture("png/logo.png"));
        logo.setSize(0.7f * WORLD_WIDTH, 0.1f * WORLD_HEIGHT);
        logoPosition = new Vector2(0.1f * WORLD_WIDTH, 0.65f * WORLD_HEIGHT);

        Texture birdTexture = new Texture(Gdx.files.internal("png/bird.png"));
        birdSprites = new Sprite[3];
        for (int i = 0; i < birdSprites.length; i++) {
            birdSprites[i] = new Sprite(new TextureRegion(birdTexture, 0, (59 + 11) * i, birdTexture.getWidth(), 59));
            birdSprites[i].setSize(0.15f * WORLD_WIDTH, WORLD_HEIGHT / 17f);
        }


    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        checkStartGame();
        timer += Gdx.graphics.getDeltaTime();
        while (0.01 < timer) {
            timer -= 0.01;
            groundOffset -= 0.1;
            if (-1 >= groundOffset) {
                groundOffset = 0;
            }

            counter++;

            logoPosition.y += direction * 0.0005 * WORLD_HEIGHT;
            if (0.63f * WORLD_HEIGHT > logoPosition.y) {
                logoPosition.y = 0.63f * WORLD_HEIGHT;
                direction = 1;
            }

            if (0.65f * WORLD_HEIGHT < logoPosition.y) {
                logoPosition.y = 0.65f * WORLD_HEIGHT;
                direction = -1;
            }
        }

        int birdNumber = (counter / 10) % 3;
        logo.setPosition(logoPosition.x, logoPosition.y);
        birdSprites[birdNumber].setPosition(logoPosition.x + logo.getWidth() + 0.03f * WORLD_WIDTH, logoPosition.y + 0.025f * WORLD_HEIGHT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        skyBackground.draw(batch);
        for (int i = 0; i < 21; i++) {
            batch.draw(ground, (groundOffset + i) * WORLD_WIDTH / 20f, 0, WORLD_WIDTH / 20f, 0.15f * WORLD_HEIGHT);
        }
        startGameButton.draw(batch);
        logo.draw(batch);
        birdSprites[birdNumber].draw(batch);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.position.set(100 / 2f, 200 / 2f, 0);
    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        skyBackground.getTexture().dispose();
        ground.getTexture().dispose();
        startGameButton.getTexture().dispose();
        logo.getTexture().dispose();
        birdSprites[0].getTexture().dispose();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }
}
