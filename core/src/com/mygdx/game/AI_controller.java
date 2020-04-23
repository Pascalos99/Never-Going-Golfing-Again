package com.mygdx.game;

public interface AI_controller {

    public abstract boolean requestedHit();

    public abstract boolean requestedTurnRight();

    public abstract boolean requestedTurnLeft();

    public abstract boolean requestedZoomIn();

    public abstract boolean requestedZoomOut();

    public abstract boolean requestedIncreaseHitVelocity();

    public abstract boolean requestedDecreaseHitVelocity();

    public abstract boolean requestedReset();

}
