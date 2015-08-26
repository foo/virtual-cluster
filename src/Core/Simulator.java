package Core;

import Algorithms.DP_MA_FP_NI_BW;
import Substrate.*;

import java.util.Random;

/**
 * Entry Point
 * Created by carlo on 8/5/15.
 */
public class Simulator {

    public static Substrate sub;

    public static void main(String[] args){

        Parameters.random = new Random(Parameters.randomseed);

        sub = new Substrate();

        //DP_MA_FP_NI_BW algo1 = new DP_MA_FP_NI_BW();
        //algo1.embed();

        for(Parameters.hostsPerRack = 4; Parameters.hostsPerRack < 1024; Parameters.hostsPerRack *= 2){
            for(int n = 8; n < 1024; n *= 2){
                Parameters.setN(n);
                for(int i = 0; i < Parameters.iterations; i++){
                    System.out.println(i);
                    if(i == 53 && Parameters.getN() == 512 && Parameters.hostsPerRack == 4)
                        System.out.println("DBG");
                    sub = new Substrate();
                    DP_MA_FP_NI_BW algo = new DP_MA_FP_NI_BW();
                    algo.embed();
                }

            }
        }

        for(Parameters.hostsPerRack = 4; Parameters.hostsPerRack < 1024; Parameters.hostsPerRack *= 2){
            for(int n = 100; n < 1000; n += 100){
                Parameters.setN(n);
                for(int i = 0; i < Parameters.iterations; i++){

                    sub = new Substrate();
                    DP_MA_FP_NI_BW algo = new DP_MA_FP_NI_BW();
                    algo.embed();
                }

            }
        }
    }
}
