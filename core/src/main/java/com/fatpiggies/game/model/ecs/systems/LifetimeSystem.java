package com.fatpiggies.game.model.ecs.systems;

/**
 * Manages the duration of temporary entities, such as power-up effects.
 * It decrements the timeLeft in the LifetimeComponent and removes the entity from
 * the PooledEngine once it reaches zero.
 * When a power-up is created it is attached a LifetimeComponent that say when it die,
 * no matter what happen to the power-up.
 */
public class LifetimeSystem {

}
