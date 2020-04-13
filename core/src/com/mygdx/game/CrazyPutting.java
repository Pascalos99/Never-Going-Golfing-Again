package com.mygdx.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
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

public class CrazyPutting implements ApplicationListener {

    private ModelBatch modelBatch;
    private Model arrow;
    private Model ball;
    private PuttingCourse course;
    private float cameraRotationSpeed = 100;
    private float cameraZoomSpeed = 0.7f;
    private float resolution = 0.2f;
    private float worldScaling = 8;
    private ModelInstance arrowInstance;
    private ModelInstance [] terrainInstance;
    private ModelInstance waterInstance;
    private ModelInstance wallInstance;
    private ArrayList<Vector3> borderPoints1= new ArrayList<Vector3>();
    private ArrayList<Vector3> borderPoints2= new ArrayList<Vector3>();
    private ArrayList<Vector3> borderPoints3= new ArrayList<Vector3>();
    private ArrayList<Vector3> borderPoints4= new ArrayList<Vector3>();
    private Environment environment;
    private PhysicsEngine world_physics;
    private double previous_time;
    private List<Player> players;

    public CrazyPutting( PuttingCourse c, GameInfo gameAspects){
        GAME_ASPECTS = gameAspects;
        this.course=c;
        players = new ArrayList<Player>(gameAspects.players);
    }

    public static float getBallRadius() {
        return Variables.BALL_RADIUS;
    }

    public static PerspectiveCamera getCamera(){
        return CAMERA;
    }

    @Override
    public void create() {
        // Create camera sized to screens width/height with Field of View of 75 degrees
        CAMERA = new PerspectiveCamera(75,
                                        Gdx.graphics.getWidth(),
                                        Gdx.graphics.getHeight());

        // Move the camera 3 units back along the z-axis and look at the origin
        CAMERA.position.set(-5f, 5f, -5f);
        CAMERA.lookAt(0f, 0f, 0f);

        // Near and Far (plane) repesent the minimum and maximum ranges of the camera in, um, units
        CAMERA.near = 0.1f;
        CAMERA.far = 200.0f;

        // A ModelBatch is like a SpriteBatch, just for models.  Use it to batch up geometry for OpenGL
        modelBatch = new ModelBatch();

        // A ModelBuilder can be used to build meshes by hand
        ModelBuilder modelBuilder = new ModelBuilder();

        // It also has the handy ability to make certain premade shapes, like a Cube
        // We pass in a ColorAttribute, making our cubes diffuse ( aka, color ) red.
        // And let openGL know we are interested in the Position and Normal channels
        ball = modelBuilder.createSphere(BALL_RADIUS * 2, BALL_RADIUS * 2, BALL_RADIUS * 2, 30, 30,
                new Material(ColorAttribute.createDiffuse(Variables.BALL_COLOR)),
                Usage.Position | Usage.Normal
        );
        arrow = modelBuilder.createBox(1, 0.05f, 0.05f,
                new Material(new ColorAttribute(ColorAttribute.Emissive, Color.YELLOW)),
                Usage.Position | Usage.Normal
        );

        terrainInstance = buildTerrain();
        waterInstance = buildWater();
        wallInstance = buildWalls();
//        flagBoxInstance = new ModelInstance(box,0,0, 0);

        // A model holds all of the information about an, um, model, such as vertex data and texture info
        // However, you need an instance to actually render it.  The instance contains all the
        // positioning information ( and more ).  Remember Model==heavy ModelInstance==Light

        world_physics = new PuttingCoursePhysics(course);
        previous_time = System.currentTimeMillis() / 1000.0;

        for(Player p : GAME_ASPECTS.players){
            Vector2d start = GAME_ASPECTS.getStart();
            double x = start.get_x();
            double y = start.get_y();
            ModelInstance model = new ModelInstance(ball, 0, 0, 0);
            Ball ball_obj = new Ball(PuttingCoursePhysics.BALL_SIZE, x, y, model);
            p.setBall(ball_obj);
            world_physics.addBody(ball_obj);
        }

        arrowInstance = new ModelInstance(arrow, 0, 0, 0);

        // Finally we want some light, or we wont see our color.  The environment gets passed in during
        // the rendering process.  Create one, then create an Ambient ( non-positioned, non-directional ) light.
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.2f, 0.2f, 1.f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -1.0f, 1f));
    }

    public ModelInstance buildWater(){
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("grid", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(new Color(0.2f, 0.2f, 1, 1f)), new BlendingAttribute(0.5f)));
        Vector3 pos1 = new Vector3(0,0,0);
        Vector3 pos2 = new Vector3(0,0,50);
        Vector3 pos3 = new Vector3(50,0,50);
        Vector3 pos4 = new Vector3(50,0,0);
        VertexInfo v1 = new VertexInfo().setPos(pos1).setNor(new Vector3(0,1,0)).setCol(null).setUV(0.5f, 0.0f);
        VertexInfo v2 = new VertexInfo().setPos(pos2).setNor(new Vector3(0,1,0)).setCol(null).setUV(0.0f, 0.0f);
        VertexInfo v3 = new VertexInfo().setPos(pos3).setNor(new Vector3(0,1,0)).setCol(null).setUV(0.0f, 0.5f);
        VertexInfo v4 = new VertexInfo().setPos(pos4).setNor(new Vector3(0,1,0)).setCol(null).setUV(0.5f, 0.5f);
        builder.rect(v1, v2, v3, v4);
        Model water = modelBuilder.end();
        ModelInstance waterInstance = new ModelInstance(water, 0, 0, 0);
        return waterInstance;
    }

    public ModelInstance buildWalls(){
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("grid", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(IntAttribute.createCullFace(GL20.GL_NONE),ColorAttribute.createDiffuse(Color.BROWN)));
        VertexInfo v1, v2, v3, v4;
        Vector3 nor1=new Vector3(-1,0,0);
        for(int i=0;i<borderPoints1.size()-1;i++){
            Vector3 p1=new Vector3(borderPoints1.get(i).x,borderPoints1.get(i).y,borderPoints1.get(i).z);
            Vector3 p2=new Vector3(borderPoints1.get(i+1).x,borderPoints1.get(i+1).y,borderPoints1.get(i+1).z);

            v1 = new VertexInfo().setPos(new Vector3(p1.x,1+Math.max(p1.y,0),p1.z)).setNor(nor1).setCol(null).setUV(0.5f, 0.0f);
            v2 = new VertexInfo().setPos(new Vector3(p1.x,-10,p1.z)).setNor(nor1).setCol(null).setUV(0.0f, 0.0f);
            v4 = new VertexInfo().setPos(new Vector3(p2.x,1+Math.max(p2.y,0),p2.z)).setNor(nor1).setCol(null).setUV(0.0f, 0.5f);
            v3 = new VertexInfo().setPos(new Vector3(p2.x,-10,p2.z)).setNor(nor1).setCol(null).setUV(0.5f, 0.5f);

            builder.rect(v1, v2, v3, v4);
        }
        nor1=new Vector3(1,0,0);
        for(int i=0;i<borderPoints3.size()-1;i++){
            Vector3 p1=new Vector3(borderPoints3.get(i).x,borderPoints3.get(i).y,borderPoints3.get(i).z);
            Vector3 p2=new Vector3(borderPoints3.get(i+1).x,borderPoints3.get(i+1).y,borderPoints3.get(i+1).z);

            v2 = new VertexInfo().setPos(new Vector3(p1.x,1+Math.max(p1.y,0),p1.z)).setNor(nor1).setCol(null).setUV(0.5f, 0.0f);
            v1 = new VertexInfo().setPos(new Vector3(p1.x,-10,p1.z)).setNor(nor1).setCol(null).setUV(0.0f, 0.0f);
            v3 = new VertexInfo().setPos(new Vector3(p2.x,1+Math.max(p2.y,0),p2.z)).setNor(nor1).setCol(null).setUV(0.0f, 0.5f);
            v4 = new VertexInfo().setPos(new Vector3(p2.x,-10,p2.z)).setNor(nor1).setCol(null).setUV(0.5f, 0.5f);

            builder.rect(v1, v2, v3, v4);
        }
        nor1=new Vector3(0,0,-1);
        for(int i=0;i<borderPoints2.size()-1;i++){
            Vector3 p1=new Vector3(borderPoints2.get(i).x,borderPoints2.get(i).y,borderPoints2.get(i).z);
            Vector3 p2=new Vector3(borderPoints2.get(i+1).x,borderPoints2.get(i+1).y,borderPoints2.get(i+1).z);

            v2 = new VertexInfo().setPos(new Vector3(p1.x,1+Math.max(p1.y,0),p1.z)).setNor(nor1).setCol(null).setUV(0.5f, 0.0f);
            v1 = new VertexInfo().setPos(new Vector3(p1.x,-10,p1.z)).setNor(nor1).setCol(null).setUV(0.0f, 0.0f);
            v3 = new VertexInfo().setPos(new Vector3(p2.x,1+Math.max(p2.y,0),p2.z)).setNor(nor1).setCol(null).setUV(0.0f, 0.5f);
            v4 = new VertexInfo().setPos(new Vector3(p2.x,-10,p2.z)).setNor(nor1).setCol(null).setUV(0.5f, 0.5f);

            builder.rect(v1, v2, v3, v4);
        }
        nor1=new Vector3(0,0,1);
        for(int i=0;i<borderPoints4.size()-1;i++){
            Vector3 p1=new Vector3(borderPoints4.get(i).x,borderPoints4.get(i).y,borderPoints4.get(i).z);
            Vector3 p2=new Vector3(borderPoints4.get(i+1).x,borderPoints4.get(i+1).y,borderPoints4.get(i+1).z);

            v1 = new VertexInfo().setPos(new Vector3(p1.x,1+Math.max(p1.y,0),p1.z)).setNor(nor1).setCol(null).setUV(0.5f, 0.0f);
            v2 = new VertexInfo().setPos(new Vector3(p1.x,-10,p1.z)).setNor(nor1).setCol(null).setUV(0.0f, 0.0f);
            v4 = new VertexInfo().setPos(new Vector3(p2.x,1+Math.max(p2.y,0),p2.z)).setNor(nor1).setCol(null).setUV(0.0f, 0.5f);
            v3 = new VertexInfo().setPos(new Vector3(p2.x,-10,p2.z)).setNor(nor1).setCol(null).setUV(0.5f, 0.5f);

            builder.rect(v1, v2, v3, v4);
        }

        nor1=new Vector3(0,-1,0);
        v2 = new VertexInfo().setPos(new Vector3(0,-10,0)).setNor(nor1).setCol(null).setUV(0.5f, 0.0f);
        v1 = new VertexInfo().setPos(new Vector3(0,-10,50)).setNor(nor1).setCol(null).setUV(0.0f, 0.0f);
        v4 = new VertexInfo().setPos(new Vector3(50,-10,50)).setNor(nor1).setCol(null).setUV(0.0f, 0.5f);
        v3 = new VertexInfo().setPos(new Vector3(50,-10,0)).setNor(nor1).setCol(null).setUV(0.5f, 0.5f);

        builder.rect(v1, v2, v3, v4);

        Model wall = modelBuilder.end();
        ModelInstance wallInstance = new ModelInstance(wall, 0, 0, 0);
        return wallInstance;
    }

    public ModelInstance[] buildTerrain() {
        Vector3 pos1, pos2, pos3, pos4;
        Vector3 nor1, nor2, nor3, nor4;
        Vector2d vec1, vec2, vec3, vec4;
        VertexInfo v1, v2, v3, v4;
        Model rect;
        ModelInstance[] terrainInstance = new ModelInstance[25];
        ModelBuilder modelBuilder = new ModelBuilder();
        MeshPartBuilder builder;
        AtomFunction2d func = new AtomFunction2d(FunctionParser.parse("sin(x)+cos(y)"));
        try{
            func = new AtomFunction2d(FunctionParser.parse(GAME_ASPECTS.getHeightFunction()));
        } catch(Error e) {
            System.out.println(e);
        }

        int gridWidth = 50;
        int gridDepth = 50;

        for (int a = 0; a < 5; a++) {
            for (int b = 0; b < 5; b++) {
                float gw = (float) (2f*Math.PI) / (gridWidth * 5f);
                float gd = (float) (2f*Math.PI) / (gridDepth * 5f);
                modelBuilder.begin();
//                builder = modelBuilder.part("grid", GL20.GL_LINES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.GREEN)));
                builder = modelBuilder.part("grid", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(new Color(0.2f, 1f, 0.2f, 1f))));
                float x, y = 0;
                for (int i = 0; i < gridWidth; i++) {
                    for (int k = 0; k < gridDepth; k++) {

                        pos1 = new Vector3(i*resolution, (float) func.evaluate((i+(a*gridWidth)) * gw, (k+(b*gridDepth)) * gd), k*resolution);
                        pos2 = new Vector3(i*resolution, (float) func.evaluate((i+(a*gridWidth)) * gw, (k+1+(b*gridDepth)) * gd), (k + 1)*resolution);
                        pos3 = new Vector3((i + 1)*resolution, (float) func.evaluate((i+1+(a*gridWidth)) * gw, (k+1+(b*gridDepth)) * gd), (k + 1)*resolution);
                        pos4 = new Vector3((i + 1)*resolution, (float) func.evaluate((i+1+(a*gridWidth)) * gw, (k+(b*gridDepth)) * gd), k*resolution);
                        if(i==0&&a==0){
                            borderPoints1.add(new Vector3(pos1.x+a*10,pos1.y,pos1.z+b*10));
                            if(k==gridDepth-1&&b==4)
                                borderPoints1.add(new Vector3(pos2.x+a*10,pos2.y,pos2.z+b*10));
                        }
                        if(k==0&&b==0){
                            borderPoints2.add(new Vector3(pos1.x+a*10,pos1.y,pos1.z+b*10));
                            if(i==gridDepth-1&&a==4)
                                borderPoints2.add(new Vector3(pos4.x+a*10,pos4.y,pos4.z+b*10));
                        }
                        if(i==gridWidth-1&&a==4){
                            borderPoints3.add(new Vector3(pos3.x+a*10,pos3.y,pos3.z+b*10));
                            if(k==0&&b==0)
                                borderPoints3.add(new Vector3(pos4.x+a*10,pos4.y,pos4.z+b*10));
                        }
                        if(k==gridDepth-1&&b==4){
                            borderPoints4.add(new Vector3(pos3.x+a*10,pos3.y,pos3.z+b*10));
                            if(i==0&&a==0)
                                borderPoints4.add(new Vector3(pos2.x+a*10,pos2.y,pos2.z+b*10));
                        }
                        float d=1/resolution;
                        vec1 = new Vector2d((i+(a*gridWidth)) * gw, (k+(b*gridDepth)) * gd);
                        nor1 = new Vector3(-(float) (func.gradient(vec1).get_x()), d, 0).add(new Vector3(0, d, -(float) (func.gradient(vec1).get_y())));
                        nor1.nor();

                        vec2 = new Vector2d((i+(a*gridWidth)) * gw, (k + 1+(b*gridDepth)) * gd);
                        nor2 = new Vector3(-(float) (func.gradient(vec2).get_x()), d, 0).add(new Vector3(0, d, -(float) (func.gradient(vec2).get_y())));
                        nor2.nor();

                        vec3 = new Vector2d((i + 1+(a*gridWidth)) * gw, (k + 1+(b*gridDepth)) * gd);
                        nor3 = new Vector3(-(float) (func.gradient(vec3).get_x()), d, 0).add(new Vector3(0, d, -(float) (func.gradient(vec3).get_y())));
                        nor3.nor();

                        vec4 = new Vector2d((i + 1+(a*gridWidth)) * gw, (k+(b*gridDepth)) * gd);
                        nor4 = new Vector3(-(float) (func.gradient(vec4).get_x()), d, 0).add(new Vector3(0, d, -(float) (func.gradient(vec4).get_y())));
                        nor4.nor();

                        v1 = new VertexInfo().setPos(pos1).setNor(nor1).setCol(null).setUV(0.5f, 0.0f);
                        v2 = new VertexInfo().setPos(pos2).setNor(nor2).setCol(null).setUV(0.0f, 0.0f);
                        v3 = new VertexInfo().setPos(pos3).setNor(nor3).setCol(null).setUV(0.0f, 0.5f);
                        v4 = new VertexInfo().setPos(pos4).setNor(nor4).setCol(null).setUV(0.5f, 0.5f);

                        builder.rect(v1, v2, v3, v4);
//                        builder.line(pos1,nor1.add(pos1));
                    }
                }
                rect = modelBuilder.end();
                terrainInstance[a * 5 + b] = new ModelInstance(rect, a * 10, 0, b * 10);
            }
        }
        return terrainInstance;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void render() {
        // You've seen all this before, just be sure to clear the GL_DEPTH_BUFFER_BIT when working in 3D
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        previous_time = world_physics.frameStep(previous_time);
        Player currentPlayer = GAME_ASPECTS.players.get(0);
        float ballX = currentPlayer.getBall().realX;
        float ballY = currentPlayer.getBall().realY;
        float ballZ = currentPlayer.getBall().realZ;
        Vector3 currentBallPos = new Vector3(ballX, ballY, ballZ);


//        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
//            float x = Gdx.input.getDeltaX();
//            float y = Gdx.input.getDeltaY();
//            camera.rotateAround(new Vector3(GAME_ASPECTS.players.get(0).getBall().realX,GAME_ASPECTS.players.get(0).getBall().realY,GAME_ASPECTS.players.get(0).getBall().realZ),Vector3.Y,Gdx.graphics.getDeltaTime()*10*x);
//            //CAMERA.rotate(new Vector3(-CAMERA.direction.z,0,CAMERA.direction.x),-y/3f);
//        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            CAMERA.rotateAround(new Vector3(ballX,ballY,ballZ),Vector3.Y,-Gdx.graphics.getDeltaTime()*cameraRotationSpeed);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            CAMERA.rotateAround(new Vector3(ballX,ballY,ballZ),Vector3.Y,Gdx.graphics.getDeltaTime()*cameraRotationSpeed);
        }

        if(Gdx.input.isKeyPressed(Input.Keys.SPACE)){
            currentPlayer.getBall().addVelocity(new Vector2d(CAMERA.direction.x/2.0,CAMERA.direction.z/2.0));
        }

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            if (CAMERA.position.dst(currentBallPos) > 1) {
                CAMERA.translate(new Vector3(CAMERA.direction.x * 0.7f, CAMERA.direction.y * cameraZoomSpeed, CAMERA.direction.z * cameraZoomSpeed));
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            if (CAMERA.position.dst(currentBallPos) < 50) {
                CAMERA.translate(new Vector3(CAMERA.direction.x * -0.7f, CAMERA.direction.y * -cameraZoomSpeed, CAMERA.direction.z * -cameraZoomSpeed));
            }
        }

        if(Gdx.input.isKeyPressed(Input.Keys.R)){
            for(Player p : GAME_ASPECTS.players){
                p.getBall().velocity=(new Vector2d(0,0));
                p.getBall().x=1;
                p.getBall().y=1;
            }
        }
        for(Player p : GAME_ASPECTS.players) {
            if (p.getBall().x < BALL_RADIUS/worldScaling){
                p.getBall().x=(BALL_RADIUS+0.01)/worldScaling;
                p.getBall().velocity = (new Vector2d(-p.getBall().velocity.get_x(), p.getBall().velocity.get_y()));
            }
            if (p.getBall().x >(50-BALL_RADIUS)/worldScaling){
                p.getBall().x=(49.9-BALL_RADIUS)/worldScaling;
                p.getBall().velocity = (new Vector2d(-p.getBall().velocity.get_x(), p.getBall().velocity.get_y()));
            }
            if (p.getBall().y<BALL_RADIUS/worldScaling){
                p.getBall().y=(BALL_RADIUS+0.01)/worldScaling;
                p.getBall().velocity = (new Vector2d(p.getBall().velocity.get_x(), -p.getBall().velocity.get_y()));
            }
            if (p.getBall().y >(50-BALL_RADIUS)/worldScaling){
                p.getBall().y=(49.9-BALL_RADIUS)/worldScaling;
                p.getBall().velocity = (new Vector2d(p.getBall().velocity.get_x(), -p.getBall().velocity.get_y()));
            }
        }
        CAMERA.update();
//        flagBoxInstance.transform.set(new Vector3(x2,(float) course.getHeightAt(x2/8,y2/8),y2),new Quaternion(0,0,0,0));

        // When you change the camera details, you need to call update();
        // Also note, you need to call update() at least once.


        modelBatch.begin(CAMERA);

        for(Player p : GAME_ASPECTS.players)
            modelBatch.render(p.getBall().getModel(course), environment);

        for (int i = 0; i < 25; i++)
            modelBatch.render(terrainInstance[i], environment);
        modelBatch.render(waterInstance, environment);
        modelBatch.render(wallInstance, environment);
        //modelBatch.render(flagBoxInstance,environment);
        modelBatch.end();

        if(currentPlayer.getBall().velocity.get_x()==0&&currentPlayer.getBall().velocity.get_y()==0){
            if(new Vector3(CAMERA.direction.x, 0, CAMERA.direction.z).nor().z>0)
                arrowInstance.transform.setToRotation(Vector3.Y, 90+(float)Math.toDegrees((Math.asin(new Vector3(CAMERA.direction.x, 0, CAMERA.direction.z).nor().x))));
            else
                arrowInstance.transform.setToRotation(Vector3.Y, 90-(float)Math.toDegrees((Math.asin(new Vector3(CAMERA.direction.x, 0, CAMERA.direction.z).nor().x))));
            arrowInstance.transform.setTranslation(new Vector3(ballX, ballY, ballZ).add(new Vector3(CAMERA.position.x, 0, CAMERA.position.z).sub(new Vector3(ballX, 0, ballZ)).nor().scl(-0.7f)));
            modelBatch.begin(CAMERA);
            Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
            modelBatch.render(arrowInstance, environment);
            modelBatch.end();
        }
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
}