package Substrate;

import Core.Parameters;

/**
 * Host (Level1) in the fat tree substrate
 * Created by carlo on 8/5/15.
 */
public class Host extends Node {

    private static int ID = 0;

    public int podId;
    public int rackId;
    public int hostId;


    public Host(int podId, int rackId, int hostId){
        super();

        this.id = "Host" + ID++;


        this.podId = podId;
        this.rackId = rackId;
        this.hostId = hostId;

        // Assuming uniform distribution
        this.capacity = + Parameters.minHostRes + (int)Math.ceil(Parameters.random.nextInt(Parameters.maxHostRes - Parameters.minHostRes + 1));
        if (this.capacity < 0 ) this.capacity = 0;
    }

}
