package com.mygdx.game;

/**
 *  To make your own bot, steps:
 *  1. Make your own class implementing this interface, let's stick to the naming convention of "AI_*.java"
 *  2. implement getTypeName() with a UNIQUE name as return value that describes your bot in one to three words
 *  3. implement the calculation step of your bot
 *  4. implement the getters for shot angle and shot velocity
 *  5. go to Variables.java and add an instance of your bot to array AVAILABLE_BOTS,
 *      if you want to add multiple different instances of your bot to this list, make sure they will all return a
 *      <b>different</b> value for getTypeName(), otherwise only the first instance will be used at all times.
 */
public interface AI_controller {

    /** this string has to be UNIQUE for each class that implements AI_controller */
    String getName();

    /** Accurately describes this AI in one or two sentences */
    String getDescription();

    /** Gets ran at the start of your turn ONCE, the player parameter is the current player the bot is controlling during
     * its turn.<br> It can be used to get information on the ball
     *   (current position for instance would be new Vector2d(player.getBall().x, player.getBall().y) ) */
    void startCalculation(Player player);

    /**
     * @return {@code true} if calculation is completed and the bot is ready to return the correct shot angle and velocity upon request.
     *          this enables you to multi-thread in order to calculate, if you are not multi-threading, you will likely not need to
     *          do more than just always return {@code true}.
     */
    boolean finishedCalculation();

    /** @return the desired shot angle in radians (positive x is 2k rad, negative x is 2k + pi rad,
     *      positive y is 2k + pi/2 rad and negative y is 2k - pi/2 rad; here k is any member of Z,
     *      this basically means that <b>increasing</b> the angle will turn your direction
     *      <b>clockwise</b> in-game, but <b>counter-clockwise</b> in cartesian coordinates) */
    double getShotAngle();

    /** @return the desired shot velocity in m/s */
    double getShotVelocity();

}
