package minecraft.rolest.utils.waveycapes.config;


import minecraft.rolest.utils.waveycapes.CapeMovement;
import minecraft.rolest.utils.waveycapes.CapeStyle;
import minecraft.rolest.utils.waveycapes.WindMode;

public class Config {

    public int configVersion = 2;
    public WindMode windMode = WindMode.NONE;
    public CapeStyle capeStyle = CapeStyle.SMOOTH;
    public CapeMovement capeMovement = CapeMovement.BASIC_SIMULATION;
    //public int capeParts = 16;
    public int gravity = 25;
    public int heightMultiplier = 6;
    public int straveMultiplier = 2;
    //public int maxBend = 5;
}
