package minecraft.rolest.modules.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Category {

    Combat("Combat", "A"),
    Movement("Movement", "B"),
    Render("Render", "C"),
    Player("Player", "D"),
    Misc("Misc", "E");
    private final String name;
    private final String icon;


}
