package com.fatpiggies.game.model;

import java.util.ArrayList;
import java.util.List;

public class Snapshot {

    /* Menu :
    - Nothing needed
    */

    /* Lobby :
    - Player list
    - Host ID to show who it is
    */
    private List<String> names = new ArrayList<>();
    private String id;

    public Snapshot() {
        names.add("Alice");
        names.add("Bob");
        names.add("Alice");
        names.add("Bob");
        names.add("Alice");
        names.add("Bob");

        id = new String("2345");
    }

    public List<String> getNames() {return names; }

    public String getCode() {
        return id;
    }



    /* Play :
    - Player list and masses
    - Positions and orientations
    - Number of health
    - Power ups list on the area
     */

    /* Over :
    - Player list with winner
     */


}
