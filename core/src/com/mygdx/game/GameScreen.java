package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sun.org.apache.xml.internal.utils.StringToIntTable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;

public class GameScreen implements Screen{



    //screen
    private Camera camera;
    private Viewport viewport;

    //graphics
    private SpriteBatch batch;
    private  TextureAtlas textureAtlas;
    private Texture explosionTexture;

    private TextureRegion[] backgrounds;
    private float backgroundHeight; //height of background in World units


    private  TextureRegion playerAbilityTextureRegion, playerShieldTextureRegion,
            enemyAbilityTextureRegion, enemyShiledTextureRegion,
            playerLaserTextureRegion, enemyLaserTextureRegion;



    //timing

    private  float[] backgroundOffsets = {0,0,0,0};
    private float backgroundMaxScrollingSpeed;
    private float timeBetweenEnemySpawns = 3f;
    private float enemySpawnTimer = 0;

    //world parameters
    private final float WORLD_WIDTH = 72;
    private final float WORLD_HEIGHT = 128;
    private  final  float TOUCH_MOVEMENT_THRESHOLD = 0.5f;

    // game objects
    private PlayerAbility playerAbility;
    private LinkedList<EnemyAbility> enemyAbilityList;
    private LinkedList<Laser> playerLaserList;
    private LinkedList<Laser> enemyLaserList;
    private LinkedList<Explosion> explosionsList;
    private int score = 0;

    //Head-up Display
    BitmapFont font;
    float hudVerticalMargin,hudLeftX,hudRightX,hudCentreX, hudRow1Y, hudRow2Y,hudSectionWidth;

    GameScreen(){

        camera = new OrthographicCamera();
        viewport = new StretchViewport(WORLD_WIDTH,WORLD_HEIGHT,camera);

        //set up the texture atlas
        textureAtlas = new TextureAtlas("images.atlas");


        //setting up the background
        backgrounds = new TextureRegion[4];
        backgrounds[0] = textureAtlas.findRegion("sea");
        backgrounds[2] = textureAtlas.findRegion("sun");
        backgrounds[1] = textureAtlas.findRegion("skygrass");
        backgrounds[3] = textureAtlas.findRegion("people");


        backgroundHeight = WORLD_HEIGHT *2;
        backgroundMaxScrollingSpeed = (float)(WORLD_HEIGHT)/4;

        //initialize texture regions
        playerAbilityTextureRegion = textureAtlas.findRegion("player");
        enemyAbilityTextureRegion = textureAtlas.findRegion("enemies");
        playerShieldTextureRegion = textureAtlas.findRegion("shield1");
        enemyShiledTextureRegion = textureAtlas.findRegion("shield2");
        enemyShiledTextureRegion.flip(false,true);
        playerLaserTextureRegion = textureAtlas.findRegion("laserBlue03");
        enemyLaserTextureRegion = textureAtlas.findRegion("laserRed03");
        explosionTexture = new Texture("explosion.png");
        //set up game objects

        playerAbility = new PlayerAbility(WORLD_WIDTH/2,WORLD_HEIGHT/4,
                10,20,
                48,3,
                0.4f,4,45,0.5f,
                playerAbilityTextureRegion, playerShieldTextureRegion,
                playerLaserTextureRegion);


        enemyAbilityList = new LinkedList<>();



        playerLaserList = new LinkedList<>();
        enemyLaserList = new LinkedList<>();
        explosionsList = new LinkedList<>();


        batch = new SpriteBatch();

        prepareHUD();
    }

    private void prepareHUD() {
        //Create a BitmapFont from our font file
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("EdgeOfTheGalaxyRegular-OVEa6.otf"));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        fontParameter.size = 72;
        fontParameter.borderWidth = 3.6f;
        fontParameter.color = new Color(1,1,1,0.3f);
        fontParameter.borderColor = new Color(0,0,0,0.3f);


        font = fontGenerator.generateFont(fontParameter);

        //scale the font to fit world
        font.getData().setScale(0.08f);


        //calculate hud margins,etc.
        hudVerticalMargin = font.getCapHeight() / 2;
        hudLeftX = hudVerticalMargin;
        hudRightX = WORLD_WIDTH * 2 / 3 - hudLeftX;
        hudCentreX = WORLD_WIDTH / 3;
        hudRow1Y = WORLD_HEIGHT - hudVerticalMargin;
        hudRow2Y = hudRow1Y - hudVerticalMargin - font.getCapHeight();
        hudSectionWidth = WORLD_WIDTH / 3;

    }


    @Override
    public void render(float deltaTime) {
        batch.begin();


        //scrolliing background
        renderBackground(deltaTime);

        detectInput(deltaTime);
        playerAbility.update(deltaTime);
        spawnEnemyAbility(deltaTime);
//        spawnEnemyAbility(deltaTime);


        ListIterator<EnemyAbility> enemyAbilityListIterator = enemyAbilityList.listIterator();
        while (enemyAbilityListIterator.hasNext()) {
            EnemyAbility enemyAbility = enemyAbilityListIterator.next();
            moveEnemy(enemyAbility,deltaTime);
            enemyAbility.update(deltaTime);

            enemyAbility.draw(batch);
        }
        //player ability
        playerAbility.draw(batch);

        //lasers
        renderLasers(deltaTime);

        //detect collisions between lasers and ability
        detectCollisions();

        //explosions
        updateAndRenderExplosions(deltaTime);

        //hud rendering
        updateAndRenderHUD();

        batch.end();


    }
    private  void updateAndRenderHUD(){
        //render top row labels
        font.draw(batch, "Score ", hudLeftX, hudRow1Y, hudSectionWidth, Align.left, false);
        font.draw(batch, "Shield ", hudCentreX, hudRow1Y, hudSectionWidth, Align.center, false);
        font.draw(batch, "Lives ", hudRightX, hudRow1Y, hudSectionWidth, Align.right, false);
        //render second row values

        font.draw(batch,String.format(Locale.getDefault(),"%06d",score),hudLeftX, hudRow2Y, hudSectionWidth, Align.left, false);
        font.draw(batch,String.format(Locale.getDefault(),"%02d",playerAbility.shield),hudCentreX, hudRow2Y, hudSectionWidth, Align.center, false);
        font.draw(batch,String.format(Locale.getDefault(),"%02d",playerAbility.lives),hudRightX, hudRow2Y, hudSectionWidth, Align.right, false);

//            font.draw(batch , String.format(Locale.getDefault(),"%06d"),hudLeftX, hudRow1Y, hudSectionWidth, Align.left, false);
//        font.draw(batch , String.format(Locale.getDefault(),"%02d"),hudCentreX, hudRow1Y, hudSectionWidth, Align.center, false);
//        font.draw(batch , String.format(Locale.getDefault(),"%02d"),hudRightX, hudRow1Y, hudSectionWidth, Align.right, false);
    }

    private  void spawnEnemyAbility(float deltaTime){
        enemySpawnTimer += deltaTime;

        if (enemySpawnTimer > timeBetweenEnemySpawns) {
            enemyAbilityList.add(new EnemyAbility(mainclass.random.nextFloat() * (WORLD_WIDTH - 10) + 5, WORLD_HEIGHT * 3 / 4,
                    10, 15,
                    35, 1,
                    0.3f, 5, 50, 0.8f,
                    enemyAbilityTextureRegion, enemyShiledTextureRegion, enemyLaserTextureRegion));
            enemySpawnTimer -= timeBetweenEnemySpawns;
        }
    }


    private  void detectInput(float deltaTime){
        //keyboard input


        //strategy: determine the max distance the ability can move
        //checl each key that matters and move accordingly

        float leftLimit, rightLimit, upLimit, downLimit;

        leftLimit = -playerAbility.boundingBox.x;
        downLimit = -playerAbility.boundingBox.y + playerAbility.boundingBox.height;
        rightLimit = WORLD_WIDTH - playerAbility.boundingBox.x - playerAbility.boundingBox.width;
        upLimit = (float) WORLD_HEIGHT/2 - playerAbility.boundingBox.y - playerAbility.boundingBox.height;

        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT) && rightLimit > 0) {
            playerAbility.translate(Math.min(playerAbility.movementSpeed*deltaTime, rightLimit),0f);

        }
        if(Gdx.input.isKeyPressed(Input.Keys.UP) && upLimit > 0) {
            playerAbility.translate(0f, Math.min(playerAbility.movementSpeed*deltaTime, upLimit));

        }
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT) && leftLimit < 0) {
            playerAbility.translate(Math.max(-playerAbility.movementSpeed*deltaTime, leftLimit),0f);

        }
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN) && downLimit < 0) {
            playerAbility.translate(0f, Math.max(-playerAbility.movementSpeed*deltaTime, downLimit));

        }


        //touch input (also mouse)
        if(Gdx.input.isTouched()){
            //get the screen position of the touch
            float xTouchPixels = Gdx.input.getX();
            float yTouchPixels = Gdx.input.getY();

            //convert to world position
            Vector2 touchPoint = new Vector2(xTouchPixels, yTouchPixels);
            touchPoint = viewport.unproject(touchPoint);

            //calculate the x and y differences
            Vector2 playerAbilityCentre = new Vector2(
                    playerAbility.boundingBox.x+ playerAbility.boundingBox.width,
                    playerAbility.boundingBox.y + playerAbility.boundingBox.height);

            float touchDistance = touchPoint.dst(playerAbilityCentre);
            if(touchDistance > TOUCH_MOVEMENT_THRESHOLD){
                float xTouchDifference = touchPoint.x - playerAbilityCentre.x;
                float yTouchDifference = touchPoint.y - playerAbilityCentre.y;

                //scale to the maximum speed of the ability
                float xMove = xTouchDifference / touchDistance * playerAbility.movementSpeed*deltaTime;
                float yMove = yTouchDifference / touchDistance * playerAbility.movementSpeed*deltaTime;

                if(xMove > 0) xMove = Math.min(xMove, rightLimit);
                else xMove = Math.max(xMove, leftLimit);


                if(yMove > 0) yMove = Math.min(yMove, upLimit);
                else yMove = Math.max(yMove, downLimit);

                playerAbility.translate(xMove,yMove);




            }
        }
    }

    private void moveEnemy(EnemyAbility enemyAbility,float deltaTime){
        //strategy: determine the max distance the ability can move
        //checl each key that matters and move accordingly

        float leftLimit, rightLimit, upLimit, downLimit;

        leftLimit = -enemyAbility.boundingBox.x;
        downLimit = (float) WORLD_HEIGHT/2 - enemyAbility.boundingBox.y;
        rightLimit = WORLD_WIDTH - enemyAbility.boundingBox.x - enemyAbility.boundingBox.width;
        upLimit = WORLD_HEIGHT - enemyAbility.boundingBox.y - enemyAbility.boundingBox.height;


        float xMove = enemyAbility.getDirectionVector().x * enemyAbility.movementSpeed*deltaTime;
        float yMove = enemyAbility.getDirectionVector().y  * enemyAbility.movementSpeed*deltaTime;

        if(xMove > 0) xMove = Math.min(xMove, rightLimit);
        else xMove = Math.max(xMove, leftLimit);


        if(yMove > 0) yMove = Math.min(yMove, upLimit);
        else yMove = Math.max(yMove, downLimit);

        enemyAbility .translate(xMove,yMove);

    }


    private  void detectCollisions(){
        //for each player laser, check whether it intersects an enemy ability
        ListIterator<Laser> laserListIterator = playerLaserList.listIterator();
        while (laserListIterator.hasNext()) {
            Laser laser = laserListIterator.next();
            ListIterator<EnemyAbility> enemyAbilityListIterator=enemyAbilityList.listIterator();
            while (enemyAbilityListIterator.hasNext()) {
                EnemyAbility enemyAbility = enemyAbilityListIterator.next();


                if (enemyAbility.intersects(laser.boundingBox)) {
                    //contact with enemy ability
                    if (enemyAbility.hitAndCheckDestroyed(laser))
                    {
                        enemyAbilityListIterator.remove();
                        explosionsList.add(
                                new Explosion(explosionTexture,
                                new Rectangle(enemyAbility.boundingBox),
                                0.7f));

                        score += 100;

                    }
                    laserListIterator.remove();
                    break;
                }
            }
        }
        laserListIterator = enemyLaserList.listIterator();
        while (laserListIterator.hasNext()){
            Laser laser = laserListIterator.next();
            if (playerAbility.intersects(laser.boundingBox)){
                if(playerAbility.hitAndCheckDestroyed(laser)){
                    explosionsList.add(
                            new Explosion(explosionTexture,
                                    new Rectangle(playerAbility.boundingBox),1.6f));
                    playerAbility.shield = 10;
                    playerAbility.lives--;



                }
                laserListIterator.remove();
            }
        }


    }




    private  void updateAndRenderExplosions(float deltaTime){
        ListIterator<Explosion> explosionListIterator = explosionsList.listIterator();
        while (explosionListIterator.hasNext()){
            Explosion explosion = explosionListIterator.next();
            explosion.update(deltaTime);
            if(explosion.isFinished()){
                explosionListIterator.remove();
            }
            else{
                explosion.draw(batch);
            }
        }
    }


    private  void  renderLasers(float deltaTime){
        //create news lasers
        //player laser
        if(playerAbility.canFireLaser()){
            Laser[] lasers = playerAbility.fireLaser();
            playerLaserList.addAll(Arrays.asList(lasers));
        }
        //enemy lasers
        ListIterator<EnemyAbility> enemyAbilityListIterator = enemyAbilityList.listIterator();
        while (enemyAbilityListIterator.hasNext()) {
            EnemyAbility enemyAbility = enemyAbilityListIterator.next();
            if (enemyAbility.canFireLaser()) {
                Laser[] lasers = enemyAbility.fireLaser();
                enemyLaserList.addAll(Arrays.asList(lasers));
            }
        }
        //draw lasers
        //remove old lasers
        ListIterator<Laser> iterator = playerLaserList.listIterator();
        while (iterator.hasNext()){
            Laser laser = iterator.next();
            laser.draw(batch);
            laser.boundingBox.y += laser.movementSpeed * deltaTime;
            if(laser.boundingBox.y > WORLD_HEIGHT){
                iterator.remove();
            }
        }

        iterator = enemyLaserList.listIterator();
        while (iterator.hasNext()){
            Laser laser = iterator.next();
            laser.draw(batch);
            laser.boundingBox.y -= laser.movementSpeed * deltaTime;
            if(laser.boundingBox.y + laser.boundingBox.height < 0){
                iterator.remove();
            }
        }

    }


    private void renderBackground(float deltaTime){

        backgroundOffsets[0] += deltaTime * backgroundMaxScrollingSpeed / 8;
        backgroundOffsets[1] += deltaTime * backgroundMaxScrollingSpeed / 4;
        backgroundOffsets[2] += deltaTime * backgroundMaxScrollingSpeed / 2;
        backgroundOffsets[3] += deltaTime * backgroundMaxScrollingSpeed ;

        for (int layer = 0; layer < backgroundOffsets.length; layer++){
            if(backgroundOffsets[layer] > WORLD_WIDTH){
                backgroundOffsets[layer] = 0;
            }
            batch.draw(backgrounds[layer],
                    -backgroundOffsets[layer],
                    0,
                    WORLD_WIDTH,WORLD_HEIGHT );
            batch.draw(backgrounds[layer],
                    -backgroundOffsets[layer] + WORLD_WIDTH,
                    0,
                    WORLD_WIDTH,WORLD_HEIGHT );


        }

    }






    @Override
    public void resize(int width, int height) {
        viewport.update(width, height,true);
        batch.setProjectionMatrix(camera.combined);
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
    public void show() {

    }
    @Override
    public void dispose() {

    }
}
