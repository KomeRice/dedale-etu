package eu.su.mas.dedaleEtu.mas.knowledge;

import dataStructures.tuple.Couple;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Serializable;

import java.time.Instant;
import java.util.*;

/** Classe de tous les donnes dont l'agent a besoin
 * */
public class AgentMeta implements Serializable {

    /**Liste des autres agents*/
    private List<String> listReceiverAgents;
    /**Representation graphique de la carte*/
    private MapRepresentation myMap;
    /**Noeuds non exploré*/
    private Set<String> openNodes;
    /**Noeud exploré*/
    private Set<String> closedNodes;
    /**Liste des ressources enregistré */
    private List<Position> interests;
    /**Liste des noeuds occupés */
    private Hashtable<String,String> blockedNodes;
    /**Noeud cible */
    private String targetNode = null;
    /**Trajectoire vers le noeud cible */
    private List<String> currentTrajectory;
    /**Dernier correspondant */
    private String lastReceiver ="";

    /**Temps de debut de l'exploration */
    private final long explorationStart;
    /**Temps de timeout */
    private final int explorationTimeout = 240000;

    /**Table des characteristiques des agents */
    private Hashtable<String,AgentSpecs> agentSpecsHashtable;
    /**Mes characteristiques */
    private AgentSpecs mySpecs;
    /**Ma position */
    private String myPosition ="";
    /**Table des agents et de leurs carte depuis la derniere rencontre */
    private Hashtable<String,MapData> toShare ;

    /**position de regroupement(non utilisé) */
    private String rdvPoint = "";
    /**Liste des agents croisée */
    private List<String> met;
    /**Chemin de l'agent bloqué */
    private List<String> blockedPath;

    /**Mon plan de collecte */
    private List<Position> myPlan;

    /**etape du processus de blocage */
    private int blockStep = -1;
    /**etape du processus de collecte */
    private int collectStep = -1;
    /**tresor cible */
    private Position targetTreasure;

    /**Exploration fini ou pas */
    private boolean exploEnded = false;
    /**Exploration : ping ou pas */
    private boolean doPing = false;
    /**Collecte fini ou pas */
    private boolean finished = false;

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

        this.explorationStart = Instant.now().toEpochMilli();
    }

    /**Met a jour la carte et la liste des cartes avec les information des voisins */
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
    /**Met a jour la position courante dans les cartes */
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

    /**Trouve le chemin vers le noeud non bloque le plus proche */
    public void findTrajectory(String myPosition){
        List<String> shortestPath = null;
        String node = null;
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
            if (shortestPath == null){
                shortestPath = pathToNode;
                node = n;
            }
            if (shortestPath.size() > pathToNode.size()){
                shortestPath = pathToNode;
                node = n;
            }

        }
        this.setTargetNode(node, shortestPath);
    }

    /**Ajoute une ressource dans la liste*/
    public void addInterest(Position pos){
        for (Position p : interests) {
            if (Objects.equals(p.getNodeName(), pos.getNodeName())) {
                p.updatePos(pos);
                return;
            }
        }
        interests.add(pos);
    }

    /**Ajoute un noeud dans la liste des noeuds bloqués */
    public void flagBlockedNode(ACLMessage msg){
        blockedNodes.put(msg.getContent(), String.valueOf(msg.getSender().getLocalName()));
    }

    public void addBlockedNode(String node){
        if (node != null) {
            blockedNodes.put(node, "WUMPUS");
        }
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

    /**Retourne le prochain noeud non bloqué du chemin */
    public String getNextNode(){
        String out;
        try{
            out = this.currentTrajectory.remove(0);
        }
        catch (IndexOutOfBoundsException e){
            System.out.println("no next node");
            this.clearBlockedNodes();
            out = "";
        }catch (NullPointerException e1){
            this.clearBlockedNodes();
            out = "";
        }
        return out;
    }

    /**Annule un mouvemenet */
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

    public boolean noTargetNode() {
        return targetNode == null;
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

    /**Retourne la carte de l'agent depuis la derniere rencontre et la remplace par une nouvelle qui contient que les noeuds ouverts de l'ancien */
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

    /**Fussionne la carte recu dans sa propre carte */
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

    /**Fusionne les points de ressources */
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

    /**Ajoute dans la liste des rencontres */
    public void addMet(String name){
        met.add(name);
    }

    public boolean didMet(String name){
        return met.contains(name);
    }
    /**Ajoute dans la table des characteristiques */
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

    public boolean hasExplorationTimedOut() {
        return Instant.now().toEpochMilli() >= explorationStart + explorationTimeout;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished() {
        this.finished = true;
    }
}
