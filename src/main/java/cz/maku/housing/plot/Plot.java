package cz.maku.housing.plot;

import cz.maku.housing.AddonData;
import cz.maku.housing.HousingApplication;
import cz.maku.housing.shared.HousingConfiguration;
import cz.maku.housing.shared.PlotTheme;
import cz.maku.housing.shared.PlotThemesService;
import cz.maku.mommons.storage.database.SQLRow;
import cz.maku.mommons.worker.WorkerReceiver;
import cz.maku.spigotcontainers.data.WorldRestrictedContainer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class Plot {

    private final String id;
    private final String owner;
    private final Material icon;
    private final AddonData data;
    private final String plotTheme;
    @Nullable
    private WorldRestrictedContainer container;
    @Nullable
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private BukkitTask unloadTask;

    public Plot(String id, String owner, Material icon, AddonData data, String plotTheme) {
        this.id = id;
        this.owner = owner;
        this.icon = icon;
        this.data = data;
        this.plotTheme = plotTheme;
    }

    public static Plot from(SQLRow row) {
        String id = row.getString("id");
        String owner = row.getString("owner");
        Material icon = Material.getMaterial(row.getString("icon"));
        AddonData data = AddonData.deserialize(row.getString("data"));
        String theme = row.getString("theme");

        return new Plot(id, owner, icon, data, theme);
    }
}
