package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import static com.mygdx.game.Variables.*;

public abstract class Player {
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

    public abstract boolean requestedHit();

    public abstract boolean requestedTurnRight();

    public abstract boolean requestedTurnLeft();

    public abstract boolean requestedZoomIn();

    public abstract boolean requestedZoomOut();

    public abstract boolean requestedIncreaseHitVelocity();

    public abstract boolean requestedDecreaseHitVelocity();

    public abstract boolean requestedReset();

    static class Human extends Player {

        public Human(String name, int id, String color) {
            super(name, id, color);
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

    static class Bot extends Player {

        private AI_controller bot;

        public Bot(String name, int id, String color, AI_controller bot) {
            super(name, id, color);
            this.bot = bot;
        }

        public String getBotName() {
            return bot.toString();
        }
        public AI_controller getAI() {
            return bot;
        }

        public boolean requestedHit(){
            return bot.requestedHit();
        }

        public boolean requestedTurnRight(){
            return bot.requestedTurnRight();
        }

        public boolean requestedTurnLeft(){
            return bot.requestedTurnLeft();
        }

        public boolean requestedZoomIn(){
            return bot.requestedZoomIn();
        }

        public boolean requestedZoomOut(){
            return bot.requestedZoomOut();
        }

        public boolean requestedIncreaseHitVelocity(){
            return bot.requestedIncreaseHitVelocity();
        }

        public boolean requestedDecreaseHitVelocity(){
            return bot.requestedDecreaseHitVelocity();
        }

        public boolean requestedReset(){
            return bot.requestedReset();
        }
    }

}