package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.courses.CourseBuilder;
import com.mygdx.game.courses.GameInfo;
import com.mygdx.game.parser.AtomFunction2d;
import com.mygdx.game.courses.IO_course_module;
import com.mygdx.game.utils.Variables;
import com.mygdx.game.utils.Vector2d;

import java.io.File;

import static com.mygdx.game.utils.Variables.*;

public class SettingsScreen implements Screen {
    private Menu parent;
    private Stage stage;

    private TextField gravity;
    private TextField ballMass;
    private TextField coefff;

    private TextField vMax;
    private TextField tolerance;

    private TextField startX;
    private TextField startY;

    private TextField goalX;
    private TextField goalY;

    private TextField height;
    private TextField sandFunction;
    private TextField sandFriciton;
    static CourseBuilder cb;

    IO_course_module io;

    public SettingsScreen(Menu menu){
        parent=menu;

        cb = new CourseBuilder();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        //tables
        Table table = new Table();
        table.setFillParent(true);
        table.setBackground(MENU_BKG);

        stage.addActor(table);

        //right hand side
        Table tabler = new Table();
        //left hand side
        Table tablel = new Table();
        TABLE_BKG.setColor(0,0,0,100);
        tablel.setBackground(TABLE_BKG);
        BLANK_BKG.setColor(0,0,0,100);
        tabler.setBackground(BLANK_BKG);
        table.setFillParent(true);

        TextButton play=new TextButton("PLAY", MENU_SKIN);
        TextButton save = new TextButton("Save course to file", MENU_SKIN);
        TextButton customizeObstacles = new TextButton("Customize Obstacles", MENU_SKIN);
        TextButton fractals = new TextButton("Generate Fractal Course", MENU_SKIN);
        TextButton load = new TextButton("Load course from file", MENU_SKIN);
        TextButton backButton= new TextButton("BACK",MENU_SKIN);
        backButton.align(Align.bottomLeft);
        backButton.addListener(new ChangeListener(){

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.changeScreen(Menu.GAME_SELECT);
            }
        });

        fractals.addListener(new ChangeListener(){

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                MAX_SHOT_VELOCITY = getMaxV();
                GameInfo g = getGameInfo();
                cb.loadInfo(g);
                parent.changeScreen(Menu.FRACTAL_SELECT);
            }
        });
        TextField savePath= new TextField("", MENU_SKIN);
        TextField loadPath =new TextField("", MENU_SKIN);

        customizeObstacles.addListener(new ChangeListener() {

               @Override
               public void changed(ChangeEvent event, Actor actor) {
                   MAX_SHOT_VELOCITY = getMaxV();
                   GAME_ASPECTS = getGameInfo();
                   cb.loadInfo(getGameInfo());
                   parent.changeScreen(Menu.CUSTOMIZE_OBSTACLES);
               }
       });
        save.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                try{
                    File f = new File(savePath.getText());
                    if (!f.createNewFile()) {
                        f.delete();
                        f.createNewFile();
                    }
                    setCoords();
                    io.outputFile(f, getGravity(),getMassofBall(),getFrictionc(), getSandFriction(),getMaxV(),getTolerance(),
                            new Vector2d(start_x, start_y),new Vector2d(goal_x, goal_y), height.getText(), sandFunction.getText());

                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        });
        load.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                File f = new File(loadPath.getText());
                if (!f.exists() && IO_course_module.isDefaultCourseName(loadPath.getText()))
                    f = new File(IO_course_module.default_courses_path + loadPath.getText());
                if (f.exists()) {
                    io = new IO_course_module(f.getPath());
                    gravity.setText("" + io.getGravity());
                    ballMass.setText("" + io.getMassofBall());
                    coefff.setText(("" + io.getFrictionc()));
                    sandFriciton.setText("" + io.getSandFrictionc());
                    vMax.setText(""+io.getMaxV());
                    tolerance.setText(""+io.getTolerance());
                    startX.setText(""+io.getStartX());
                    startY.setText(""+io.getStartY());
                    goalX.setText(""+io.getGoalX());
                    goalY.setText(""+io.getGoalY());
                    height.setText(""+io.getHeightFunction());
                    sandFunction.setText("" + io.getSandFunction());
                    cb.addObstacles(io.getObstacles());
                }
            }
        });

        play.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                MAX_SHOT_VELOCITY = getMaxV();
                GameInfo g = getGameInfo();
                cb.loadInfo(g);
                parent.changeScreen(Menu.PLAY);
            }
        });

        gravity = new TextField("9.81", MENU_SKIN);
        Label g =new Label("Gravity in m/s^2: ", MENU_SKIN);
        ballMass = new TextField("45.93", MENU_SKIN);
        Label bm =new Label("Mass of Ball (grams): ", MENU_SKIN);
        coefff = new TextField("0.131", MENU_SKIN);
        Label cf =new Label("Coefficient of friction: ", MENU_SKIN);

        vMax = new TextField(""+MAX_SHOT_VELOCITY, MENU_SKIN);
        Label vm =new Label("Maximum Velocity (m/s): ", MENU_SKIN);
        tolerance = new TextField("0.1", MENU_SKIN);
        Label t =new Label("Hole tolerance: ", MENU_SKIN);

        startX = new TextField("0.01", MENU_SKIN);
        Label sX =new Label("Start X: ", MENU_SKIN);
        startY = new TextField("0.01", MENU_SKIN);
        Label sY =new Label("Start Y: ", MENU_SKIN);

        goalX = new TextField("0", MENU_SKIN);
        Label gX =new Label("Goal X: ", MENU_SKIN);
        goalY = new TextField("10", MENU_SKIN);
        Label gY =new Label("Goal Y: ", MENU_SKIN);

        height = new TextField("-0.01x+0.003x^2+0.04y", MENU_SKIN);
        Label h =new Label("Height function: ", MENU_SKIN);

        sandFunction= new TextField("sin(x)+cos(y)", MENU_SKIN);
        Label sf= new Label("Sand function: ", MENU_SKIN);


        sandFriciton = new TextField(""+ DEFAULT_SAND_FRICTION, MENU_SKIN);
        Label sandCoeff =new Label("Coefficient of friction for sand: ", MENU_SKIN);
        int margine =10;
       /* tablel.row().pad(margine, 0, 0, 0);
        tabler.row().pad(margine, 0, 0, 0);
*/
        Table sett =new Table();
        sett.row().pad(90,10,0,10);
        sett.add(tabler,tablel);
        table.add(sett);
        tablel.padRight(margine);
        tablel.padLeft(margine);
        tabler.padRight(margine);
        tabler.padLeft(margine);
        addToTable(tabler, g,gravity,margine,0,margine,0);
        addToTable(tabler, bm,ballMass,0,0,margine,0);
        addToTable(tabler,cf,coefff,0,0,margine,0);
        addToTable(tabler,vm,vMax,0,0,margine,0);
        addToTable(tabler,t,tolerance,0,0,margine,0);
        addToTable(tabler,sandCoeff,sandFriciton,margine,0,margine,0);
        addToTable(tablel,sX,startX,margine,0,margine,0);
        addToTable(tablel,sY,startY,0,0,margine,0);
        addToTable(tablel,gX,goalX,0,0,margine,0);
        addToTable(tablel,gY,goalY,0,0,margine,0);
        addToTable(tablel,h,height,0,0,margine,0);
        addToTable(tablel,sf,sandFunction,0,0,margine,0);


        Table fileConfig = new Table();
        Table buttons = new Table();
        buttons.row().pad(20,0,10,10);
        buttons.add(play).minWidth(customizeObstacles.getPrefWidth());
        buttons.add(customizeObstacles);
        table.row().pad(0, 0, 10, 0);
        table.add(buttons);
        table.row().pad(0, 0, 10, 0);
        table.add(fractals);
        table.row().pad(0,0,10,0);
        fileConfig.row().pad(0, 5, 10, 5);
        fileConfig.add(save).minWidth(load.getPrefWidth()).maxHeight(savePath.getPrefHeight());
        fileConfig.add(savePath);
        fileConfig.row().pad(0, 5, 10, 5);
        fileConfig.add(load).maxHeight(loadPath.getPrefHeight());
        fileConfig.add(loadPath);
        table.add(fileConfig);


        stage.addActor(backButton);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    public static SettingsScreen randomGame(Menu menu) {
        SettingsScreen scr = new SettingsScreen(menu);
        String func = AtomFunction2d.randomPolynomial(System.currentTimeMillis());
        scr.height.setText(func);
        System.out.println("random function = "+func);
        return scr;
    }

    @Override
    public void show() {

    }

    private void addToTable(Table table,Label label, Widget field, int top,int left,int bottom,int right) {
        table.add(label).expand();
        table.add(field).expand();
        table.row().pad(top, left, bottom, right);
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

    //getters

    public double getGravity(){
        return Double.parseDouble(gravity.getText());
    }

    public double getMassofBall(){
        return Double.parseDouble(ballMass.getText());
    }

    public double getFrictionc(){
        return Double.parseDouble(coefff.getText());
    }

    public double getMaxV(){
        return Double.parseDouble(vMax.getText());
    }

    public double getTolerance(){
        return Double.parseDouble(tolerance.getText());
    }


    private double start_x, start_y, goal_x, goal_y;
    private boolean calculated_coords = false;

    private void setCoords() {
        if (calculated_coords) return;
        start_x = Double.parseDouble(startX.getText());
        start_y = Double.parseDouble(startY.getText());
        goal_x = Double.parseDouble(goalX.getText());
        goal_y = Double.parseDouble(goalY.getText());
        calculated_coords = true;
    }

    public double getStartX(){
        shiftCalculation();
        return start_x;
    }

    public double getStartY(){
        shiftCalculation();
        return start_y;
    }

    public double getGoalX(){
        shiftCalculation();
        return goal_x;
    }
    public double getGoalY(){
        shiftCalculation();
        return goal_y;
    }

    public String getHeightFunction() {
        shiftCalculation();
        return height.getText();
    }

    public String getSandFunction(){
        shiftCalculation();
        return sandFunction.getText();
    }

    public double getSandFriction(){
        return Double.parseDouble(sandFriciton.getText());
    }

    private boolean calculated_world_shift = false;

    private void shiftCalculation() {
        setCoords();
        if (!calculated_world_shift) setWorldShift(start_x, start_y, goal_x, goal_y, BOUNDED_WORLD_SIZE);
    }

    public void setWorldShift(double start_x, double start_y, double goal_x, double goal_y, double world_range) {
        double delta_x = Math.abs(start_x - goal_x);
        double delta_y = Math.abs(start_y - goal_y);
        double min_x = Math.min(start_x, goal_x);
        double min_y = Math.min(start_y, goal_y);
        double new_min_x = world_range/2 - delta_x/2;
        double new_min_y = world_range/2 - delta_y/2;
        WORLD_SHIFT = new Vector2d(new_min_x - min_x, new_min_y - min_y);
        calculated_world_shift = true;
    }


    public GameInfo getGameInfo(){
        return new GameInfo(parent.players.getPlayers(),getGravity(),getMassofBall(),
                getFrictionc(),getMaxV(),getTolerance(),getStartX(),
                getStartY(),getGoalX(),getGoalY(),getHeightFunction(),
                getSandFriction(),getSandFunction());
    }
    public static class ColorSelection {
        public String name;
        public Color color;
        public ColorSelection(String name, Color color) {
            this.name = name;
            this.color = color;
        }
    }

}
