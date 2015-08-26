package Substrate;

import Core.Parameters;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;

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
            Edge e = new Edge(this, pods[podId]);
            e.capacity = Parameters.hostsPerRack * Parameters.racksPerPod *
                    (Parameters.random.nextInt(Parameters.maxEdgeRes - Parameters.minEdgeRes + 1) + Parameters.minEdgeRes)
                    / Parameters.AGG_TO_CORE_OVERSUBSCRIPTION * Parameters.TOR_TO_AGG_OVERSUBSCRIPTION;

            for(int rackId = 0; rackId < Parameters.racksPerPod; rackId++){

                racks[podId][rackId] = new Rack(podId, rackId);
                e = new Edge(pods[podId], racks[podId][rackId]);
                e.capacity = Parameters.hostsPerRack *
                        (Parameters.random.nextInt(Parameters.maxEdgeRes - Parameters.minEdgeRes + 1) + Parameters.minEdgeRes)
                        / Parameters.TOR_TO_AGG_OVERSUBSCRIPTION;

                for(int hostId = 0; hostId < Parameters.hostsPerRack; hostId++){


                    hosts[podId][rackId][hostId] = new Host(podId, rackId, hostId);
                    new Edge(racks[podId][rackId], hosts[podId][rackId][hostId]);
                }
            }
        }
    }

    public void printDPAllocation(){
        int n = this.dpAssignment.length -1;
        for(int i = 0; i < this.dpAssignment[n].length; i++){
            if(this.dpAssignment[n][i] > 0){
                int nodesOnPod = this.dpAssignment[n][i];
                System.out.println("Pod " + i + ": " + nodesOnPod);
                for(int j = 0; j < this.pods[i].dpAssignment[nodesOnPod].length; j++) {
                    if (this.pods[i].dpAssignment[nodesOnPod][j] > 0) {
                        int nodesOnRack = this.pods[i].dpAssignment[nodesOnPod][j];
                        System.out.println("\tRack " + j + ": " + nodesOnRack);
                    }
                }
            }
        }
    }
}
