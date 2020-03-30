package com.mygdx.game;

public class Player {
    private String name;
    private int id;
    private int shots;
    private int score;
    private Ball ball;
    //private camerax cameraY

    public Player(String name,int id){
        this.name = name;
        this.id=id;
        shots=0;
        score=0;
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
