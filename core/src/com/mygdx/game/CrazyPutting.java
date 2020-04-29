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
import java.util.ArrayList;
import java.util.List;
import static com.mygdx.game.Variables.*;
import static com.mygdx.game.AIUtils.*;

public class CrazyPutting  implements ApplicationListener {

    private ModelBatch modelBatch;
    private Model arrow;
    private double lastShotVelocity = SHOT_VELOCITY;
    private PuttingCourse course;
    private float cameraRotationSpeed = 1.4f;
    private float cameraZoomSpeed = 0.5f;

    private ModelInstance arrowInstance;
    private ModelInstance [] terrainInstance;
    private ModelInstance waterInstance;
    private ModelInstance wallInstance;

    private ModelInstance [] flagInstances;

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

    public CrazyPutting( PuttingCourse c, GameInfo gameAspects,GameScreen p){
        this.gameScreen=p;
        this.course=c;
        this.players = new ArrayList<>(gameAspects.players);
        this.currentPlayer=players.get(0);
        GAME_ASPECTS = gameAspects;
        WORLD = c;
        GAME = this;
        if (SHOT_VELOCITY > gameAspects.maxVelocity) SHOT_VELOCITY = gameAspects.maxVelocity;
    }

    @Override
    public void create() {
        //initialize graphics setter
        GameSetup graphicsSetup=new GameSetup();

        graphicsSetup.initCamera();

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
        terrainInstance = TerrainBuilder.buildTerrain();
        waterInstance = TerrainBuilder.buildWater();
        wallInstance = TerrainBuilder.buildWalls();

        //initialize physics
        world_physics = new PuttingCoursePhysics();
        previous_time = System.currentTimeMillis() / 1000.0;

        //setup players
        for(Player p : GAME_ASPECTS.players){
            p.init(world_physics);
        }
        currentPlayer.notifyStartOfTurn();
        currentPlayer.loadCamera();

        //initialize environment and lights
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.2f, 0.2f, 1.f));

        if(CAST_SHADOWS){
            shadowBatch = new ModelBatch(new DepthShaderProvider());
            shadowLight = new DirectionalShadowLight(16384, 16384, 130f, 130f, 1f, 150f);
            environment.add(shadowLight.set(0.8f, 0.8f, 0.8f, -0.5f, -1.0f, 0.5f));
            environment.shadowMap = shadowLight;
        } else {
            environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -1.0f, 1f));
        }

        //TODO remove?
        System.out.println("Corner to corner distance: " + unfoldDistance(
                new Vector2d(0, 0),
                new Vector2d(50d / WORLD_SCALING, 50d / WORLD_SCALING),
                WORLD.get_height(),
                100
        ));
    }

    @Override
    public void dispose() {
    }

    @Override
    public void render() {

        if(Gdx.input.isKeyPressed(Input.Keys.P)){
            gameScreen.endGame=true;
        }

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        batch.begin();
        skySprite.draw(batch);
        batch.end();

        previous_time = world_physics.frameStep(previous_time);

        Vector3d real_pos = currentPlayer.getBall().getPosition();
        float ballX = (float) real_pos.get_x();
        float ballY = (float) real_pos.get_y();
        float ballZ = (float) real_pos.get_z();
        Vector3 currentBallPos = new Vector3(ballX, ballY, ballZ);

        CAMERA.position.set(currentPlayer.getCameraPosition());
        CAMERA.up.set(Vector3.Y);
        CAMERA.lookAt(currentBallPos);
        CAMERA.update();

        if (currentPlayer.requestedTurnRight()) {
            YAW += -Gdx.graphics.getDeltaTime()*cameraRotationSpeed;
        }

        if (currentPlayer.requestedTurnLeft()) {
            YAW += Gdx.graphics.getDeltaTime()*cameraRotationSpeed;
        }

        if(currentPlayer.requestedHit() && currentPlayer.getBall().turn_state == TURN_STATE_START) {
            currentPlayer.saveCamera();
            double standard_factor = Math.sqrt(3)/Math.sqrt(2);

            if (currentPlayer instanceof Player.Human)
                SHOT_VELOCITY=gameScreen.getInputVelocity();

            if (!currentPlayer.getBall().is_moving) {
                currentPlayer.getBall().hit(
                        (new Vector2d(CAMERA.direction.x, CAMERA.direction.z)).normalize(),
                        SHOT_VELOCITY * standard_factor
                );

                Vector2d vec = (new Vector2d(CAMERA.direction.x, CAMERA.direction.z)).normalize();
                System.out.println("Hit direction is " + vec.angle());
            }

        }

        if (currentPlayer.requestedZoomIn()) {

            if (VIEW_ZOOM > 1f) {
                VIEW_ZOOM -= cameraZoomSpeed;
            }

            else {
                VIEW_ZOOM = 1f;
            }

        }

        if (currentPlayer.requestedZoomOut()) {

            if (VIEW_ZOOM < 50f) {
                VIEW_ZOOM += cameraZoomSpeed;
            }

            else{
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

        if(CAST_SHADOWS) {
            shadowLight.begin(Vector3.Zero, CAMERA.direction);
            shadowBatch.begin(shadowLight.getCamera());
        }

        for(Player p : GAME_ASPECTS.players) {

            if ((p.getBall().hit_count > 0 || p == currentPlayer)) {
                if (CAST_SHADOWS) shadowBatch.render(p.getBall().getModel(), environment);
                modelBatch.render(p.getBall().getModel(), environment);
            }
        }

        for (int i = 0; i < 25; i++)
            modelBatch.render(terrainInstance[i], environment);

        modelBatch.render(flagInstances[0], environment);
        modelBatch.render(flagInstances[1], environment);
        modelBatch.render(flagInstances[2], environment);

        modelBatch.render(waterInstance, environment);
        modelBatch.render(wallInstance, environment);

        if(CAST_SHADOWS) {
            shadowBatch.render(flagInstances[0], environment);
            shadowBatch.render(flagInstances[1], environment);
            shadowBatch.end();
            shadowLight.end();
        }

        modelBatch.end();

        if(!currentPlayer.getBall().is_moving){

            if(currentPlayer.getBall().isTouchingFlag()){
                Player next_player = getNextPlayer();
                gameScreen.winners.add(currentPlayer);
                System.out.println(currentPlayer + " reached flag after " + currentPlayer.getBall().hit_count + " shots.");
                players.remove(currentPlayer);

                currentPlayer.getBall().turn_state = TURN_STATE_END;

                if(players.isEmpty()){
                    gameScreen.endGame=true;
                }

                else{
                    currentPlayer = next_player;
                    currentPlayer.getBall().turn_state = TURN_STATE_START;
                    currentPlayer.notifyStartOfTurn();
                    currentPlayer.loadCamera();
                }

            }

            else if(currentPlayer.getBall().isStuck()){

                if(currentPlayer.requestedReset()){
                    currentPlayer.getBall().rewind();
                    currentPlayer.getBall().hit_count += 2;
                    currentPlayer.getBall().turn_state = TURN_STATE_START;
                    currentPlayer.notifyStartOfTurn();
                }

            }

            else if(currentPlayer.getBall().turn_state == TURN_STATE_WAIT){
                currentPlayer.getBall().turn_state = TURN_STATE_END;
                currentPlayer = getNextPlayer();
                currentPlayer.getBall().turn_state = TURN_STATE_START;
                currentPlayer.notifyStartOfTurn();
                currentPlayer.loadCamera();
            }

            if (lastShotVelocity != SHOT_VELOCITY) {
                lastShotVelocity = SHOT_VELOCITY;
                ModelBuilder modelBuilder = new ModelBuilder();
                arrow = modelBuilder.createBox((float)((2 * SHOT_VELOCITY) / MAX_SHOT_VELOCITY), 0.05f, 0.05f,
                        new Material(new ColorAttribute(ColorAttribute.Emissive, Color.YELLOW)),
                        Usage.Position | Usage.Normal
                );
                arrowInstance = new ModelInstance(arrow, 0, 0, 0);
            }

            if(new Vector3(CAMERA.direction.x, 0, CAMERA.direction.z).nor().z>0)
                arrowInstance.transform.setToRotation(Vector3.Y, 90+(float)Math.toDegrees((Math.asin(new Vector3(CAMERA.direction.x, 0, CAMERA.direction.z).nor().x))));
            else
                arrowInstance.transform.setToRotation(Vector3.Y, 90-(float)Math.toDegrees((Math.asin(new Vector3(CAMERA.direction.x, 0, CAMERA.direction.z).nor().x))));

            arrowInstance.transform.setTranslation(new Vector3(ballX, ballY, ballZ).add(new Vector3(CAMERA.position.x, 0, CAMERA.position.z).sub(new Vector3(ballX, 0, ballZ)).nor().scl(
                    (float)(-0.3f - 0.7f * (2 * SHOT_VELOCITY) / MAX_SHOT_VELOCITY))));
            modelBatch.begin(CAMERA);
            Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
            modelBatch.render(arrowInstance, environment);
            modelBatch.end();
        }

        if(CAMERA.position.y<0){
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

    public Player getCurrentPlayer(){
        return currentPlayer;
    }

    public Player getNextPlayer(){

        if(players.indexOf(currentPlayer) == players.size() - 1)
            return players.get(0);

        else
            return players.get(players.indexOf(currentPlayer) + 1);

    }

}
