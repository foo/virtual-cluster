package Substrate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Parent class for all nodes in the fat tree substrate
 * Created by carlo on 8/5/15.
 */
public class Node {

    public Collection<Edge> edges;
    public String id;
    public int capacity;
    public int[] dpCosts;
    public int[][] dpAssignment;
    public List<Integer> chunks;
    public Edge uplink;

    public boolean matchingFlag = false;

    public Node(){
        edges = new ArrayList<>();
        id = "";
        capacity = 0;
        chunks = new ArrayList<>();
    }
}
