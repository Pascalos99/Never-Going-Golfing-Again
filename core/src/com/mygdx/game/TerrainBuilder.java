package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.PropertiesUtils;
import com.mygdx.game.courses.CourseBuilder;
import com.mygdx.game.courses.GameInfo;
import com.mygdx.game.courses.PuttingCourse;
import com.mygdx.game.parser.AtomFunction2d;
import com.mygdx.game.parser.Function2d;
import com.mygdx.game.utils.Vector2d;

import java.util.ArrayList;
import java.util.function.Function;

import static com.mygdx.game.utils.Variables.*;

public class TerrainBuilder {

    private static ArrayList<Vector3> borderPoints1 = new ArrayList<Vector3>();
    private static ArrayList<Vector3> borderPoints2 = new ArrayList<Vector3>();
    private static ArrayList<Vector3> borderPoints3 = new ArrayList<Vector3>();
    private static ArrayList<Vector3> borderPoints4 = new ArrayList<Vector3>();
    private static float scalingFactor = GRAPHICS_SCALING;
    private static float resolution = 0.2f * scalingFactor;

    public ModelInstance[] initFlag(PuttingCourse course) {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("grid", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new com.badlogic.gdx.graphics.g3d.Material(ColorAttribute.createDiffuse(Color.BROWN)));
        builder.cylinder(0.1f, FLAGPOLE_HEIGHT, 0.1f, 30);
        Model pole = modelBuilder.end();
        modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        builder = modelBuilder.part("grid", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new com.badlogic.gdx.graphics.g3d.Material(ColorAttribute.createDiffuse(Color.RED)));
        builder.box(0.5f, 3.1f, 0, 1f, 0.7f, 0.03f);
        Model flag = modelBuilder.end();
        ModelInstance poleInstance = new ModelInstance(pole, (float) course.flag_position.get_x() * WORLD_SCALING, (float) (WORLD_SCALING * course.getHeightAt(course.flag_position.get_x(), course.flag_position.get_y())), (float) course.flag_position.get_y() * WORLD_SCALING);
        ModelInstance flagInstance = new ModelInstance(flag, (float) course.flag_position.get_x() * WORLD_SCALING, (float) (WORLD_SCALING * course.getHeightAt(course.flag_position.get_x(), course.flag_position.get_y())), (float) course.flag_position.get_y() * WORLD_SCALING);
        float side = (float) (2 * GAME_ASPECTS.getTolerance()) * WORLD_SCALING;
        Model poleRange = modelBuilder.createCylinder(side, FLAGPOLE_HEIGHT, side, 40, new Material(ColorAttribute.createDiffuse(new Color(1, 0.4f, 1, 1f)), new BlendingAttribute(0.3f)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        ModelInstance flagRangeInstance = new ModelInstance(poleRange, (float) course.flag_position.get_x() * WORLD_SCALING, (float) (WORLD_SCALING * course.getHeightAt(course.flag_position.get_x(), (float) course.flag_position.get_y())), (float) course.flag_position.get_y() * WORLD_SCALING);
        ModelInstance[] flagInstances = new ModelInstance[]{poleInstance, flagInstance, flagRangeInstance};
        return flagInstances;
    }

    public Sprite initWater(Texture waterTexture) {
        Sprite waterSprite = new Sprite(waterTexture);
        waterSprite.setColor(0, 0, 1, 0.3f);
        waterSprite.setOrigin(0, 0);
        waterSprite.setPosition(0, 0);
        waterSprite.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        return waterSprite;
    }

    public Sprite initSky(Texture waterTexture) {
        Sprite skySprite = new Sprite(waterTexture);
        skySprite.setColor(1, 1, 1, 0.2f);
        skySprite.setOrigin(0, 0);
        skySprite.setPosition(0, 0);
        skySprite.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        return skySprite;
    }

    public static ModelInstance buildWater() {
        ModelBuilder modelBuilder = new ModelBuilder();

        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("grid", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new com.badlogic.gdx.graphics.g3d.Material(ColorAttribute.createDiffuse(new Color(0.2f, 0.2f, 1, 1f)), new BlendingAttribute(0.5f)));

        Vector3 pos1 = new Vector3(0, 0, 0);
        Vector3 pos2 = new Vector3(0, 0, 50 * scalingFactor);
        Vector3 pos3 = new Vector3(50 * scalingFactor, 0, 50 * scalingFactor);
        Vector3 pos4 = new Vector3(50 * scalingFactor, 0, 0);

        MeshPartBuilder.VertexInfo v1 = new MeshPartBuilder.VertexInfo().setPos(pos1).setNor(new Vector3(0, 1, 0)).setCol(null).setUV(0.5f, 0.0f);
        MeshPartBuilder.VertexInfo v2 = new MeshPartBuilder.VertexInfo().setPos(pos2).setNor(new Vector3(0, 1, 0)).setCol(null).setUV(0.0f, 0.0f);
        MeshPartBuilder.VertexInfo v3 = new MeshPartBuilder.VertexInfo().setPos(pos3).setNor(new Vector3(0, 1, 0)).setCol(null).setUV(0.0f, 0.5f);
        MeshPartBuilder.VertexInfo v4 = new MeshPartBuilder.VertexInfo().setPos(pos4).setNor(new Vector3(0, 1, 0)).setCol(null).setUV(0.5f, 0.5f);

        builder.rect(v1, v2, v3, v4);
        Model water = modelBuilder.end();
        ModelInstance waterInstance = new ModelInstance(water, 0, 0, 0);
        return waterInstance;
    }

    public static ModelInstance buildWalls() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part(
                "grid",
                GL20.GL_TRIANGLES,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new com.badlogic.gdx.graphics.g3d.Material(IntAttribute.createCullFace(GL20.GL_NONE), ColorAttribute.createDiffuse(Color.BROWN))
        );
        MeshPartBuilder.VertexInfo v1, v2, v3, v4;
        Vector3 nor1 = new Vector3(-1, 0, 0);

        for (int i = 0; i < borderPoints1.size() - 1; i++) {
            Vector3 p1 = new Vector3(borderPoints1.get(i).x, borderPoints1.get(i).y, borderPoints1.get(i).z);
            Vector3 p2 = new Vector3(borderPoints1.get(i + 1).x, borderPoints1.get(i + 1).y, borderPoints1.get(i + 1).z);

            v1 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p1.x, 1 + Math.max(p1.y, 0), p1.z)).setNor(nor1).setCol(null).setUV(0.5f, 0.0f);
            v2 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p1.x, -grid_spacing * scalingFactor, p1.z)).setNor(nor1).setCol(null).setUV(0.0f, 0.0f);
            v4 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p2.x, 1 + Math.max(p2.y, 0), p2.z)).setNor(nor1).setCol(null).setUV(0.0f, 0.5f);
            v3 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p2.x, -grid_spacing * scalingFactor, p2.z)).setNor(nor1).setCol(null).setUV(0.5f, 0.5f);

            builder.rect(v1, v2, v3, v4);
        }

        nor1 = new Vector3(1, 0, 0);

        for (int i = 0; i < borderPoints3.size() - 1; i++) {
            Vector3 p1 = new Vector3(borderPoints3.get(i).x, borderPoints3.get(i).y, borderPoints3.get(i).z);
            Vector3 p2 = new Vector3(borderPoints3.get(i + 1).x, borderPoints3.get(i + 1).y, borderPoints3.get(i + 1).z);

            v2 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p1.x, 1 + Math.max(p1.y, 0), p1.z)).setNor(nor1).setCol(null).setUV(0.5f, 0.0f);
            v1 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p1.x, -grid_spacing * scalingFactor, p1.z)).setNor(nor1).setCol(null).setUV(0.0f, 0.0f);
            v3 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p2.x, 1 + Math.max(p2.y, 0), p2.z)).setNor(nor1).setCol(null).setUV(0.0f, 0.5f);
            v4 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p2.x, -grid_spacing * scalingFactor, p2.z)).setNor(nor1).setCol(null).setUV(0.5f, 0.5f);

            builder.rect(v1, v2, v3, v4);
        }

        nor1 = new Vector3(0, 0, -1);

        for (int i = 0; i < borderPoints2.size() - 1; i++) {
            Vector3 p1 = new Vector3(borderPoints2.get(i).x, borderPoints2.get(i).y, borderPoints2.get(i).z);
            Vector3 p2 = new Vector3(borderPoints2.get(i + 1).x, borderPoints2.get(i + 1).y, borderPoints2.get(i + 1).z);

            v2 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p1.x, 1 + Math.max(p1.y, 0), p1.z)).setNor(nor1).setCol(null).setUV(0.5f, 0.0f);
            v1 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p1.x, -grid_spacing * scalingFactor, p1.z)).setNor(nor1).setCol(null).setUV(0.0f, 0.0f);
            v3 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p2.x, 1 + Math.max(p2.y, 0), p2.z)).setNor(nor1).setCol(null).setUV(0.0f, 0.5f);
            v4 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p2.x, -grid_spacing * scalingFactor, p2.z)).setNor(nor1).setCol(null).setUV(0.5f, 0.5f);

            builder.rect(v1, v2, v3, v4);
        }

        nor1 = new Vector3(0, 0, 1);

        for (int i = 0; i < borderPoints4.size() - 1; i++) {
            Vector3 p1 = new Vector3(borderPoints4.get(i).x, borderPoints4.get(i).y, borderPoints4.get(i).z);
            Vector3 p2 = new Vector3(borderPoints4.get(i + 1).x, borderPoints4.get(i + 1).y, borderPoints4.get(i + 1).z);

            v1 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p1.x, 1 + Math.max(p1.y, 0), p1.z)).setNor(nor1).setCol(null).setUV(0.5f, 0.0f);
            v2 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p1.x, -grid_spacing * scalingFactor, p1.z)).setNor(nor1).setCol(null).setUV(0.0f, 0.0f);
            v4 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p2.x, 1 + Math.max(p2.y, 0), p2.z)).setNor(nor1).setCol(null).setUV(0.0f, 0.5f);
            v3 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p2.x, -grid_spacing * scalingFactor, p2.z)).setNor(nor1).setCol(null).setUV(0.5f, 0.5f);

            builder.rect(v1, v2, v3, v4);
        }

        nor1 = new Vector3(0, -1, 0);
        v2 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(0, -grid_spacing * scalingFactor, 0)).setNor(nor1).setCol(null).setUV(0.5f, 0.0f);
        v1 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(0, -grid_spacing * scalingFactor, gridDepth * scalingFactor)).setNor(nor1).setCol(null).setUV(0.0f, 0.0f);
        v4 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(gridWidth * scalingFactor, -grid_spacing * scalingFactor, gridDepth * scalingFactor)).setNor(nor1).setCol(null).setUV(0.0f, 0.5f);
        v3 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(gridWidth * scalingFactor, -grid_spacing * scalingFactor, 0)).setNor(nor1).setCol(null).setUV(0.5f, 0.5f);

        builder.rect(v1, v2, v3, v4);

        Model wall = modelBuilder.end();
        ModelInstance wallInstance = new ModelInstance(wall, 0, 0, 0);
        return wallInstance;
    }

    // I am basically increasing the number of squares and decreasing the amount of triangles per square
    // this is not a perfect solution, because it gets really slow for larger numbers than this

    public static int gridWidth = 5; // was 50
    public static int gridDepth = 5; // was 50

    public static int bigWidth = 50; // was 5
    public static int bigDepth = 50; // was 5

    public static float grid_spacing = 100f / (bigDepth + bigWidth);

    public static boolean sand_grid = true;

    public static ModelInstance[] buildTerrain() {
        Vector3 pos1, pos2, pos3, pos4;
        Vector3 nor1, nor2, nor3, nor4;
        Vector2d vec1, vec2, vec3, vec4;
        MeshPartBuilder.VertexInfo v1, v2, v3, v4;
        Model rect;
        ModelInstance[] terrainInstance = new ModelInstance[bigWidth * bigDepth];
        ModelBuilder modelBuilder = new ModelBuilder();
        MeshPartBuilder builder;

        Function2d func = new AtomFunction2d("sin(x)+cos(y)");

        Function<Vector2d, Boolean> sand = v -> WORLD.isSandAt(v.get_x(), v.get_y());
        // TODO maybe you can use this,
        //  usage:
        //  if (sand.apply(new Vector2d(x, y))) then it is sand at the physics position (x,y) in [0, 20] x [0, 20]

        Color grass_color = new Color(0.2f, 1f, 0.2f, 1f);
        Color sand_color = new Color(0.9f, 1f, 0.4f, 1f);

        double y_scalar = WORLD_SCALING;

        try {
            func = WORLD.height_function;
        } catch (Error e) {
            System.out.println(e);
        }

        Texture terrainTexture = new Texture(Gdx.files.internal("tx.png"));

        for (int a = 0; a < bigWidth; a++) {
            for (int b = 0; b < bigDepth; b++) {
                float gw = (float) BOUNDED_WORLD_SIZE / (gridWidth * (float) bigWidth);
                float gd = (float) BOUNDED_WORLD_SIZE / (gridDepth * (float) bigDepth);
                modelBuilder.begin();
                builder = modelBuilder.part("grid", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, new Material(TextureAttribute.createDiffuse(terrainTexture)));

                //                builder = modelBuilder.part("grid", GL20.GL_LINES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(new Color(0.2f, 1f, 0.2f, 1f))));
                float x, y = 0;

                for (int i = 0; i < gridWidth; i++) {
                    for (int k = 0; k < gridDepth; k++) {

                        /*if (sand_grid && sand.apply(new Vector2d((i + (a * gridWidth)) * gw, (k + (b * gridDepth)) * gd)))
                            color = sand_color;
                        else color = grass_color;*/

                        float posx1 = (i + (a * gridWidth)) * gw;
                        float posy1 = (k + (b * gridDepth)) * gd;
                        pos1 = new Vector3(i * resolution, (float) (func.evaluate(posx1, posy1) * y_scalar), k * resolution);
                        float posx2 = (i + (a * gridWidth)) * gw;
                        float posy2 = (k + 1 + (b * gridDepth)) * gd;
                        pos2 = new Vector3(i * resolution, (float) (func.evaluate(posx2, posy2) * y_scalar), (k + 1) * resolution);
                        float posx3 = (i + 1 + (a * gridWidth)) * gw;
                        float posy3 = (k + 1 + (b * gridDepth)) * gd;
                        pos3 = new Vector3((i + 1) * resolution, (float) (func.evaluate(posx3, posy3) * y_scalar), (k + 1) * resolution);
                        float posx4 = (i + 1 + (a * gridWidth)) * gw;
                        float posy4 = (k + (b * gridDepth)) * gd;
                        pos4 = new Vector3((i + 1) * resolution, (float) (func.evaluate(posx4, posy4) * y_scalar), k * resolution);

                        if (i == 0 && a == 0) {
                            borderPoints1.add(new Vector3(pos1.x + a * grid_spacing * scalingFactor, pos1.y, pos1.z + b * grid_spacing * scalingFactor));
                            if (k == gridDepth - 1 && b == bigDepth - 1)
                                borderPoints1.add(new Vector3(pos2.x + a * grid_spacing * scalingFactor, pos2.y, pos2.z + b * grid_spacing * scalingFactor));
                        }
                        if (k == 0 && b == 0) {
                            borderPoints2.add(new Vector3(pos1.x + a * grid_spacing * scalingFactor, pos1.y, pos1.z + b * grid_spacing * scalingFactor));
                            if (i == gridDepth - 1 && a == bigWidth - 1)
                                borderPoints2.add(new Vector3(pos4.x + a * grid_spacing * scalingFactor, pos4.y, pos4.z + b * grid_spacing * scalingFactor));
                        }
                        if (i == gridWidth - 1 && a == bigWidth - 1) {
                            borderPoints3.add(new Vector3(pos3.x + a * grid_spacing * scalingFactor, pos3.y, pos3.z + b * grid_spacing * scalingFactor));
                            if (k == 0 && b == 0)
                                borderPoints3.add(new Vector3(pos4.x + a * grid_spacing * scalingFactor, pos4.y, pos4.z + b * grid_spacing * scalingFactor));
                        }
                        if (k == gridDepth - 1 && b == bigDepth - 1) {
                            borderPoints4.add(new Vector3(pos3.x + a * grid_spacing * scalingFactor, pos3.y, pos3.z + b * grid_spacing * scalingFactor));
                            if (i == 0 && a == 0)
                                borderPoints4.add(new Vector3(pos2.x + a * grid_spacing * scalingFactor, pos2.y, pos2.z + b * grid_spacing * scalingFactor));
                        }

                        float d = (float) (1.0f / y_scalar);

                        vec1 = new Vector2d((i + (a * gridWidth)) * gw, (k + (b * gridDepth)) * gd);
                        nor1 = new Vector3(0, (float) (func.gradient(vec1).get_y()), 1).crs(new Vector3(1, (float) (func.gradient(vec1).get_x()), 0));
                        nor1.nor();

                        vec2 = new Vector2d((i + (a * gridWidth)) * gw, (k + 1 + (b * gridDepth)) * gd);
                        nor2 = new Vector3(0, (float) (func.gradient(vec2).get_y()), 1).crs(new Vector3(1, (float) (func.gradient(vec2).get_x()), 0));
                        nor2.nor();

                        vec3 = new Vector2d((i + 1 + (a * gridWidth)) * gw, (k + 1 + (b * gridDepth)) * gd);
                        nor3 = new Vector3(0, (float) (func.gradient(vec3).get_y()), 1).crs(new Vector3(1, (float) (func.gradient(vec3).get_x()), 0));
                        nor3.nor();

                        vec4 = new Vector2d((i + 1 + (a * gridWidth)) * gw, (k + (b * gridDepth)) * gd);
                        nor4 = new Vector3(0, (float) (func.gradient(vec4).get_y()), 1).crs(new Vector3(1, (float) (func.gradient(vec4).get_x()), 0));
                        nor4.nor();

                        float difference = (float) (WORLD.getSandZeroFunction().evaluate(posx1, posy1));
                        if (difference > 0.5f) difference = 0.5f;
                        else if (difference < -0.5f) difference = -0.5f;
                        difference+=0.5f;
                        v1 = new MeshPartBuilder.VertexInfo().setPos(pos1).setNor(nor1).setCol(null).setUV(difference, 1.0f);
                        difference = (float) (WORLD.getSandZeroFunction().evaluate(posx2, posy2));
                        if (difference > 0.5f) difference = 0.5f;
                        else if (difference < -0.5f) difference = -0.5f;
                        difference+=0.5f;
                        v2 = new MeshPartBuilder.VertexInfo().setPos(pos2).setNor(nor2).setCol(null).setUV(difference, 1.0f);
                        difference = (float) (WORLD.getSandZeroFunction().evaluate(posx3, posy3));
                        if (difference > 0.5f) difference = 0.5f;
                        else if (difference < -0.5f) difference = -0.5f;
                        difference+=0.5f;
                        v3 = new MeshPartBuilder.VertexInfo().setPos(pos3).setNor(nor3).setCol(null).setUV(difference, 1.0f);
                        difference = (float) (WORLD.getSandZeroFunction().evaluate(posx4, posy4));
                        if (difference > 0.5f) difference = 0.5f;
                        else if (difference < -0.5f) difference = -0.5f;
                        difference+=0.5f;
                        v4 = new MeshPartBuilder.VertexInfo().setPos(pos4).setNor(nor4).setCol(null).setUV(difference, 1.0f);

                        builder.rect(v1, v2, v3, v4);
//                        builder.line(pos1,pos1.cpy().add(nor1.cpy().scl(0.4f)));
                    }
                }

                rect = modelBuilder.end();
                terrainInstance[a * bigWidth + b] = new ModelInstance(rect, a * grid_spacing * scalingFactor, 0, b * grid_spacing * scalingFactor);
            }
        }

        return terrainInstance;
    }

    public ModelInstance initArrow() {
        ModelBuilder modelBuilder = new ModelBuilder();
        Model arrow = modelBuilder.createBox((float) ((2 * SHOT_VELOCITY) / MAX_SHOT_VELOCITY), 0.05f, 0.05f,
                new Material(new ColorAttribute(ColorAttribute.Emissive, Color.YELLOW)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );
        ModelInstance arrowInstance = new ModelInstance(arrow, 0, 0, 0);
        return arrowInstance;
    }
}
