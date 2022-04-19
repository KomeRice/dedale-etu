package eu.su.mas.dedaleEtu.mas.knowledge;

import dataStructures.tuple.Couple;
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
    private String lastReceiver ="";
    private String myPosition ="";

    private Hashtable<String,MapData> toShare ;

    public AgentMeta(List<String> listReceiverAgents) {
        this.listReceiverAgents = listReceiverAgents;
        this.openNodes = new ArrayList<String>();
        this.closedNodes=new HashSet<String>();
        this.interests = new ArrayList<Position>();
        this.blockedNodes = new Hashtable<>();
        this.currentTrajectory = new LinkedList<>();

        this.toShare = new Hashtable<>();

    }

    public void updateMaps(String myPosition, String nodeId){
        if (!this.getOpenNodes().contains(nodeId)) {
            this.getOpenNodes().add(nodeId);
            this.getMyMap().addNode(nodeId, MapRepresentation.MapAttribute.open);
            this.getMyMap().addEdge(myPosition, nodeId);

            for (String receiver : listReceiverAgents){
                this.toShare.computeIfAbsent(receiver,k-> new MapData()).addNode(myPosition,nodeId);
            }
        } else {
            //the node exist, but not necessarily the edge
            this.getMyMap().addEdge(myPosition, nodeId);
            for (String receiver : listReceiverAgents){
                this.toShare.computeIfAbsent(receiver,k-> new MapData()).addEdge(myPosition,nodeId);
            }
        }
    }

    public void updatePosition(String myPosition){
        this.getClosedNodes().add(myPosition);
        this.getOpenNodes().remove(myPosition);
        for (String receiver : listReceiverAgents){
            this.toShare.computeIfAbsent(receiver,k-> new MapData()).addNewPosition(myPosition);
        }
    }

    public List<Position> getInterests() {
        return interests;
    }

    public boolean isTrajectoryEmpty(){
        return currentTrajectory.isEmpty();
    }

    public void findTrajectory(String myPosition){
        for(String n : this.getOpenNodes()){
            if(n.equals(myPosition))
                continue;
            System.out.println("CONSIDERING " + n);
            List<String> pathToNode = this.getMyMap().getShortestPath(myPosition, n);
            if(pathToNode == null)
                continue;
            boolean discard = false;
            for(String step : pathToNode){
                if(this.isNodeBlocked(step)){
                    discard = true;
                    break;
                };
            }
            if(discard) continue;
            this.setTargetNode(n, pathToNode);
            break;
        }
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
        String out;
        try{
            out = this.currentTrajectory.remove(0);
        }
        catch (IndexOutOfBoundsException e){
            System.out.println("Died");
            out = "";
        }
        return out;
    }

    public void cancelMove(String node){
        this.currentTrajectory.add(0,node);
    }

    public String getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(String targetNode, List<String> path) {
        this.targetNode = targetNode;
        this.currentTrajectory = path;
        System.out.println("SET TARGET TO " + targetNode);
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

    public void setLastReceiver(String lastReceiver) {
        this.lastReceiver = lastReceiver;
    }

    public String getLastReceiver() {
        return lastReceiver;
    }

    public MapData getToSendMap(String receiver){
        MapData mapData =  this.toShare.get(receiver);
        this.toShare.replace(receiver,new MapData());
        return mapData;
    }

    public void mergeMap(MapData sgreceived){
        for (String node : sgreceived.getOpenNodes()){
            if (this.myPosition.equals(node)){
                continue;
            }
            this.myMap.addNode(node,MapRepresentation.MapAttribute.open);
            if (!this.openNodes.contains(node) && !this.closedNodes.contains(node)){
                this.openNodes.add(node);
            }
        }
        for (String closed : sgreceived.getClosedNodes()){
            this.myMap.addNode(closed,MapRepresentation.MapAttribute.closed);
            if(this.openNodes.contains(closed)){
                this.openNodes.remove(closed);
            }
            if (!this.closedNodes.contains(closed)){
                this.closedNodes.add(closed);
            }
        }
        for (Couple<String,String> c : sgreceived.getEdges()){
            this.myMap.addEdge(c.getLeft(),c.getRight());
        }

    }
    public String getMyPosition() {
        return myPosition;
    }

    public void setMyPosition(String myPosition) {
        this.myPosition = myPosition;
    }
}
