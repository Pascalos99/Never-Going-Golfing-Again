package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;

import static com.mygdx.game.Variables.*;

public class TerrainBuilder {

    private static ArrayList<Vector3> borderPoints1= new ArrayList<Vector3>();
    private static ArrayList<Vector3> borderPoints2= new ArrayList<Vector3>();
    private static ArrayList<Vector3> borderPoints3= new ArrayList<Vector3>();
    private static ArrayList<Vector3> borderPoints4= new ArrayList<Vector3>();
    private static float resolution = 0.2f;

    public static ModelInstance buildWater(){
        ModelBuilder modelBuilder = new ModelBuilder();

        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("grid", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new com.badlogic.gdx.graphics.g3d.Material(ColorAttribute.createDiffuse(new Color(0.2f, 0.2f, 1, 1f)), new BlendingAttribute(0.5f)));

        Vector3 pos1 = new Vector3(0,0,0);
        Vector3 pos2 = new Vector3(0,0,50);
        Vector3 pos3 = new Vector3(50,0,50);
        Vector3 pos4 = new Vector3(50,0,0);

        MeshPartBuilder.VertexInfo v1 = new MeshPartBuilder.VertexInfo().setPos(pos1).setNor(new Vector3(0,1,0)).setCol(null).setUV(0.5f, 0.0f);
        MeshPartBuilder.VertexInfo v2 = new MeshPartBuilder.VertexInfo().setPos(pos2).setNor(new Vector3(0,1,0)).setCol(null).setUV(0.0f, 0.0f);
        MeshPartBuilder.VertexInfo v3 = new MeshPartBuilder.VertexInfo().setPos(pos3).setNor(new Vector3(0,1,0)).setCol(null).setUV(0.0f, 0.5f);
        MeshPartBuilder.VertexInfo v4 = new MeshPartBuilder.VertexInfo().setPos(pos4).setNor(new Vector3(0,1,0)).setCol(null).setUV(0.5f, 0.5f);

        builder.rect(v1, v2, v3, v4);
        Model water = modelBuilder.end();
        ModelInstance waterInstance = new ModelInstance(water, 0, 0, 0);
        return waterInstance;
    }

    public static ModelInstance buildWalls(){
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part(
                "grid",
                GL20.GL_TRIANGLES,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new com.badlogic.gdx.graphics.g3d.Material(IntAttribute.createCullFace(GL20.GL_NONE),ColorAttribute.createDiffuse(Color.BROWN))
        );
        MeshPartBuilder.VertexInfo v1, v2, v3, v4;
        Vector3 nor1 = new Vector3(-1,0,0);

        for(int i=0;i<borderPoints1.size()-1;i++){
            Vector3 p1=new Vector3(borderPoints1.get(i).x,borderPoints1.get(i).y,borderPoints1.get(i).z);
            Vector3 p2=new Vector3(borderPoints1.get(i+1).x,borderPoints1.get(i+1).y,borderPoints1.get(i+1).z);

            v1 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p1.x,1+Math.max(p1.y,0),p1.z)).setNor(nor1).setCol(null).setUV(0.5f, 0.0f);
            v2 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p1.x,-10,p1.z)).setNor(nor1).setCol(null).setUV(0.0f, 0.0f);
            v4 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p2.x,1+Math.max(p2.y,0),p2.z)).setNor(nor1).setCol(null).setUV(0.0f, 0.5f);
            v3 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p2.x,-10,p2.z)).setNor(nor1).setCol(null).setUV(0.5f, 0.5f);

            builder.rect(v1, v2, v3, v4);
        }

        nor1=new Vector3(1,0,0);

        for(int i=0;i<borderPoints3.size()-1;i++){
            Vector3 p1=new Vector3(borderPoints3.get(i).x,borderPoints3.get(i).y,borderPoints3.get(i).z);
            Vector3 p2=new Vector3(borderPoints3.get(i+1).x,borderPoints3.get(i+1).y,borderPoints3.get(i+1).z);

            v2 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p1.x,1+Math.max(p1.y,0),p1.z)).setNor(nor1).setCol(null).setUV(0.5f, 0.0f);
            v1 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p1.x,-10,p1.z)).setNor(nor1).setCol(null).setUV(0.0f, 0.0f);
            v3 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p2.x,1+Math.max(p2.y,0),p2.z)).setNor(nor1).setCol(null).setUV(0.0f, 0.5f);
            v4 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p2.x,-10,p2.z)).setNor(nor1).setCol(null).setUV(0.5f, 0.5f);

            builder.rect(v1, v2, v3, v4);
        }

        nor1=new Vector3(0,0,-1);

        for(int i=0;i<borderPoints2.size()-1;i++){
            Vector3 p1=new Vector3(borderPoints2.get(i).x,borderPoints2.get(i).y,borderPoints2.get(i).z);
            Vector3 p2=new Vector3(borderPoints2.get(i+1).x,borderPoints2.get(i+1).y,borderPoints2.get(i+1).z);

            v2 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p1.x,1+Math.max(p1.y,0),p1.z)).setNor(nor1).setCol(null).setUV(0.5f, 0.0f);
            v1 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p1.x,-10,p1.z)).setNor(nor1).setCol(null).setUV(0.0f, 0.0f);
            v3 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p2.x,1+Math.max(p2.y,0),p2.z)).setNor(nor1).setCol(null).setUV(0.0f, 0.5f);
            v4 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p2.x,-10,p2.z)).setNor(nor1).setCol(null).setUV(0.5f, 0.5f);

            builder.rect(v1, v2, v3, v4);
        }

        nor1=new Vector3(0,0,1);

        for(int i=0;i<borderPoints4.size()-1;i++){
            Vector3 p1=new Vector3(borderPoints4.get(i).x,borderPoints4.get(i).y,borderPoints4.get(i).z);
            Vector3 p2=new Vector3(borderPoints4.get(i+1).x,borderPoints4.get(i+1).y,borderPoints4.get(i+1).z);

            v1 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p1.x,1+Math.max(p1.y,0),p1.z)).setNor(nor1).setCol(null).setUV(0.5f, 0.0f);
            v2 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p1.x,-10,p1.z)).setNor(nor1).setCol(null).setUV(0.0f, 0.0f);
            v4 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p2.x,1+Math.max(p2.y,0),p2.z)).setNor(nor1).setCol(null).setUV(0.0f, 0.5f);
            v3 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(p2.x,-10,p2.z)).setNor(nor1).setCol(null).setUV(0.5f, 0.5f);

            builder.rect(v1, v2, v3, v4);
        }

        nor1=new Vector3(0,-1,0);
        v2 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(0,-10,0)).setNor(nor1).setCol(null).setUV(0.5f, 0.0f);
        v1 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(0,-10,50)).setNor(nor1).setCol(null).setUV(0.0f, 0.0f);
        v4 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(50,-10,50)).setNor(nor1).setCol(null).setUV(0.0f, 0.5f);
        v3 = new MeshPartBuilder.VertexInfo().setPos(new Vector3(50,-10,0)).setNor(nor1).setCol(null).setUV(0.5f, 0.5f);

        builder.rect(v1, v2, v3, v4);

        Model wall = modelBuilder.end();
        ModelInstance wallInstance = new ModelInstance(wall, 0, 0, 0);
        return wallInstance;
    }

    public static ModelInstance[] buildTerrain() {
        Vector3 pos1, pos2, pos3, pos4;
        Vector3 nor1, nor2, nor3, nor4;
        Vector2d vec1, vec2, vec3, vec4;
        MeshPartBuilder.VertexInfo v1, v2, v3, v4;
        Model rect;
        ModelInstance[] terrainInstance = new ModelInstance[25];
        ModelBuilder modelBuilder = new ModelBuilder();
        MeshPartBuilder builder;
        AtomFunction2d func = new AtomFunction2d(FunctionParser.parse("sin(x)+cos(y)"));
        double y_scalar = WORLD_SCALING;
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
                if(WIREFRAME){
                    builder = modelBuilder.part("grid", GL20.GL_LINES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(new Color(0.2f, 1f, 0.2f, 1f))));
                } else {
                    builder = modelBuilder.part("grid", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(new Color(0.2f, 1f, 0.2f, 1f))));
                }

                float x, y = 0;

                for (int i = 0; i < gridWidth; i++) {

                    for (int k = 0; k < gridDepth; k++) {

                        pos1 = new Vector3(i*resolution, (float) (func.evaluate((i+(a*gridWidth)) * gw, (k+(b*gridDepth)) * gd) * y_scalar), k*resolution);
                        pos2 = new Vector3(i*resolution, (float) (func.evaluate((i+(a*gridWidth)) * gw, (k+1+(b*gridDepth)) * gd) * y_scalar), (k + 1)*resolution);
                        pos3 = new Vector3((i + 1)*resolution, (float) (func.evaluate((i+1+(a*gridWidth)) * gw, (k+1+(b*gridDepth)) * gd) * y_scalar), (k + 1)*resolution);
                        pos4 = new Vector3((i + 1)*resolution, (float) (func.evaluate((i+1+(a*gridWidth)) * gw, (k+(b*gridDepth)) * gd) * y_scalar), k*resolution);

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

                        v1 = new MeshPartBuilder.VertexInfo().setPos(pos1).setNor(nor1).setCol(null).setUV(0.5f, 0.0f);
                        v2 = new MeshPartBuilder.VertexInfo().setPos(pos2).setNor(nor2).setCol(null).setUV(0.0f, 0.0f);
                        v3 = new MeshPartBuilder.VertexInfo().setPos(pos3).setNor(nor3).setCol(null).setUV(0.0f, 0.5f);
                        v4 = new MeshPartBuilder.VertexInfo().setPos(pos4).setNor(nor4).setCol(null).setUV(0.5f, 0.5f);

                        builder.rect(v1, v2, v3, v4);
                        builder.line(pos1,pos1.add(nor1));
                    }
                }

                rect = modelBuilder.end();
                terrainInstance[a * 5 + b] = new ModelInstance(rect, a * 10, 0, b * 10);
            }
        }

        return terrainInstance;
    }

}
