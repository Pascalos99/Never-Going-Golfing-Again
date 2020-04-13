package com.mygdx.game;
import static com.mygdx.game.Variables.MENU_SKIN;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;

public class PlayerScreen implements Screen {
    private Menu parent;
    private Stage stage;
    private static int playerNumber=1;
    private ArrayList<Player> players =new ArrayList<Player>();
    private Table playerTable;
    public PlayerScreen(Menu menu){
        parent=menu;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        Table buttons = new Table();
        Table overall= new Table();
        playerTable = new Table();

       // buttons.setDebug(true);
        overall.setFillParent(true);
        //overall.setDebug(true);
        overall.add(playerTable);
        overall.row().pad(0, 0, 10, 0);
        overall.add(buttons);
        stage.addActor(overall);

        enterPlayer();
        TextButton addPlayer = new TextButton("Add Player", MENU_SKIN);
     //   TextButton deletePlayer =new TextButton("Delete Player", MenuSkin);
        TextButton chooseGameMode = new TextButton("Choose Game Mode", MENU_SKIN);
        buttons.add(addPlayer);
        buttons.row().padBottom(10);
        buttons.add(chooseGameMode).expandX();

        addPlayer.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                enterPlayer();
            }
        });
/*
        deletePlayer.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              //  players.remove();
            }
        });

*/
        chooseGameMode.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //TODO: if anyone can figure out a better way to turn the table to players, go for it
                Label dummy = new Label("AA" , MENU_SKIN);
                int id =-1;
                String name="";
               for(int i=0;i<playerTable.getCells().size;i++){
                   if(playerTable.getCells().get(i).getActor().getClass().equals(dummy.getClass())){
                       id = (Integer.parseInt(String.valueOf(((Label)(playerTable.getCells().get(i).getActor())).getText())));
                   }else{
                       name = ((TextField)(playerTable.getCells().get(i).getActor())).getText();
                   }
                   if((id!=-1)&&(!name.equals("")) &&(!players.contains(new Player(name,id)))){

                       players.add(new Player(name,id));
                        id=-1;
                        name="";
                   }
               }
                System.out.println(players.toString());
                parent.changeScreen(Menu.GAME_SELECT);
            }
        });



        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();

    }

    private void enterPlayer(){
        Label id = new Label(""+playerNumber, MENU_SKIN);
        TextField name = new TextField(" ", MENU_SKIN);
        playerTable.add(id);
        playerTable.add(name);
        playerTable.row().pad(0, 0, 5, 0);
        ++playerNumber;

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
        stage.dispose();
    }

    public ArrayList<Player> getPlayers(){
        return players;
    }

}
