package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import static com.mygdx.game.utils.Variables.*;
public class ObstacleSelect implements Screen {
    private Stage stage;
    private Menu parent;

    public ObstacleSelect(Menu menu){
        parent=menu;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        Table overall= new Table();
        overall.setFillParent(true);
        overall.setBackground(MENU_BKG);
        stage.addActor(overall);



        TextButton play= play=new TextButton("PLAY", MENU_SKIN);

        play.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.changeScreen(Menu.PLAY);
            }
        });

        overall.add(play);

        TextButton backButton= new TextButton("BACK",MENU_SKIN);
        backButton.align(Align.bottomLeft);
        backButton.addListener(new ChangeListener(){

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.changeScreen(Menu.CUSTOM_GAME);
            }
        });



        stage.addActor(backButton);
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
