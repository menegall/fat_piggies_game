package com.fatpiggies.game.model.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import java.util.ArrayList;
import java.util.List;

public class CollisionEventComponent implements Component {
    public List<Entity> collidedWith = new ArrayList<>();
}
