package minecraft.rolest.events;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import minecraft.rolest.utils.rotation.VecRotation;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public final class EventLook {
    private VecRotation rotation;

    public VecRotation getRotation() {
        return this.rotation;
    }

    public void setRotation(VecRotation rotation) {
        this.rotation = rotation;
    }

    public EventLook(VecRotation rotation) {
        this.rotation = rotation;
    }
}