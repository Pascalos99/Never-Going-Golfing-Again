package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import static com.mygdx.game.Variables.*;

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
    public String getName(){
        return name;
    }
    public void setCameraPosition(Vector3 pos){
        cameraPosition.x=pos.x;
        cameraPosition.y=pos.y;
        cameraPosition.z=pos.z;
    }

    public Vector3 getCameraPosition(){
        double xzLen = Math.cos(pitch);
        double _x = xzLen * Math.cos(yaw);
        double _y = Math.sin(pitch);
        double _z = xzLen * Math.sin(-yaw);

        _x *= view_zoom;
        _y *= view_zoom;
        _z *= view_zoom;

        Vector3d add = ball.getPosition();
        Vector3 pre = new Vector3((float)_x, (float)_y, (float)_z);

        return pre.add(new Vector3((float) add.get_x(), (float) add.get_y(), (float) add.get_z()));
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

    public boolean requestedHit(){
        return Gdx.input.isKeyPressed(Input.Keys.SPACE);
    }

    public boolean requestedTurnRight(){
        return Gdx.input.isKeyPressed(Input.Keys.RIGHT);
    }

    public boolean requestedTurnLeft(){
        return Gdx.input.isKeyPressed(Input.Keys.LEFT);
    }

    public boolean requestedZoomIn(){
        return Gdx.input.isKeyPressed(Input.Keys.W);
    }

    public boolean requestedZoomOut(){
        return Gdx.input.isKeyPressed(Input.Keys.S);
    }

    public boolean requestedIncreaseHitVelocity(){
        return Gdx.input.isKeyPressed(Input.Keys.UP);
    }

    public boolean requestedDecreaseHitVelocity(){
        return Gdx.input.isKeyPressed(Input.Keys.DOWN);
    }

    public boolean requestedReset(){
        return Gdx.input.isKeyPressed(Input.Keys.R);
    }

}