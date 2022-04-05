package eu.su.mas.dedaleEtu.mas.knowledge;

import jade.util.leap.Serializable;

import java.util.*;

public class AgentMeta implements Serializable {
    //todo: smart send/receive feature, delta? register already sent info and send the differences depending on receiver
    //todo: universal message format, taking maps, wumpus, positions and else into account
    private List<String> listReceiverAgents;
    private MapRepresentation myMap;
    private List<String> openNodes;
    private Set<String> closedNodes;
    private List<Position> interests;

    public AgentMeta(List<String> listReceiverAgents) {
        this.listReceiverAgents = listReceiverAgents;
        this.openNodes = new ArrayList<String>();
        this.closedNodes=new HashSet<String>();
        this.interests = new ArrayList<Position>();
    }

    public List<Position> getInterests() {
        return interests;
    }

    public boolean addInterest(Position pos){
        for(Position p : interests){
            if(Objects.equals(p.getNodeName(), pos.getNodeName())){
                return false;
            }
        }

        interests.add(pos);
        return true;
    }

    public List<String> getListReceiverAgents() {
        return listReceiverAgents;
    }

    public List<String> getOpenNodes() {
        return openNodes;
    }

    public Set<String> getClosedNodes() {
        return closedNodes;
    }

    public void setListReceiverAgents(List<String> listReceiverAgents) {
        this.listReceiverAgents = listReceiverAgents;
    }

    public void setMyMap(MapRepresentation myMap) {
        this.myMap = myMap;
    }

    public MapRepresentation getMyMap() {
        return myMap;
    }
}
