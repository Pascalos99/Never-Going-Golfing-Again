package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.courses.CourseBuilder;
import com.mygdx.game.courses.MiniMapDrawer;
import com.mygdx.game.utils.Vector2d;


import java.awt.geom.Rectangle2D;

import static com.mygdx.game.utils.Variables.*;

public class ObstacleSelect implements Screen {
    private Stage stage;
    private Menu parent;
    Texture minimapTexture;
    MiniMapDrawer minimapDraw;
    Image map;
    Label coords;

    private final static int SMALL_TREE =0;
    private final static int MED_TREE=1;
    private final static int LARGE_TREE=2;
    private final static int WALL_START=3;
    private final static int WALL_END=4;

    private Vector2d dummy_start;
    private Vector2d dummy_end;
    private double selectedThickness;

    private Texture selectBoxTxt = new Texture(Gdx.files.internal("misc/SelectionBox.png"));
    private Drawable sTreeSelectDraw = new TextureRegionDrawable(sumTextures(selectBoxTxt, new Texture(Gdx.files.internal("misc/SmallTreeSelect.png"))));
    private Drawable mTreeSelectDraw = new TextureRegionDrawable(sumTextures(selectBoxTxt, new Texture(Gdx.files.internal("misc/MediumTreeSelect.png"))));
    private Drawable lTreeSelectDraw = new TextureRegionDrawable(sumTextures(selectBoxTxt, new Texture(Gdx.files.internal("misc/LargeTreeSelect.png"))));
    private Drawable wallSelectDraw  = new TextureRegionDrawable(sumTextures(selectBoxTxt, new Texture(Gdx.files.internal("misc/WallSelect.png"))));
    private Drawable wallThickSelectDraw  = new TextureRegionDrawable(sumTextures(selectBoxTxt, new Texture(Gdx.files.internal("misc/WallSelectThick.png"))));

    private Image selectionImage;

    private CourseBuilder courseBuilder;

    private int selected=-1;
    public ObstacleSelect(Menu menu){
        parent=menu;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        Table overall = new Table();
        overall.setFillParent(true);
        //overall.setDebug(true);

        courseBuilder = SettingsScreen.cb;
        courseBuilder.addSmallTree(new Vector2d(3.0,4.0));
        courseBuilder.addMediumTree(new Vector2d(5.0,6.0));
        courseBuilder.addLargeTree(new Vector2d(1.0,2.0));
        courseBuilder.addTree(new Vector2d(2.0,3.0),10.0,1.0);

        courseBuilder.addWall(new Vector2d(1.0, 2.0), new Vector2d(3.0, 3.0), 0.2);

        minimapDraw = MiniMapDrawer.defaultDrawer(20, 20,30, Vector2d.ZERO.sub(WORLD_SHIFT));
        minimapDraw.draw(courseBuilder);
        minimapTexture = minimapDraw.getTexture();

        map = new Image(minimapTexture);
        overall.add(map);

        selectionImage = new Image(selectBoxTxt);

        coords= new Label("", MENU_SKIN);
        Table buttons = new Table();
        buttons.align(Align.bottomLeft);

        TextButton play=new TextButton("PLAY", MENU_SKIN);
        play.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.changeScreen(Menu.PLAY);
            }
        });
        TextButton smallTree= new TextButton("Small Tree", MENU_SKIN);
        TextButton medTree= new TextButton("Medium Tree", MENU_SKIN);
        TextButton largeTree= new TextButton("Large Tree", MENU_SKIN);
        TextButton thickWall= new TextButton("Thick Wall", MENU_SKIN);
        TextButton thinWall= new TextButton("Thin Wall", MENU_SKIN);
        Table obstacleT= new Table();
        obstacleT.add(selectionImage).prefSize(100).center();
        obstacleT.row().pad(10, 10, 0, 10);
        obstacleT.pad(10,10,0,10);
        obstacleT.add(smallTree);
        obstacleT.row().pad(10,10,0,10);
        obstacleT.add(medTree);
        obstacleT.row().pad(10,10,0,10);
        obstacleT.add(largeTree);
        obstacleT.row().pad(10,10,0,10);;
        obstacleT.add(thickWall);;
        obstacleT.row().pad(10,10,0,10);;
        obstacleT.add(thinWall);;
        obstacleT.row().pad(10,10,0,10);;

        smallTree.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectionImage.setDrawable(sTreeSelectDraw);
                selected=0;
            }
        });

        medTree.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectionImage.setDrawable(mTreeSelectDraw);
                selected=1;
            }
        });

        largeTree.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectionImage.setDrawable(lTreeSelectDraw);
                selected=2;
            }
        });

        thinWall.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectionImage.setDrawable(wallSelectDraw);
                selectedThickness=0.2;
                selected=3;
            }
        });
        thickWall.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectionImage.setDrawable(wallThickSelectDraw);
                selectedThickness=0.4;
                selected=3;
            }
        });

        overall.add(obstacleT);

        buttons.add(play,coords);
        stage.addActor(overall);
        stage.addActor(buttons);
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
        Vector2d mousePos = new Vector2d(Gdx.input.getX(), Gdx.input.getY());
        Rectangle2D map_rectangle = new Rectangle2D.Double(map.getX(), map.getY(), map.getWidth(), map.getHeight());
        boolean is_on_map = map_rectangle.contains(mousePos.get_x(), mousePos.get_y());
        Vector2d pos_in_actor = mousePos.sub(new Vector2d(map.getX(), map.getY()));
        Vector2d pos_on_map = pos_in_actor.scaleXY(
                map.getWidth() / minimapTexture.getWidth(), map.getHeight() / minimapTexture.getHeight());
        Vector2d pos_in_world = minimapDraw.getRealPos(pos_on_map);
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
                    dummy_start=pos_in_world;
                    selected=WALL_END;
                    break;
                case WALL_END:
                    dummy_end=pos_in_world;
                    courseBuilder.addWall(dummy_start, dummy_end, selectedThickness);
                    selected=WALL_START;
                    break;
                default: update_minimap = false;
            }
            if (update_minimap) {
                minimapDraw.draw(courseBuilder);
                minimapTexture = minimapDraw.getTexture();
                map.setDrawable(new TextureRegionDrawable(minimapTexture));
            }
        }

        coords.setText("   ("+String.format("% .2f",pos_in_world.get_x())+ " , "+String.format("% .2f",pos_in_world.get_y())+")");
        minimapTexture.draw(minimapDraw.getPixmap(),0,0);
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

    private Texture sumTextures(Texture a, Texture b) {
        TextureData tdA = a.getTextureData();
        TextureData tdB = b.getTextureData();
        tdA.prepare(); tdB.prepare();
        Pixmap pm = tdA.consumePixmap();
        pm.drawPixmap(tdB.consumePixmap(), 0, 0);
        return new Texture(pm);
    }

}
