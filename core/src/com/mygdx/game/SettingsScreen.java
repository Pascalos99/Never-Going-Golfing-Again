package com.mygdx.game;

import static com.mygdx.game.Variables.MenuSkin;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.io.File;
import java.io.IOException;

public class SettingsScreen implements Screen {
    private Menu parent;
    private Stage stage;

    private TextField gravity;
    private TextField ballMass;
    private TextField coefff;
    private SelectBox<String> color_select;

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
        TextButton play=new TextButton("PLAY",MenuSkin);
        TextButton input = new TextButton("Save course to file", MenuSkin);
        TextButton output = new TextButton("Load course from file", MenuSkin);
        table.add(play);
        TextField inputPath= new TextField("", MenuSkin);
        TextField outputPath =new TextField("",MenuSkin);

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

                setBallColor();

                parent.changeScreen(Menu.PLAY);
            }
        });

        gravity = new TextField("9.81",MenuSkin);
        Label g =new Label("Gravity in m/s^2: ",MenuSkin);
        ballMass = new TextField("45.93", MenuSkin);
        Label bm =new Label("Mass of Ball (grams): ",MenuSkin);
        coefff = new TextField("0.131", MenuSkin);
        Label cf =new Label("Coefficient of friction: ",MenuSkin);

        vMax = new TextField("3", MenuSkin);
        Label vm =new Label("Maximum Velocity (m/s): ",MenuSkin);
        tolerance = new TextField("0.02", MenuSkin);
        Label t =new Label("Hole tolerance: ",MenuSkin);

        color_select=new SelectBox<String>(MenuSkin);
        Array<String> items = new Array<String>();
        for (int i=0; i < Variables.BALL_COLORS.length; i++)
            items.add(Variables.BALL_COLORS[i].name);
        color_select.setItems(items);
        Label cs = new Label("Ball Color: ",MenuSkin);

        startX = new TextField("1", MenuSkin);
        Label sX =new Label("Start X: ",MenuSkin);
        startY = new TextField("1", MenuSkin);
        Label sY =new Label("Start Y: ",MenuSkin);

        goalX = new TextField("0", MenuSkin);
        Label gX =new Label("Goal X: ",MenuSkin);
        goalY = new TextField("10", MenuSkin);
        Label gY =new Label("Goal Y: ",MenuSkin);

        height = new TextField("sin(x)+cos(y)", MenuSkin);
        Label h =new Label("Height function: ",MenuSkin);


        int margine =10;
       /* tablel.row().pad(margine, 0, 0, 0);
        tabler.row().pad(margine, 0, 0, 0);
*/
        addToTable(tabler, g,gravity,margine,0,margine,0);
        addToTable(tabler, bm,ballMass,0,0,margine,0);
        addToTable(tabler,cf,coefff,0,0,margine,0);
        addToTable(tabler,vm,vMax,0,0,margine,0);
        addToTable(tabler,t,tolerance,margine,0,margine,0);
        addToTable(tabler,cs,color_select, margine, 0, margine, 0);
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



        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }
    @Override
    public void show() {

    }

    private void addToTable(Table table,Label label, Widget field, int top,int left,int bottom,int right) {
        table.add(label).expand();
        table.add(field).expand();
        table.row().pad(top, left, bottom, right);
    }

    private void setBallColor() {
        String selected = color_select.getSelected();
        for (int i=0; i < Variables.BALL_COLORS.length; i++)
            if (selected.equals(Variables.BALL_COLORS[i].name)) Variables.BALL_COLOR = Variables.BALL_COLORS[i].color;
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

    public double getStartX(){
        return Double.parseDouble(startX.getText());
    }

    public double getStartY(){
        return Double.parseDouble(startY.getText());
    }

    public double getGoalX(){
        return Double.parseDouble(goalX.getText());
    }
    public double getGoalY(){
        return Double.parseDouble(goalY.getText());
    }

    public String getHeightFunction(){
        return height.getText();
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