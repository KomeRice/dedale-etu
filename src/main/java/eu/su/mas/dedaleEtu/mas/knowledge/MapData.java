package eu.su.mas.dedaleEtu.mas.knowledge;

import dataStructures.tuple.Couple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MapData implements Serializable {

    private List<String> openNodes;
    private List<String> closedNodes;
    private List<Couple<String,String>> edges;

    public MapData() {
        this.openNodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.closedNodes = new ArrayList<>();
    }
    public MapData(List<String> openNodes){
        this.openNodes = openNodes;
        this.edges = new ArrayList<>();
        this.closedNodes = new ArrayList<>();
    }

    public MapData newMapFromOld(){
        MapData m = new MapData(openNodes);
        for (Couple<String,String> c : edges){
            if (m.getOpenNodes().contains(c.getRight()) || m.getOpenNodes().contains(c.getLeft()) ){
                m.addEdge(c.getLeft(),c.getRight());
            }
        }
        return m;
    }

    public void addNewPosition(String myPosition){
        this.openNodes.remove(myPosition);
        this.closedNodes.add(myPosition);
    }

    public void addNode(String myPosition, String node){
        this.openNodes.add(node);
        this.addEdge(myPosition,node);
    }

    public void addEdge(String myPosition,String node){
        this.edges.add(new Couple<>(myPosition,node));
    }

    public List<String> getOpenNodes() {
        return openNodes;
    }

    public List<Couple<String, String>> getEdges() {
        return edges;
    }

    public List<String> getClosedNodes() {
        return closedNodes;
    }
}
