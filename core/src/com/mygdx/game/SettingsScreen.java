package com.mygdx.game;

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

import java.io.File;

import static com.mygdx.game.Variables.*;

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

    IO_course_module io;

    public SettingsScreen(Menu menu){
        parent=menu;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        //tables
        Table table = new Table();
        table.setFillParent(true);

        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
        //right hand side
        Table tabler = new Table();
        //left hand side
        Table tablel = new Table();

        table.setFillParent(true);
        table.add(tabler).expandY().expandX();
        table.add(tablel).expandY().expandX();
        TextButton play=new TextButton("PLAY", MENU_SKIN);
        TextButton input = new TextButton("Save course to file", MENU_SKIN);
        TextButton output = new TextButton("Load course from file", MENU_SKIN);
        TextButton backButton= new TextButton("BACK",MENU_SKIN);
        backButton.align(Align.bottomLeft);
        backButton.addListener(new ChangeListener(){

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.changeScreen(Menu.GAME_SELECT);
            }
        });
        table.add(play);
        TextField inputPath= new TextField("", MENU_SKIN);
        TextField outputPath =new TextField("", MENU_SKIN);

        input.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                File f = new File(inputPath.getText());
                if(f.exists()) {
                    io = new IO_course_module(inputPath.getText());
                    gravity.setText("" + io.getGravity());
                    ballMass.setText("" + io.getMassofBall());
                    coefff.setText(("" + io.getFrictionc()));
                    vMax.setText(""+io.getMaxV());
                    tolerance.setText(""+io.getTolerance());
                    startX.setText(""+io.getStartX());
                    startY.setText(""+io.getStartY());
                    goalX.setText(""+io.getGoalX());
                    goalY.setText(""+io.getGoalY());
                    height.setText(""+io.getHeightFunction());
                }

            }
        });
        output.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                try{
                    File f = new File(outputPath.getText());
                    if (!f.createNewFile()) {
                        f.delete();
                        f.createNewFile();
                    }
                    io.outputFile(f, getGravity(),getMassofBall(),getFrictionc(),getMaxV(),getTolerance(),
                            new Vector2d( getStartX(),getStartY()),new Vector2d(getGoalX(),getGoalY()), getHeightFunction());

                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        });
        play.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                MAX_SHOT_VELOCITY = getMaxV();
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
        tolerance = new TextField("0.5", MENU_SKIN);
        Label t =new Label("Hole tolerance: ", MENU_SKIN);

        startX = new TextField("1", MENU_SKIN);
        Label sX =new Label("Start X: ", MENU_SKIN);
        startY = new TextField("2", MENU_SKIN);
        Label sY =new Label("Start Y: ", MENU_SKIN);

        goalX = new TextField("5", MENU_SKIN);
        Label gX =new Label("Goal X: ", MENU_SKIN);
        goalY = new TextField("5", MENU_SKIN);
        Label gY =new Label("Goal Y: ", MENU_SKIN);

        height = new TextField("sin(2x+y) + cos(2y+y) + 3 - 0.2(x-3)^2 - 0.2(y-3)^2", MENU_SKIN);
        Label h =new Label("Height function: ", MENU_SKIN);


        int margine =10;
       /* tablel.row().pad(margine, 0, 0, 0);
        tabler.row().pad(margine, 0, 0, 0);
*/
        addToTable(tabler, g,gravity,margine,0,margine,0);
        addToTable(tabler, bm,ballMass,0,0,margine,0);
        addToTable(tabler,cf,coefff,0,0,margine,0);
        addToTable(tabler,vm,vMax,0,0,margine,0);
        addToTable(tabler,t,tolerance,margine,0,margine,0);
        addToTable(tablel,sX,startX,margine,0,margine,0);
        addToTable(tablel,sY,startY,0,0,margine,0);
        addToTable(tablel,gX,goalX,0,0,margine,0);
        addToTable(tablel,gY,goalY,0,0,margine,0);
        addToTable(tablel,h,height,0,0,margine,0);


        table.row().pad(0, 0, 10, 0);
        table.add(input);
        table.add(inputPath);
        table.row().pad(0, 0, 10, 0);
        table.add(output);
        table.add(outputPath);


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
        return start_x + WORLD_SHIFT.get_x();
    }

    public double getStartY(){
        shiftCalculation();
        return start_y + WORLD_SHIFT.get_y();
    }

    public double getGoalX(){
        shiftCalculation();
        return goal_x + WORLD_SHIFT.get_x();
    }
    public double getGoalY(){
        shiftCalculation();
        return goal_y + WORLD_SHIFT.get_y();
    }

    public String getHeightFunction() {
        shiftCalculation();
        return "("+applyWorldShift(height.getText())+")/"+WORLD_SCALING;
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

    public String applyWorldShift(String function) {
        double dx = WORLD_SHIFT.get_x();
        double dy = WORLD_SHIFT.get_y();
        StringBuilder sb = new StringBuilder();
        for (int i=0; i < function.length(); i++) {
            char c = function.charAt(i);
            if (c=='x') sb.append(String.format("(x-%.6f)", dx));
            else if (c=='y') sb.append(String.format("(y-%.6f)", dy));
            else sb.append(c);
        }
        return sb.toString();
    }

    static class ColorSelection {
        String name;
        Color color;
        public ColorSelection(String name, Color color) {
            this.name = name;
            this.color = color;
        }
    }

}
