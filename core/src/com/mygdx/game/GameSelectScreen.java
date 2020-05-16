package com.mygdx.game;

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

import static com.mygdx.game.Variables.MENU_BKG;
import static com.mygdx.game.Variables.MENU_SKIN;

public class GameSelectScreen implements Screen {
    private Menu parent;
    private Stage stage;
    public GameSelectScreen(Menu menu){
        parent =menu;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        Table table = new Table();
        table.setFillParent(true);
        //table.setDebug(true);
        stage.addActor(table);
        table.setBackground(MENU_BKG);
        TextButton defGame = new TextButton("Default Game", Variables.MENU_SKIN);
        TextButton customGame = new TextButton("Custom Game", Variables.MENU_SKIN);
        TextButton randomGame = new TextButton("Random Game", Variables.MENU_SKIN);
        TextButton backButton= new TextButton("BACK",MENU_SKIN);
        backButton.align(Align.bottomLeft);
        backButton.addListener(new ChangeListener(){

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.changeScreen(Menu.PLAYER_SELECT);
            }
        });
        table.row().pad(10, 0, 10, 0);
        table.add(defGame).fillX().uniformX();
        table.row().pad(0, 0, 10, 0);
        table.add(customGame).fillX().uniformX();
        table.row().pad(0, 0, 10, 0);
        table.add(randomGame).fillX().uniformX();

        defGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.changeScreen(Menu.DEFAULT_GAME);
            }

        });

        customGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.changeScreen(Menu.CUSTOM_GAME);
            }

        });

        randomGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.changeScreen(Menu.RANDOM_GAME);
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
    public void resize(int width, int height) {

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
