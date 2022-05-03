package eu.su.mas.dedaleEtu.mas.knowledge;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.util.leap.Serializable;

import java.util.Random;

public class AgentSpecs implements Serializable {
    private final int prio;
    private Observation type;
    private int goldCap;
    private int diamondCap;
    private int strength;
    private int lockpick;

    public AgentSpecs(AbstractDedaleAgent a) {
        this.prio = new Random().nextInt(Integer.MAX_VALUE);
        this.type = a.getMyTreasureType();
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

    public AgentSpecs(Integer[] arg) {
        this.prio = new Random().nextInt(Integer.MAX_VALUE);
        this.type = Observation.ANY_TREASURE;
        this.goldCap = arg[0];
        this.diamondCap = arg[1];
        this.strength = arg[2];
        this.lockpick = arg[3];

    }

    public int getPrio() {
        return prio;
    }

    public Observation getType() {
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

    public void setType(Observation type) {
        this.type = type;
    }

    public int getCap(){
        if(type == Observation.DIAMOND){
            return diamondCap;
        }else if (type == Observation.GOLD){
            return goldCap;
        }else{
            return Math.max(goldCap,diamondCap);
        }
    }

    public void appointRessources(int value){
        if (type == Observation.DIAMOND){
            diamondCap -=value;
        }else{
            goldCap -= value;
        }
    }

    public void setRessources(int value){
        if (type == Observation.DIAMOND){
            diamondCap =value;
        }else{
            goldCap = value;
        }
    }
}
