package com.mygdx.game.bots;

import com.mygdx.game.Player;
import com.mygdx.game.PuttingCourse;

import static com.mygdx.game.Variables.*;

/**
 *  To make your own bot, steps:
 *  1. Make your own class extending this class, let's stick to the naming convention of "AI_*.java"
 *  2. implement getTypeName() with a UNIQUE name as return value that describes your bot in one to three words
 *  3. implement getDescription() with a short description of your bot (this will be shown when hovering over it in PlayerScreen)
 *  3. implement the calculation step of your bot
 *  4. implement the getters for shot angle and shot velocity OR use the setters of them *inside* your calculate() method
 *  5. go to Variables.java and add an instance of your bot to the array AVAILABLE_BOTS,<br>
 *      if you want to add multiple different instances of your bot to this list, make sure they will all return a
 *      <b>different</b> value for getTypeName(), otherwise only the first instance will be used at all times.
 */
public abstract class AI_controller {
    protected PuttingCourse world;

    AI_controller(PuttingCourse world){
        this.world = null;
    }

    AI_controller(){
        this(WORLD);
    }

    protected PuttingCourse getWorld(){

        if(world == null)
            return WORLD;

        return world;
    }

    /** this string has to be UNIQUE for each class that implements AI_controller */
    public abstract String getName();

    /** Accurately describes this AI in one or two sentences */
    public abstract String getDescription();

    /** Gets ran at the start of your turn ONCE, the player parameter is the current player the bot is controlling during
     * its turn.<br> It can be used to get information on the ball
     *   (current position for instance would be new Vector2d(player.getBall().x, player.getBall().y) ) */
    protected abstract void calculate(Player player);

    /** This method is ran when the game is restart at the end of the game. It should erase any game-specific information
     * that was previously stored in the AI after playing a game (unless this doesn't inhibit performance of coarse, in which
     * case you can leave this method empty)<br><br>
     * By default, this method call is empty. Overwrite it if you wish.*/
    public void clear() {}

    /** Use this method inside the calculate() method to set the shot angle of the next shot. This value will be queried after
     *  calculate() has finished (and therefore will just *update* the 'current' value until calculation is done, after which
     *  the last value will be used to make the actual shot) */
    protected void setShotAngle(double angle) { shotAngle = angle; }

    /** Use this method inside the calculate() method to set the shot velocity of the next shot. This value will be queried after
     *  calculate() has finished (and therefore will just *update* the 'current' value until calculation is done, after which
     *  the last value will be used to make the actual shot) */
    protected void setShotVelocity(double velocity) { shotVelocity = velocity; }

    /** @return the last value from setShotAngle() after calculate() has finished.
     *  You can overwrite this method if you so please to determine the shot angle of the shot manually */
    public double getShotAngle() { return shotAngle; }

    /** @return the last value from setShotVelocity() after calculate() has finished.
     *  You can overwrite this method if you so please to determine the shot velocity of the shot manually */
    public double getShotVelocity() { return shotVelocity; }

    /** the desired shot angle in radians (positive x is 2k rad, negative x is 2k + pi rad,
     *      positive y is 2k + pi/2 rad and negative y is 2k - pi/2 rad; here k is any member of Z,
     *      this basically means that <b>increasing</b> the angle will turn your direction
     *      <b>clockwise</b> in-game, but <b>counter-clockwise</b> in cartesian coordinates) */
    private double shotAngle;

    /** the desired shot velocity in m/s */
    private double shotVelocity;

    private boolean finished_calculation;

    public final void startCalculation(Player player) {
        finished_calculation = false;
        Thread t = new Thread(()->{
            calculate(player);
            finished_calculation = true;
        });
        t.start();
    }

    public final boolean finishedCalculation() { return finished_calculation; }

    public void clearAI(){
        //AIs that need to be notified at the end of a game must implement this.
    }

}
