package com.mygdx.game.courses;

import com.mygdx.game.Player;
import com.mygdx.game.utils.Vector2d;

import java.util.ArrayList;

public class GameInfo {

    public ArrayList<Player> players;
    public double gravity;
    public double ballMass;
    public double friction;
    public double maxVelocity;
    public double tol;
    public double startX;
    public double startY;
    public double goalX;
    public double goalY;
    public double sandFriciton;
    private String sandFunction;
    private String heightFunction;

    public FractalInfo fractalInfo;


    public GameInfo(ArrayList<Player> players, double gravity, double ballMass,double friction, double maxVelocity,
                    double tol, double startX, double startY, double goalX,double goalY,String heightFunction, double sandFriction,String sandFunction)
    {
        this.players=players;
        this.gravity=gravity;
        this.ballMass= ballMass;
        this.friction=friction;
        this.maxVelocity=maxVelocity;
        this.tol=tol;
        this.startX=startX;
        this.startY=startY;
        this.goalX=goalX;
        this.goalY=goalY;
        this.heightFunction= heightFunction;
        this.sandFriciton=sandFriction;
        this.sandFunction=sandFunction;

    }

    public double getGravity(){
        return gravity;
    }

    public double getMassofBall(){
        return ballMass;
    }

    public double getFriction(){
        return friction;
    }

    public double getMaxV(){
        return maxVelocity;
    }

    public double getTolerance(){
        return tol;
    }

    public double getStartX(){
        return startX;
    }

    public double getStartY(){
        return startY;
    }
    public Vector2d getStart(){
        return new Vector2d(startX,startY);
    }
    public Vector2d getGoal(){
        return new Vector2d(goalX,goalY);
    }
    public double getGoalX(){
        return goalX;
    }
    public double getGoalY(){
        return goalY;
    }

    public String getSandFunction(){ return sandFunction; }
    public double getSandFriction(){ return sandFriciton; }

    @Deprecated
    /** should not be used for any other purpose than initializing the world, and thus is not supposed to be public */
    public String getHeightFunction(){
        return heightFunction;
    }

    public String toString(){
        String res="Game with Players :"+ players+"\nGravity:"+gravity+"\nballMass:"+ballMass+"\nBall Friction:"+
                friction+"\nMaximumVelocity:"+maxVelocity+"\nHole tolerance:"+tol+"\nStart(x,y):("+startX+","+startY+
                ")\nGoal(x,y):("+goalX+","+goalY+")\nHeight Function:"+heightFunction+"\nSand Function: "+sandFunction+"\nSandFriction: "+sandFriciton;
        return res;
    }
}
