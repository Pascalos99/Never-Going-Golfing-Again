package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

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
    public TextField inputVelocity;
    CrazyPutting game;
    public  GameScreen(Menu menu, GameInfo gameInfo) {
        parent = menu;
        gameAspects=gameInfo;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        //table
        Table table = new Table();
        //let it fill the window
        table.setFillParent(true);
       // table.setDebug(true);
        table.right().bottom();
        stage.addActor(table);

        generator.setPathPreference(true);
        if(gameAspects!=null){
          //  figure out how to amke function 2D
            course = new PuttingCourse(new AtomFunction2d(gameAspects.getHeightFunction()) ,Function2d.getConstant(gameAspects.getFriction()) ,size,size,gameAspects.getGoal(),gameAspects.getStart()
                    ,gameAspects.getTolerance(),gameAspects.getMaxV() ,gameAspects.getGravity());
        }else{
            course = generator.fractalGeneratedCourse(size, 1,0.8,0.7,size/200,3,9.81);
        }
        game =new CrazyPutting(course, gameAspects, this);
        game.create();
        currentPlayerShotNum=new Label("", Variables.MENU_SKIN);
        currentPlayerLabel = new Label("", Variables.MENU_SKIN);
        inputVelocity=new TextField(""+Variables.SHOT_VELOCITY,Variables.MENU_SKIN);
        Label inputVel= new Label("Initial Velocity: ",Variables.MENU_SKIN);
        table.row().pad(0, 0, 10, 0);
        table.add(currentPlayerLabel);
        table.add(currentPlayerShotNum);
        table.row().pad(0, 0, 10, 0);
        table.add(inputVel);
        table.add(inputVelocity);
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
        game.render();
        currentPlayerLabel.setText("CurrentPlayer : "+game.getCurrentPlayer().getName());
        currentPlayerShotNum.setText("Attempts: "+game.getCurrentPlayer().getshots());
        stage.act(delta);
        stage.draw();
        if(Gdx.input.isKeyPressed(Input.Keys.U)){
            parent.changeScreen(0);
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

    public double getInputVelocity(){

        double input = Double.parseDouble(inputVelocity.getText().replaceAll("\\s",""));
        if(input<=gameAspects.maxVelocity){
            return input;
        }else{
            inputVelocity.setText(""+gameAspects.maxVelocity);
            return gameAspects.maxVelocity;
        }

    }
}
