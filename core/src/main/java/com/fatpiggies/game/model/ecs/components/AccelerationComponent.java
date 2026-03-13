package com.fatpiggies.game.model.ecs.components;

import com.badlogic.ashley.core.Component;

public class AccelerationComponent implements Component {
    public double baseMaxAcceleration;
    public double currentMaxAcceleration;
    public double ax;
    public double ay;
}
