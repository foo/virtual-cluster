package Core;

import java.util.Random;

/**
 * Global Config - Use only this random to ensure repeatability
 * Created by carlo on 8/5/15.
 */
public class Parameters {


    public static long randomseed = 1234567890;

    public static Random random;

    public static boolean MA = true;
    public static boolean RS = false;

    public static int pods = 10;
    public static int racksPerPod = 48;
    public static int hostsPerRack = 48;

    public static int maxHostRes = 4;
    public static int minHostRes = 0;

    public static double maxEdgeRes = 1000;
    public static double minEdgeRes = 0;

    public static int minMaFactor = 2;
    public static int maxMaFactor = 10;

    public static double TOR_TO_AGG_OVERSUBSCRIPTION = 2;
    public static double AGG_TO_CORE_OVERSUBSCRIPTION = 2;

    public static int getMaFactor(){
        if(MA)
            return minMaFactor + random.nextInt(maxMaFactor - minMaFactor + 1);
        else
            return 1;
    }

    public static boolean printDistribution = true;

    public static int minRsFactor = 2;
    public static int maxRsFactor = 10;

    public static int getRsFactor(){
        if(RS)
            return minRsFactor + random.nextInt(maxRsFactor - minRsFactor + 1);
        else
            return 1;
    }


    public static int getN(){
        return 100;
    }

    public static double getB1(){
        return 1;
    }

    public static double getB2(){
        return 1;
    }

    private static void printDistribution(int[][][] dist){
        int chunkTypes = dist.length;
        int replicas = dist[0].length;
        for(int chunkType  = 0; chunkType < chunkTypes; chunkType++){
            for(int replica = 0; replica < replicas; replica++) {
                System.out.println("ChunkType " + chunkType + " Replica " + replica + " is located on Pod "
                 + dist[chunkType][replica][0] + " Rack " + dist[chunkType][replica][1] + " Host " + dist[chunkType][replica][2]);
            }
        }
        System.out.println("");

    }

    public static int[][][] getChunkDistribution(int chunkTypes, int replicas){
        int[][][] dist = new int[chunkTypes][replicas][3];
        for(int chunkType = 0; chunkType < chunkTypes; chunkType ++){
            for(int replica = 0; replica < replicas; replica++) {
                int podId = random.nextInt(pods);
                int rackId = random.nextInt(racksPerPod);
                int hostId = random.nextInt(hostsPerRack);

                if(Simulator.sub.hosts[podId][rackId][hostId].chunks.contains(chunkType)) {
                    replica--;
                }else{
                    Simulator.sub.hosts[podId][rackId][hostId].chunks.add(chunkType);
                    int[] tmp = {podId,rackId,hostId};
                    dist[chunkType][replica]= tmp;
                }
            }
        }
        if(printDistribution)
            printDistribution(dist);
        return dist;
    }


}
