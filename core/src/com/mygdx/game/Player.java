package com.mygdx.game;

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

    public Vector3 getCameraPosition(PuttingCourse world){
        Vector2d view_vector = new Vector2d(view_zoom, 0).rotate(view_angle);
        Vector3 pre = new Vector3((float) view_vector.get_x(), (float) ball.getPosition(world).get_y() + 5f, (float) view_vector.get_y());

        Vector3d ball_pos = ball.getPosition(world);
        return pre.add(new Vector3((float) ball_pos.get_x(), (float) ball_pos.get_y(), (float) ball_pos.get_z()));
        //return cameraPosition;
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