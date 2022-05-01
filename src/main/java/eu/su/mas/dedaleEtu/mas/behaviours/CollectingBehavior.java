package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentMeta;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentSpecs;
import eu.su.mas.dedaleEtu.mas.knowledge.Position;
import jade.core.behaviours.OneShotBehaviour;

import java.util.*;

public class CollectingBehavior extends OneShotBehaviour {
    private AgentMeta info;
    private int state;
    private Hashtable<String, AgentSpecs> agentSpecsHashtable;
    private List<Position> interest;
    private int blockedCounter = 0;

    //step 1 return to rdv point
    //step 2 share prio and backpack capacity
    //step 3 the highest prio do the plan :
        //the backpack capacity the closest to the resources
    //step 4 go to the assigned locations
        //if encountering someone not in the plan, tell him which resource are taken, and the rdv point
        //if 2 different teams member encounter the highest prio one take the resources,
            // and the lowest one recalculate another path,
        //if encounter a not repertoiried node register the resource
    //step 5
        //if backpack full done
        //if not full (treasure stolen or not enough info)-> return to the rdv point to wait


    public CollectingBehavior(AbstractDedaleAgent a, AgentMeta info) {
        super(a);
        this.info = info;
        this.agentSpecsHashtable = info.getAgentSpecsHashtable();
        this.interest = info.getInterests();
    }

    @Override
    public void action() {
        state = 0;
        String nextPos;
        switch (info.getCollectStep()){
            case 0:
                if (!info.hasTargetNode()){
                    info.setTargetNode(info.getRdvPoint(),info.getMyMap().getShortestPath(info.getMyPosition(), info.getRdvPoint()));
                }
                nextPos = info.getNextNode();
                if (((AbstractDedaleAgent) this.myAgent).moveTo(nextPos)) {
                    if (nextPos.equals(info.getTargetNode())){
                        info.setTargetReached();
                        info.setCollectStep(1);
                    }
                }
                break;


            case 1:
                info.setMyPlan(makePlan().get(myAgent.getLocalName()));
                info.setCollectStep(2);
                info.setTargetReached();
                break;


            case 2:
                if (!info.hasTargetNode()){
                    if (info.getMyPlan().isEmpty()){
                        info.setCollectStep(0);
                        System.out.println("Plan vide");
                        for(Couple<Observation,Integer> c : ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace()){
                            if(c.getLeft() == agentSpecsHashtable.get(myAgent.getLocalName()).getType()){
                                if(c.getRight() == 0){
                                    System.out.println("BACKPACK FULL");
                                    state = -1;//finished
                                }
                            }
                        }
                    }
                    info.setMyPosition(((AbstractDedaleAgent) myAgent).getCurrentPosition());
                    Position target = info.getMyPlan().remove(0);
                    info.setTargetTreasure(target);
                    info.setTargetNode(target.getNodeName(),info.getMyMap().getShortestPath(info.getMyPosition(),target.getNodeName()));
                }
                nextPos = info.getNextNode();
                if (((AbstractDedaleAgent) this.myAgent).moveTo(nextPos)) {
                    if (nextPos.equals(info.getTargetNode())){
                        info.setTargetReached();
                        ((AbstractDedaleAgent) this.myAgent).openLock(info.getTargetTreasure().getTreasureType());
                        ((AbstractDedaleAgent) this.myAgent).pick();
                    }
                }else {
                    this.info.cancelMove(nextPos);
                    this.blockedCounter = this.blockedCounter + 1;
                    if (blockedCounter == 20) {
                        info.setBlockStep(1);
                        state = 3; //Blocked
                    }
                }

        }
    }

    @Override
    public int onEnd() {
        return state;
    }

    public Hashtable<String,List<Position>> makePlan(){
        List<String> agents = prioritySort();
        Position toAdd;
        Hashtable<String,List<Position>> missions = new Hashtable<>();
        boolean repass = true;
        while (repass){
            repass = false;
            for (String agent : agents){
                AgentSpecs specs = agentSpecsHashtable.get(agent);

                if(specs.getCap()>getTheoricalSpace(missions.get(agent))){
                    repass = true;
                }else {
                    continue;
                }

                if (specs.getType() == Observation.DIAMOND){
                    toAdd = findClosest(interest,specs.getDiamondCap(),-1);
                }else if (specs.getType() == Observation.GOLD){
                    toAdd = findClosest(interest,-1, specs.getGoldCap());
                }else {
                    toAdd = findClosest(interest,specs.getDiamondCap(),specs.getGoldCap());
                    Observation treasureType = toAdd.getTreasureType();
                    if (treasureType == Observation.DIAMOND ){
                        specs.setType(Observation.DIAMOND);
                    }else {
                        specs.setType(Observation.GOLD);
                    }
                }
                missions.computeIfAbsent(agent,k->new ArrayList<>()).add(toAdd);
                int v = toAdd.getTreasureValue();
                if (v < specs.getCap()){
                    interest.remove(toAdd);
                }else {
                    toAdd.setTreasureValue(v-specs.getCap());
                }

                if (interest.isEmpty()){
                    return missions;
                }
            }
        }

        return missions;
    }

    public int getTheoricalSpace(List<Position> pos){
        if (pos == null) return 0;
        int sum =0;
        for (Position p : pos){
            sum = sum + p.getTreasureValue();
        }
        return sum;
    }

    public List<String> prioritySort(){
        ArrayList<Integer> prio = new ArrayList<>();
        List<String> sorted = new ArrayList<>();
        Hashtable<Integer,String> temp = new Hashtable<>();

        for (Map.Entry<String,AgentSpecs> entry : agentSpecsHashtable.entrySet()){
            String name = entry.getKey();
            AgentSpecs specs = entry.getValue();
            temp.put(specs.getPrio(),name);
            prio.add(specs.getPrio());
        }

        Collections.sort(prio);
        Collections.reverse(prio);

        for (int p : prio){
            sorted.add(temp.get(p));
        }
        return sorted;
    }

    public Position findClosest(List<Position> interest, int diamondCap,int goldCap){
        Position bestPosGold = null;
        Position bestPosDiamond = null;
        for (Position p : interest){
            Observation type = p.getTreasureType();
            if (type == Observation.GOLD) {
                if (goldCap == -1){
                    continue;
                }
                if (bestPosGold == null){
                    bestPosGold = p;
                }else{
                    bestPosGold = getClosest(bestPosGold,p,goldCap);
                }
            } else {
                if (diamondCap == -1){
                    continue;
                }
                if (bestPosDiamond == null){
                    bestPosDiamond = p;
                }else{
                    bestPosDiamond = getClosest(bestPosDiamond,p,diamondCap);
                }
            }
        }
        if (diamondCap == -1){
            return bestPosGold;
        }
        if (goldCap == -1){
            return bestPosDiamond;
        }

        if (bestPosDiamond.getTreasureValue()>bestPosGold.getTreasureValue()){
            return bestPosDiamond;
        }
        return bestPosGold;
    }

    public Position getClosest(Position p1, Position p2 , int value){
        if (p1.getTreasureValue() -value >= p2.getTreasureValue()-value){
            return p2;
        }else {
            return p1;
        }
    }



}
