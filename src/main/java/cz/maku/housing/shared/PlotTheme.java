package cz.maku.housing.shared;

import cz.maku.housing.AddonData;
import cz.maku.mommons.storage.database.SQLRow;
import lombok.Getter;
import org.bukkit.Material;

@Getter
public class PlotTheme {

    private final String id;
    private final String description;
    private final String storageWorldName;
    private final Material icon;
    private final AddonData data;

    public PlotTheme(String id, String description, String storageWorldName, Material icon, AddonData data) {
        this.id = id;
        this.description = description;
        this.storageWorldName = storageWorldName;
        this.icon = icon;
        this.data = data;
    }

    public static PlotTheme from(SQLRow row) {
        String id = row.getString("id");
        String description = row.getString("description");
        String storageWorldName = row.getString("storage_world_name");
        Material icon = Material.getMaterial(row.getString("icon"));
        AddonData data = AddonData.deserialize(row.getString("data"));

        return new PlotTheme(id, description, storageWorldName, icon, data);
    }
}
