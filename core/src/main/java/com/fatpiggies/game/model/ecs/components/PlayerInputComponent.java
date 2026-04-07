package com.fatpiggies.game.model.ecs.components;

import com.badlogic.ashley.core.Component;

public class PlayerInputComponent implements Component {
    public float joystickPercentageX;
    public float joystickPercentageY;
    public float multiplier;
}
