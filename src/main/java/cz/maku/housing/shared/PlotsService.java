package cz.maku.housing.shared;

import com.google.common.reflect.TypeToken;
import cz.maku.housing.plot.Plot;
import cz.maku.mommons.Mommons;
import cz.maku.mommons.storage.database.type.MySQL;
import cz.maku.mommons.worker.annotation.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class PlotsService {

    public Optional<Plot> getPlot(String id) {
        return MySQL.getApi().query(HousingConfiguration.HOUSING_SQL_PLOTS_TABLE, "select * from {table} where id = ?", id).stream()
                .map(row -> {
                    String owner = row.getString("owner");
                    String data = row.getString("data");
                    return new Plot(id, owner, Mommons.GSON.fromJson(data, new TypeToken<Map<String, String>>() {
                    }.getType()));
                })
                .findAny();
    }

    public CompletableFuture<Optional<Plot>> getPlotAsync(String id) {
        return CompletableFuture.supplyAsync(() -> getPlot(id));
    }

}
