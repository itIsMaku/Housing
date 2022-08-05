package cz.maku.housing.plot;

import cz.maku.spigotcontainers.data.WorldRestrictedContainer;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Getter
@Setter
public class Plot {

    private final String id;
    private final String owner;
    private final Map<String, String> data;
    @Nullable
    private WorldRestrictedContainer container;

    public Plot(String id, String owner, Map<String, String> data) {
        this.id = id;
        this.owner = owner;
        this.data = data;
    }
}
