package com.fatpiggies.game.model.ecs;

import static com.fatpiggies.game.model.utils.GameConstants.BASE_LIFE;
import static com.fatpiggies.game.model.utils.GameConstants.BOTTOM_BOUND;
import static com.fatpiggies.game.model.utils.GameConstants.LEFT_BOUND;
import static com.fatpiggies.game.model.utils.GameConstants.PLAYER_BASE_ACCELERATION;
import static com.fatpiggies.game.model.utils.GameConstants.PLAYER_BASE_MASS;
import static com.fatpiggies.game.model.utils.GameConstants.PLAYER_BASE_VELOCITY;
import static com.fatpiggies.game.model.utils.GameConstants.PLAYER_COLLISION_RADIUS;
import static com.fatpiggies.game.model.utils.GameConstants.POWERUP_COLLISION_RADIUS;
import static com.fatpiggies.game.model.utils.GameConstants.POWERUP_MAX_LIFETIME;
import static com.fatpiggies.game.model.utils.GameConstants.POWERUP_MIN_LIFETIME;
import static com.fatpiggies.game.model.utils.GameConstants.POWER_MASS_MODIFIER;
import static com.fatpiggies.game.model.utils.GameConstants.POWER_VELOCITY_MODIFIER;
import static com.fatpiggies.game.model.utils.GameConstants.RIGHT_BOUND;
import static com.fatpiggies.game.model.utils.GameConstants.TOP_BOUND;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.fatpiggies.game.model.ecs.components.HealthComponent;
import com.fatpiggies.game.model.ecs.components.PlayerInputComponent;
import com.fatpiggies.game.model.ecs.components.RenderComponent;
import com.fatpiggies.game.model.ecs.components.TransformComponent;
import com.fatpiggies.game.model.ecs.components.collision.ColliderComponent;
import com.fatpiggies.game.model.ecs.components.collision.CollisionEventComponent;
import com.fatpiggies.game.model.ecs.components.item.CollectibleComponent;
import com.fatpiggies.game.model.ecs.components.item.LifetimeComponent;
import com.fatpiggies.game.model.ecs.components.modifier.InputModifierComponent;
import com.fatpiggies.game.model.ecs.components.modifier.MassModifierComponent;
import com.fatpiggies.game.model.ecs.components.modifier.VelocityModifierComponent;
import com.fatpiggies.game.model.ecs.components.network.NetworkIdentityComponent;
import com.fatpiggies.game.model.ecs.components.network.NetworkSyncComponent;
import com.fatpiggies.game.model.ecs.components.physics.AccelerationComponent;
import com.fatpiggies.game.model.ecs.components.physics.MassComponent;
import com.fatpiggies.game.model.ecs.components.physics.VelocityComponent;
import com.fatpiggies.game.model.utils.PowerUpType;

public class EntityFactory {


}
