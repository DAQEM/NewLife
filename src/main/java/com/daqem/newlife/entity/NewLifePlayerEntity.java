package com.daqem.newlife.entity;

import io.github.apace100.origins.origin.Origin;
import net.minecraft.util.Identifier;

import java.util.List;

public interface NewLifePlayerEntity {

    int getLives();
    void setLives(int lives);
    Origin assignRandomOrigin(boolean sendDeathMessage);
    boolean getDied();
    List<Identifier> getOrigins();
    int getRerolls();
    void setRerolls(int rerolls);
}
