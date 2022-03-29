package eu.su.mas.dedaleEtu.mas.knowledge;

import jade.util.leap.Serializable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AgentMeta implements Serializable{
    private List<String> list_agentNames;
    private MapRepresentation myMap;
    private List<String> openNodes;
    private Set<String> closedNodes;

    public AgentMeta(List<String> list_agentNames) {
        this.list_agentNames = list_agentNames;
        this.openNodes = new ArrayList<String>();
        this.closedNodes=new HashSet<String>();
    }

    public List<String> getList_agentNames() {
        return list_agentNames;
    }

    public List<String> getOpenNodes() {
        return openNodes;
    }

    public Set<String> getClosedNodes() {
        return closedNodes;
    }

    public void setList_agentNames(List<String> list_agentNames) {
        this.list_agentNames = list_agentNames;
    }

    public void setMyMap(MapRepresentation myMap) {
        this.myMap = myMap;
    }

    public MapRepresentation getMyMap() {
        return myMap;
    }





}
