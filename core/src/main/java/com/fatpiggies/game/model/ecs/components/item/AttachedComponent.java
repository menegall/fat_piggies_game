package com.fatpiggies.game.model.ecs.components.item;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;

public class AttachedComponent implements Component {
    public Entity targetEntity;
}
