package com.xili7.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
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
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

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
    private Texture gameOverTexture;
    private TextureRegion numbers[];
    private Texture scoreBoardTexture;
    private Texture newTexture;
    private Texture okTexture;
    private Texture shareTexture;
    private Image newRecordImage;
    private Image okImage;

    private Group groundGroup;
    private Group notPlaying;
    private Group gameOverGroup;

    private boolean gameOver;
    private boolean notReady;
    private boolean birdInAction;
    private Animation birdAnimation;
    private Image birdActor;
    private float timer;
    private float birdVelocity;
    private float initialY;
    private float idleTime;


    private final float pipeSpaceWidth = 4 * WORLD_WIDTH / 6;
    private final float pipeSpaceHeight = WORLD_HEIGHT / 3;
    private Vector2 pipes[];
    private boolean scoreCounted[];
    private Random random;
    private float pipeTimer;

    private static int bestScore;
    private int currentScore;
    private boolean afterShake;

    private class BirdListener extends ClickListener {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            super.clicked(event, x, y);
            System.out.println("CLicked");
            notPlaying.setVisible(false);
            notReady = false;
            if(gameOver) {
                return;
            }
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

    class OkListener extends ClickListener {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            okImage.setPosition(0.15f * WORLD_WIDTH, 0.24f * WORLD_HEIGHT);
            return true;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            okImage.setPosition(0.15f * WORLD_WIDTH, 0.25f * WORLD_HEIGHT);
            stage.addAction(Actions.fadeOut(0.5f));
            ((Game)Gdx.app.getApplicationListener()).setScreen(new MainScreen());
        }
    }

    private BirdListener birdListener;

    @Override
    public void show() {
        notReady = true;
        MainGameViewport viewport = new MainGameViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage = new Stage(viewport);
        stage.getRoot().setBounds(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        stage.getRoot().getColor().a = 0;
        stage.getRoot().addAction(Actions.fadeIn(0.5f));
        birdListener = new BirdListener();
        stage.addListener(birdListener);

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
        scoreCounted = new boolean[4];
        random = new Random();
        for (int i = 1; i < 4; i++) {
            pipes[i] = new Vector2(pipes[i - 1].x + pipeSpaceWidth, (random.nextFloat() * 0.4f + 0.2f) * WORLD_HEIGHT);
        }

        gameOverTexture = new Texture("png/game_over.png");
        Image gameOverImage = new Image(gameOverTexture);
        gameOverImage.setBounds(0.2f * WORLD_WIDTH, 0.75f * WORLD_HEIGHT, 0.6f * WORLD_WIDTH, 0.1f * WORLD_HEIGHT);
        gameOverGroup = new Group();
        gameOverGroup.addActor(gameOverImage);

        scoreBoardTexture = new Texture("png/score_board.png");
        Image scoreBoardImage = new Image(scoreBoardTexture);
        scoreBoardImage.setBounds(0.1f * WORLD_WIDTH, 0.4f * WORLD_HEIGHT, 0.8f * WORLD_WIDTH, 0.28f * WORLD_HEIGHT);
        gameOverGroup.addActor(scoreBoardImage);


        okTexture = new Texture("png/ok.png");
        okImage = new Image(okTexture);
        okImage.setBounds(0.15f * WORLD_WIDTH, 0.25f * WORLD_HEIGHT, 0.3f * WORLD_WIDTH, 0.070f * WORLD_HEIGHT);
        okImage.addListener(new OkListener());
        gameOverGroup.addActor(okImage);

        shareTexture = new Texture("png/share.png");
        Image shareImage = new Image(shareTexture);
        shareImage.setBounds(0.5f * WORLD_WIDTH, 0.25f * WORLD_HEIGHT, 0.3f * WORLD_WIDTH, 0.070f * WORLD_HEIGHT);
        gameOverGroup.addActor(shareImage);

        newTexture = new Texture("png/new_record.png");
        newRecordImage = new Image(newTexture);
        newRecordImage.setBounds(0.56f * WORLD_WIDTH, 0.507f * WORLD_HEIGHT, 0.13f * WORLD_WIDTH, 0.03f * WORLD_HEIGHT);
        newRecordImage.setVisible(false);
        gameOverGroup.addActor(newRecordImage);

        gameOverGroup.setVisible(false);
        stage.addActor(gameOverGroup);

        numbers = new TextureRegion[10];
        Texture numberTexture = new Texture("png/numbers.png");
        int numberWidth = 35;
        for (int i = 0; i < 10; i++) {
            numbers[i] = new TextureRegion(numberTexture, (45) * i, 0, numberWidth, numberTexture.getHeight());
        }


        Gdx.input.setInputProcessor(stage);
    }

    private void checkCollision() {
        if (birdActor.getY() >= WORLD_HEIGHT - birdActor.getHeight()) {
            birdActor.setY(WORLD_HEIGHT - birdActor.getHeight());
        }

        Action shakeAction = Actions.sequence(Actions.moveBy(-0.05f * WORLD_WIDTH, 0, 0.5f, Interpolation.bounceIn), Actions.moveBy(0.05f * WORLD_WIDTH, 0, 0.5f, Interpolation.bounceIn));
        Action afterShakeAction = Actions.sequence(shakeAction, shakeAction, Actions.run(new Runnable() {
            public void run() {
                afterShake = true;
                if (currentScore > bestScore) {
                    bestScore = currentScore;
                    newRecordImage.setVisible(true);
                }
            }
        }));
        Action birdFallAction1 = Actions.parallel(Actions.moveBy(0, 0.15f * WORLD_HEIGHT - birdActor.getY(), 0.4f), Actions.rotateTo(-90, 0.4f));
        Action birdFallAction = Actions.sequence(birdFallAction1, Actions.run(new Runnable() {
            @Override
            public void run() {
                gameOverGroup.setVisible(true);
                gameOverGroup.getColor().a = 0;
                gameOverGroup.addAction(Actions.fadeIn(0.5f));
            }
        }));

        if (0.15f * WORLD_HEIGHT > birdActor.getY()) {
            gameOver = true;
            stage.removeListener(birdListener);
            birdActor.clearActions();
            stage.addAction(afterShakeAction);
            birdActor.addAction(birdFallAction);
            return;
        }

        for (Vector2 pipe : pipes) {
            if (birdActor.getX() + birdActor.getWidth() >= pipe.x && birdActor.getX() < pipe.x + WORLD_WIDTH / 6f) {
                if (birdActor.getY() < pipe.y || birdActor.getY() + birdActor.getHeight() > pipe.y + pipeSpaceHeight) {
                    gameOver = true;
                    stage.removeListener(birdListener);
                    birdActor.clearActions();
                    stage.addAction(afterShakeAction);
                    birdActor.addAction(birdFallAction);
                    break;
                }
            }
        }
    }

    private void printScoreAt(float x, float y, int score) {
        if (0 == score) {
            stage.getBatch().draw(numbers[score], x, y, 0.07f * WORLD_WIDTH, 0.05f * WORLD_HEIGHT);
        } else {
            int i = 0;
            while (score > 0) {
                int currentDigit = score % 10;
                stage.getBatch().draw(numbers[currentDigit], x - (i * 0.073f * WORLD_WIDTH), y, 0.07f * WORLD_WIDTH, 0.05f * WORLD_HEIGHT);
                score /= 10;
                i++;
            }
        }
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!gameOver) {
            checkCollision();
        }

        if (!notReady && !gameOver) {
            pipeTimer += Gdx.graphics.getDeltaTime();
            while (0.005 < pipeTimer) {
                pipeTimer -= 0.005;
                for (int i = 0; i < 4; i++) {
                    pipes[i].x -= 0.0025 * WORLD_WIDTH;

                    if (pipes[i].x < -WORLD_WIDTH / 6f) {
                        pipes[i].x = pipes[((i - 1) + 4) % 4].x + pipeSpaceWidth;
                        pipes[i].y = (random.nextFloat() * 0.4f + 0.2f) * WORLD_HEIGHT;
                        scoreCounted[i] = false;
                    }

                    if (!scoreCounted[i] && pipes[i].x < (birdActor.getX() + birdActor.getWidth() / 2)) {
                        currentScore++;
                        scoreCounted[i] = true;
                    }
                }
            }
        }

        timer += Gdx.graphics.getDeltaTime();
        idleTime += Gdx.graphics.getDeltaTime();
        birdActor.setDrawable(new TextureRegionDrawable(birdAnimation.getKeyFrame(timer)));

        if (!gameOver && 0 == groundGroup.getActions().size) {
            groundGroup.setX(0);
            groundGroup.addAction(Actions.moveBy(-WORLD_WIDTH / 20f, 0, 1f / 25));
        }

        if (!gameOver && !birdInAction) {
            birdVelocity -= 400 * Gdx.graphics.getDeltaTime(); //-400 is the gravity value
            float newY = initialY + (birdVelocity) / 2 * idleTime; //y-initial + 1/2at^2
            birdActor.setY(newY);
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
        if (!gameOver) {
            printScoreAt(0.45f * WORLD_WIDTH, 0.9f * WORLD_HEIGHT, currentScore);
        }
        stage.getBatch().end();
        stage.draw();
        if (afterShake) {
            stage.getBatch().begin();
            printScoreAt(0.75f * WORLD_WIDTH, 0.55f * WORLD_HEIGHT, currentScore);
            printScoreAt(0.75f * WORLD_WIDTH, 0.44f * WORLD_HEIGHT, bestScore);
            stage.getBatch().end();
        }
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
        dispose();
    }

    @Override
    public void dispose() {
        skyTexture.dispose();
        groundTexture.dispose();
        getReadyTexture.dispose();
        tapTexture.dispose();
        birdTexture.dispose();
        numbers[0].getTexture().dispose();
        scoreBoardTexture.dispose();
        gameOverTexture.dispose();
        okTexture.dispose();
        newTexture.dispose();
        shareTexture.dispose();
        pipeHeadTexture1.dispose();
        pipeHeadTexture2.dispose();
        pipeBodyTexture.dispose();
        stage.dispose();
    }

}
