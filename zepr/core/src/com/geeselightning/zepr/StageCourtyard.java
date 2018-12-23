package com.geeselightning.zepr;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Arrays;

public class StageCourtyard extends Stage {

    private static final String mapLocation = "core/assets/maps/courtyard.tmx";
    private static final Vector2 playerSpawn = new Vector2(0, 0);
    //private static final Vector2 testZombieSpawn = new Vector2(0, 50);
    // Defining possible zombie spawn locations on this map
    private static final ArrayList<Vector2> zombieSpawnPoints = new ArrayList<Vector2>(
            Arrays.asList(new Vector2(0,0), new Vector2(0,50),
                    new Vector2(50,50), new Vector2(50,0))
    );

    public StageCourtyard(Zepr zepr) {
        super(zepr, mapLocation, playerSpawn, zombieSpawnPoints);
    }

    public void complete() {
        // Update progress
        if (parent.progress == parent.HALIFAX) {
            parent.progress = parent.COURTYARD;
        }
        // else the stage is being replayed
    }
}
