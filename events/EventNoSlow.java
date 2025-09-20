package minecraft.rolest.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class EventNoSlow extends CancelEvent {

    private final float forward, strafe;

}