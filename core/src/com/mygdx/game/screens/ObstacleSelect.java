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
    MiniMapDrawer m;

    public ObstacleSelect(Menu menu){
        parent=menu;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        Table overall = new Table();
        overall.setFillParent(true);
        overall.setDebug(true);

        CourseBuilder cb = SettingsScreen.cb;
        cb.addDummy(1, 2);

        m = MiniMapDrawer.defaultDrawer(20, 20,30, Vector2d.ZERO.sub(WORLD_SHIFT));
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
        TextButton smallTree= new TextButton("Small Tree", MENU_SKIN);
        TextButton medTree= new TextButton("Medium Tree", MENU_SKIN);
        TextButton largeTree= new TextButton("Large Tree", MENU_SKIN);
        TextButton wall= new TextButton("Wall", MENU_SKIN);
        Table obstacleT= new Table();
        obstacleT.pad(10,10,0,10);
        obstacleT.add(smallTree);
        obstacleT.row().pad(10,10,0,10);
        obstacleT.add(medTree);
        obstacleT.row().pad(10,10,0,10);
        obstacleT.add(largeTree);
        obstacleT.row().pad(10,10,0,10);;
        obstacleT.add(wall);;
        obstacleT.row().pad(10,10,0,10);;

        smallTree.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

            }
        });

        medTree.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

            }
        });

        largeTree.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

            }
        });

        wall.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

            }
        });

        overall.add(obstacleT);

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
        txt.draw(m.getPixmap(),0,0);
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
