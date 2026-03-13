package com.fatpiggies.game.model.ecs.components;

import com.badlogic.ashley.core.Component;

public class VelocityComponent implements Component {
    public double baseMaxVelocity;
    public double currentMaxVelocity;
    public double vx;
    public double vy;
}
