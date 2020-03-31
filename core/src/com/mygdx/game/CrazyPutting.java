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
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class CrazyPutting implements ApplicationListener {
    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Model box;
    private Model shadow;
    private Model ball;
    private Model water;
    private PuttingCourse course;
    private static GameInfo game_aspects;
    private static float resolution = 0.2f;
    private float ballRadius = 0.2f;
    private static ModelInstance shadowInstance;
    private static ModelInstance [] rectInstance;
    private static ModelInstance waterInstance;
    private static ModelInstance wallInstance;
    private static ArrayList<Vector3> borderPoints1= new ArrayList<Vector3>();
    private static ArrayList<Vector3> borderPoints2= new ArrayList<Vector3>();
    private static ArrayList<Vector3> borderPoints3= new ArrayList<Vector3>();
    private static ArrayList<Vector3> borderPoints4= new ArrayList<Vector3>();
    private ModelInstance flagBoxInstance;
    private static ModelInstance [] boxInstance;
    private float x2=0f;
    private float y2=0f;

    private MeshPartBuilder meshPartBuilder;
    private Environment environment;
    private float speed = 5f;
    private float ballVelocity = 0f;

    private PhysicsEngine world_physics;
    private double previous_time;

    public PerspectiveCamera getCamera(){
        return camera;
    }

    private List<Player> players;

    public CrazyPutting( PuttingCourse c, GameInfo game_aspects){
        this.game_aspects = game_aspects;
        this.course=c;
        players = new ArrayList<Player>(game_aspects.players);
    }

    @Override
    public void create() {
        // Create camera sized to screens width/height with Field of View of 75 degrees
        camera = new PerspectiveCamera(
                75,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());

        // Move the camera 3 units back along the z-axis and look at the origin
        camera.position.set(-10f, 10f, -10f);
        camera.lookAt(0f, 0f, 0f);

        // Near and Far (plane) repesent the minimum and maximum ranges of the camera in, um, units
        camera.near = 1f;
        camera.far = 200.0f;

        // A ModelBatch is like a SpriteBatch, just for models.  Use it to batch up geometry for OpenGL
        modelBatch = new ModelBatch();

        // A ModelBuilder can be used to build meshes by hand
        ModelBuilder modelBuilder = new ModelBuilder();

        // It also has the handy ability to make certain premade shapes, like a Cube
        // We pass in a ColorAttribute, making our cubes diffuse ( aka, color ) red.
        // And let openGL know we are interested in the Position and Normal channels
        box = modelBuilder.createBox(1f, 1f, 1f,
                new Material(ColorAttribute.createDiffuse(Color.BLUE)),
                Usage.Position | Usage.Normal
        );

        ball = modelBuilder.createSphere(ballRadius * 2, ballRadius * 2, ballRadius * 2, 30, 30,
                new Material(ColorAttribute.createDiffuse(Color.YELLOW)),
                Usage.Position | Usage.Normal
        );

        shadow = modelBuilder.createBox(ballRadius * 2, 0.01f, ballRadius * 2,
                new Material(ColorAttribute.createDiffuse(Color.BLACK)),
                Usage.Position | Usage.Normal
        );

        rectInstance = buildTerrain();

        waterInstance = buildWater();
        wallInstance = buildWalls();
        flagBoxInstance = new ModelInstance(box,0,0, 0);

        // A model holds all of the information about an, um, model, such as vertex data and texture info
        // However, you need an instance to actually render it.  The instance contains all the
        // positioning information ( and more ).  Remember Model==heavy ModelInstance==Light

        world_physics = new PuttingCoursePhysics(course);
        previous_time = System.currentTimeMillis() / 1000.0;

        for(Player p : game_aspects.players){
            Vector2d start = game_aspects.getStart();
            double x = start.get_x();
            double y = start.get_y();
            ModelInstance model = new ModelInstance(ball, 0, 0, 0);
            Ball ball_obj = new Ball(PuttingCoursePhysics.BALL_SIZE, x, y, model);
            p.setBall(ball_obj);
            world_physics.addBody(ball_obj);
        }

        shadowInstance = new ModelInstance(shadow, 0, 0.5f, 0);

        // Finally we want some light, or we wont see our color.  The environment gets passed in during
        // the rendering process.  Create one, then create an Ambient ( non-positioned, non-directional ) light.
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.2f, 0.2f, 1.f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -1.0f, 1f));
    }

    public static ModelInstance buildWater(){
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

    public static ModelInstance buildWalls(){
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
//        MeshPartBuilder builder = modelBuilder.part("grid", GL20.GL_LINES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.BROWN)));
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

    public static ModelInstance[] buildTerrain() {
        Vector3 pos1, pos2, pos3, pos4;
        Vector3 nor1, nor2, nor3, nor4;
        Vector2d vec1, vec2, vec3, vec4;
        VertexInfo v1, v2, v3, v4;
        Model rect;
        ModelInstance[] rectInstance = new ModelInstance[25];
        ModelBuilder modelBuilder = new ModelBuilder();
        MeshPartBuilder builder;
        AtomFunction2d func = new AtomFunction2d(FunctionParser.parse("sin(x)+sin(y)"));
        try{
            func = new AtomFunction2d(FunctionParser.parse(game_aspects.getHeightFunction()));
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
                rectInstance[a * 5 + b] = new ModelInstance(rect, a * 10, 0, b * 10);
            }
        }
        return rectInstance;
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        box.dispose();
    }

    @Override
    public void render() {
        // You've seen all this before, just be sure to clear the GL_DEPTH_BUFFER_BIT when working in 3D
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        previous_time = world_physics.frameStep(previous_time);

        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
            float x = Gdx.input.getDeltaX();
            float y = Gdx.input.getDeltaY();
            camera.rotate(new Vector3(0,1,0),-x/3f);
            camera.rotate(new Vector3(-camera.direction.z,0,camera.direction.x),-y/3f);
        }

        if(Gdx.input.isButtonPressed(Input.Buttons.RIGHT)){
            for(Player p : game_aspects.players)
                p.getBall().velocity.add(new Vector2d(camera.direction.x,camera.direction.z));
        }

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            camera.translate(new Vector3(camera.direction.x * 0.7f, camera.direction.y * 0.7f, camera.direction.z * 0.7f));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            camera.translate(new Vector3(camera.direction.x * -0.7f, camera.direction.y * -0.7f, camera.direction.z * -0.7f));
        }

        if(Gdx.input.isKeyPressed(Input.Keys.R)){
            for(Player p : game_aspects.players){
                p.getBall().velocity=(new Vector2d(0,0));
                p.getBall().x=1;
                p.getBall().y=1;
            }
        }
        for(Player p : game_aspects.players) {
            if (p.getBall().x < 0){
                p.getBall().x=0;
                p.getBall().velocity = (new Vector2d(-p.getBall().velocity.get_x(), p.getBall().velocity.get_y()));
            }
            if (p.getBall().x >6.25){
                p.getBall().x=6.25;
                p.getBall().velocity = (new Vector2d(-p.getBall().velocity.get_x(), p.getBall().velocity.get_y()));
            }
            if (p.getBall().y<0){
                p.getBall().y=0;
                p.getBall().velocity = (new Vector2d(p.getBall().velocity.get_x(), -p.getBall().velocity.get_y()));
            }
            if (p.getBall().y >6.25){
                p.getBall().y=6.25;
                p.getBall().velocity = (new Vector2d(p.getBall().velocity.get_x(), -p.getBall().velocity.get_y()));
            }
        }

        /*x2+=Gdx.graphics.getDeltaTime()*5f;
        y2+=Gdx.graphics.getDeltaTime()*2f;
        if(x2>50)x2=0;
        if(y2>50)y2=0;
        flagBoxInstance.transform.set(new Vector3(x2,(float) course.getHeightAt(x2/8,y2/8),y2),new Quaternion(0,0,0,0));*/

        //shadowInstance.transform.setTranslation(new Vector3(-ballInstance.transform.getTranslation(new Vector3()).y, 0, ballInstance.transform.getTranslation(new Vector3()).y));

        // When you change the camera details, you need to call update();
        // Also note, you need to call update() at least once.
        camera.update();
        // Like spriteBatch, just with models!  pass in the box Instance and the environment

        modelBatch.begin(camera);
        //modelBatch.render(boxInstance, environment);

        for(Player p : game_aspects.players)
            modelBatch.render(p.getBall().getModel(course), environment);
        //modelBatch.render(shadowInstance, environment);
        for (int i = 0; i < 25; i++)
            modelBatch.render(rectInstance[i], environment);
        modelBatch.render(waterInstance, environment);
        modelBatch.render(wallInstance, environment);
        //modelBatch.render(flagBoxInstance,environment);
        modelBatch.end();
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