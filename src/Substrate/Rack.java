package Substrate;

/**
 * Represents a Rack (Level 2) of the fat tree substrate
 * Created by carlo on 8/5/15.
 */
public class Rack extends Node {

    private static int ID = 0;

    public int rackId;
    public int podId;

    public Rack(int podId, int rackId){
        super();

        this.id = "Rack" + ID++;

        this.rackId = rackId;
        this.podId = podId;
    }


        
}
