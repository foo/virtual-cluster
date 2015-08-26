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

    public static int pods = 16;
    public static int racksPerPod = 32;
    public static int hostsPerRack = 256;

    public static int iterations = 100;

    public static int maxHostRes = 4;
    public static int minHostRes = 0;

    public static int maxEdgeRes = 1500;
    public static int minEdgeRes = 0;

    public static int minMaFactor = 2;
    public static int maxMaFactor = 10;

    public static int TOR_TO_AGG_OVERSUBSCRIPTION = 2;
    public static int AGG_TO_CORE_OVERSUBSCRIPTION = 2;

    public static int getMaFactor(){
        if(MA)
            return minMaFactor + random.nextInt(maxMaFactor - minMaFactor + 1);
        else
            return 1;
    }

    public static boolean printDistribution = false;

    public static int minRsFactor = 2;
    public static int maxRsFactor = 10;

    public static int getRsFactor(){
        if(RS)
            return minRsFactor + random.nextInt(maxRsFactor - minRsFactor + 1);
        else
            return 1;
    }

    private static int n = 1024;

    public static void setN(int n){
        Parameters.n = n;
    }

    public static int getN(){
        return n;
    }

    public static int getB1(){
        return 1;
    }

    public static int getB2(){
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
