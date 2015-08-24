package Substrate;

import Core.Parameters;

/**
 * Core Switch of the fat tree substrate. Contains information about all other nodes
 * Created by carlo on 8/5/15.
 */
public class Substrate extends Node{


    public Pod[] pods;
    public Rack[][] racks;
    public Host[][][] hosts;

    public Substrate(){
        super();

        pods = new Pod[Parameters.pods];
        racks = new Rack[Parameters.pods][Parameters.racksPerPod];
        hosts = new Host[Parameters.pods][Parameters.racksPerPod][Parameters.hostsPerRack];

        for(int podId = 0; podId < Parameters.pods; podId++){

            pods[podId] = new Pod(podId);
            new Edge(this, pods[podId]);

            for(int rackId = 0; rackId < Parameters.racksPerPod; rackId++){

                racks[podId][rackId] = new Rack(podId, rackId);
                new Edge(pods[podId], racks[podId][rackId]);

                for(int hostId = 0; hostId < Parameters.hostsPerRack; hostId++){


                    hosts[podId][rackId][hostId] = new Host(podId, rackId, hostId);
                    new Edge(racks[podId][rackId], hosts[podId][rackId][hostId]);
                }
            }
        }
    }
}
