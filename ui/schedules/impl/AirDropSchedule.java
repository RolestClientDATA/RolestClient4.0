package minecraft.rolest.ui.schedules.impl;

import minecraft.rolest.ui.schedules.Schedule;
import minecraft.rolest.ui.schedules.TimeType;

public class AirDropSchedule
        extends Schedule {
    @Override
    public String getName() {
        return "Аир дроп";
    }

    @Override
    public TimeType[] getTimes() {
        return new TimeType[]{TimeType.NINE, TimeType.ELEVEN, TimeType.THIRTEEN, TimeType.FIFTEEN, TimeType.SEVENTEEN, TimeType.NINETEEN, TimeType.TWENTY_ONE, TimeType.TWENTY_THREE, TimeType.ONE};
    }
}