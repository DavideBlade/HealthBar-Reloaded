package com.gmail.davideblade99.healthbar.hooks;

import io.github.arcaneplugins.levelledmobs.LevelInterface;
import io.github.arcaneplugins.levelledmobs.LevelledMobs;

public final class LevelledMobsHook implements HealthBarHook<LevelInterface> {

    /**
     * {@inheritDoc}
     */
    @Override
    public LevelInterface getAPI() {
        return LevelledMobs.getInstance().getLevelInterface();
    }
}
