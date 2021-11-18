package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import org.w3c.dom.css.Rect;

class Explosion  {

    private Animation<TextureRegion> explosionAnimation;
    private float explosionTimer;

    private Rectangle boundingBox;

    Explosion(Texture texture, Rectangle boundingBox, float totalAnimationTime){
        this.boundingBox = boundingBox;

        //split texture
        TextureRegion[][] textureRegions2D = TextureRegion.split(texture, 64,64);

        //convert to 1D array
        TextureRegion[] textureRegions1D = new TextureRegion[16];
        int index = 0;
        for(int i = 0;     i<4;  i++){
            for(int j = 0; j<4;  j++){
                textureRegions1D[index] = textureRegions2D[i][j];
                index++;
            }
        }

        explosionAnimation = new Animation<TextureRegion>(totalAnimationTime/16, textureRegions1D);
        explosionTimer = 0;



    }

    public void  update(float deltaTime){
        explosionTimer += deltaTime;
    }

    public void draw (SpriteBatch batch){
        batch.draw(explosionAnimation.getKeyFrame(explosionTimer),
                boundingBox.x,
                boundingBox.y,
                boundingBox.width,
                boundingBox.height );

    }
    public boolean isFinished(){
        return explosionAnimation.isAnimationFinished(explosionTimer);
    }

}
