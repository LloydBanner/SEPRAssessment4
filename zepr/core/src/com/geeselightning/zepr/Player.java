package com.geeselightning.zepr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class Player extends Character {

    //#changed:   Removed Player instance attribute from here
    private int boostDamage;
    private Texture mainTexture;
    private Texture attackTexture;
    private boolean attackReady = false;
    private float HPMult;
    private static PlayerType playertype;
    private boolean isImmune;
    private boolean canBeSeen = true;
    int attackTime;
    private boolean attacking;
    boolean ability = true;
    boolean abilityUsed = false;
    public boolean isZombie = false; // Added to check if player is a zombie
    private long abilityCooldown;
    String abilityString;

    //#changed:   Added this enum
    public enum PlayerType { SPORTY, NERDY, ARTSY, ZOMBIE1, ZOMBIE2, ZOMBIE3 }

    /**
     * Constructor for the player class
     * @param texture the texture to use for the player
     * @param playerSpawn coordinates to spawn the player at
     * @param world the Box2D world to spawn the player in
     * #changed:   Added Box2D body code and texture attribute for player types
     */
    public Player(Texture texture, Vector2 playerSpawn, World world) {
        super(world);
    	
        set(new Sprite(texture));

        body.setFixedRotation(true);
        body.setLinearDamping(50.f);

        setCharacterPosition(playerSpawn);
        refreshAttributes();
    }

    /**
     * Set the player type attribute
     * @param playerType the new player type value
     */
    public static void setType(PlayerType playerType){
        Player.playertype = playerType;
    }

    /**
     * Update the attributes based on the player type
     * Call this after changing the player type attribute
     * #changed:   Added this method to assign attributes based on player type
     */
    public void refreshAttributes() {
        float dmgMult, speedMult;
        if (playertype == PlayerType.NERDY) {
            dmgMult = Constant.NERDYDMGMULT;
            HPMult = Constant.NERDYHPMULT;
            speedMult = Constant.NERDYSPEEDMULT;
            mainTexture = new Texture("player01.png");
            attackTexture = new Texture("player01_attack.png");
            isZombie = false;
        } else if (playertype == PlayerType.SPORTY) {
            dmgMult = Constant.SPORTYDMGMULT;
            HPMult = Constant.SPORTYHPMULT;
            speedMult = Constant.SPORTYSPEEDMULT;
            mainTexture = new Texture("player02.png");
            attackTexture = new Texture("player02_attack.png");
            isZombie = false;
        } else if (playertype == PlayerType.ARTSY) {
            //ARTSY player
            dmgMult = Constant.ARTSYDMGMULT;
            HPMult = Constant.ARTSYHPMULT;
            speedMult = Constant.ARTSYSPEEDMULT;
            mainTexture = new Texture("player03.png");
            attackTexture = new Texture("player03_attack.png");
            isZombie = false;
        } 
        // Added by Shaun of the Devs to allow player to become a zombie
        else if (playertype == PlayerType.ZOMBIE1) {
            dmgMult = Constant.NERDYDMGMULT;
            HPMult = Constant.NERDYHPMULT;
            speedMult = Constant.NERDYSPEEDMULT;
            mainTexture = new Texture("zombie01.png");
            attackTexture = new Texture("zombie01_attack.png");
            isZombie = true;
        } else if (playertype == PlayerType.ZOMBIE2) {
            dmgMult = Constant.SPORTYDMGMULT;
            HPMult = Constant.SPORTYHPMULT;
            speedMult = Constant.SPORTYSPEEDMULT;
            mainTexture = new Texture("zombie02.png");
            attackTexture = new Texture("zombie02_attack.png");
            isZombie = true;
        } else {
            dmgMult = Constant.ARTSYDMGMULT;
            HPMult = Constant.ARTSYHPMULT;
            speedMult = Constant.ARTSYSPEEDMULT;
            mainTexture = new Texture("zombie03.png");
            attackTexture = new Texture("zombie03_attack.png");
            isZombie = true;
        }

        setTexture(mainTexture);
        
        if(ability) {
        	health = maxhealth = (int) (HPMult * 100);
        }
        
        attackDamage = (int)(Constant.PLAYERDMG * dmgMult);
        boostDamage = 1;
        speed = Constant.PLAYERSPEED * speedMult;
        
        isImmune = false;
        abilityUsed = false;
    }
    
    /**
     * Added by Shaun of the Devs to switch player between nonZombie and zombie
     */
    public void switchType() {
    	switch (playertype) {
			case NERDY:
				setType(PlayerType.ZOMBIE1);
				refreshAttributes();
				break;
    		case SPORTY:
    			setType(PlayerType.ZOMBIE2);
    			refreshAttributes();
    			break;
    		case ARTSY:
    			setType(PlayerType.ZOMBIE3);
    			refreshAttributes();
    			break;
			case ZOMBIE1:
				setType(PlayerType.NERDY);
				refreshAttributes();
				break;
    		case ZOMBIE2:
    			setType(PlayerType.SPORTY);
    			refreshAttributes();
    			break;
    		case ZOMBIE3:
    			setType(PlayerType.ARTSY);
    			refreshAttributes();
    			break;
    	}
    }
    

    /**
     * Routine to perform an attack move, damaging nearby enemies
     * @param zombie the zombie to test for promiximity and to damage
     * @param delta the time between the start of the previous call and now
     * #changed:   Implemented attack cooldown system so player can't hit continually, and sound
     */
    public void attack(Zombie zombie, float delta) {

        if (canHitGlobal(zombie, Constant.PLAYERRANGE) && hitRefresh > Constant.PLAYERHITCOOLDOWN 
        		&& attacking) {
            zombie.takeDamage(attackDamage*boostDamage);
            Sound sound = Zepr.manager.get("zombie_take_dmg.wav", Sound.class);
            sound.play(0.2f);
            hitRefresh = 0;
        } else
            hitRefresh += delta;
        }
    
    
    /**
     * Manages the abilities when special ability is triggered by E
     * #changed:   Added this method
     */
    private void triggerAbility() {
    	
		if(Gdx.input.isKeyPressed(Keys.E)) {
			ability = false;
			abilityUsed = true;
			abilityCooldown = this.timer();
	    	if(playertype == PlayerType.SPORTY) {
	    		speed += 0.05;
	    		abilityString = "Worked Out: Temporary Speed Boost";
	    	}
	    	else if(playertype == PlayerType.NERDY) {
	    		isImmune = true;
	    		abilityString = " Mech Suit: Temporary Immunity";
	    	}
	    	else {
	    	    //ARTSY type
	    		attackDamage *= 2;
	    		abilityString = "Creative Juices: Temporary Damage Boost";
	    	}
		}
    }
		
    
    /**
     * Returns the value of time since beginning of stage
     * @return the value of time since beginning of stage
     * #changed:   Added this method
     */
    private long timer() {
		return System.nanoTime()/1000000000;
	}

    /**
     * Respawn the player, resetting the health attribute
     * @param playerSpawn the position to respawn in
     * #changed:   Moved most of the code from this method to the constructor and refreshAttributes()
     */
    public void respawn(Vector2 playerSpawn){

        setCharacterPosition(playerSpawn);
        health = maxhealth;
    }


    /**
     * Routine to set the sprite direction to look at the mouse
     * @param mouseCoordinates coordinates of the mouse
     * #changed:   Added this method, moving code from Level
     */
    void look(Vector2 mouseCoordinates) {
     	// Update the direction the player is facing.
        direction = getDirectionTo(mouseCoordinates);
    }

    /**
     * Update method to process control processing and attacking action
     * @param delta the time between the start of the previous call and now
     * #changed:   Added ability system code
     */
    @Override
    public void update(float delta) {
        super.update(delta);
        
        control();
        
        if(ability) {
        	triggerAbility();
        }
        else if(this.timer()>abilityCooldown+2 && abilityUsed) {
        	this.refreshAttributes();
        }
       
        attackTime++;
       
        // Gives the player the attack texture for 0.1s after an attack.
        //if (hitRefresh <= 0.1 && getTexture() != attackTexture) {
        if (attackReady && attackTime < 30) {
            setTexture(attackTexture);
        	attacking = true;
        }
        else {
        // Changes the texture back to the main one after 0.1s.
        //if (hitRefresh > 0.1 && getTexture() == attackTexture) {
            setTexture(mainTexture);
        	attacking = false;
        }
    }

    /**
     * Handle player keyboard controls
     * #changed:   Added this method, moving code from ZeprInputProcessor
     */
    private void control() {
    	   	
    	Vector2 playerPosition = body.getPosition();

    	//Apply Box2D body impulses in specific direction when keys pressed
    	if (Gdx.input.isKeyPressed(Keys.W))
			body.applyLinearImpulse(new Vector2(0, speed), playerPosition, true);
		else if (Gdx.input.isKeyPressed(Keys.S))
			body.applyLinearImpulse(new Vector2(0, -speed), playerPosition, true);

		if (Gdx.input.isKeyPressed(Keys.A))
			body.applyLinearImpulse(new Vector2(-speed, 0), playerPosition, true);
		else if (Gdx.input.isKeyPressed(Keys.D))
			body.applyLinearImpulse(new Vector2(speed, 0), playerPosition, true);
    }

    /**
     * Reduce the player health if not immune
     * @param dmg the amount to reduce the health by
     */
    @Override
    public void takeDamage(int dmg){
        if(!isImmune)
            //If powerUpImmunity is activated
            health -= dmg;
    }

    public float getHPMult() {
        return HPMult;
    }

    boolean isVisible() {
        return canBeSeen;
    }

    public void setVisible(boolean visible) {
        canBeSeen = visible;
    }

    public void setImmune(boolean immune) {
        this.isImmune = immune;
    }

    public void setBoostDamage(int boostDamage) {
        this.boostDamage = boostDamage;
    }

    public void setAttackReady(boolean attackReady) {
        this.attackReady = attackReady;
    }

    boolean isAttackReady(){
        return attackReady;
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
    }
}
