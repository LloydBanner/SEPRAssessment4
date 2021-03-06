package com.geeselightning.zepr;

import java.util.ArrayList;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Vector2;

//#changed:   Added this class
public class LevelConfig {
	int level;
	String mapLocation;
    Vector2 playerSpawn;
    public Vector2 powerSpawn;
    // Defining possible zombie spawn locations on this map
    ArrayList<Vector2> zombieSpawnPoints;
    // Defining the waves of zombies to be spawned
    Wave[] waves;
    Zepr.Location location;
    // nonZombie info added by Shaun of the Devs
	ArrayList<Vector2> nonZombieSpawnPoints;
	Wave[] nonZombieWaves;
	Music backgroundMusic;
}
