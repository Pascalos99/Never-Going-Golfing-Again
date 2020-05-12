package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import static com.mygdx.game.Variables.AVAILABLE_BOTS;
import static com.mygdx.game.Variables.MENU_SKIN;

public class GameScreen implements Screen {

    private Menu parent;
    private GameInfo gameAspects;
    private Stage stage;
    static PuttingCourseGenerator generator = new PuttingCourseGenerator(System.currentTimeMillis());
    static int size = 55;
    static double hole_tolerance = 10;
    static double max_speed = 20;
    static double gravity = 9.812;
    static PuttingCourse course = generator.randomCourse(size, hole_tolerance, max_speed, gravity);
    Label currentPlayerLabel;
    Label currentPlayerShotNum;
    Label currentAction;
    public TextField inputVelocity;
    CrazyPutting game;
    public boolean allowNextTurn=true;
    public boolean endGame=false;
    private final String inAction= "P to force end\nW & S to zoom in/out\nUP & DOWN to increase/decrease\nLEFT & RIGHT rotate camera";
    private final String inWater="R is your only option";
    public ArrayList<Player> winners;
    private ArrayList<Label> playerLabels=new ArrayList<>();
    private Table playerOverview;
    public  GameScreen(Menu menu, GameInfo gameInfo) {
        parent = menu;
        gameAspects=gameInfo;
        winners= new ArrayList<>();
        stage = new Stage(new ScreenViewport());
        for(Player p:gameAspects.players)
            playerLabels.add(new Label(p.getName(),MENU_SKIN));

        Gdx.input.setInputProcessor(stage);
        //table
        Table table = new Table();
        playerOverview=new Table();
        playerOverview.left().top();
        playerOverview.setDebug(false); // debug
        //let it fill the window
        table.setFillParent(true);
        table.setDebug(false); // debug
        table.right().bottom();
        stage.addActor(table);
        stage.addActor(playerOverview);
        playerOverview.setFillParent(true);
        playerOverview.add(new Label("Name", Variables.QUANTUM_SKIN));
        playerOverview.add(new Label("Shots", Variables.QUANTUM_SKIN));
        playerOverview.row();
        //add All Players
        for(Label player : playerLabels){
            playerOverview.add(player);
            playerOverview.add(new Label("0", MENU_SKIN));
            playerOverview.row();
        }

        generator.setPathPreference(true);
        if(gameAspects!=null){
          //  figure out how to make function 2D
            course = new PuttingCourse(new AtomFunction2d(gameAspects.getHeightFunction()) ,Function2d.getConstant(gameAspects.getFriction()) ,size,size,gameAspects.getGoal(),gameAspects.getStart()
                    ,gameAspects.getTolerance(),gameAspects.getMaxV() ,gameAspects.getGravity());
        }else{
            course = generator.fractalGeneratedCourse(size, 1,0.8,0.7,size/200,3,9.81);
        }
        game =new CrazyPutting(course, gameAspects, this);
        game.create();

        currentPlayerShotNum=new Label("", Variables.GLASSY);
        currentPlayerLabel = new Label("", Variables.GLASSY);
        currentAction= new Label("",Variables.GLASSY);
        currentAction.setAlignment(Align.bottomLeft);
        inputVelocity=new TextField(""+Variables.SHOT_VELOCITY,Variables.GLASSY);
        inputVelocity.setTextFieldListener((textField, c) -> {
            String input = inputVelocity.getText();
            if (input.matches("[0-9]*\\.*[0-9]+")) setInputVel(Double.parseDouble(input));
            else setInputVel(0.0);
        });
        stage.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y) {
                Rectangle2D rect = new Rectangle2D.Double(
                        inputVelocity.getX(), inputVelocity.getY(), inputVelocity.getWidth(), inputVelocity.getHeight());
                if (stage.getKeyboardFocus() == inputVelocity && !rect.contains(x, y)) {
                    stage.unfocusAll();
                }
            }
        });
        Label inputVel= new Label("Initial Velocity: ",Variables.GLASSY);
        table.row().pad(0, 0, 10, 0);
        table.add(currentPlayerLabel);
        table.add(currentPlayerShotNum);
        table.row().pad(0, 0, 10, 0);
        table.add(inputVel);
        table.add(inputVelocity);
        stage.addActor(currentAction);
        System.out.println("END");
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
        if(!endGame ) {
            game.render();
        } else {
            //endgame screen
            Table winList = new Table();
            winList.setFillParent(true);
            winList.center();
            stage.addActor(winList);
            for(int i=0;i<winners.size();i++){
                winList.add(new Label(winners.get(i).toString()+" "+ winners.get(i).getBall().hit_count+"pts", Variables.MENU_SKIN));
                winList.row();
            }
            TextButton replayButton= new TextButton("Play Again?",MENU_SKIN);
            replayButton.addListener(new ChangeListener(){
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    PlayerScreen.playerNumber=1;
                    PlayerScreen.playerTable=null;
                    parent.players=null;
                    for (AI_controller bot : AVAILABLE_BOTS) bot.clear();
                    parent.create();

                }
            });
            if(!gameAspects.players.isEmpty()){
                for(int i=0;i<gameAspects.players.size();i++) {
                    if (!winners.contains(gameAspects.players.get(i))){
                        winList.add(new Label(gameAspects.players.get(i).toString() + " " + gameAspects.players.get(i).getBall().hit_count + "pts", Variables.MENU_SKIN));
                        winList.row();
                    }
                }
            }
            winList.add(replayButton).padTop(30);
        }

        if(!game.getCurrentPlayer().getBall().isOnWater()){
            currentAction.setText(inAction);
        }else{
            currentAction.setText(inWater);
        }
        playerHighlight(game.getCurrentPlayer());
        currentPlayerLabel.setText("CurrentPlayer : "+game.getCurrentPlayer().getName());
        currentPlayerShotNum.setText("Attempts: "+game.getCurrentPlayer().getBall().hit_count);

        stage.act(delta);
        stage.draw();

    }

    private void playerHighlight(Player currentPlayer) {

        for(int i=0;i<playerOverview.getCells().size;i++){
            int cur= playerLabels.indexOf(((Label)playerOverview.getCells().get(i).getActor()));
            if(cur!=-1) {
                if (((Label) playerOverview.getCells().get(i).getActor()).getText().toString().equals(currentPlayer.getName())) {
                    ((Label) playerOverview.getCells().get(i + 1).getActor()).setText(game.getCurrentPlayer().getBall().hit_count);
                    playerLabels.get(cur).getStyle().fontColor = Color.GREEN;
                    ((Label) playerOverview.getCells().get(i + 1).getActor()).getStyle().fontColor = Color.GREEN;
                } else {
                    playerLabels.get(cur).getStyle().fontColor = Color.ORANGE;
                    ((Label) playerOverview.getCells().get(i + 1).getActor()).getStyle().fontColor = Color.ORANGE;
                }
            }
        }
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

    public static PuttingCourse getCourse(){
        return course;
    }

    public void setInputVel(double i){
        if (i > gameAspects.maxVelocity) i = gameAspects.maxVelocity;
        if (i < 0) i = 0;
        inputVelocity.setText(String.format("%.2f",i));
        Variables.SHOT_VELOCITY = i;
    }

    public double getInputVelocity(){
        String inputtxt =inputVelocity.getText();
        double input;
        try {
             input = Double.parseDouble(inputtxt.replaceAll("\\s", ""));
        }catch(NumberFormatException e) {
            String res= "";
            for(int i=0;i<inputtxt.length();i++){
                if(Character.isDigit(inputtxt.charAt(i)) ||inputtxt.charAt(i)=='.' ){
                   res=res+inputtxt.charAt(i);
                }
            }
            inputVelocity.setText(res);
            return Double.parseDouble(res);
        }
        if (input <= gameAspects.maxVelocity) {
            return input;
        } else {
            inputVelocity.setText("" + gameAspects.maxVelocity);
            return gameAspects.maxVelocity;
        }


    }
}
