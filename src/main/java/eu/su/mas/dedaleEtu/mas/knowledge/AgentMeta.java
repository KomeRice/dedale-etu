package eu.su.mas.dedaleEtu.mas.knowledge;

import dataStructures.tuple.Couple;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Serializable;

import java.util.*;

public class AgentMeta implements Serializable {

    private List<String> listReceiverAgents;
    private MapRepresentation myMap;
    private Set<String> openNodes;
    private Set<String> closedNodes;
    private List<Position> interests;
    private Hashtable<String,String> blockedNodes;
    private String targetNode = null;
    private List<String> currentTrajectory;
    private String lastReceiver ="";

    private Hashtable<String,AgentSpecs> agentSpecsHashtable;
    private AgentSpecs mySpecs;

    private String myPosition ="";

    private Hashtable<String,MapData> toShare ;

    private String rdvPoint = "";
    private List<String> met;
    private List<String> blockedPath;

    private List<Position> myPlan;

    private int blockStep = -1;
    private int collectStep = -1;
    private Position targetTreasure;

    private boolean exploEnded = false;
    private boolean doPing = false;

    public AgentMeta(List<String> listReceiverAgents) {
        this.listReceiverAgents = listReceiverAgents;

        this.openNodes = new HashSet<>();
        this.closedNodes=new HashSet<>();
        this.interests = new ArrayList<>();
        this.toShare = new Hashtable<>();

        this.blockedNodes = new Hashtable<>();
        this.currentTrajectory = new LinkedList<>();

        this.agentSpecsHashtable = new Hashtable<>();
        this.met = new ArrayList<>();


    }

    public void updateMaps(String myPosition, String nodeId) {
        if (!this.getOpenNodes().contains(nodeId)) {
            openNodes.add(nodeId);
            myMap.addNode(nodeId, MapRepresentation.MapAttribute.open);
            myMap.addEdge(myPosition, nodeId);

            for (String receiver : listReceiverAgents) {
                this.toShare.computeIfAbsent(receiver, k -> new MapData()).addNode(myPosition, nodeId);
            }
        } else {
            //the node exist, but not necessarily the edge
            myMap.addEdge(myPosition, nodeId);
            for (String receiver : listReceiverAgents) {
                this.toShare.computeIfAbsent(receiver, k -> new MapData()).addEdge(myPosition, nodeId);
            }
        }
    }

    public void updatePosition(String myPosition){
        closedNodes.add(myPosition);
        openNodes.remove(myPosition);
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
            //System.out.println("CONSIDERING " + n);
            List<String> pathToNode = this.getMyMap().getShortestPath(myPosition, n);
            if(pathToNode == null)
                continue;
            boolean discard = false;
            for(String step : pathToNode){
                if(this.isNodeBlocked(step)){
                    discard = true;
                    break;
                }
            }
            if(discard) continue;
            this.setTargetNode(n, pathToNode);
            break;
        }
    }

    public boolean addInterest(Position pos){
        for (Position p : interests) {
            if (Objects.equals(p.getNodeName(), pos.getNodeName())) {
                p.updatePos(pos);
                return false;
            }
        }
        interests.add(pos);
        return true;
    }

    public void flagBlockedNode(ACLMessage msg){
        blockedNodes.put(msg.getContent(), String.valueOf(msg.getSender().getLocalName()));
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
            System.out.println("no next node");
            this.clearBlockedNodes();
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
        //System.out.println("SET TARGET TO " + targetNode);
    }

    public boolean hasTargetNode() {
        return targetNode != null;
    }

    public List<String> getListReceiverAgents() {
        return listReceiverAgents;
    }

    public Set<String> getOpenNodes() {
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
        if (mapData != null){
            this.toShare.replace(receiver,mapData.newMapFromOld());
        }else{
            this.toShare.replace(receiver,new MapData());
        }


        return mapData;
    }

    public Hashtable<String, AgentSpecs> getAgentSpecsHashtable() {
        return agentSpecsHashtable;
    }

    public void mergeMap(MapData sgreceived){
        for (String node : sgreceived.getOpenNodes()){
            if (this.myPosition.equals(node)){
                continue;
            }
            if (!this.openNodes.contains(node) && !this.closedNodes.contains(node)){
                this.openNodes.add(node);
                this.myMap.addNode(node,MapRepresentation.MapAttribute.open);
            }
        }
        for (String closed : sgreceived.getClosedNodes()){
            this.myMap.addNode(closed,MapRepresentation.MapAttribute.closed);
            this.openNodes.remove(closed);
            this.closedNodes.add(closed);
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

    public void mergeInterest(List<Position> interests){
        for (Position p : interests){
            addInterest(p);
        }
    }

    public String getRdvPoint() {
        return rdvPoint;
    }


    public void setRdvPoint(String rdvPoint) {
        if (Objects.equals(this.rdvPoint, "")){
            this.rdvPoint = rdvPoint;
        }
    }

    public void addMet(String name){
        met.add(name);
    }

    public boolean didMet(String name){
        return met.contains(name);
    }

    public void addSpecs(String name, AgentSpecs a){
        agentSpecsHashtable.computeIfAbsent(name,k->a);
    }

    public void setExploEnded(){
        exploEnded = true;
        collectStep = 0;
    }

    public int getBlockStep() {
        return blockStep;
    }

    public void setBlockStep(int blockStep) {
        this.blockStep = blockStep;
    }

    public int getCollectStep() {
        return collectStep;
    }

    public void setCollectStep(int collectStep) {
        this.collectStep = collectStep;
    }

    public List<Position> getMyPlan() {
        return myPlan;
    }

    public void setMyPlan(List<Position> myPlan) {
        this.myPlan = myPlan;
    }

    public Position getTargetTreasure() {
        return targetTreasure;
    }

    public void setTargetTreasure(Position targetTreasure) {
        this.targetTreasure = targetTreasure;
    }

    public List<String> getCurrentTrajectory() {
        return currentTrajectory;
    }

    public List<String> getBlockedPath() {
        return blockedPath;
    }

    public void setBlockedPath(List<String> blockedPath) {
        this.blockedPath = blockedPath;
    }

    public String getNextBlockedPath(){
        if(!blockedPath.isEmpty()){
            return blockedPath.remove(0);
        }
        return null;
    }

    public boolean isExploEnded() {
        return exploEnded;
    }

    public void setSpecs(AgentSpecs agentSpecs){
        this.mySpecs = agentSpecs;
    }

    public AgentSpecs getMySpecs() {
        return mySpecs;
    }

    public boolean doPing() {
        return doPing;
    }

    public void switchPing() {
        doPing = !doPing;
    }
}
