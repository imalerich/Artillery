package com.mygdx.game.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.mygdx.game.Game;

public class HtmlLauncher extends GwtApplication {

        @Override
        public GwtApplicationConfiguration getConfig () {
                return new GwtApplicationConfiguration(960, 600);
        }

        @Override
        public ApplicationListener getApplicationListener () {
                return new Game(960, 600);
        }
}