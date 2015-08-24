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

        DP_MA_FP_NI_BW algo1 = new DP_MA_FP_NI_BW();
        algo1.embed();

    }
}
