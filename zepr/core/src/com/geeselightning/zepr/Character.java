package com.geeselightning.zepr;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import static java.lang.Math.abs;

public class Character extends Sprite implements Steerable<Vector2> {

    public float speed;
    public int health;
    int maxhealth;
    // direction is a bearing in radians
    double direction = 0;
    World world;
    // All characters start ready to hit.
    float hitRefresh = 2;
    protected Body body;
    private static BodyDef characterBodyDef = new BodyDef() {{ type = BodyDef.BodyType.DynamicBody; }};

    public Character(Sprite sprite, Vector2 spawnPosition, World world) {
        super(sprite);
        this.world = world;
        GenerateBodyFromSprite(sprite);
        body.setFixedRotation(true);
        body.setLinearDamping(50.f);
        setCharacterPosition(spawnPosition);
        health = maxhealth = 100;
    }

    private void GenerateBodyFromSprite(Sprite sprite) {

    	body = world.createBody(characterBodyDef);
    	
    	final float scale = 1.6f;
    	
    	PolygonShape shape = new PolygonShape();
        shape.setAsBox(sprite.getWidth() / scale / 2 / Constant.physicsDensity,
    			 sprite.getHeight() / scale / 2 / Constant.physicsDensity);
    	
    	FixtureDef fixtureDef = new FixtureDef();
    	fixtureDef.shape = shape;
    	fixtureDef.density = 1f;
    	
    	body.createFixture(fixtureDef);
    	shape.dispose();
    }

    public int getHealth() {
        return health;
    }

    /**
     * Uses circles with diameter to determine if this character collides with the passed character.
     *
     * @param character Character to check if this collides with
     * @return boolean true if they collide, false otherwise
     */
    public boolean collidesWith(Character character) {
        // Circles work better than character.getBoundingRectangle()
        double diameter = 10;
        Vector2 center1 = getCenter();
        Vector2 center2 = character.getCenter();
        double distanceBetweenCenters = (Math.pow(center1.x - center2.x, 2)
                + Math.pow(center1.y - center2.y, 2));
        return (0 <= distanceBetweenCenters && distanceBetweenCenters <= Math.pow(diameter, 2));
    }

    public void setCharacterPosition(Vector2 position) {
        body.setTransform(position.x / Constant.physicsDensity, position.y / Constant.physicsDensity, 0);
        updatePosition();
    }

    @Override
    public void draw(Batch batch) {
    	setRotation((float) Math.toDegrees(-direction));
        super.draw(batch);
        // Draw health bar
        final int fillAmount = health > 0 ? (int)(32 * (float)health/maxhealth) : 0;
        batch.setColor(Color.BLACK);
        batch.draw(Level.blank, getX(), getY()+32, 32, 3);
        batch.setColor(Color.RED);
        batch.draw(Level.blank, getX() + 1, getY() + 33, fillAmount, 1);
        batch.setColor(Color.WHITE);
    }

    // hitRange has to be passed by the subclass from the canHit method.
    protected boolean canHitGlobal(Character character, int hitRange) {
        double directionToCharacter = this.getDirectionTo(character.getCenter());
        double angle = abs(directionToCharacter - direction);
        double distance = this.getCenter().sub(character.getCenter()).len();

        return (angle < 0.8 && distance < hitRange);
    }

    public Vector2 getCenter() {
        return new Vector2(getX() + (getHeight() / 2), getY() + (getWidth() / 2));
    }

    /**
     * Finds the direction (in radians) that an object is in relative to the character.
     *
     * @param coordinate 2d vector representing the position of the object
     * @return bearing   double in radians of the bearing from the character to the coordinate
     */
    public double getDirectionTo(Vector2 coordinate) {
        Vector2 charCenter = getCenter();

        // atan2 is uses the signs of both variables the determine the correct quadrant (relative to the character) of the
        // result.
        // Modulus 2pi of the angle must be taken as the angle is negative for the -x quadrants.
        // The angle must first be displaced by 2pi because the Java modulus function can return a -ve value.

        return(Math.atan2((coordinate.x - charCenter.x), (coordinate.y - charCenter.y)) + (2 * Math.PI))
                % (2 * Math.PI);
    }

    /**
     * Calculates a normalised vector that points towards given coordinate.
     *
     * @param coordinate Vector2 representing the position of the object
     * @return normalised Vector2 that from this will point towards given coordinate
     */
    public Vector2 getDirNormVector(Vector2 coordinate) {
        Vector2 charCenter = getCenter();
        // create vector that is the difference between character and the coordinate, and return it normalised
        Vector2 diffVector = new Vector2((coordinate.x - charCenter.x), (coordinate.y - charCenter.y));
        return diffVector.nor();
    }

    /**
     * Gets the position in Box2D physics coordinates
     * @return the position as Vector2
     */
    public Vector2 getPhysicsPosition() {
        return body.getPosition().scl(Constant.physicsDensity);
    }

    public void updatePosition() {
        Vector2 position = getPhysicsPosition();
        setPosition(position.x-getWidth()/2, position.y-getHeight()/2);
    }

    /**
     * This method updates the character properties.
     */
    public void update(float delta) {
        // Update x, y position of character.
        updatePosition();

        if (steeringBehavior != null) { // update character based on assigned steering behaviour
            steeringBehavior.calculateSteering(steeringOutput);
            applySteering(steeringOutput, delta);
        }
    }

    /**
     * Decreases health by value of damage
      */
    public void takeDamage(int damage){
        health -= damage;
    }
    
    public void dispose() {
    	getTexture().dispose();
    	world.destroyBody(body);
    }

    // Implementation of Steerable<Vector2> Interface
    public enum SteeringState {WANDER, SEEK, ARRIVE, NONE}
    public SteeringState currentMode = SteeringState.WANDER;

    private float maxLinearSpeed = Constant.ZOMBIESPEED;
    private float maxLinearAcceleration = 2f;
    private float maxAngularSpeed = 20f;
    private float maxAngularAcceleration = 2f;
    private float zeroThreshold = 0.01f;
    SteeringBehavior<Vector2> steeringBehavior;
    private static final SteeringAcceleration<Vector2> steeringOutput = new SteeringAcceleration<>(new Vector2());
    private float boundingRadius = 100f;
    private boolean tagged = true;
    private boolean independentFacing = false;

    public boolean isIndependentFacing() {
        return independentFacing;
    }

    public void setIndependentFacing(boolean independentFacing) {
        this.independentFacing = independentFacing;
    }

    /**
     * Apply assigned steering functionality to the character
     * @param steering steering behaviour to apply
     * @param delta time update
     */
    protected void applySteering(SteeringAcceleration<Vector2> steering, float delta) {
        boolean anyAccelerations = false;
        // Update position and linear velocity
        if (!steeringOutput.linear.isZero()) {
            body.applyForceToCenter(steeringOutput.linear, true);
            anyAccelerations = true;
        }
        //Update orientation and angular velocity
        if (isIndependentFacing()) {
            if (steeringOutput.angular != 0) {
                body.applyTorque(steeringOutput.angular, true);
                anyAccelerations = true;
            }
        } else {
            Vector2 linearVelocity = getLinearVelocity();
            if (!linearVelocity.isZero(getZeroLinearSpeedThreshold())) {
                float newOrientation = vectorToAngle(linearVelocity);
                body.setAngularVelocity((newOrientation - getAngularVelocity()) * delta);
                body.setTransform(body.getPosition(), newOrientation);
            }
        }

        if (anyAccelerations) {
            Vector2 velocity = body.getLinearVelocity();
            float currentSpeedSquare = velocity.len2();
            float maxLinearSpeed = getMaxLinearSpeed();
            if (currentSpeedSquare > (maxLinearSpeed*maxLinearSpeed)) {
                body.setLinearVelocity(velocity.scl(maxLinearSpeed/(float)Math.sqrt(currentSpeedSquare)));
            }
            float maxAngVelocity = getMaxAngularSpeed();
            if (body.getAngularVelocity() > maxAngVelocity) {
                body.setAngularVelocity(maxAngVelocity);
            }
        }
    }

    @Override
    public Vector2 getPosition() {
        return body.getPosition();
    }

    @Override
    public float getOrientation() {
        return body.getAngle();
    }

    @Override
    public void setOrientation(float orientation) {
        body.setTransform(getPosition(), orientation);
    }

    @Override
    public float vectorToAngle(Vector2 vector) {
        return (float) Math.atan2(-vector.x, vector.y);
    }

    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle) {
        outVector.x = -(float)Math.sin(angle);
        outVector.y = (float)Math.cos(angle);
        return outVector;
    }

    @Override
    public Location<Vector2> newLocation() {
        return null;
    }

    @Override
    public float getAngularVelocity() {
        return body.getAngularVelocity();
    }

    @Override
    public float getBoundingRadius() {
        return boundingRadius;
    }

    @Override
    public boolean isTagged() {
        return tagged;
    }

    @Override
    public void setTagged(boolean tagged) {
        this.tagged = tagged;
    }

    @Override
    public Vector2 getLinearVelocity() {
        return body.getLinearVelocity();
    }

    @Override
    public float getZeroLinearSpeedThreshold() {
        return zeroThreshold;
    }

    @Override
    public void setZeroLinearSpeedThreshold(float value) {
        zeroThreshold = value;
    }

    @Override
    public float getMaxLinearSpeed() {
        return maxLinearSpeed;
    }

    @Override
    public void setMaxLinearSpeed(float maxLinearSpeed) {
        this.maxLinearSpeed = maxLinearSpeed;
    }

    @Override
    public float getMaxLinearAcceleration() {
        return maxLinearAcceleration;
    }

    @Override
    public void setMaxLinearAcceleration(float maxLinearAcceleration) {
        this.maxLinearAcceleration = maxLinearAcceleration;
    }

    @Override
    public float getMaxAngularSpeed() {
        return maxAngularSpeed;
    }

    @Override
    public void setMaxAngularSpeed(float maxAngularSpeed) {
        this.maxAngularSpeed = maxAngularSpeed;
    }

    @Override
    public float getMaxAngularAcceleration() {
        return maxAngularAcceleration;
    }

    @Override
    public void setMaxAngularAcceleration(float maxAngularAcceleration) {
        this.maxLinearAcceleration = maxAngularAcceleration;
    }
}
