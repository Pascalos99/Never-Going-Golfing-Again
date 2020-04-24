package com.mygdx.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

import static com.mygdx.game.Variables.CAMERA;
import static com.mygdx.game.Variables.BALL_RADIUS;
import static com.mygdx.game.Variables.GAME_ASPECTS;


import java.util.ArrayList;
import java.util.List;

import static com.mygdx.game.Variables.*;

public class CrazyPutting  implements ApplicationListener {

    private ModelBatch modelBatch;
    private Model arrow;
    private double lastShotVelocity = SHOT_VELOCITY;
    private PuttingCourse course;
    private float cameraRotationSpeed = 1.4f;//100;
    private float cameraZoomSpeed = 0.5f;

    private ModelInstance arrowInstance;
    private ModelInstance [] terrainInstance;
    private ModelInstance waterInstance;
    private ModelInstance wallInstance;
    private ModelInstance flagPoleInstance;
    private ModelInstance flagRangeInstance;

    private Environment environment;
    private PhysicsEngine world_physics;
    private SpriteBatch batch;
    private Texture waterTexture;
    private Sprite waterSprite;
    private Sprite skySprite;
    private double previous_time;
    private List<Player> players;
    private Player currentPlayer;
    private GameScreen gameScreen;

    public CrazyPutting( PuttingCourse c, GameInfo gameAspects,GameScreen p){
        this.gameScreen=p;
        GAME_ASPECTS = gameAspects;
        this.course=c;
        players = new ArrayList<>(gameAspects.players);
        currentPlayer=players.get(0);
        if (SHOT_VELOCITY > gameAspects.maxVelocity) SHOT_VELOCITY = gameAspects.maxVelocity;
        WORLD = c;
        GAME = this;
    }

    @Override
    public void create() {
        System.out.println(new Vector2d(5, 0).rotate(1.5708));
        CAMERA = new PerspectiveCamera(75,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());

        CAMERA.position.set(-5f, 5f, -5f);
        CAMERA.lookAt(0f, 0f, 0f);

        CAMERA.near = 0.1f;
        CAMERA.far = 200.0f;

        modelBatch = new ModelBatch();

        batch = new SpriteBatch();
        waterTexture = new Texture(Gdx.files.internal("water.jpg"));
        waterSprite = new Sprite(waterTexture);
        waterSprite.setColor(0, 0, 1, 0.3f);
        waterSprite.setOrigin(0,0);
        waterSprite.setPosition(0,0);
        waterSprite.setSize(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());

        skySprite = new Sprite(waterTexture);
        skySprite.setColor(1, 1, 1, 0.2f);
        skySprite.setOrigin(0,0);
        skySprite.setPosition(0,0);
        skySprite.setSize(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());

        ModelBuilder modelBuilder = new ModelBuilder();

        float side =(float) (2*GAME_ASPECTS.getTolerance())*WORLD_SCALING;
        Model pole = modelBuilder.createBox( 0.1f, FLAGPOLE_HEIGHT, 0.1f, new Material(ColorAttribute.createDiffuse(Color.PURPLE)),
                Usage.Position | Usage.Normal);
        flagPoleInstance = new ModelInstance(pole,(float) course.get_flag_position().get_x() * WORLD_SCALING, (float) course.getHeightAt(course.get_flag_position().get_x(), course.get_flag_position().get_y()), (float) course.get_flag_position().get_y() * WORLD_SCALING);

        Model poleRange = modelBuilder.createCylinder( side, FLAGPOLE_HEIGHT, side, 40, new Material(ColorAttribute.createDiffuse(new Color(1, 0.4f, 1, 1f)), new BlendingAttribute(0.3f)),
                Usage.Position | Usage.Normal);

        flagPoleInstance = new ModelInstance(pole, (float) course.get_flag_position().get_x()*WORLD_SCALING, (float) course.getHeightAt(course.get_flag_position().get_x(), (float) course.get_flag_position().get_y()), (float) (course.get_flag_position().get_y()*WORLD_SCALING));
        flagRangeInstance = new ModelInstance(poleRange,  (float) course.get_flag_position().get_x()*WORLD_SCALING, (float) course.getHeightAt(course.get_flag_position().get_x(), (float) course.get_flag_position().get_y()), (float) course.get_flag_position().get_y()*WORLD_SCALING);
        terrainInstance = TerrainBuilder.buildTerrain();
        waterInstance = TerrainBuilder.buildWater();
        wallInstance = TerrainBuilder.buildWalls();

        world_physics = new PuttingCoursePhysics();
        previous_time = System.currentTimeMillis() / 1000.0;

        for(Player p : GAME_ASPECTS.players){
            Vector2d start = GAME_ASPECTS.getStart();
            double x = start.get_x();
            double y = start.get_y();

            Color ball_color = null;

            for (int i=0; i < BALL_COLORS.length; i++)
                if (p.getBallColor().equals(BALL_COLORS[i].name)) {
                    ball_color = BALL_COLORS[i].color;
                    break;
                }

            Model ball = modelBuilder.createSphere(BALL_RADIUS * WORLD_SCALING * 2, BALL_RADIUS * WORLD_SCALING * 2, BALL_RADIUS * WORLD_SCALING * 2, 30, 30,
                    new Material(ColorAttribute.createDiffuse(ball_color)),
                    Usage.Position | Usage.Normal
            );
            ModelInstance model = new ModelInstance(ball, 0, 0, 0);
            Ball ball_obj = new Ball(PuttingCoursePhysics.BALL_SIZE, x, y, model, p);
            p.setBall(ball_obj);
            world_physics.addBody(ball_obj);
        }

        currentPlayer.notifyStartOfTurn();

        arrow = modelBuilder.createBox((float)((2 * SHOT_VELOCITY) / MAX_SHOT_VELOCITY), 0.05f, 0.05f,
                new Material(new ColorAttribute(ColorAttribute.Emissive, Color.YELLOW)),
                Usage.Position | Usage.Normal
        );
        arrowInstance = new ModelInstance(arrow, 0, 0, 0);
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.2f, 0.2f, 1.f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -1.0f, 1f));
    }

    @Override
    public void dispose() {
    }

    @Override
    public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

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
            yaw += -Gdx.graphics.getDeltaTime()*cameraRotationSpeed;
        }

        if (currentPlayer.requestedTurnLeft()) {
            yaw += Gdx.graphics.getDeltaTime()*cameraRotationSpeed;
        }

        if(currentPlayer.requestedHit() && currentPlayer.getBall().turn_state == TURN_STATE_START) {
            double standard_factor = Math.sqrt(3)/Math.sqrt(2);

            if (currentPlayer instanceof Player.Human)
                SHOT_VELOCITY=gameScreen.getInputVelocity();

            if (!currentPlayer.getBall().is_moving)
                currentPlayer.getBall().hit(
                        (new Vector2d(CAMERA.direction.x, CAMERA.direction.z)).normalize(),
                        SHOT_VELOCITY * standard_factor
                );
        }

        if (currentPlayer.requestedZoomIn()) {

            if (view_zoom > 1f) {
                view_zoom -= cameraZoomSpeed;
            }

            else{
                view_zoom = 1f;
            }

        }

        if (currentPlayer.requestedZoomOut()) {

            if (view_zoom < 50f) {
                view_zoom += cameraZoomSpeed;
            }

            else{
                view_zoom = 50f;
            }

        }

        if (currentPlayer.requestedIncreaseHitVelocity()) {
            add_shot_velocity(SHOT_VELOCITY_INCREASE());
        }

        if (currentPlayer.requestedDecreaseHitVelocity()) {
            add_shot_velocity(-SHOT_VELOCITY_INCREASE());
        }

        for(Player p : GAME_ASPECTS.players) {
            if (p.getBall().hit_count > 0 || p == currentPlayer)
                modelBatch.render(p.getBall().getModel(), environment);
        }

        modelBatch.begin(CAMERA);

        for (int i = 0; i < 25; i++)
            modelBatch.render(terrainInstance[i], environment);

        modelBatch.render(flagPoleInstance, environment);
        modelBatch.render(flagRangeInstance, environment);

        modelBatch.render(waterInstance, environment);
        modelBatch.render(wallInstance, environment);
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

        isolate(currentPlayer);
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

    public PuttingCoursePhysics isolate(Player p){
        Ball ball = (Ball) p.getBall().dupe();
        PuttingCoursePhysics world = (PuttingCoursePhysics) world_physics.dupe();
        world.addBody(ball);
        return world;
    }

    public static Ball findIsolatedBall(PuttingCoursePhysics world){

        for(TopDownPhysicsObject obj : world.getBodies()){

            if(obj instanceof Ball)
                return (Ball)obj;

        }

        return null;
    }

}
