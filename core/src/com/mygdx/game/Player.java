package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public class Player {
    private String name;
    private int id;
    private int shots;
    private String ballColor;
    private Ball ball;
    private Vector3 cameraPosition=new Vector3(-5f,5f,-5f);


    public Player(String name,int id, String color){
        this.name = name;
        this.id=id;
        this.ballColor=color;
        shots=0;
    }


    public int Vector3(){
        return shots;
    }

    public int getshots(){
        return shots;
    }

    public void newShot(){
        ++shots;
    }

    public void setCameraPosition(Vector3 pos){
        cameraPosition.x=pos.x;
        cameraPosition.y=pos.y;
        cameraPosition.z=pos.z;
    }

    public Vector3 getCameraPosition(){
        return cameraPosition;
    }

    public int getId(){
        return id;
    }

    public String toString(){
        return id + " "+name+" "+ballColor;
    }

    public Ball getBall(){
        return ball;
    }

    public void setBall(Ball ball){
        this.ball = ball;
    }
    public String getBallColor(){
        return ballColor;
    }

}