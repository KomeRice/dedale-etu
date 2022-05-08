package eu.su.mas.dedaleEtu.mas.knowledge;

import dataStructures.tuple.Couple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Classe modélisant les donnes de la carte
 * Utilisé pour creer des parties de cartes a envoyer
 * */
public class MapData implements Serializable {
    /**Noeuds ouverts*/
    private Set<String> openNodes;
    /**Noeuds fermés*/
    private Set<String> closedNodes;
    /**Liste des arcs*/
    private List<Couple<String,String>> edges;

    public MapData() {
        this.openNodes = new HashSet<>();
        this.edges = new ArrayList<>();
        this.closedNodes = new HashSet<>();
    }
    public MapData(Set<String> openNodes){
        this.openNodes = openNodes;
        this.edges = new ArrayList<>();
        this.closedNodes = new HashSet<>();
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

    public Set<String> getOpenNodes() {
        return openNodes;
    }

    public List<Couple<String, String>> getEdges() {
        return edges;
    }

    public Set<String> getClosedNodes() {
        return closedNodes;
    }
}
