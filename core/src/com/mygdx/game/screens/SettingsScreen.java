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
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.courses.CourseBuilder;
import com.mygdx.game.courses.GameInfo;
import com.mygdx.game.parser.AtomFunction2d;
import com.mygdx.game.courses.IO_course_module;
import com.mygdx.game.parser.Function2d;
import com.mygdx.game.utils.ColorProof;
import com.mygdx.game.utils.Vector2d;

import java.io.File;
import java.util.Random;
import java.util.TimerTask;

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

    private TextField shiftX;
    private TextField shiftY;

    private TextField height;
    private TextField sandFunction;
    private TextField sandFriciton;
    private CheckBox usingFractal;
    private Button allignOrigin;

    private boolean shift_updated = true;
    static CourseBuilder cb;

    IO_course_module io;

    public SettingsScreen(Menu menu){
        parent=menu;

        cb = new CourseBuilder();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        //overall screen table
        Table table = new Table();
        table.setFillParent(true);
        table.setBackground(MENU_BKG);

        stage.addActor(table);

        //right hand side
        Table tableLeft = new Table();
        //left hand side
        Table tableRight = new Table();
        TABLE_BKG.setColor(0,0,0,200);
        tableRight.setBackground(TABLE_BKG);
        BLANK_BKG.setColor(0,0,0,200);
        tableLeft.setBackground(BLANK_BKG);
        table.setFillParent(true);
        tableLeft.padTop(10);
        tableRight.padTop(10);

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
                switchToFractal();
            }
        });
        TextField savePath= new TextField("", MENU_SKIN);
        TextField loadPath =new TextField("", MENU_SKIN);

        customizeObstacles.addListener(new ChangeListener() {

               @Override
               public void changed(ChangeEvent event, Actor actor) {
                   MAX_SHOT_VELOCITY = getMaxV();
                   GAME_ASPECTS = getGameInfo();
                   cb.loadInfo(GAME_ASPECTS);
                   parent.changeScreen(Menu.CUSTOMIZE_OBSTACLES);
               }
       });
        save.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                try{
                    File f = new File(savePath.getText());
                    if (!IO_course_module.isDefaultCourseName(savePath.getText()) && !f.createNewFile()) {
                        f.delete();
                        f.createNewFile();
                    }
                    io.outputFile(f, getGameInfo());

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
                    GAME_ASPECTS = getGameInfo();
                    GAME_ASPECTS.fractalInfo = io.getFractalInfo();
                    GAME_ASPECTS.use_fractals = io.useFractals();
                    usingFractal.setChecked(GAME_ASPECTS.use_fractals);
                    WORLD_SHIFT = io.getWorldShift();
                    shiftX.setText(String.format("%.3f",WORLD_SHIFT.get_x()));
                    shiftY.setText(String.format("%.3f",WORLD_SHIFT.get_y()));
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

        usingFractal = new CheckBox(" Fractals Enabled", MENU_SKIN);
        if (GAME_ASPECTS != null && GAME_ASPECTS.use_fractals) usingFractal.setChecked(true);

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
        Label sandCoeff =new Label("Coefficient of sand friction: ", MENU_SKIN);

        Label originX = new Label("Origin X: ", MENU_SKIN);
        shiftX = new TextField("9.995", MENU_SKIN);
        Label originY = new Label("Origin Y: ", MENU_SKIN);
        shiftY = new TextField("4.995", MENU_SKIN);

        allignOrigin = new CheckBox(" Allign Origin", MENU_SKIN);

        ChangeListener shift_calculator = new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                try {
                    Vector2d shift = calculateWorldShift(getStartX(), getStartY(), getGoalX(), getGoalY(), BOUNDED_WORLD_SIZE);
                    shiftX.setText(String.format("%.3f", shift.get_x()));
                    shiftY.setText(String.format("%.3f", shift.get_y()));
                    shift_updated = true;
                } catch (NumberFormatException e) {}
            }
        };
        ChangeListener shift_checker = new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                shift_updated = true;
            }};
        shiftX.addListener(shift_checker);
        shiftY.addListener(shift_checker);
        startX.addListener(shift_calculator);
        startY.addListener(shift_calculator);
        goalX.addListener(shift_calculator);
        goalY.addListener(shift_calculator);

        allignOrigin.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                if (allignOrigin.isChecked()) {
                    shift_calculator.changed(null, null);
                    Timer tim = new Timer();
                    tim.scheduleTask(new Timer.Task() {
                        public void run() {
                            allignOrigin.setChecked(false);
                        }
                    }, 0.1f);
                    tim.start();
                }
            }
        });

        height.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                if (GAME_ASPECTS != null && GAME_ASPECTS.use_fractals) usingFractal.setChecked(false);
            }
        });

        if (GAME_ASPECTS != null) loadGameInfo(GAME_ASPECTS);

        int margine =10;
       /* tableRight.row().pad(margine, 0, 0, 0);
        tableLeft.row().pad(margine, 0, 0, 0);
*/
        Table sett =new Table();
        sett.row().pad(40,10,0,10);
        sett.add(tableLeft,tableRight);
        table.add(sett);
        tableRight.padRight(margine);
        tableRight.padLeft(margine);
        tableLeft.padRight(margine);
        tableLeft.padLeft(margine);
        addToTable(tableLeft, g,gravity,margine,0,margine,0);
        addToTable(tableLeft, bm,ballMass,0,0,margine,0);
        addToTable(tableLeft,cf,coefff,0,0,margine,0);
        addToTable(tableLeft,vm,vMax,0,0,margine,0);
        addToTable(tableLeft,t,tolerance,0,0,margine,0);
        addToTable(tableLeft,originX,shiftX,0,0,margine,0);
        addToTable(tableLeft,originY,shiftY,margine,0,margine,0);
        addToTable(tableRight,sX,startX,margine,0,margine,0);
        addToTable(tableRight,sY,startY,0,0,margine,0);
        addToTable(tableRight,gX,goalX,0,0,margine,0);
        addToTable(tableRight,gY,goalY,0,0,margine,0);
        addToTable(tableRight,h,height,0,0,margine,0);
        addToTable(tableRight,sf,sandFunction,0,0,margine,0);
        addToTable(tableRight,sandCoeff,sandFriciton,margine,0,margine,0);


        Table fileConfig = new Table();
        Table buttons = new Table();
        Table chkbx=new Table();
        chkbx.setBackground(EXTRA_BKG);
        EXTRA_BKG.setColor(0,0,0,200);
        buttons.row().pad(15,13,10,10);
        //buttons.add(allignOrigin);
        buttons.add(play).minWidth(customizeObstacles.getPrefWidth());
        buttons.add(customizeObstacles);
        table.row().pad(0, 0, 10, 0);
        table.add(buttons);
       // table.row().pad(0, 0, 10, 0);
        Table fractal = new Table();
        fractal.add(fractals).pad(0,0,0,10);
        chkbx.add(usingFractal).pad(16,10,16,5);
        fractal.add(chkbx);
        buttons.add(fractal);
        table.row().pad(0,0,10,0);
        fileConfig.row().pad(0, 5, 10, 5);
        fileConfig.add(save).minWidth(load.getPrefWidth()).maxHeight(savePath.getPrefHeight());
        fileConfig.add(savePath);
        fileConfig.row().pad(0, 5, 10, 5);
        fileConfig.add(load).maxHeight(loadPath.getPrefHeight());
        fileConfig.add(loadPath);
        table.add(fileConfig);

        WORLD_SHIFT = new Vector2d(Double.parseDouble(shiftX.getText()), Double.parseDouble(shiftY.getText()));

        stage.addActor(backButton);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    private void switchToFractal() {
        MAX_SHOT_VELOCITY = getMaxV();
        GAME_ASPECTS = getGameInfo();
        cb.loadInfo(GAME_ASPECTS);
        GAME_ASPECTS.use_fractals = true;
        parent.changeScreen(Menu.FRACTAL_SELECT);
    }

    public static SettingsScreen randomGame(Menu menu) {
        SettingsScreen scr = new SettingsScreen(menu);
        String func = AtomFunction2d.randomPolynomial(System.currentTimeMillis());
        String func2 = AtomFunction2d.randomPolynomial(System.currentTimeMillis());
        Function2d test = new AtomFunction2d(func);
        while (test.evaluate(scr.getStartX(), scr.getStartY()) < 0 ||
            test.evaluate(scr.getGoalX(), scr.getGoalY()) < 0 ||
                test.gradient(scr.getGoalX(), scr.getGoalY()).get_length() > GRADIENT_CUTTOFF) {
            func = AtomFunction2d.randomPolynomial(new Random(System.currentTimeMillis()).nextLong());
            test = new AtomFunction2d(func);
        }
        scr.height.setText(func);
        scr.sandFunction.setText(func2);
        System.out.println("random function = "+func);
        System.out.println("sand function = "+func2);
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

        if (shift_updated) {
            try {
                WORLD_SHIFT = new Vector2d(Double.parseDouble(shiftX.getText()), Double.parseDouble(shiftY.getText()));
                shift_updated = false;
            } catch (NumberFormatException e) {}
        }

        if (usingFractal.isChecked() && (GAME_ASPECTS == null || GAME_ASPECTS.fractalInfo == null)) switchToFractal();
        else if (GAME_ASPECTS != null) GAME_ASPECTS.use_fractals = usingFractal.isChecked();
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

    public String getHeightFunction() {
        return height.getText();
    }

    public String getSandFunction(){
        return sandFunction.getText();
    }

    public double getSandFriction(){
        return Double.parseDouble(sandFriciton.getText());
    }

    public Vector2d calculateWorldShift(double start_x, double start_y, double goal_x, double goal_y, double world_range) {
        double delta_x = Math.abs(start_x - goal_x);
        double delta_y = Math.abs(start_y - goal_y);
        double min_x = Math.min(start_x, goal_x);
        double min_y = Math.min(start_y, goal_y);
        double new_min_x = world_range/2 - delta_x/2;
        double new_min_y = world_range/2 - delta_y/2;
        return new Vector2d(new_min_x - min_x, new_min_y - min_y);
    }


    public GameInfo getGameInfo(){
        GameInfo info = new GameInfo(parent.players.getPlayers(),getGravity(),getMassofBall(),
                getFrictionc(),getMaxV(),getTolerance(),getStartX(),
                getStartY(),getGoalX(),getGoalY(),getHeightFunction(),
                getSandFriction(),getSandFunction());
        if (GAME_ASPECTS != null) info.fractalInfo = GAME_ASPECTS.fractalInfo;
        if (info.fractalInfo != null && usingFractal.isChecked()) info.use_fractals = true;
        if (WORLD_SHIFT == null) WORLD_SHIFT = new Vector2d(Double.parseDouble(shiftX.getText()), Double.parseDouble(shiftY.getText()));
        return info;
    }

    public void loadGameInfo(GameInfo info) {
        gravity.setText("" + info.getGravity());
        ballMass.setText("" + info.getMassofBall());
        coefff.setText(("" + info.getFriction()));
        sandFriciton.setText("" + info.getSandFriction());
        vMax.setText(""+info.getMaxV());
        tolerance.setText(""+info.getTolerance());
        startX.setText(""+info.getStartX());
        startY.setText(""+info.getStartY());
        goalX.setText(""+info.getGoalX());
        goalY.setText(""+info.getGoalY());
        height.setText(""+info.getHeightFunction());
        sandFunction.setText("" + info.getSandFunction());
        shiftX.setText("" + WORLD_SHIFT.get_x());
        shiftY.setText("" + WORLD_SHIFT.get_y());
    }

    public static class ColorSelection {
        public String name;
        public String cb_name;
        public ColorProof color;
        public ColorSelection(String name, String cb_name, ColorProof color) {
            this.name = name;
            this.cb_name = cb_name;
            this.color = color;
        }
        public String getName() {
            if (ColorProof.COLOR_BLIND_MODE) return cb_name;
            return name;
        }
    }

}
