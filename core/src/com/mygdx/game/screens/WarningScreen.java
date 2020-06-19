package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.courses.GameInfo;

import static com.mygdx.game.utils.Variables.*;

public class WarningScreen implements Screen {
    private Menu parent;
    private Stage stage;
    private Label msg;

    public WarningScreen(Menu menu){
        this.parent=menu;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Table table = new Table();
        table.setFillParent(true);
        table.setBackground(MENU_BKG);
        stage.addActor(table);

        Table overall = new Table();
        table.add(overall);


        msg= new Label("EITHER THE GOAL OR THE FLAG ARE IN WATER",MENU_SKIN);
        overall.add(msg);

        TextButton playAnyway=new TextButton("Play Anyway",MENU_SKIN);
        TextButton obstacleSelect=new TextButton("Fix Via Obstacle", MENU_SKIN);
        playAnyway.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                parent.changeScreen(Menu.PLAY);
            }
        });
        obstacleSelect.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                parent.changeScreen(Menu.CUSTOMIZE_OBSTACLES);
            }
        });
        Table buttons= new Table();
        buttons.add(obstacleSelect).pad(30,0,0,30);
        buttons.add(playAnyway).pad(30,30,0,0);

        overall.row().pad(10,0,10,0);
        overall.add(buttons);



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
