package cz.maku.housing.shared;

import cz.maku.housing.AddonData;
import cz.maku.housing.plot.Plot;
import cz.maku.mommons.player.CloudPlayer;
import cz.maku.mommons.storage.database.type.MySQL;
import cz.maku.mommons.worker.annotation.Service;
import org.bukkit.Material;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class PlotsService {

    public Optional<Plot> getPlot(String id) {
        return MySQL.getApi().query(HousingConfiguration.HOUSING_SQL_PLOTS_TABLE, "select * from {table} where id = ?", id).stream()
                .map(Plot::from)
                .findAny();
    }

    public CompletableFuture<Optional<Plot>> getPlotAsync(String id) {
        return CompletableFuture.supplyAsync(() -> getPlot(id));
    }

    public void createPlot(String id, CloudPlayer cloudPlayer, Material icon, PlotTheme plotTheme) {
        Plot plot = new Plot(id, cloudPlayer.getNickname(), icon, new AddonData(), plotTheme.getId());
        MySQL.getApi().queryAsync(
                HousingConfiguration.HOUSING_SQL_PLOTS_TABLE,
                "insert into {table} (id, owner, icon, data, theme) values (?, ?, ?, ?, ?)",
                id, cloudPlayer.getNickname(), icon.name(), plot.getData().serialize(), plot.getPlotTheme()
        );

    }

}
