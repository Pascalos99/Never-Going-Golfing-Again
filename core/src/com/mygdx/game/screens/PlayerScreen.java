package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.Player;
import com.mygdx.game.utils.ColorProof;
import com.mygdx.game.utils.Variables;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static com.mygdx.game.utils.Variables.*;

public class PlayerScreen implements Screen {
    private Menu parent;
    private Stage stage;
    protected static int playerNumber=1;
    private ArrayList<Player> players =new ArrayList<Player>();
    protected static Table playerTable;
    private static String humanPlayer = "None";
    private Label bot_description;
    private ClickListener hover_listener;

    private Texture cross;

    private static boolean last_color_blind_setting = false;

    public PlayerScreen(Menu menu){

        parent=menu;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        Table buttons = new Table();
        Table overall= new Table();
        overall.setBackground(MENU_BKG);
        if(playerTable==null || last_color_blind_setting != ColorProof.COLOR_BLIND_MODE) {
            playerTable = new Table();
            playerNumber = 1;
        }
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

        try {
            if (ColorProof.COLOR_BLIND_MODE) cross = new Texture(Gdx.files.internal("misc/cb-better_cross.png"));
            else cross = new Texture(Gdx.files.internal("misc/better_cross.png"));
            last_color_blind_setting = ColorProof.COLOR_BLIND_MODE;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("cross could not be loaded: \""+e.getMessage()+"\"");
        }

        playerTable.row();
        if(playerNumber==1)
            enterPlayer();
        TextButton addPlayer = new TextButton("Add Player", MENU_SKIN);
        TextButton chooseGameMode = new TextButton("Choose Game Mode", MENU_SKIN);
        buttons.add(addPlayer);
        buttons.row();
        buttons.add(chooseGameMode).pad(10,0,0,0).expandX();

        addPlayer.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                enterPlayer();
            }
        });

        TextButton backButton= new TextButton("BACK",MENU_SKIN);
        backButton.align(Align.bottomLeft);
        backButton.addListener(new ChangeListener(){

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.changeScreen(Menu.MAIN_MENU);
            }
        });

        chooseGameMode.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                int id = -1;
                String name = "";
                String playerType = "";
                String ballColor = null;

                for (int i = 0; i < playerTable.getCells().size; i++) {
                    if (playerTable.getCells().get(i).getActor() instanceof Label) {
                        id = (Integer.parseInt(((Label) (playerTable.getCells().get(i).getActor())).getText().toString().replaceAll(" ", "")));
                    } else if (playerTable.getCells().get(i).getActor() instanceof SelectBox) {
                        if (ballColor == null) {
                            ballColor = ((SelectBox<String>) (playerTable.getCells().get(i).getActor())).getSelected();
                        } else {
                            playerType = ((SelectBox<String>) (playerTable.getCells().get(i).getActor())).getSelected();
                        }
                    } else if (playerTable.getCells().get(i).getActor() instanceof TextField) {
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
        stage.addActor(backButton);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();


    }

    private void enterPlayer() {
        try {
            if (playerNumber <= 8) {
                Label id = new Label(playerNumber + " ", MENU_SKIN);
                TextField name = new TextField(pickName(), MENU_SKIN);
                playerTable.add(id);
                playerTable.add(name);
                playerTable.add(addColorSelect()).maxHeight(name.getPrefHeight()).padLeft(5);
                playerTable.add(addPlayerTypeSelect()).maxHeight(name.getPrefHeight()).padLeft(5);
                playerTable.add(addPlayerRemove(playerNumber - 1)).maxHeight(name.getPrefHeight());
                playerTable.row();
                ++playerNumber;
                addHoverListener();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            items.add(BALL_COLORS[i].getName());
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
        //playerTypeSelect.setSelectedIndex(0);
        return playerTypeSelect;
    }

    private ImageButton addPlayerRemove(int index) {
        Sprite sprite = new Sprite(cross);
        sprite.setSize(50f, 50f);
        Drawable draw = new SpriteDrawable(sprite);
        ImageButton button = new ImageButton(draw);
        // ImageButton button = new ImageButton(MENU_SKIN);
        button.addListener(new RemoveListener(index));
        return button;
    }

    private class RemoveListener extends ClickListener {
        TextField tf = new TextField("playername",MENU_SKIN);
        int index;
        public RemoveListener(int index) {
            this.index = index;
        }
        public void clicked(InputEvent event, float x, float y) {
            Array<Cell> cells = playerTable.getCells();
            ArrayList<Actor> to_replace = new ArrayList<>();
            for (Cell cell : cells) to_replace.add(cell.getActor());

            // updating the index number for each of the delete listeners below the current element:
            for (int i = (index + 1) * playerTable.getColumns(); i < cells.size; i++) {
                Actor actor = cells.get(i).getActor();
                if (actor instanceof Label) {
                    String txt = ((Label)actor).getText().toString();
                    int value = Integer.parseInt(txt.replaceAll(" ","")) - 1;
                    String mod = txt.replaceAll("[0-9]", value+"");
                    ((Label)actor).setText(mod);
                }
                Array<EventListener> listeners = actor.getListeners();
                for (EventListener listener : listeners)
                    if (listener instanceof RemoveListener)
                        ((RemoveListener)listener).index--;
            }
            // deleting all the parts belonging to the current element:
            for (int i = index * playerTable.getColumns(); i < (index + 1) * playerTable.getColumns(); i++) {
                to_replace.remove(cells.get(i).getActor());
                cells.get(i).getActor().remove();
            }
            // replacing the elements in the table with new versions of the same ones (bc for it didn't work
            //   just removing the cells from playerTable.getCells())
            int cols = playerTable.getColumns();
            playerTable.clear();
            int k = 0;
            // make sure to add in the row breaks at the right intervals!
            for (int i=0; i < to_replace.size()/cols; i++) {
                playerTable.row();
                for (int j=0; j < cols; j++) playerTable.add(to_replace.get(k++)).maxHeight(tf.getPrefHeight()).padLeft(5);;
                playerTable.row();
            }

            // return to previous screen if we suddenly have 0 players left
            playerNumber--;
            if (playerNumber <= 1)
                parent.changeScreen(Menu.MAIN_MENU);
        }
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
