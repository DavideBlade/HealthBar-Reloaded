package com.gmail.davideblade99.healthbar.hooks;

import io.lumine.mythic.bukkit.MythicBukkit;

public final class MythicMobsHook implements HealthBarHook<MythicBukkit> {

    /**
     * {@inheritDoc}
     */
    @Override
    public MythicBukkit getAPI() {
        return MythicBukkit.inst();
    }
}
