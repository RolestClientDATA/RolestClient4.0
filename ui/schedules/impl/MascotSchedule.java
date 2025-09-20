package minecraft.rolest.ui.schedules.impl;

import minecraft.rolest.ui.schedules.Schedule;
import minecraft.rolest.ui.schedules.TimeType;

public class MascotSchedule
        extends Schedule {
    @Override
    public String getName() {
        return "Талисман";
    }

    @Override
    public TimeType[] getTimes() {
        return new TimeType[]{TimeType.NINETEEN_HALF};
    }
}
