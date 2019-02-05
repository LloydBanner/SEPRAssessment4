package com.geeselightning.zepr;

import java.util.ArrayList;
import com.badlogic.gdx.math.Vector2;

public class LevelConfig {
	
	String mapLocation;
    Vector2 playerSpawn;
    Vector2 powerSpawn;
    // Defining possible zombie spawn locations on this map
    ArrayList<Vector2> zombieSpawnPoints;
    // Defining the number of zombies to be spawned for each wave
    int[] waves;
    Zepr.location location;
    boolean isFinal;
}