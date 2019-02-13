package com.geeselightning.zepr;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;

public class ZeprInputProcessor implements InputProcessor {

    protected Vector2 mousePosition = new Vector2(0, 0);

    @Override
    public boolean keyDown(int keycode) {
        return true;
    }

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    /**
     * Run when a touch or mouse button is pressed
     * @param screenX mouse x coordinate
     * @param screenY mouse y coordinate
     * @param pointer the pointer used
     * @param button the mouse button pressed
     * @return whether the input was processed
     */
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    	Level.getPlayer().setAttack(true);
        return true;
    }

    /**
     * Run when a touch or mouse button is released
     * @param screenX mouse x coordinate
     * @param screenY mouse y coordinate
     * @param pointer the pointer used
     * @param button the mouse button pressed
     * @return whether the input was processed
     */
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    	Level.getPlayer().setAttack(false);
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    /**
     * Run when the mouse is moved
     * @param screenX mouse x coordinate
     * @param screenY mouse y coordinate
     * @return whether the input was processed
     */
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        mousePosition.set(screenX, screenY);
        return true;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
