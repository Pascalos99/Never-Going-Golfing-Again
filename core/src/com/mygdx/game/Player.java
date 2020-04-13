package com.mygdx.game;

import com.badlogic.gdx.math.Vector3;

public class Player {
    private String name;
    private int id;
    private int shots;
    private int score;
    private Ball ball;
    private Vector3 cameraPosition=new Vector3(-5f,5f,-5f);


    public Player(String name,int id){
        this.name = name;
        this.id=id;
        shots=0;
        score=0;
    }


    public int Vector3(){
        return shots;
    }

    public int getshots(){
        return shots;
    }
    public void setShots(int n){
        shots=n;
    }
    public int getScore(){
        return score;
    }
    public void addScore(int pts){
        score+=pts;
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
        return id + " "+name;
    }

    public Ball getBall(){
        return ball;
    }

    public void setBall(Ball ball){
        this.ball = ball;
    }

}
