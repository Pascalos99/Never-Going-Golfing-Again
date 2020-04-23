package com.mygdx.game;
import static com.mygdx.game.Variables.MENU_SKIN;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
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
        addColorSelect();
       // buttons.setDebug(true);
        overall.setFillParent(true);
        //overall.setDebug(true);
        overall.add(playerTable);
        overall.row().pad(0, 0, 10, 0);
        overall.add(buttons);
        stage.addActor(overall);

        enterPlayer();
        TextButton addPlayer = new TextButton("Add Player", MENU_SKIN);
        //TextButton deletePlayer =new TextButton("Delete Player", MENU_SKIN);
        TextButton chooseGameMode = new TextButton("Choose Game Mode", MENU_SKIN);
        buttons.add(addPlayer);
       // buttons.add(deletePlayer);
        buttons.row().padBottom(10);
        buttons.add(chooseGameMode).expandX();

        addPlayer.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                enterPlayer();
            }
        });

        /*deletePlayer.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                playerTable.getCells().get(playerTable.getCells().size-1).getActor().remove();
                playerTable.getCells().get(playerTable.getCells().size-2).getActor().remove();
                --playerNumber;
            }
        });*/


        chooseGameMode.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //TODO: if anyone can figure out a better way to turn the table to players, go for it
                Label dummy = new Label("AA" , MENU_SKIN);
                int id=-1;
                String name="";
                SelectBox<String> color_select=new SelectBox<String>(MENU_SKIN);
                String ballColor=null;
               for(int i=0;i<playerTable.getCells().size;i++){
                   if(playerTable.getCells().get(i).getActor().getClass().equals(dummy.getClass())){
                       id = (Integer.parseInt(String.valueOf(((Label)(playerTable.getCells().get(i).getActor())).getText())));
                   }else if(playerTable.getCells().get(i).getActor().getClass().equals(color_select.getClass())){
                       ballColor=((SelectBox<String>)(playerTable.getCells().get(i).getActor())).getSelected();
                   }else{
                       name = ((TextField)(playerTable.getCells().get(i).getActor())).getText();
                   }
                   if((id!=-1)&&(!name.equals("")) &&(ballColor!=null)&&(!players.contains(new Player.Human(name,id,ballColor)))){
                       players.add(new Player.Human(name,id,ballColor));
                        id=-1;
                        name="";
                        ballColor=null;
                   }
               }
                System.out.println(players.toString());
                parent.changeScreen(Menu.GAME_SELECT);
            }
        });



        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();

    }

    private void enterPlayer() {
        if (playerNumber <= 8){
            Label id = new Label("" + playerNumber, MENU_SKIN);
            TextField name = new TextField(" ", MENU_SKIN);
            playerTable.add(id);
            playerTable.add(name);
            playerTable.add(addColorSelect());
            playerTable.row().pad(0, 0, 5, 0);
            ++playerNumber;
        }

    }
    private SelectBox addColorSelect(){
        SelectBox<String> color_select = new SelectBox<String>(MENU_SKIN);
        Array<String> items = new Array<String>();
        for (int i=0; i < Variables.BALL_COLORS.length; i++)
            items.add(Variables.BALL_COLORS[i].name);
        color_select.setItems(items);
        return color_select;
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
