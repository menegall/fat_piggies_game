package com.fatpiggies.game.model;

import java.util.List;

public class GameWorld {

    public List<Entity> opponents;
    public List<Entity> powerUps;
    public Entity localPig;

    //first name in list pigNames is always the localPig, all remaining names are names of opponent pigs
    //TODO: communicate with the group what information is saved where (?)
    public GameWorld(List<String> pigNames){
        //TODO: call Entity constructor with all pig names to create pig entities
    }

    public void setBaseSpeed(){}

}
