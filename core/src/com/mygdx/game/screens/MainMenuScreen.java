package com.mygdx.game.screens;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.physics.PhysicsSetting;
import com.mygdx.game.utils.ColorProof;
import com.mygdx.game.utils.Variables;

import static com.mygdx.game.utils.Variables.*;

public class MainMenuScreen implements Screen {
    private Menu parent;
    private static Stage stage;


    public MainMenuScreen(Menu menu) {
        parent = menu;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        Table table = new Table();
        Table phys = new Table();
        table.setFillParent(true);
        //  table.setDebug(true);
        stage.addActor(table);
        table.setBackground(MENU_BKG);
        SelectBox<PhysicsSetting> physicsSelect = new SelectBox<>(MENU_SKIN);
        physicsSelect.setItems(PhysicsSetting.values());
        physicsSelect.setSelected(CURRENT_PHYSICS_SETTING);
        TextButton chosePlayers = new TextButton("Add Players", Variables.MENU_SKIN);

        CheckBox color_blind_mode = new CheckBox("  Enable Color-blind Mode",MENU_SKIN);
        color_blind_mode.setChecked(ColorProof.COLOR_BLIND_MODE);
        color_blind_mode.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                ColorProof.COLOR_BLIND_MODE = color_blind_mode.isChecked();
            }
        });

        TextButton exit = new TextButton("Exit", Variables.MENU_SKIN);

        int margin = 10;
        phys.row().pad(0, 0, 0, margin);
        phys.add(new Label("Select Physics", MENU_SKIN));

        phys.add(physicsSelect);
        table.add(phys);
        TABLE_BKG.setColor(0,0,0,100);
        phys.setBackground(TABLE_BKG);
        table.row().pad(margin, 0, 0, 0);
        table.add(color_blind_mode);
        table.row().pad(margin,0,0,0);
        table.add(chosePlayers).fillX().uniformX();
        table.row().pad(margin, 0, margin, 0);
        table.add(exit).fillX().uniformX();
        table.row().pad(0, 0, margin, 0);

        chosePlayers.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    CURRENT_PHYSICS_SETTING = ((SelectBox<PhysicsSetting>) phys.getCells().get(1).getActor()).getSelected();
                }catch(java.lang.IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                parent.changeScreen(Menu.PLAYER_SELECT);
            }

        });

        exit.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println("Exit");
                System.exit(0);
            }
        });
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();

    }

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
        stage.getViewport().update(width, height, true);
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
        stage.dispose();
    }
}
