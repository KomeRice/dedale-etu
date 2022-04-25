package eu.su.mas.dedaleEtu.mas.knowledge;

import jade.util.leap.Serializable;

public class AgentSpecs implements Serializable {
    private int type;
    private int goldCap;
    private int diamondCap;
    private int strentgh;
    private int lockpinck;

    public AgentSpecs(int type, int goldCap, int diamondCap, int strentgh, int lockpinck) {
        this.type = type;
        this.goldCap = goldCap;
        this.diamondCap = diamondCap;
        this.strentgh = strentgh;
        this.lockpinck = lockpinck;
    }

    public int getType() {
        return type;
    }

    public int getGoldCap() {
        return goldCap;
    }

    public int getDiamondCap() {
        return diamondCap;
    }

    public int getStrentgh() {
        return strentgh;
    }

    public int getLockpinck() {
        return lockpinck;
    }




}
