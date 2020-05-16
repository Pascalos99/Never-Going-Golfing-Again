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

    private String heightFunction;


    public GameInfo(ArrayList<Player> players, double gravity, double ballMass,double friction, double maxVelocity,
                    double tol, double startX, double startY, double goalX,double goalY,String heightFunction)
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

    public  String getHeightFunction(){
        return heightFunction;
    }

    public String toString(){
        String res="Game with Players :"+ players+"\nGravity:"+gravity+"\nballMass:"+ballMass+"\nBall Friction:"+
                friction+"\nMaximumVelocity:"+maxVelocity+"\nHole tolerance:"+tol+"\nStart(x,y):("+startX+","+startY+
                ")\nGoal(x,y):("+goalX+","+goalY+")\nHeight Function:"+heightFunction;
        return res;
    }
}
