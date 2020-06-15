package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.courses.CourseBuilder;
import com.mygdx.game.courses.GameInfo;
import com.mygdx.game.courses.MiniMapDrawer;
import com.mygdx.game.courses.PuttingCourse;
import com.mygdx.game.utils.Vector2d;


import static com.mygdx.game.utils.Variables.*;

public class ObstacleSelect implements Screen {
    private Stage stage;
    private Menu parent;

    Texture txt;

    public ObstacleSelect(Menu menu){
        parent=menu;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        Table overall = new Table();
        overall.setFillParent(true);
        overall.setDebug(true);

        CourseBuilder cb = SettingsScreen.cb;
        MiniMapDrawer m = MiniMapDrawer.defaultDrawer(20,20,30,new Vector2d(0,0));
        m.draw(cb);
        txt= m.getTexture();

       Image map = new Image(txt);
        overall.add(map);


        Table buttons = new Table();
        buttons.align(Align.bottomLeft);


        TextButton play=new TextButton("PLAY", MENU_SKIN);
        play.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.changeScreen(Menu.PLAY);
            }
        });


        buttons.add(play);
        stage.addActor(overall);
        stage.addActor(buttons);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }



    @Override
    public void resize(int i, int i1) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
