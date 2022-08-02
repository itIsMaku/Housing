package cz.maku.housing.plot;

import cz.maku.spigotcontainers.data.WorldRestrictedContainer;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class Plot {

    private final String id;
    private final String owner;
    @Nullable
    private WorldRestrictedContainer container;

    public Plot(String id, String owner) {
        this.id = id;
        this.owner = owner;
    }
}
