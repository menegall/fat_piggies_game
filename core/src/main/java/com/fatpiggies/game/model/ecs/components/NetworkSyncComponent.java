package com.fatpiggies.game.model.ecs.components;

import com.badlogic.ashley.core.Component;

public class NetworkSyncComponent implements Component {
    public float targetX;
    public float targetY;
    public float targetAngle;
    public float targetVx;
    public float targetVy;
}
