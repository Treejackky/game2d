package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

abstract public class Ability {

    //ability characteristics
    float movementSpeed; // world units per second
    int shield;


    //position & dimension
    Rectangle boundingBox;

    //laser info
    float laserWidth, laserHeight;
    float laserMovementSpeed;
    float timeBetweenShots;
    float timeSinceLastShot =0;

    //graphics
    TextureRegion abilityTextureRegion, shieldTextureRegion, laserTextureRegion;

    public Ability(float xCentre, float yCentre,
                   float width, float height,
                   float movementSpeed, int shield,
                   float laserWidth, float laserHeight, float laserMovementSpeed,
                   float timeBetweenShots,
                   TextureRegion abilityTextureRegion, TextureRegion shieldTextureRegion,
                   TextureRegion laserTextureRegion) {
        this.movementSpeed = movementSpeed;
        this.shield = shield;

        this.boundingBox = new Rectangle(xCentre - width/2,yCentre - height/2,width,height);


        this.laserWidth = laserWidth;
        this.laserHeight = laserHeight;
        this.laserMovementSpeed = laserMovementSpeed;
        this.timeBetweenShots =timeBetweenShots;
        this.abilityTextureRegion = abilityTextureRegion;
        this.shieldTextureRegion = shieldTextureRegion;
        this.laserTextureRegion = laserTextureRegion;
    }

    public void update(float deltaTime){
        timeSinceLastShot += deltaTime;
    }

    public boolean canFireLaser(){
        return (timeSinceLastShot - timeBetweenShots >= 0);
    }

    public abstract Laser[] fireLaser();

    public  boolean intersects(Rectangle otherRectangle){
        return boundingBox.overlaps(otherRectangle);
    }

    public  boolean hitAndCheckDestroyed(Laser laser){
        if(shield > 0){
            shield --;
            return false;
        }
        return true;

    }

    public void  translate(float xChange, float yChange){
        boundingBox.setPosition(boundingBox.x+xChange, boundingBox.y+yChange);
    }

    public  void draw(Batch batch){
        batch.draw(abilityTextureRegion,boundingBox.x,boundingBox.y,boundingBox.width,boundingBox.height);
            if(shield > 0){
                batch.draw(shieldTextureRegion, boundingBox.x,boundingBox.y,boundingBox.width,boundingBox.height);
            }
    }

}
