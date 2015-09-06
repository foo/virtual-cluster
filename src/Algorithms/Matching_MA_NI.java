package Algorithms;

import Core.DefaultHashMap;
import Core.Parameters;
import Core.Simulator;
import Substrate.Host;
import Substrate.Pod;
import Substrate.Rack;
import Substrate.Substrate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Created by carlo on 9/4/15.
 */
public class Matching_MA_NI {

    public boolean embed() {

        File yourFile = new File("matching.log");
        if(!yourFile.exists()) {
            try {
                yourFile.createNewFile();
            } catch (IOException e) {
                System.out.println("[Error] Could not create logfile: dp.log");
                e.printStackTrace();
            }
        }

        Substrate sub = Simulator.sub;

        Parameters.RS = false;
        Parameters.MA = false;

        int n = Parameters.getN();
        int niBw = Parameters.getB1();
        int chunkBW = Parameters.getB2();

        int maFactor = Parameters.getMaFactor();

        int rsFactor = Parameters.getRsFactor();

        int[][][] dist = Parameters.getChunkDistribution(n * maFactor, rsFactor);

        Map<Host,Integer> hostsWithNodes = new DefaultHashMap<>(0);
        Map<Rack, Integer> racksWithNodes = new DefaultHashMap<>(0);
        Map<Pod, Integer> podsWithNodes = new DefaultHashMap<>(0);

        for(int nodeId = 0; nodeId < n; nodeId++){
            int podId = Parameters.random.nextInt(Parameters.pods);
            int rackId = Parameters.random.nextInt(Parameters.racksPerPod);
            int hostId = Parameters.random.nextInt(Parameters.hostsPerRack);

            if(hostsWithNodes.get(sub.hosts[podId][rackId][hostId]) < sub.hosts[podId][rackId][hostId].capacity) {
                podsWithNodes.put(sub.pods[podId], podsWithNodes.get(sub.pods[podId]) + 1);
                racksWithNodes.put(sub.racks[podId][rackId], racksWithNodes.get(sub.racks[podId][rackId]) + 1);
                hostsWithNodes.put(sub.hosts[podId][rackId][hostId], hostsWithNodes.get(sub.hosts[podId][rackId][hostId]) + 1);
            }
        }

        long time = System.currentTimeMillis();

        // Allocate the Chunk Transport
        for(Map.Entry<Host,Integer> entry : hostsWithNodes.entrySet()){
            Host host = entry.getKey();
            host.matchingFlag = true;
            if(host.chunks.size() != entry.getValue()){
                host.uplink.capacity -= Math.abs(host.chunks.size() - entry.getValue()) * chunkBW;
                if (host.uplink.capacity <= 0)
                    return fail(time, n);
                if(!sub.racks[host.podId][host.rackId].matchingFlag){
                    Rack rack = sub.racks[host.podId][host.rackId];
                    rack.matchingFlag = true;
                    if(rack.chunks.size() != racksWithNodes.get(rack)) {
                        rack.uplink.capacity -= Math.abs(racksWithNodes.get(rack) - rack.chunks.size()) * chunkBW;
                         if (rack.uplink.capacity <= 0)
                             return fail(time, n);
                        if(!sub.pods[rack.podId].matchingFlag){
                            Pod pod = sub.pods[rack.podId];
                            pod.matchingFlag = true;
                            if(pod.chunks.size() != podsWithNodes.get(pod)){
                                pod.uplink.capacity -= Math.abs(podsWithNodes.get(pod) - pod.chunks.size()) * chunkBW;
                                if(pod.uplink.capacity <= 0){
                                    return fail(time, n);
                                }
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
                int dif = host.chunks.size() - hostsWithNodes.get(host);
                if(dif > 0) {
                    host.uplink.capacity -= dif * chunkBW;
                    if(host.uplink.capacity <= 0)
                        return fail(time, n);
                    if (!sub.racks[podId][rackId].matchingFlag) {
                        Rack rack = sub.racks[podId][rackId];
                        rack.matchingFlag = true;
                        dif = rack.chunks.size() - racksWithNodes.get(rack);
                        if(dif > 0) {
                            rack.uplink.capacity -= dif * chunkBW;
                            if (rack.uplink.capacity <= 0)
                                return fail(time,n);
                            if (!sub.pods[podId].matchingFlag) {
                                Pod pod = sub.pods[podId];
                                pod.matchingFlag = true;
                                dif = pod.chunks.size() - podsWithNodes.get(pod);
                                pod.uplink.capacity -= dif * chunkBW;
                                if(pod.uplink.capacity <= 0)
                                    return fail(time,n);
                            }
                        }
                    }
                }
            }
        }
        time = System.currentTimeMillis() - time;
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date today = Calendar.getInstance().getTime();
        try {
            Files.write(Paths.get("matching.log"), (df.format(today) + "\t1\t" + n + "\t" + Parameters.hostsPerRack + "\t" + time + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e){
            System.out.println("[Error] Cannot append to logfile: matching.log");
            e.printStackTrace();
        }
        return true;
    }

    private boolean fail(long time, int n){
        time = System.currentTimeMillis() - time;
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date today = Calendar.getInstance().getTime();
        try {
            Files.write(Paths.get("matching.log"), (df.format(today) + "\t0\t" + n + "\t" + Parameters.hostsPerRack + "\t" + time + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e){
            System.out.println("[Error] Cannot append to logfile: matching.log");
            e.printStackTrace();
        }
        return false;
    }
}
