package com.mygdx.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.courses.GameInfo;
import com.mygdx.game.courses.PuttingCourse;
import com.mygdx.game.obstacles.Obstacle;
import com.mygdx.game.physics.PuttingCoursePhysics;
import com.mygdx.game.screens.GameScreen;
import com.mygdx.game.utils.ColorProof;
import com.mygdx.game.utils.Vector2d;
import com.mygdx.game.utils.Vector3d;

import java.util.ArrayList;
import java.util.List;

import static com.mygdx.game.utils.Variables.*;

public class CrazyPutting implements ApplicationListener {

    private ModelBatch modelBatch;
    private Model arrow;
    private double lastShotVelocity = SHOT_VELOCITY;
    private PuttingCourse course;
    private float cameraRotationSpeed = 1.4f;
    private float cameraZoomSpeed = 0.5f;

    private ModelInstance arrowInstance;
    private ModelInstance[] terrainInstance;
    private ModelInstance waterInstance;
    private ModelInstance wallInstance;

    private ModelInstance[] flagInstances;

    private Environment environment;
    private PuttingCoursePhysics world_physics;
    private SpriteBatch batch;
    private DirectionalShadowLight shadowLight;
    private ModelBatch shadowBatch;
    private Sprite waterSprite;
    private Sprite skySprite;
    private double previous_time;
    private List<Player> players;
    private Player currentPlayer;
    private GameScreen gameScreen;

    public CrazyPutting(PuttingCourse c, GameInfo gameAspects, GameScreen p) {
        this.gameScreen = p;
        this.course = c;
        this.players = new ArrayList<>(gameAspects.players);
        this.currentPlayer = players.get(0);
        GAME_ASPECTS = gameAspects;
        WORLD = c;
        GAME = this;
        if (SHOT_VELOCITY > gameAspects.maxVelocity) SHOT_VELOCITY = gameAspects.maxVelocity;
    }

    public void initCamera() {
        CAMERA = new PerspectiveCamera(75,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());

        CAMERA.position.set(-5f, 5f, -5f);
        CAMERA.lookAt(0f, 0f, 0f);

        CAMERA.near = 2.5f;
        CAMERA.far = 200.0f;
    }

    @Override
    public void create() {
        //initialize graphics setter
        initCamera();

        //prepare model and sprite batches for rendering
        modelBatch = new ModelBatch();
        batch = new SpriteBatch();

        //initialize terrain and world objects
        TerrainBuilder terrainBuilder = new TerrainBuilder();
        Texture waterTexture = new Texture(Gdx.files.internal("water.jpg"));
        waterSprite = terrainBuilder.initWater(waterTexture);
        skySprite = terrainBuilder.initSky(waterTexture);
        flagInstances = terrainBuilder.initFlag(course); //0 is pole, 1 is flag, 2 is range
        arrowInstance = terrainBuilder.initArrow();
        terrainInstance = terrainBuilder.buildTerrain();
        waterInstance = terrainBuilder.buildWater();
        wallInstance = terrainBuilder.buildWalls();

        //initialize physics
        world_physics = new PuttingCoursePhysics();
        world_physics.useFixedDelta(true, DELTA);
        previous_time = System.currentTimeMillis() / 1000.0;

        //setup players
        for (Player p : GAME_ASPECTS.players) {
            p.init(world_physics);
        }
        currentPlayer.notifyStartOfTurn();
        currentPlayer.loadCamera();

        //initialize environment and lights
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.2f, 0.2f, 1.f));

        if (CAST_SHADOWS) {
            shadowBatch = new ModelBatch(new DepthShaderProvider());
            shadowLight = new DirectionalShadowLight(16384, 16384, 130f, 130f, 1f, 150f);
            environment.add(shadowLight.set(0.8f, 0.8f, 0.8f, -0.5f, -1.0f, 0.5f));
            environment.shadowMap = shadowLight;
        } else {
            environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -1.0f, 1f));
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public void render() { //TODO cleanup
        if (Gdx.input.isKeyPressed(Input.Keys.P)) {
            gameScreen.endGame = true;
        }

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        batch.begin();
        skySprite.draw(batch);
        batch.end();

        previous_time = world_physics.frameStep(previous_time);

        Vector3d real_pos = currentPlayer.getBall().getGraphicsPosition();
        float ballX = (float) real_pos.get_x();
        float ballY = (float) real_pos.get_y();
        float ballZ = (float) real_pos.get_z();
        Vector3 currentBallPos = new Vector3(ballX, ballY, ballZ);

        CAMERA.position.set(currentPlayer.getCameraPosition());
        CAMERA.up.set(Vector3.Y);
        CAMERA.lookAt(currentBallPos);
        CAMERA.update();

        if (currentPlayer.requestedTurnRight()) {
            YAW += -Gdx.graphics.getDeltaTime() * cameraRotationSpeed;
        }

        if (currentPlayer.requestedTurnLeft()) {
            YAW += Gdx.graphics.getDeltaTime() * cameraRotationSpeed;
        }

        if (currentPlayer.requestedHit() && currentPlayer.getBall().turn_state == TURN_STATE_START) {
            currentPlayer.saveCamera();

            if (currentPlayer instanceof Player.Human)
                SHOT_VELOCITY = gameScreen.getInputVelocity();

            if (!currentPlayer.getBall().is_moving) {
                currentPlayer.getBall().hit(
                        (new Vector2d(CAMERA.direction.x, CAMERA.direction.z)).normalize(),
                        SHOT_VELOCITY
                );
            }
        }

        if (currentPlayer.requestedZoomIn()) {

            if (VIEW_ZOOM > 5f) {
                VIEW_ZOOM -= cameraZoomSpeed;
            } else {
                VIEW_ZOOM = 5f;
            }
        }

        if (currentPlayer.requestedZoomOut()) {

            if (VIEW_ZOOM < 50f) {
                VIEW_ZOOM += cameraZoomSpeed;
            } else {
                VIEW_ZOOM = 50f;
            }
        }

        if (currentPlayer.requestedIncreaseHitVelocity()) {
            add_shot_velocity(SHOT_VELOCITY_INCREASE());
        }

        if (currentPlayer.requestedDecreaseHitVelocity()) {
            add_shot_velocity(-SHOT_VELOCITY_INCREASE());
        }

        modelBatch.begin(CAMERA);

        if (CAST_SHADOWS) {
            shadowLight.begin(Vector3.Zero, CAMERA.direction);
            shadowBatch.begin(shadowLight.getCamera());
        }

        for (Player p : GAME_ASPECTS.players) {
            if ((p.getBall().hit_count > 0 || p == currentPlayer)) {
                if (CAST_SHADOWS) shadowBatch.render(p.getBall().getModel()[0], environment);
                modelBatch.render(p.getBall().getModel()[0], environment);
            }
        }

        /*for (int i = 0; i < TerrainBuilder.bigDepth * TerrainBuilder.bigWidth; i++)
            modelBatch.render(terrainInstance[i], environment);*/

        for (Obstacle obstacle : course.getObstacles()) {
            if (obstacle.getModel() != null) {
                for (ModelInstance mi : obstacle.getModel()) {
                    modelBatch.render(mi, environment);
                }
            }
        }

        modelBatch.render(flagInstances[0], environment);
        modelBatch.render(flagInstances[1], environment);
        modelBatch.render(flagInstances[2], environment);

        modelBatch.render(waterInstance, environment);
        modelBatch.render(wallInstance, environment);

        if (CAST_SHADOWS) {
            shadowBatch.render(flagInstances[0], environment);
            shadowBatch.render(flagInstances[1], environment);
            shadowBatch.end();
            shadowLight.end();
        }

        modelBatch.end();

        if (!currentPlayer.getBall().is_moving) {

            if (currentPlayer.getBall().isTouchingFlag()) {
                Player next_player = getNextPlayer();
                gameScreen.winners.add(currentPlayer);
                System.out.println(currentPlayer + " reached flag after " + currentPlayer.getBall().hit_count + " shots.");
                players.remove(currentPlayer);

                currentPlayer.getBall().turn_state = TURN_STATE_END;

                if (players.isEmpty()) {
                    gameScreen.endGame = true;
                } else {
                    currentPlayer = next_player;
                    currentPlayer.getBall().turn_state = TURN_STATE_START;
                    currentPlayer.notifyStartOfTurn();
                    currentPlayer.loadCamera();
                }

            } else if (currentPlayer.getBall().isStuck()) {

                if (currentPlayer.requestedReset()) {
                    currentPlayer.getBall().rewind();
                    currentPlayer.getBall().hit_count += 2;
                    currentPlayer.getBall().turn_state = TURN_STATE_START;
                    currentPlayer.notifyStartOfTurn();
                    currentPlayer.loadCamera();
                }

            } else if (currentPlayer.getBall().turn_state == TURN_STATE_WAIT) {
                currentPlayer.getBall().turn_state = TURN_STATE_END;
                currentPlayer.saveCamera();
                currentPlayer = getNextPlayer();
                currentPlayer.getBall().turn_state = TURN_STATE_START;
                currentPlayer.notifyStartOfTurn();
                currentPlayer.loadCamera();
            }

            if (lastShotVelocity != SHOT_VELOCITY) {
                lastShotVelocity = SHOT_VELOCITY;
                ModelBuilder modelBuilder = new ModelBuilder();
                arrow = modelBuilder.createBox((float) ((2 * SHOT_VELOCITY) / MAX_SHOT_VELOCITY), 0.05f, 0.05f,
                        new Material(new ColorAttribute(ColorAttribute.Emissive, ColorProof.SHOT_ARROW())),
                        Usage.Position | Usage.Normal
                );
                arrowInstance = new ModelInstance(arrow, 0, 0, 0);
            }

            if (new Vector3(CAMERA.direction.x, 0, CAMERA.direction.z).nor().z > 0)
                arrowInstance.transform.setToRotation(Vector3.Y, 90 + (float) Math.toDegrees((Math.asin(new Vector3(CAMERA.direction.x, 0, CAMERA.direction.z).nor().x))));
            else
                arrowInstance.transform.setToRotation(Vector3.Y, 90 - (float) Math.toDegrees((Math.asin(new Vector3(CAMERA.direction.x, 0, CAMERA.direction.z).nor().x))));

            arrowInstance.transform.setTranslation(new Vector3(ballX, ballY, ballZ).add(new Vector3(CAMERA.position.x, 0, CAMERA.position.z).sub(new Vector3(ballX, 0, ballZ)).nor().scl(
                    (float) (-0.3f - 0.7f * (2 * SHOT_VELOCITY) / MAX_SHOT_VELOCITY))));
            modelBatch.begin(CAMERA);
            Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
            modelBatch.render(arrowInstance, environment);
            modelBatch.end();
        }

        if (CAMERA.position.y < 0) {
            batch.begin();
            waterSprite.draw(batch);
            batch.end();
        }

    }

    public void add_shot_velocity(double amount) {
        double temp = SHOT_VELOCITY + amount;
        if (temp < 0) temp = 0;
        if (temp > GAME_ASPECTS.maxVelocity) temp = GAME_ASPECTS.maxVelocity;
        SHOT_VELOCITY = temp;
        gameScreen.setInputVel(SHOT_VELOCITY);
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Player getNextPlayer() {

        if (players.indexOf(currentPlayer) == players.size() - 1)
            return players.get(0);

        else
            return players.get(players.indexOf(currentPlayer) + 1);

    }

}
