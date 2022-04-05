package eu.su.mas.dedaleEtu.mas.knowledge;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import jade.util.leap.Serializable;

import java.util.List;

public class Position implements Serializable {
    private Observation treasureType;
    private int treasureValue;
    private String nodeName;
    private boolean lockOpen;
    private int lockpickReq;
    private int strengthReq;

    private Position(Observation treasureType, int treasureValue, String nodeName, boolean lockOpen, int lockpickReq, int strengthReq) {
        this.treasureType = treasureType;
        this.treasureValue = treasureValue;
        this.nodeName = nodeName;
        this.lockOpen = lockOpen;
        this.lockpickReq = lockpickReq;
        this.strengthReq = strengthReq;
    }

    public Observation getTreasureType() {
        return treasureType;
    }

    public int getTreasureValue() {
        return treasureValue;
    }

    public String getNodeName() {
        return nodeName;
    }

    public boolean isLockOpen() {
        return lockOpen;
    }

    public int getLockpickReq() {
        return lockpickReq;
    }

    public int getStrengthReq() {
        return strengthReq;
    }

    public static void GeneratePositionFromObservations(List<Couple<String, List<Couple<Observation,Integer>>>> lobs, AgentMeta a){
        for(Couple<String, List<Couple<Observation,Integer>>> obs : lobs){
            if(obs.getRight().size() == 0){
                continue;
            }
            String nodeName = obs.getLeft();
            Observation treasureType = null;
            int treasureValue = 0;
            boolean lockOpen = true;
            int lockpickReq = 0;
            int strengthReq = 0;
            boolean unhandledData = false;

            for(Couple<Observation, Integer> localData : obs.getRight()){
                switch(localData.getLeft()){
                    case GOLD: case DIAMOND:
                        treasureType = localData.getLeft();
                        treasureValue = localData.getRight();
                        break;
                    case LOCKSTATUS:
                        if (localData.getRight() == 0)
                            lockOpen = false;
                        break;
                    case LOCKPICKING:
                        lockpickReq = localData.getRight();
                        break;
                    case STRENGH:
                        strengthReq = localData.getRight();
                        break;
                    default:
                        unhandledData = true;
                }
            }

            if(unhandledData){
                continue;
            }

            // todo: state changes
            a.addInterest(new Position(treasureType, treasureValue, nodeName, lockOpen, lockpickReq, strengthReq));

        }
    }
}
