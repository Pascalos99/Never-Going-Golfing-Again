package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static com.mygdx.game.Variables.*;

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
      //  addColorSelect();
       // addPlayerTypeSelect();
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
                String playerType="";
                SelectBox<String> color_select=new SelectBox<String>(MENU_SKIN);
                String ballColor=null;
               for(int i=0;i<playerTable.getCells().size;i++){
                   if(playerTable.getCells().get(i).getActor().getClass().equals(dummy.getClass())){
                       id = (Integer.parseInt(((Label)(playerTable.getCells().get(i).getActor())).getText().toString().replaceAll(" ","")));
                   }else if(playerTable.getCells().get(i).getActor().getClass().equals(color_select.getClass()) &&ballColor.equals("")){
                       ballColor=((SelectBox<String>)(playerTable.getCells().get(i).getActor())).getSelected();
                   }else if((playerTable.getCells().get(i).getActor().getClass().equals(color_select.getClass()))){
                       playerType=((SelectBox<String>)(playerTable.getCells().get(i).getActor())).getSelected();
                   }else{
                       name = ((TextField)(playerTable.getCells().get(i).getActor())).getText();
                   }

                   if((id!=-1)&&(!name.equals("")) &&(ballColor!=null) && (!playerType.equals(""))&&
                           (players.contains(new Player.Human(name,id,ballColor))||players.contains(new Player.bot(name,id,ballColor)))){
                       if(playerType.equals(BOT_TYPE[0])){
                            players.add(new Player.Human(name,id,ballColor));
                        } else if(playerType.equals(BOT_TYPE[1])){
                           players.add(new Player.Bot(name,id,ballColor));
                       }
                       id = -1;
                       name = "";
                       ballColor = null;
                       playerType="";
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
            Label id = new Label(playerNumber+" ", MENU_SKIN);
            TextField name = new TextField(pickName(), MENU_SKIN);
            playerTable.add(id);
            playerTable.add(name);
            playerTable.add(addColorSelect());
            playerTable.add(addPlayerTypeSelect());
            playerTable.row().pad(0, 0, 5, 0);
            ++playerNumber;
        }

    }
    private int next_color_selected =  BALL_COLORS.length-1;
    private SelectBox addColorSelect(){
        SelectBox<String> color_select = new SelectBox<>(MENU_SKIN);
        Array<String> items = new Array<>();
        for (int i=0; i < BALL_COLORS.length; i++)
            items.add(BALL_COLORS[i].name);
        color_select.setItems(items);
        color_select.setSelectedIndex(next_color_selected++);
        if (next_color_selected >= BALL_COLORS.length) next_color_selected = 0;
        return color_select;
    }

    private SelectBox addPlayerTypeSelect(){
        SelectBox<String> playerTypeSelect = new SelectBox<>(MENU_SKIN);
        Array<String> items = new Array<>();
        for (int i=0; i < BOT_TYPE.length; i++)
            items.add(BOT_TYPE[i]);
        playerTypeSelect.setItems(items);

        return playerTypeSelect;
    }

    private String pickName() {
        String name = "Player "+(playerNumber-1);
        HashSet<String> available_names = new HashSet<>(Arrays.asList(Variables.PLAYER_NAMES));
        for (int i=0; i < playerNumber-1; i++) {
            String playername = ((TextField) playerTable.getCells().get(i*3 + 1).getActor()).getText();
            available_names.remove(playername); }
        if (available_names.size() > 0) {
            int i = 0, item = (int)(Math.random() * available_names.size());
            for (String select : available_names) if (i++==item) name = select;
        }
        return name;
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
