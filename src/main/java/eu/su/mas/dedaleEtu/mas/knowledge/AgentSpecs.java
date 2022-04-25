package eu.su.mas.dedaleEtu.mas.knowledge;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.util.leap.Serializable;

public class AgentSpecs implements Serializable {
    private String type;
    private int goldCap;
    private int diamondCap;
    private int strength;
    private int lockpick;

    public AgentSpecs(AbstractDedaleAgent a) {
        this.type = a.getMyTreasureType().getName();
        for (Couple<Observation,Integer> c : a.getBackPackFreeSpace()) {
            if (c.getLeft() == Observation.GOLD){
                this.goldCap = c.getRight();
            }
            if (c.getLeft() == Observation.DIAMOND){
                this.diamondCap = c.getRight();
            }
        }
        for (Couple<Observation,Integer> c : a.getMyExpertise()){
            if (c.getLeft() == Observation.STRENGH){
                this.strength = c.getRight();
            }
            if (c.getLeft() == Observation.LOCKPICKING){
                this.lockpick = c.getRight();
            }
        }


    }

    public String getType() {
        return type;
    }

    public int getGoldCap() {
        return goldCap;
    }

    public int getDiamondCap() {
        return diamondCap;
    }

    public int getStrength() {
        return strength;
    }

    public int getLockpick() {
        return lockpick;
    }




}
