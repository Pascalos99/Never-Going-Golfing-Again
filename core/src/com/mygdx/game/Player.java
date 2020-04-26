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

    private double pitch = PITCH;
    private double yaw = YAW;
    private double view_zoom = VIEW_ZOOM;

    public Player(String name,int id, String color){
        this.name = name;
        this.id=id;
        this.ballColor=color;
        shots=0;
    }

    public void loadCamera() {
        PITCH = pitch;
        YAW = yaw;
        VIEW_ZOOM = view_zoom;
    }

    public void saveCamera() {
        pitch = PITCH;
        yaw = YAW;
        view_zoom = VIEW_ZOOM;
    }

    public abstract void notifyStartOfTurn();

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
        double xzLen = Math.cos(PITCH);
        double _x = xzLen * Math.cos(YAW);
        double _y = Math.sin(PITCH);
        double _z = xzLen * Math.sin(-YAW);

        _x *= VIEW_ZOOM;
        _y *= VIEW_ZOOM;
        _z *= VIEW_ZOOM;

        Vector3d add = ball.getPosition();
        Vector3 pre = new Vector3((float)_x, (float)_y, (float)_z);

        return pre.add(new Vector3((float) add.get_x(), (float) add.get_y(), (float) add.get_z()));
    }

    public int getId(){
        return id;
    }

    public String toString(){
        return "["+id+"] "+name+" ("+ballColor+")";
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

        public void notifyStartOfTurn() {}

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

        private double velocity_inching_bound = SHOT_VELOCITY_INCREASE() * 2;

        private AI_controller bot;
        private double desired_shot_velocity;
        private double desired_shot_angle;
        private boolean adjusted_speed;
        private boolean turn_over;

        public Bot(String name, int id, String color, AI_controller bot) {
            super(name, id, color);
            this.bot = bot;
        }

        public String getBotName() {
            return bot.getName();
        }
        public AI_controller getAI() {
            return bot;
        }

        public void notifyStartOfTurn() {
            bot.calculate(this);
            adjusted_speed = false;
            desired_shot_velocity = bot.getShotVelocity();
            if (desired_shot_velocity > GAME_ASPECTS.maxVelocity) desired_shot_velocity = GAME_ASPECTS.maxVelocity;
            if (desired_shot_velocity < 0) desired_shot_velocity = 0;
            desired_shot_angle = bot.getShotAngle();
            while (desired_shot_angle > Math.PI) desired_shot_angle -= Math.PI * 2;
            while (desired_shot_angle < -Math.PI) desired_shot_angle += Math.PI * 2;
            turn_over = false;
        }

        public boolean requestedHit(){
            if (turn_over) return false;
            if (Math.abs(getShotAngle() - desired_shot_angle) < AI_SHOT_ANGLE_BOUND)
                if (adjusted_speed) {
                    turn_over = true;
                    return true;
                }
            return false;
        }

        public boolean requestedTurnRight(){

            if(getBall().turn_state == TURN_STATE_WAIT){
                return Gdx.input.isKeyPressed(Input.Keys.RIGHT);
            }

            else {
                if (!turnRight(getShotAngle(), desired_shot_angle)) return false;
                else if (Math.abs(getShotAngle() - desired_shot_angle) > AI_SHOT_ANGLE_BOUND) return true;
                return false;
            }

        }

        public boolean requestedTurnLeft(){

            if(getBall().turn_state == TURN_STATE_WAIT){
                return Gdx.input.isKeyPressed(Input.Keys.LEFT);
            }

            else {
                if (turnRight(getShotAngle(), desired_shot_angle)) return false;
                else if (Math.abs(getShotAngle() - desired_shot_angle) > AI_SHOT_ANGLE_BOUND) return true;
                return false;
            }

        }

        private static boolean turnRight(double current_angle, double target_angle) {
            double diff = target_angle - current_angle;
            while (diff < 0) diff += Math.PI * 2;
            if (diff > Math.PI) return false;
            return true;
        }

        public boolean requestedZoomIn(){
            return Gdx.input.isKeyPressed(Input.Keys.W);
        }

        public boolean requestedZoomOut(){
            return Gdx.input.isKeyPressed(Input.Keys.S);
        }

        public boolean requestedIncreaseHitVelocity(){
            if (SHOT_VELOCITY > desired_shot_velocity) return false;
            if (Math.abs(SHOT_VELOCITY - desired_shot_velocity) > velocity_inching_bound) return true;
            SHOT_VELOCITY = desired_shot_velocity;
            adjusted_speed = true;
            return false;
        }

        public boolean requestedDecreaseHitVelocity(){
            if (SHOT_VELOCITY < desired_shot_velocity) return false;
            if (Math.abs(SHOT_VELOCITY - desired_shot_velocity) > velocity_inching_bound) return true;
            SHOT_VELOCITY = desired_shot_velocity;
            adjusted_speed = true;
            return false;
        }

        public boolean requestedReset(){
            if (getBall().isOnWater()) {
                turn_over = false;
                return true;
            }
            return false;
        }
    }

}