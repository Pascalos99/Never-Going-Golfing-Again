package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
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
    private static String humanPlayer = "None";
    private Label bot_description;
    private ClickListener hover_listener;

    public PlayerScreen(Menu menu){
        parent=menu;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        Table buttons = new Table();
        Table overall= new Table();
        playerTable = new Table();
        bot_description = new Label("", MENU_SKIN);
        addColorSelect();
       // buttons.setDebug(true);
        overall.setFillParent(true);
        //overall.setDebug(true);
        overall.add(bot_description).pad(0,0,20,0);
        overall.row().pad(0,0,10,0);
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
                Label dummy = new Label("AA", MENU_SKIN);
                int id = -1;
                String name = "";
                String playerType = "";
                SelectBox<String> color_select = new SelectBox<String>(MENU_SKIN);
                String ballColor = null;

                /*for(int i=0;i<playerTable.getRows();i++){
                    int correctedIndex =i*(playerTable.getColumns()-1);
                    id=(Integer.parseInt(((Label)(playerTable.getCells().get(correctedIndex).getActor())).getText().toString().replaceAll(" ","")));
                    name = ((TextField)(playerTable.getCells().get(correctedIndex+1).getActor())).getText();
                    ballColor =((SelectBox<String>) (playerTable.getCells().get(i).getActor())).getSelected();
                    playerType = ((SelectBox<String>) (playerTable.getCells().get(i).getActor())).getSelected();

                    if(playerType.equals(humanPlayer)){
                        players.add(new Player.Human(name,id,ballColor));
                    } else if(playerType.equals(AVAILABLE_BOTS[0].getTypeName())){
                        players.add(new Player.Bot(name,id,ballColor,AVAILABLE_BOTS[0]));
                    }
                }
              */

                for (int i = 0; i < playerTable.getCells().size; i++) {
                    if (playerTable.getCells().get(i).getActor().getClass().equals(dummy.getClass())) {
                        id = (Integer.parseInt(((Label) (playerTable.getCells().get(i).getActor())).getText().toString().replaceAll(" ", "")));
                    } else if (playerTable.getCells().get(i).getActor().getClass().equals(color_select.getClass())) {
                        if (ballColor == null) {
                            ballColor = ((SelectBox<String>) (playerTable.getCells().get(i).getActor())).getSelected();
                        } else {
                            playerType = ((SelectBox<String>) (playerTable.getCells().get(i).getActor())).getSelected();
                        }
                    } else {
                        name = ((TextField) (playerTable.getCells().get(i).getActor())).getText();
                    }
                    if ((id != -1) && (!name.equals("")) && (ballColor != null) && (!playerType.equals(""))) {
                        if (playerType.equals(humanPlayer)) {
                            players.add(new Player.Human(name, id, ballColor));
                        } else {
                            for (int k=0; k < AVAILABLE_BOTS.length; k++)
                                if (playerType.equals(AVAILABLE_BOTS[k].getName())) {
                                    players.add(new Player.Bot(name, id, ballColor, AVAILABLE_BOTS[k]));
                                    break;
                                }
                        }
                        id = -1;
                        name = "";
                        ballColor = null;
                        playerType = "";
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
            addHoverListener();
        }
    }

    private void addHoverListener() {
        if (hover_listener==null) {
            hover_listener = new ClickListener() {
                Timer timer;
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if (timer != null) timer.clear();
                    int index = 0;
                    for (int i=0; i < playerTable.getCells().size; i++)
                        if (playerTable.getCells().get(i).getActor().equals(event.getTarget())) {
                            index = i;
                            break; }
                    String botSetting = ((SelectBox<String>)playerTable.getCells().get((index / playerTable.getColumns()) * playerTable.getColumns() + 3).getActor()).getSelected();
                    String result = "";
                    if (botSetting.equals("None")) result = "A normal human player";
                    else
                        for (int i=0; i < AVAILABLE_BOTS.length; i++)
                            if (AVAILABLE_BOTS[i].getName().equals(botSetting)) {
                                result = AVAILABLE_BOTS[i].getDescription(); break; }
                    bot_description.setText(result);
                }
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    if (timer==null) timer = new Timer();
                    Timer.Task task = new Timer.Task() {
                      public void run() {
                          bot_description.setText("");
                      }
                    }; timer.scheduleTask(task, 0.5f);
                }
            };
        }
        for (Cell cell : playerTable.getCells()) cell.getActor().addListener(hover_listener);
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
        items.add(humanPlayer);
        for (int i=0; i < AVAILABLE_BOTS.length; i++)
            items.add(AVAILABLE_BOTS[i].getName());
        playerTypeSelect.setItems(items);

       // playerTypeSelect.setSelectedIndex(0);

        return playerTypeSelect;
    }

    private String pickName() {
        String name = "Player "+(playerNumber-1);
        HashSet<String> available_names = new HashSet<>(Arrays.asList(Variables.PLAYER_NAMES));
        for (int i=0; i < playerNumber-1; i++) {
            String playername = ((TextField) playerTable.getCells().get(i*playerTable.getColumns() + 1).getActor()).getText();
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
