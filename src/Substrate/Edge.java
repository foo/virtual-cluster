package Substrate;

import Core.Parameters;

/**
 * Links in the Substrate.
 * Created by carlo on 8/5/15.
 */
public class Edge {
    private Node[] nodes;
    public int capacity;


    public Edge(Node n1, Node n2){
        nodes = new Node[2];
        nodes[0] = n1;
        nodes[1] = n2;
        n1.edges.add(this);
        n2.uplink = this;


        this.capacity = Parameters.minEdgeRes + Parameters.random.nextInt(Parameters.maxEdgeRes - Parameters.minEdgeRes + 1);
    }
}
