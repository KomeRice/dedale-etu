package eu.su.mas.dedaleEtu.mas.knowledge;

import jade.lang.acl.ACLMessage;
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
    private Hashtable<String,String> blockedNodes;
    private String targetNode = null;
    private List<String> currentTrajectory;

    public AgentMeta(List<String> listReceiverAgents) {
        this.listReceiverAgents = listReceiverAgents;
        this.openNodes = new ArrayList<String>();
        this.closedNodes=new HashSet<String>();
        this.interests = new ArrayList<Position>();
        this.blockedNodes = new Hashtable<>();
        this.currentTrajectory = new LinkedList<>();
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

    public void flagBlockedNode(ACLMessage msg){
        blockedNodes.put(msg.getContent(), String.valueOf(msg.getSender()));
    }

    public boolean isNodeBlocked(String node){
        return blockedNodes.containsKey(node);
    }

    public void clearBlockedNodes(){
        blockedNodes.clear();
    }

    public void setTargetReached(){
        this.targetNode = null;
    }

    public String getNextNode(){
        String out = this.currentTrajectory.remove(0);
        if(this.currentTrajectory.isEmpty()){
            this.setTargetReached();
        }
        return out;
    }

    public String getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(String targetNode, List<String> path) {
        this.targetNode = targetNode;
        this.currentTrajectory = path;
    }

    public boolean hasTargetNode() {
        return targetNode != null;
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
