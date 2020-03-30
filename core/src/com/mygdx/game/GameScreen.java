package com.mygdx.game;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;

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

    CrazyPutting game;
    public  GameScreen(Menu menu, GameInfo gameInfo) {
        parent = menu;
        gameAspects=gameInfo;
        generator.setPathPreference(true);
        if(gameAspects!=null){
          //  figure out how to amke function 2D
            course = new PuttingCourse(new AtomFunction2d(gameAspects.getHeightFunction()) ,Function2d.getConstant(gameAspects.getFriction()) ,size,size,gameAspects.getGoal(),gameAspects.getStart()
                    ,gameAspects.getTolerance(),gameAspects.getMaxV() ,gameAspects.getGravity());
        }else{
            course = generator.fractalGeneratedCourse(size, 1,0.8,0.7,size/200,3,9.81);
        }
        game =new CrazyPutting(course, gameAspects);
        game.create();

        System.out.println("END");


    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        game.render();
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
}
