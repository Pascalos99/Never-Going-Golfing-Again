package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Align;
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
    protected static int playerNumber=1;
    private ArrayList<Player> players =new ArrayList<Player>();
    protected static Table playerTable;
    private static String humanPlayer = "None";
    private Label bot_description;
    private ClickListener hover_listener;
   // private static boolean firstSelection=true;

    public PlayerScreen(Menu menu){
        parent=menu;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        Table buttons = new Table();
        Table overall= new Table();
        if(playerTable==null)
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

        playerTable.row().pad(0, 0, 5, 0);
        if(playerNumber==1)
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
                //TODO: if anyone can figure out a better way to turn the table to players, go for it
                int id = -1;
                String name = "";
                String playerType = "";
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
                playerTable.add(addColorSelect());
                playerTable.add(addPlayerTypeSelect());
                playerTable.add(addPlayerRemove(playerNumber - 1));
                playerTable.row().pad(0, 0, 5, 0);
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
        //playerTypeSelect.setSelectedIndex(0);
        return playerTypeSelect;
    }

    private ImageButton addPlayerRemove(int index) {
        Sprite sprite = new Sprite(CROSS);
        sprite.setSize(50f, 50f);
        Drawable draw = new SpriteDrawable(sprite);
        ImageButton button = new ImageButton(draw);
        // ImageButton button = new ImageButton(MENU_SKIN);
        button.addListener(new RemoveListener(index));
        return button;
    }

    private class RemoveListener extends ClickListener {
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
                playerTable.row().pad(0, 0, 5, 0);
                for (int j=0; j < cols; j++) playerTable.add(to_replace.get(k++));
                playerTable.row().pad(0, 0, 5, 0);
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
