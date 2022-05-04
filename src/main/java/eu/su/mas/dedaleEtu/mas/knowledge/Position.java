package eu.su.mas.dedaleEtu.mas.knowledge;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import jade.util.leap.Serializable;

import java.time.Instant;
import java.util.List;

public class Position implements Serializable {
    private Observation treasureType;
    private int treasureValue;
    private String nodeName;
    private boolean lockOpen;
    private int lockpickReq;
    private int strengthReq;
    private long timeStamp;



    private Position(Observation treasureType, int treasureValue, String nodeName, boolean lockOpen, int lockpickReq, int strengthReq) {
        this.treasureType = treasureType;
        this.treasureValue = treasureValue;
        this.nodeName = nodeName;
        this.lockOpen = lockOpen;
        this.lockpickReq = lockpickReq;
        this.strengthReq = strengthReq;
        this.timeStamp = Instant.now().toEpochMilli();
    }

    public long getTimeStamp() {
        return timeStamp;
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

    public void setTreasureValue(int treasureValue) {
        this.treasureValue = treasureValue;
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

    public void updatePos(Position p){
        if (p.getTimeStamp()> this.getTimeStamp()){
            this.treasureType = p.getTreasureType();
            this.treasureValue = p.getTreasureValue();
            this.lockOpen = p.isLockOpen();
            this.lockpickReq = p.getLockpickReq();
            this.strengthReq = p.getStrengthReq();
            this.timeStamp = p.getTimeStamp();
        }
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
