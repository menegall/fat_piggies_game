package com.fatpiggies.game.model.ecs.components.collision;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;

import java.util.ArrayList;
import java.util.List;

public class CollisionEventComponent implements Component {
    public final List<Entity> collidedWith = new ArrayList<>();
}
