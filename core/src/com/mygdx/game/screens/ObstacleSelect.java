package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.courses.CourseBuilder;
import com.mygdx.game.courses.IO_course_module;
import com.mygdx.game.courses.MiniMapDrawer;
import com.mygdx.game.utils.Vector2d;


import java.awt.geom.Rectangle2D;
import java.io.File;

import static com.mygdx.game.utils.Variables.*;

public class ObstacleSelect implements Screen {
    private Stage stage;
    private Menu parent;
    Texture minimapTexture;
    MiniMapDrawer minimapDraw;
    Image map;
    Label coords;

    private final static int SMALL_TREE = 0;
    private final static int MED_TREE = 1;
    private final static int LARGE_TREE = 2;
    private final static int WALL_START = 3;
    private final static int WALL_END = 4;
    private final static int CHANGE_START = 5;
    private final static int CHANGE_GOAL = 6;
    
    private double selectedThickness;
    private final double thinThickness = 0.1;
    private final double thickThickness = 0.2;


    private Texture selectBoxTxt = new Texture(Gdx.files.internal("misc/SelectionBox.png"));
    private Drawable sTreeSelectDraw = new TextureRegionDrawable(sumTextures(selectBoxTxt, new Texture(Gdx.files.internal("misc/SmallTreeSelect.png"))));
    private Drawable mTreeSelectDraw = new TextureRegionDrawable(sumTextures(selectBoxTxt, new Texture(Gdx.files.internal("misc/MediumTreeSelect.png"))));
    private Drawable lTreeSelectDraw = new TextureRegionDrawable(sumTextures(selectBoxTxt, new Texture(Gdx.files.internal("misc/LargeTreeSelect.png"))));
    private Drawable wallSelectDraw = new TextureRegionDrawable(sumTextures(selectBoxTxt, new Texture(Gdx.files.internal("misc/WallSelect.png"))));
    private Drawable wallThickSelectDraw = new TextureRegionDrawable(sumTextures(selectBoxTxt, new Texture(Gdx.files.internal("misc/WallSelectThick.png"))));

    private Image selectionImage;

    private CourseBuilder courseBuilder;

    private int selected = -1;

    public ObstacleSelect(Menu menu) {
        parent = menu;
        stage = new Stage(new ScreenViewport());

        Gdx.input.setInputProcessor(stage);
        Table overall = new Table();
        overall.setFillParent(true);
        courseBuilder = SettingsScreen.cb;
        minimapDraw = MiniMapDrawer.defaultDrawer(20, 20, 30, Vector2d.ZERO.sub(WORLD_SHIFT));
        minimapDraw.draw(courseBuilder);
        minimapTexture = minimapDraw.getTexture();

        map = new Image(minimapTexture);
        overall.add(map);

        selectionImage = new Image(selectBoxTxt);

        coords = new Label("", MENU_SKIN);
        Table buttons = new Table();
        buttons.align(Align.bottomLeft);

        TextButton play = new TextButton("PLAY", MENU_SKIN);
        play.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.changeScreen(Menu.PLAY);
            }
        });
        TextButton smallTree = new TextButton("Small Tree", MENU_SKIN);
        TextButton medTree = new TextButton("Medium Tree", MENU_SKIN);
        TextButton largeTree = new TextButton("Large Tree", MENU_SKIN);
        TextButton thinWall = new TextButton("Thin Wall", MENU_SKIN);
        TextButton thickWall = new TextButton("Thick Wall", MENU_SKIN);
        TextButton changeStart = new TextButton("Change Start", MENU_SKIN);
        TextButton changeEnd = new TextButton("Change Goal", MENU_SKIN);
        TextButton resetObstacles = new TextButton(" Clear Obstacles", MENU_SKIN);
        Table obstacleT = new Table();
        obstacleT.add(selectionImage).prefSize(100).center();
        obstacleT.row().pad(10, 10, 0, 10);
        obstacleT.pad(10, 10, 0, 10);
        obstacleT.add(smallTree);
        obstacleT.row().pad(10, 10, 0, 10);
        obstacleT.add(medTree);
        obstacleT.row().pad(10, 10, 0, 10);
        obstacleT.add(largeTree);
        obstacleT.row().pad(10, 10, 0, 10);
        obstacleT.add(thinWall);
        obstacleT.row().pad(10, 10, 0, 10);
        obstacleT.add(thickWall);
        obstacleT.row().pad(10, 10, 0, 10);
        obstacleT.add(changeStart);
        obstacleT.row().pad(10, 10, 0, 10);
        obstacleT.add(changeEnd);
        obstacleT.row().pad(10, 10, 0, 10);
        obstacleT.add(resetObstacles);

        smallTree.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectionImage.setDrawable(sTreeSelectDraw);
                selected = 0;
            }
        });

        medTree.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectionImage.setDrawable(mTreeSelectDraw);
                selected = 1;
            }
        });

        largeTree.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectionImage.setDrawable(lTreeSelectDraw);
                selected = 2;
            }
        });

        thinWall.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectionImage.setDrawable(wallSelectDraw);
                selectedThickness = thinThickness;
                selected = 3;
            }
        });
        thickWall.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectionImage.setDrawable(wallThickSelectDraw);
                selectedThickness = thickThickness;
                selected = 3;
            }
        });
        changeStart.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectionImage.setDrawable(wallThickSelectDraw);
                selectedThickness = thickThickness;
                selected = 5;
            }
        });
        changeEnd.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectionImage.setDrawable(wallThickSelectDraw);
                selectedThickness = thickThickness;
                selected = 6;
            }
        });

        resetObstacles.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                courseBuilder.clearObstacles();
                mapUpdate();
            }
        });

        overall.add(obstacleT);
        overall.row();

        TextButton save = new TextButton("Save course to: ", MENU_SKIN);
        TextField savePath = new TextField("", MENU_SKIN);
        save.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try{
                    File f = new File(savePath.getText());
                    if (!f.createNewFile()) {
                        f.delete();
                        f.createNewFile();
                    }
                    IO_course_module.outputFile(f, GAME_ASPECTS, courseBuilder.getObstacles());

                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        });
        Table saveSett = new Table();
        saveSett.add(save, savePath);
        overall.add(saveSett);

        buttons.add(play,coords);
        stage.addActor(overall);
        stage.addActor(buttons);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(),1/30f));
        stage.draw();
    }

    @Override
    public void show() {

    }

    private Vector2d last_mousePos = Vector2d.ZERO;

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Vector2d mousePos = new Vector2d(Gdx.input.getX(), Gdx.input.getY());
        Vector2 v2_pos_in_actor = map.screenToLocalCoordinates(new Vector2((float)mousePos.get_x(), (float)mousePos.get_y()));
        Vector2d pos_in_actor = new Vector2d(v2_pos_in_actor.x, map.getHeight() - v2_pos_in_actor.y);
        Vector2d pos_on_map = pos_in_actor.scaleXY(map.getWidth() / minimapTexture.getWidth(),map.getHeight() / minimapTexture.getHeight());
        Vector2d pos_in_world = minimapDraw.getRealPos(pos_on_map);
        Rectangle2D map_rectangle = new Rectangle2D.Double(0, 0, map.getWidth(), map.getHeight());
        boolean is_on_map = map_rectangle.contains(pos_in_actor.get_x(), pos_in_actor.get_y());

        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            courseBuilder.cancelWall();
            selected = WALL_START;
            mapUpdate();
        }

        if (!mousePos.equals(last_mousePos) && courseBuilder.isBuildingWall()) {
            courseBuilder.updateWall(pos_in_world, selectedThickness);
            mapUpdate();
        }
        last_mousePos = mousePos;

        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)){

            boolean update_minimap = is_on_map;
            switch (selected){
                case SMALL_TREE:
                    if (is_on_map) courseBuilder.addSmallTree(pos_in_world);
                    break;
                case MED_TREE:
                    if (is_on_map) courseBuilder.addMediumTree(pos_in_world);
                    break;
                case LARGE_TREE:
                    if (is_on_map) courseBuilder.addLargeTree(pos_in_world);
                    break;
                case WALL_START:
                    if (is_on_map) courseBuilder.startWall(pos_in_world, selectedThickness);
                    selected=WALL_END;
                    break;
                case WALL_END:
                    if (is_on_map) courseBuilder.endWall(pos_in_world, selectedThickness);
                    if (is_on_map) {
                        courseBuilder.startWall(pos_in_world, selectedThickness);
                        selected=WALL_END;
                    } else selected=WALL_START;
                    break;
                case CHANGE_START:
                    //TODO: fillout
                    break;
                case CHANGE_GOAL:
                    //TODO: fillout
                    break;
                default: update_minimap = false;
            }
            if (update_minimap) {
               mapUpdate();
            }
        }

        coords.setText("   ("+String.format("% .2f",pos_in_world.get_x())+ " , "+String.format("% .2f",pos_in_world.get_y())+")");
        minimapTexture.draw(minimapDraw.getPixmap(),0,0);
        stage.act(delta);
        stage.draw();
    }

    private void mapUpdate(){
        minimapDraw.draw(courseBuilder);
        minimapTexture = minimapDraw.getTexture();
        map.setDrawable(new TextureRegionDrawable(minimapTexture));
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

    private Texture sumTextures(Texture a, Texture b) {
        TextureData tdA = a.getTextureData();
        TextureData tdB = b.getTextureData();
        tdA.prepare(); tdB.prepare();
        Pixmap pm = tdA.consumePixmap();
        pm.drawPixmap(tdB.consumePixmap(), 0, 0);
        return new Texture(pm);
    }

}
