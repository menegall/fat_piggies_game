package com.fatpiggies.game.model.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;

public class AttachedComponent implements Component {
    public Entity targetEntityId;
}
