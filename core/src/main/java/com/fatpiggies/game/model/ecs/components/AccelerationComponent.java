package com.fatpiggies.game.model.ecs.components;

import com.badlogic.ashley.core.Component;

public class AccelerationComponent implements Component {
    public float baseMaxAcceleration;
    public float currentMaxAcceleration;
    public float ax;
    public float ay;
}
