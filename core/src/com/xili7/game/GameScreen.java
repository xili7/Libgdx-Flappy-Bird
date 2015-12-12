package com.xili7.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

import sun.applet.Main;

/**
 * Created by liray on 12/11/2015.
 */
public class GameScreen implements Screen {
    private Stage stage;
    private final int WORLD_HEIGHT = 200;
    private final int WORLD_WIDTH = 100;

    private Texture skyTexture;
    private Texture groundTexture;
    private Texture getReadyTexture;
    private Texture tapTexture;
    private Texture birdTexture;
    private Texture pipeHeadTexture1;
    private Texture pipeHeadTexture2;
    private Texture pipeBodyTexture;

    private Group groundGroup;
    private Group notPlaying;

    private boolean notReady;
    private Animation birdAnimation;
    private Image birdActor;
    private float timer;
    private float birdVelocity;
    private float initialY;
    private float idleTime;
    private boolean birdInAction = false;

    private final float pipeSpaceWidth = 4 * WORLD_WIDTH / 6;
    private final float pipeSpaceHeight = WORLD_HEIGHT / 3;
    private Vector2 pipes[];
    private Random random;
    private float pipeTimer;

    private class BirdListener extends ClickListener {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            super.clicked(event, x, y);
            System.out.println("CLicked");
            notPlaying.setVisible(false);
            notReady = false;
            birdActor.clearActions();
            birdActor.setRotation(0);
            birdInAction = true;
            Action riseAction = Actions.parallel(Actions.moveBy(0, 0.15f * WORLD_HEIGHT, 0.25f), Actions.rotateTo(45, 0.25f));
            Action riseThenFall = Actions.sequence(riseAction, Actions.run(new Runnable() {
                public void run() {
                    birdVelocity = 0;
                    initialY = birdActor.getY();
                    idleTime = 0;
                    birdInAction = false;
                }
            }), Actions.rotateTo(-75, 0.35f));
            birdActor.addAction(riseThenFall);
        }
    }

    @Override
    public void show() {
        notReady = true;
        MainGameViewport viewport = new MainGameViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage = new Stage(viewport);
        stage.getRoot().setBounds(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        stage.getRoot().getColor().a = 0;
        stage.getRoot().addAction(Actions.fadeIn(0.5f));
        stage.addListener(new BirdListener());

        skyTexture = new Texture("png/stage_sky.png");

        groundTexture = new Texture("png/stage_ground.png");
        groundGroup = new Group();
        for (int i = 0; i < 25; i++) {
            Image image = new Image(groundTexture);
            image.setBounds(i * WORLD_WIDTH / 20f, 0, WORLD_WIDTH / 20f, 0.15f * WORLD_HEIGHT);
            groundGroup.addActor(image);
        }
        stage.addActor(groundGroup);

        notPlaying = new Group();
        getReadyTexture = new Texture("png/get_ready.png");
        Image getReadyImage = new Image(getReadyTexture);
        getReadyImage.setBounds(0.2f * WORLD_WIDTH, 0.65f * WORLD_HEIGHT, 0.6f * WORLD_WIDTH, 0.1f * WORLD_HEIGHT);
        notPlaying.addActor(getReadyImage);

        tapTexture = new Texture("png/tap.png");
        Image tapImage = new Image(tapTexture);
        tapImage.setBounds(0.45f * WORLD_WIDTH, 0.35f * WORLD_HEIGHT, 0.35f * WORLD_WIDTH, 0.25f * WORLD_HEIGHT);
        notPlaying.addActor(tapImage);
        stage.addActor(notPlaying);

        Array<TextureRegion> birdRegions = new Array<TextureRegion>();
        birdTexture = new Texture(Gdx.files.internal("png/bird.png"));
        for (int i = 0; i < 3; i++) {
            birdRegions.add(new TextureRegion(birdTexture, 0, (59 + 11) * i, birdTexture.getWidth(), 59));
        }
        birdAnimation = new Animation(1 / 14f, birdRegions, Animation.PlayMode.LOOP_REVERSED);
        birdActor = new Image(new TextureRegionDrawable(birdAnimation.getKeyFrame(0)));
        birdActor.setBounds(0.25f * WORLD_WIDTH, 0.5f * WORLD_HEIGHT, 0.15f * WORLD_WIDTH, WORLD_HEIGHT / 17f);
        birdActor.setOrigin(birdActor.getWidth() / 2, birdActor.getHeight() / 2);
        birdVelocity = 0;
        birdInAction = true;
        initialY = birdActor.getY();
        stage.addActor(birdActor);

        pipeHeadTexture1 = new Texture("png/pipe_head_1.png");
        pipeHeadTexture2 = new Texture("png/pipe_head_2.png");
        pipeBodyTexture = new Texture("png/pipe_body.png");

        pipes = new Vector2[4];
        pipes[0] = new Vector2(2f * WORLD_WIDTH, 0.5f * WORLD_HEIGHT);
        random = new Random();
        for (int i = 1; i < 4; i++) {
            pipes[i] = new Vector2(pipes[i - 1].x + pipeSpaceWidth, (random.nextFloat() * 0.4f + 0.2f) * WORLD_HEIGHT);
        }

        Gdx.input.setInputProcessor(stage);
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!notReady) {
            pipeTimer += Gdx.graphics.getDeltaTime();
            while (0.005 < pipeTimer) {
                pipeTimer -= 0.005;
                for (int i = 0; i < 4; i++) {
                    pipes[i].x -= 0.0025 * WORLD_WIDTH;

                    if (pipes[i].x < -WORLD_WIDTH / 6f) {
                        pipes[i].x = pipes[((i - 1) + 4) % 4].x + pipeSpaceWidth;
                        pipes[i].y = (random.nextFloat() * 0.4f + 0.2f) * WORLD_HEIGHT;
                    }
                }
            }
        }

        timer += Gdx.graphics.getDeltaTime();
        idleTime += Gdx.graphics.getDeltaTime();
        birdActor.setDrawable(new TextureRegionDrawable(birdAnimation.getKeyFrame(timer)));

        if (0 == groundGroup.getActions().size) {
            groundGroup.setX(0);
            groundGroup.addAction(Actions.moveBy(-WORLD_WIDTH / 20f, 0, 1f / 25));
        }

        if (!birdInAction) {
            birdVelocity -= 400 * Gdx.graphics.getDeltaTime(); //-400 is the gravity value
            float newY = initialY + (birdVelocity) / 2 * idleTime; //y-initial + 1/2at^2
            birdActor.setY(newY);
        }

        if (birdActor.getY() >= WORLD_HEIGHT - birdActor.getHeight()) {
            birdActor.setY(WORLD_HEIGHT - birdActor.getHeight());
        }

        if (0.15f * WORLD_HEIGHT > birdActor.getY()) {
            birdActor.setY(0.15f * WORLD_HEIGHT);
        }

        stage.act(delta);
        stage.getViewport().apply();
        stage.getBatch().begin();
        stage.getBatch().draw(skyTexture, 0, 0.15f * WORLD_HEIGHT, WORLD_WIDTH, 0.85f * WORLD_HEIGHT);
        for (Vector2 pipe : pipes) {
            stage.getBatch().draw(pipeHeadTexture2, pipe.x, pipe.y, WORLD_WIDTH / 6f, WORLD_HEIGHT / 30f);
            stage.getBatch().draw(pipeBodyTexture, pipe.x + (WORLD_WIDTH / 200f), 0.15f * WORLD_HEIGHT, (WORLD_WIDTH / 6f) - (WORLD_WIDTH / 100f), pipe.y - 0.15f * WORLD_HEIGHT);
            stage.getBatch().draw(pipeBodyTexture, pipe.x + (WORLD_WIDTH / 200f), pipe.y + pipeSpaceHeight + (WORLD_WIDTH / 30f), (WORLD_WIDTH / 6f) - (WORLD_WIDTH / 100f), WORLD_HEIGHT / 2f);
            stage.getBatch().draw(pipeHeadTexture1, pipe.x, pipe.y + pipeSpaceHeight, WORLD_WIDTH / 6f, WORLD_HEIGHT / 30f);

        }
        stage.getBatch().end();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().setScreenSize(width, height);
        stage.getCamera().position.set(100 / 2f, 200 / 2f, 0);
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        skyTexture.dispose();
        groundTexture.dispose();
        getReadyTexture.dispose();
        tapTexture.dispose();
        birdTexture.dispose();
    }

}
