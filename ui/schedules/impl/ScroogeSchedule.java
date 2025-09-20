package minecraft.rolest.ui.schedules.impl;

import minecraft.rolest.ui.schedules.Schedule;
import minecraft.rolest.ui.schedules.TimeType;

public class ScroogeSchedule
        extends Schedule {
    @Override
    public String getName() {
        return "Скрудж";
    }

    @Override
    public TimeType[] getTimes() {
        return new TimeType[]{TimeType.FIFTEEN_HALF};
    }
}
