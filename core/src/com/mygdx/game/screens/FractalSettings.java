package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.courses.*;
import com.mygdx.game.parser.BiLinearArrayFunction2d;
import com.mygdx.game.utils.Variables;
import com.mygdx.game.utils.Vector2d;

import java.io.File;
import java.util.Random;

import static com.mygdx.game.utils.Variables.*;

public class FractalSettings implements Screen {
    private Menu parent;
    private Stage stage;
    private CourseBuilder cb;

    SelectBox<String>resolution;
    SelectBox<String>smoothingFactor;
    SelectBox<String>interpolation;
    TextField seed;
    TextField roughness;
    TextField minimum;
    TextField maximum;

    Label waterCoverage;

    public void loadInfo(FractalInfo info) {
        if (info == null) return;
        seed.setText(info.seed+"");
        roughness.setText(info.roughness+"");
        minimum.setText(info.minimum+"");
        maximum.setText(info.maximum+"");
        resolution.setSelected(info.resolution_setting);
        smoothingFactor.setSelected(info.smoothness_setting);
        interpolation.setSelected(info.resolution_setting);
    }

    public FractalSettings(Menu menu){
        parent=menu;
        cb=SettingsScreen.cb;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        Table table = new Table();
        table.setFillParent(true);
        table.setBackground(MENU_BKG);
        stage.addActor(table);
        TextButton backButton= new TextButton("BACK",MENU_SKIN);
        backButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateFractalInfo();
                parent.changeScreen(Menu.CUSTOM_GAME);
            }
        });

        TextButton play = new TextButton("Play",MENU_SKIN);
        play.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateFractalInfo();
                cb.setFractalHeight(getSeed(),getRoughness(),getResSetting(),getSmoothingSetting(), getInterpolation(),getMin(),getMax());
                parent.changeScreen(Menu.PLAY);
            }
        });
        TextButton customizeObstacles = new TextButton("Customize Obstacles", MENU_SKIN);
        customizeObstacles.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateFractalInfo();
                cb.setFractalHeight(getSeed(),getRoughness(),getResSetting(),getSmoothingSetting(), getInterpolation(),getMin(),getMax());
                parent.changeScreen(Menu.CUSTOMIZE_OBSTACLES);
            }
        });


        Label s = new Label("Seed :",MENU_SKIN);
        seed=new TextField("",MENU_SKIN);

        Label roug = new Label("Roughness :",MENU_SKIN);
        roughness=new TextField("0.5",MENU_SKIN);

        Label res = new Label("Resolution :",MENU_SKIN);
        resolution=new SelectBox<String>(MENU_SKIN);
        resolution.setItems("Low","Medium","High");
        resolution.setSelected("Medium");

        Label smoothingf = new Label("Smoothing Factor :",MENU_SKIN);
        smoothingFactor=new SelectBox<String>(MENU_SKIN);
        smoothingFactor.setItems("None","Low","Medium","High");


        Label min= new Label("Minimum: ",MENU_SKIN);
        minimum=new TextField("-10",MENU_SKIN);

        Label max = new Label("Maximum :",MENU_SKIN);
        maximum=new TextField("15",MENU_SKIN);

        Label inter = new Label("Interpolation: ", MENU_SKIN);
        interpolation = new SelectBox<String>(MENU_SKIN);
        interpolation.setItems("bi-linear","bi-cubic");
        interpolation.setSelected("bi-cubic");

        waterCoverage=new Label("",MENU_SKIN);
        Table fields= new Table();
        fields.setBackground(TABLE_BKG);
        fields.row().pad(10,10,10,10);
        fields.add(s,seed);
        fields.row().pad(10,10,10,10);
        fields.add(roug,roughness);
        fields.row().pad(10,10,10,10);
        fields.add(res,resolution);
        fields.row().pad(10,10,10,10);
        fields.add(smoothingf,smoothingFactor);
        fields.row().pad(10,10,10,10);
        fields.add(inter, interpolation);
        fields.row().pad(10,10,10,10);
        fields.add(min,minimum);
        fields.row().pad(10,10,10,10);
        fields.add(max,maximum);
        fields.row().pad(10,10,10,10);
        fields.add(new Label("Water Coverage :",MENU_SKIN), waterCoverage);

        Table info= new Table();
        Table inner=new Table();
        info.setBackground(BLANK_BKG);
        Label title= new Label("Fractal Settings", MENU_SKIN);
        Label title2 = new Label("Extra Information", MENU_SKIN);

        Label body= new Label(""+
                "   Generates a fractal image to represent a putting\n" +
                "   course. Bicubic or Bilinear interpolation is used to\n" +
                "   compute the values in between discrete data-points.\n\n" +
                "     Any course with a unique seed will be practically\n" +
                "   unique as a course overall. Leaving the \"seed\" field\n" +
                "   empty results in a random seed being used. The\n\n" +
                "     roughness specifies the amount of distortion in between   \n" +
                "   the gradients of two determined values in the image.\n" +
                "   Roughness affects the distortion on every 'level' or\n" +
                "   'depth' of the fractal, which is generated recursively.\n" +
                "   This roughness value typically lies between 0 and 1.\n\n" +
                "     The resolution specifies how many data-points are used   \n" +
                "   for the fractal image; over which is being interpolated.\n\n" +
                "     The smoothing factor lowers the resolution while\n" +
                "   introducing a layer of bilinear interpolation, which\n" +
                "   improves overall performance while reducing quality.\n\n" +
                "     The minimum and maximum specify what the lowest\n" +
                "   and highest points in the course will be and\n" +
                "   dictates the overall scale of the world."
                ,MENU_SKIN);

        TextButton save= new TextButton("Save",MENU_SKIN);
        TextField savePath=new TextField("",MENU_SKIN);
        save.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateFractalInfo();
                try{
                    File f = new File(savePath.getText());
                    if (!f.createNewFile()) {
                        f.delete();
                        f.createNewFile();
                    }
                    IO_course_module.outputFile(f, GAME_ASPECTS);

                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        });
        title.setAlignment(Align.center);
        table.pad(50,0,10,0);
        table.add(title);
        table.add(title2);
        table.row();
        inner.add(body);
        info.add(inner).minSize(fields.getPrefWidth()/2,fields.getPrefHeight());

        table.row().pad(0,10,0,10);
        table.add(fields);
        table.add(info);
        Table navigation=new Table();
        navigation.row().pad(0,10,10,10);
        navigation.add(backButton,play,customizeObstacles);
        navigation.align(Align.bottomLeft);

        table.row().pad(0,10,0,10);
        table.add(save);
        table.add(savePath);

        stage.addActor(navigation);
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
        double cov = coverageCalc();
        if (cov != Double.NEGATIVE_INFINITY) waterCoverage.setText(String.format("%.2f%%",cov*100));
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int i, int i1) {

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
    public double coverageCalc(){
        try {
            double min = Double.parseDouble(minimum.getText());
            double max = Double.parseDouble(maximum.getText());
            if (min >= 0) return 0;
            else return Math.abs(min) / (max - min);
        } catch (NumberFormatException e) {
            return Double.NEGATIVE_INFINITY;
        }
    }

    public long getSeed(){
        String val=seed.getText();
        if(val.isBlank()||val.isEmpty()){
            return new Random(System.currentTimeMillis()).nextLong();
        }else{
            return Long.parseLong(val);
        }
    }

    public double getRoughness(){
        return Double.parseDouble(roughness.getText());
    }

    public String getResSetting(){
        return resolution.getSelected();

    }

    public String getSmoothingSetting(){
        return smoothingFactor.getSelected();
    }

    public double getMin(){
        return Double.parseDouble(minimum.getText());
    }

    public double getMax(){
        return Double.parseDouble(maximum.getText());
    }

    public String getInterpolation() {
        return interpolation.getSelected();
    }

    public FractalInfo getInfo() {
        return new FractalInfo(getSeed(), getRoughness(), getResSetting(), getSmoothingSetting(), getInterpolation(), getMin(), getMax());
    }

    private void updateFractalInfo() {
        GAME_ASPECTS.fractalInfo = getInfo();
    }

}