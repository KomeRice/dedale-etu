package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentMeta;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentSpecs;
import eu.su.mas.dedaleEtu.mas.knowledge.Position;
import jade.core.behaviours.OneShotBehaviour;
import org.glassfish.pfl.dynamic.copyobject.impl.FallbackObjectCopierImpl;

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
        state = 2;
        String nextPos;
        info.setMyPosition(((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
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
                if (stillTreasure(info.getMySpecs().getType())){
                    Observation a = info.getMySpecs().getType();
                    state = -1;//NO MORE TREASURE OF THIS TYPE FINISHED
                    info.setFinished();
                    System.out.println("BACKPACK not full Finished");
                    return;
                }
                info.setTargetReached();
                info.setMyPlan(makePlan().get(myAgent.getLocalName()));

                if(info.getMyPlan() == null){
                    System.out.println("PLAN NON GENERE");
                    for (Couple<Observation,Integer> c :((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace()){
                        if (c.getLeft() == info.getMySpecs().getType()){
                            if(c.getRight() == 0){
                                System.out.println("BACKPACK FULL Finished");
                                state = -1;//finished
                                info.setFinished();
                                return;
                            }
                            info.getMySpecs().setRessources(c.getRight());
                        }
                    }

                    info.setMyPlan(makePlanSolo());
                }
                info.setCollectStep(2);
                break;

            case 2:
                try {
                    this.myAgent.doWait(300);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (info.getMyPosition().equals(info.getTargetNode())){
                    info.setTargetReached();
                    ((AbstractDedaleAgent) this.myAgent).openLock(info.getTargetTreasure().getTreasureType());
                    ((AbstractDedaleAgent) this.myAgent).pick();
                    System.out.println(myAgent.getLocalName() + "  Picked Treasure");
                }
                if (!info.hasTargetNode()){
                    if (info.getMyPlan().isEmpty()){
                        info.setCollectStep(-1);
                        System.out.println("Plan vide");
                        for(Couple<Observation,Integer> c : ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace()){
                            if(c.getLeft() == info.getMySpecs().getType()){
                                if(c.getRight() == 0){
                                    System.out.println("BACKPACK FULL Finished");
                                    state = -1;//finished
                                    info.setFinished();
                                    return;
                                }else {
                                    System.out.println("BACKPACK not full Finished");
                                    info.getInterests().remove(info.getTargetTreasure());
                                    agentSpecsHashtable.get(myAgent.getLocalName()).setRessources(c.getRight());
                                    info.setCollectStep(1);
                                }
                                break;
                            }
                        }
                    }else {
                        Position target = info.getMyPlan().remove(0);
                        info.setTargetTreasure(target);
                        info.setTargetNode(target.getNodeName(), info.getMyMap().getShortestPath(info.getMyPosition(), target.getNodeName()));
                    }
                }
                List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
                Position.GeneratePositionFromObservations(lobs,info);

                nextPos = info.getNextNode();
                if (!Objects.equals(nextPos, "")){
                    if (((AbstractDedaleAgent) this.myAgent).moveTo(nextPos)) {
                        this.blockedCounter = 0;
                    }else{
                        this.info.cancelMove(nextPos);
                        this.blockedCounter = this.blockedCounter + 1;
                        if (blockedCounter == 20) {
                            info.setBlockStep(1);
                            state = 3; //Blocked
                        }
                    }
                }else {
                    this.blockedCounter++;
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

                if(specs.getCap()!=0){
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
                if (toAdd == null){
                    System.out.println("ERROR ON CALCULATING");
                }
                missions.computeIfAbsent(agent,k->new ArrayList<>()).add(toAdd);
                int v = toAdd.getTreasureValue();
                if (v < specs.getCap()){
                    interest.remove(toAdd);
                    specs.appointRessources(v);
                }else {
                    toAdd.setTreasureValue(v-specs.getCap());
                    Collections.shuffle(interest);
                    specs.appointRessources(specs.getCap());
                }

                if (noMore(specs.getType())){
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
        int v1 = p1.getTreasureValue();
        int v2 = p2.getTreasureValue();
        if (v1 >= value ){
            if(v2 >= value) {
                if (v1 > v2) {
                    return p2;
                } else {
                    return p1;
                }
            }else{
                return p1;
            }
        }else{
            if(v2>=value){
                return p2;
            }else{
                if(v1 > v2){
                    return p1;
                }else
                    return p2;
            }
        }
    }

    public boolean stillTreasure(Observation type){
        if (type == Observation.ANY_TREASURE){
            return interest.isEmpty();
        }
        for (Position p:interest){
            if(p.getTreasureType() == type){
                return false;
            }
        }
        return true;
    }

    public List<Position> makePlanSolo(){
        Position toAdd = null;
        AgentSpecs specs = info.getMySpecs();
        ArrayList<Position> mission = new ArrayList<>();
        while(specs.getCap()!=0) {
            if (specs.getType() == Observation.DIAMOND) {
                toAdd = findClosest(interest, specs.getDiamondCap(), -1);
            } else if (specs.getType() == Observation.GOLD) {
                toAdd = findClosest(interest, -1, specs.getGoldCap());
            } else {
                toAdd = findClosest(interest, specs.getDiamondCap(), specs.getGoldCap());
                Observation treasureType = toAdd.getTreasureType();
                if (treasureType == Observation.DIAMOND) {
                    specs.setType(Observation.DIAMOND);
                } else {
                    specs.setType(Observation.GOLD);
                }
            }
            if (toAdd == null) {
                System.out.println("ERROR ON CALCULATING");
            }
            mission.add(toAdd);
            int v = toAdd.getTreasureValue();
            if (v < specs.getCap()) {
                interest.remove(toAdd);
                specs.appointRessources(v);
            } else {
                toAdd.setTreasureValue(v - specs.getCap());
                Collections.shuffle(interest);
                specs.appointRessources(specs.getCap());
                return mission;
            }

            if (noMore(specs.getType())) {
                return mission;
            }
        }
        return mission;
    }

    public boolean noMore(Observation type){
        boolean rep = true;
        switch (type){
            case DIAMOND:case GOLD:
                for (Position p : interest){
                    if(p.getTreasureType() == type){
                        rep = false;
                    }
                }
        }
        return rep;
    }
}
