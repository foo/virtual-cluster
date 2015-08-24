package Substrate;

/**
 * Represents a Pod (Level 3) of the fat tree substrate
 * Created by carlo on 8/5/15.
 */
public class Pod extends Node {

    private static int ID = 0;

    int podId;

    public Pod(int podId){
        super();

        this.id = "Pod" + ID++;

        this.podId = podId;
    }
}
