package Algorithms;

import Core.Parameters;
import Core.Simulator;
import Substrate.Substrate;
import Substrate.Host;
import Substrate.Pod;
import Substrate.Rack;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the dynamic program, which solves the MA FP NI BW Problem.
 * Created by carlo on 8/6/15.
 */
public class DP_MA_FP_NI_BW {

    public boolean embed() {

        Substrate sub = Simulator.sub;

        Parameters.RS = false;
        Parameters.MA = false;

        int n = Parameters.getN();
        double niBw = Parameters.getB1();
        double chunkBW = Parameters.getB2();

        int maFactor = Parameters.getMaFactor();

        int rsFactor = Parameters.getRsFactor();

        int[][][] dist = Parameters.getChunkDistribution(n * maFactor, rsFactor);

        for (int podId = 0; podId < Parameters.pods; podId++) {
            for (int rackId = 0; rackId < Parameters.racksPerPod; rackId++) {
                for (int hostId = 0; hostId < Parameters.hostsPerRack; hostId++) {
                    // Init Host
                    sub.hosts[podId][rackId][hostId].dpCosts = new double[n + 1];
                    for (int i = 0; i <= n; i++)
                        sub.hosts[podId][rackId][hostId].dpCosts[i] = Double.MAX_VALUE;
                    sub.racks[podId][rackId].chunks.addAll(sub.hosts[podId][rackId][hostId].chunks);
                }
                // Init Rack
                sub.racks[podId][rackId].dpCosts = new double[n + 1];
                for (int i = 1; i <= n; i++)
                    sub.racks[podId][rackId].dpCosts[i] = Double.MAX_VALUE;
                sub.pods[podId].chunks.addAll(sub.racks[podId][rackId].chunks);
                sub.racks[podId][rackId].dpAssignment = new int[n + 1][Parameters.hostsPerRack];
            }
            // Init Pod
            sub.pods[podId].dpCosts = new double[n + 1];
            for (int i = 1; i <= n; i++)
                sub.pods[podId].dpCosts[i] = Double.MAX_VALUE;
            sub.pods[podId].dpAssignment = new int[n + 1][Parameters.racksPerPod];

        }
        sub.dpCosts = new double[n + 1];
        for (int i = 1; i <= n; i++) {
            sub.dpCosts[i] = Double.MAX_VALUE;
        }
        sub.dpAssignment = new int[n + 1][Parameters.pods];

        // Setup done - start time measurement here

        long time = System.currentTimeMillis();

        //sub.hosts[1][3][7].uplink.capacity = 9;

        for (int podId = 0; podId < Parameters.pods; podId++) {
            for (int rackId = 0; rackId < Parameters.racksPerPod; rackId++) {
                for (int hostId = 0; hostId < Parameters.hostsPerRack; hostId++) {
                    // Update Host
                    if (podId == 1 && rackId == 3 && hostId == 7) {
                        System.out.println("DBG");
                    }
                    for (int numberOfNodes = 0; numberOfNodes <= n; numberOfNodes++) {
                        if (sub.hosts[podId][rackId][hostId].capacity < numberOfNodes) {
                            break;
                        } else {
                            double chunkTraffic = Math.abs(numberOfNodes * maFactor - sub.hosts[podId][rackId][hostId].chunks.size()) * chunkBW;
                            double niTraffic = (n - numberOfNodes) * numberOfNodes * niBw;
                            // If the host uplink does not have sufficient capacity, we keep the max value
                            if (sub.hosts[podId][rackId][hostId].uplink.capacity >= chunkTraffic + niTraffic) {
                                sub.hosts[podId][rackId][hostId].dpCosts[numberOfNodes] = chunkTraffic + niTraffic;
                            }
                        }
                    }
                }
                // Update Rack
                for (int hostId = 0; hostId < Parameters.hostsPerRack; hostId++) {
                    for (int numberOfNodes = n; numberOfNodes >= 0; numberOfNodes--) {
                        double minCost = Double.MAX_VALUE;
                        int best = 0;
                        for (int nodesOnOtherHosts = 0; nodesOnOtherHosts <= numberOfNodes; nodesOnOtherHosts++) {
                            // If the Host and Rack have a valid solution...
                            if (sub.hosts[podId][rackId][hostId].dpCosts[numberOfNodes - nodesOnOtherHosts] < Double.MAX_VALUE
                                    && sub.racks[podId][rackId].dpCosts[nodesOnOtherHosts] < Double.MAX_VALUE) {
                                if (minCost > sub.hosts[podId][rackId][hostId].dpCosts[numberOfNodes - nodesOnOtherHosts] + sub.racks[podId][rackId].dpCosts[nodesOnOtherHosts]) {
                                    minCost = sub.hosts[podId][rackId][hostId].dpCosts[numberOfNodes - nodesOnOtherHosts] + sub.racks[podId][rackId].dpCosts[nodesOnOtherHosts];
                                    best = numberOfNodes - nodesOnOtherHosts;
                                }
                            }
                        }
                        sub.racks[podId][rackId].dpCosts[numberOfNodes] = minCost;
                        if(best > 0) {
                            //for (int i = 0; i < Parameters.hostsPerRack; i++) {
                            //    sub.racks[podId][rackId].dpAssignment[numberOfNodes][i] = sub.racks[podId][rackId].dpAssignment[numberOfNodes - best][i];
                            //}
                            sub.racks[podId][rackId].dpAssignment[numberOfNodes] = sub.racks[podId][rackId].dpAssignment[numberOfNodes - best].clone();
                            sub.racks[podId][rackId].dpAssignment[numberOfNodes][hostId] += best;
                        }
                    }
                }
                // Account for costs on the Uplink...
                for (int numberOfNodes = 0; numberOfNodes <= n; numberOfNodes++) {
                    if (sub.racks[podId][rackId].dpCosts[numberOfNodes] < Double.MAX_VALUE) {
                        double chunkTraffic = Math.abs(numberOfNodes * maFactor - sub.racks[podId][rackId].chunks.size()) * chunkBW;
                        double niTraffic = (n - numberOfNodes) * numberOfNodes * niBw;
                        if (sub.racks[podId][rackId].uplink.capacity >= chunkTraffic + niTraffic) {
                            sub.racks[podId][rackId].dpCosts[numberOfNodes] += chunkTraffic + niTraffic;
                        } else {
                            sub.racks[podId][rackId].dpCosts[numberOfNodes] = Double.MAX_VALUE;
                        }
                    }
                }
            }

            // Update Pod
            for (int rackId = 0; rackId < Parameters.racksPerPod; rackId++) {
                for (int numberOfNodes = n; numberOfNodes >= 0; numberOfNodes--) {
                    double minCost = Double.MAX_VALUE;
                    int best = 0;
                    for (int nodesOnOtherRacks = 0; nodesOnOtherRacks <= numberOfNodes; nodesOnOtherRacks++) {
                        // If the Rack and Pod have a valid solution...
                        if (sub.racks[podId][rackId].dpCosts[numberOfNodes - nodesOnOtherRacks] < Double.MAX_VALUE
                                && sub.pods[podId].dpCosts[nodesOnOtherRacks] < Double.MAX_VALUE) {
                            if (minCost > sub.racks[podId][rackId].dpCosts[numberOfNodes - nodesOnOtherRacks] + sub.pods[podId].dpCosts[nodesOnOtherRacks]) {
                                minCost = sub.racks[podId][rackId].dpCosts[numberOfNodes - nodesOnOtherRacks] + sub.pods[podId].dpCosts[nodesOnOtherRacks];
                                best = numberOfNodes - nodesOnOtherRacks;
                            }
                        }
                    }
                    sub.pods[podId].dpCosts[numberOfNodes] = minCost;
                    if(best > 0) {
                        //for (int i = 0; i < Parameters.racksPerPod; i++) {
                        //    sub.pods[podId].dpAssignment[numberOfNodes][i] = sub.pods[podId].dpAssignment[numberOfNodes - best][i];
                        //}
                        sub.pods[podId].dpAssignment[numberOfNodes] = sub.pods[podId].dpAssignment[numberOfNodes - best].clone();
                        sub.pods[podId].dpAssignment[numberOfNodes][rackId] += best;
                    }
                }
            }
            for (int numberOfNodes = 0; numberOfNodes <= n; numberOfNodes++) {
                if (sub.pods[podId].dpCosts[numberOfNodes] < Double.MAX_VALUE) {
                    double chunkTraffic = Math.abs(numberOfNodes * maFactor - sub.pods[podId].chunks.size()) * chunkBW;
                    double niTraffic = (n - numberOfNodes) * numberOfNodes * niBw;
                    if (sub.pods[podId].uplink.capacity >= chunkTraffic + niTraffic) {
                        sub.pods[podId].dpCosts[numberOfNodes] += chunkTraffic + niTraffic;
                    } else {
                        sub.pods[podId].dpCosts[numberOfNodes] = Double.MAX_VALUE;
                    }
                }
            }
        }
        for (int podId = 0; podId < Parameters.pods; podId++) {
            for (int numberOfNodes = n; numberOfNodes >= 0; numberOfNodes--) {
                double minCost = Double.MAX_VALUE;
                int best = 0;
                for (int nodesOnOtherPods = 0; nodesOnOtherPods <= numberOfNodes; nodesOnOtherPods++) {
                    if (sub.pods[podId].dpCosts[numberOfNodes - nodesOnOtherPods] < Double.MAX_VALUE
                            && sub.dpCosts[nodesOnOtherPods] < Double.MAX_VALUE) {
                        if (minCost > sub.pods[podId].dpCosts[numberOfNodes - nodesOnOtherPods] + sub.dpCosts[nodesOnOtherPods]) {
                            minCost = sub.pods[podId].dpCosts[numberOfNodes - nodesOnOtherPods] + sub.dpCosts[nodesOnOtherPods];
                            best = numberOfNodes - nodesOnOtherPods;
                        }
                    }
                }
                sub.dpCosts[numberOfNodes] = minCost;
                if(best > 0) {
                    //for (int i = 0; i < Parameters.pods; i++) {
                    //    sub.dpAssignment[numberOfNodes][i] = sub.dpAssignment[numberOfNodes - best][i];
                    //}
                    sub.dpAssignment[numberOfNodes] = sub.dpAssignment[numberOfNodes - best].clone();
                    sub.dpAssignment[numberOfNodes][podId] += best;
                }
            }
        }
        System.out.println(System.currentTimeMillis() - time);
        // Compute the embedding if a valid solution exists
        if(sub.dpCosts[n] < Double.MAX_VALUE){
            Map<Host,Integer> hostsWithNodes = new HashMap<>();
            Map<Rack, Integer> racksWithNodes = new HashMap<>();
            Map<Pod, Integer> podsWithNodes = new HashMap<>();
            // Allocate the Node Interconnect
            for(int podId = 0; podId < Parameters.pods; podId++){
                int nodesOnPod = sub.dpAssignment[n][podId];
                if(nodesOnPod > 0) {
                    podsWithNodes.put(sub.pods[podId], nodesOnPod);
                    sub.pods[podId].uplink.capacity -= (n - nodesOnPod) * nodesOnPod * niBw;
                    assert sub.pods[podId].uplink.capacity >= 0 : "Invalid Embedding - Negative Resources!";
                    for(int rackId = 0; rackId < Parameters.racksPerPod; rackId++){
                        int nodesOnRack = sub.pods[podId].dpAssignment[nodesOnPod][rackId];
                        if(nodesOnRack > 0){
                            racksWithNodes.put(sub.racks[podId][rackId], nodesOnRack);
                            sub.racks[podId][rackId].uplink.capacity -= (n - nodesOnRack) * nodesOnRack * niBw;
                            assert sub.racks[podId][rackId].uplink.capacity >= 0 : "Invalid Embedding - Negative Resources";
                            for(int hostId = 0; hostId < Parameters.hostsPerRack; hostId++){
                                int nodesOnHost = sub.racks[podId][rackId].dpAssignment[nodesOnRack][hostId];
                                if(nodesOnHost > 0){
                                    sub.hosts[podId][rackId][hostId].uplink.capacity -= (n - nodesOnHost) * nodesOnHost * niBw;
                                    assert sub.hosts[podId][rackId][hostId].uplink.capacity >= 0 : "Invalid Embedding - Negative Resources";
                                    hostsWithNodes.put(sub.hosts[podId][rackId][hostId], nodesOnHost);
                                }
                            }
                        }
                    }
                }
            }
            // Allocate the Chunk Transport
            for(Map.Entry<Host,Integer> entry : hostsWithNodes.entrySet()){
                Host host = entry.getKey();
                host.matchingFlag = true;
                if(host.chunks.size() != entry.getValue()){
                    host.uplink.capacity -= Math.abs(host.chunks.size() - entry.getValue()) * chunkBW;
                    assert host.uplink.capacity >= 0 : "Invalid Embedding - Negative Resources";
                    if(!sub.racks[host.podId][host.rackId].matchingFlag){
                        Rack rack = sub.racks[host.podId][host.rackId];
                        rack.matchingFlag = true;
                        if(rack.chunks.size() != racksWithNodes.get(rack)) {
                            rack.uplink.capacity -= Math.abs(racksWithNodes.get(rack) - rack.chunks.size()) * chunkBW;
                            assert rack.uplink.capacity >= 0 : "Invalid Embedding - Negative Resources";
                            if(!sub.pods[rack.podId].matchingFlag){
                                Pod pod = sub.pods[rack.podId];
                                if(pod.chunks.size() != podsWithNodes.get(pod)){
                                    pod.uplink.capacity -= Math.abs(podsWithNodes.get(pod) - pod.chunks.size()) * chunkBW;
                                    assert pod.uplink.capacity >= 0 : "Invalid Embedding - Negative Resources";
                                }
                            }
                        }
                    }
                }
            }
            for(int chunkType = 0; chunkType < n; chunkType++){
                int podId = dist[chunkType][0][0];
                int rackId = dist[chunkType][0][1];
                int hostId = dist[chunkType][0][2];
                if(!sub.hosts[podId][rackId][hostId].matchingFlag){
                    Host host = sub.hosts[podId][rackId][hostId];
                    host.matchingFlag = true;
                    host.uplink.capacity -= host.chunks.size() * chunkBW;
                    assert host.uplink.capacity >= 0 : "Invalid Embedding - Negative Resources";
                    if(!sub.racks[podId][rackId].matchingFlag) {
                        Rack rack = sub.racks[podId][rackId];
                        rack.matchingFlag = true;
                        rack.uplink.capacity -= rack.chunks.size() * chunkBW;
                        assert rack.uplink.capacity >= 0 : "Invalid Embedding - Negative Resources";
                        if(!sub.pods[podId].matchingFlag){
                            Pod pod = sub.pods[podId];
                            pod.matchingFlag = true;
                            pod.uplink.capacity -= pod.chunks.size() * chunkBW;
                            assert pod.uplink.capacity >= 0 : "Invalid Embedding - Negative Resources";
                        }
                    }
                }
            }
            time = System.currentTimeMillis() - time;
            System.out.println("[Success] Runtime: " + time);
            return true;
        }else{
            time = System.currentTimeMillis() - time;
            System.out.println("[Reject] Runtime: " + time);
            return false;
        }
    }
}
