package com.fatpiggies.game.model.ecs.components;

import com.badlogic.ashley.core.Component;

public class VelocityComponent implements Component {
    public float baseMaxVelocity;
    public float currentMaxVelocity;
    public float vx;
    public float vy;
}
