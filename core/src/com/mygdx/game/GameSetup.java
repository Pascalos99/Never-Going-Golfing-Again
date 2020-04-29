package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import static com.mygdx.game.Variables.*;

public class GameSetup {
    public void initCamera(){
        CAMERA = new PerspectiveCamera(75,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());

        CAMERA.position.set(-5f, 5f, -5f);
        CAMERA.lookAt(0f, 0f, 0f);

        CAMERA.near = 0.1f;
        CAMERA.far = 200.0f;
    }


}
