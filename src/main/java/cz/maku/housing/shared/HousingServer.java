package cz.maku.housing.shared;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import cz.maku.housing.plot.Plot;
import cz.maku.mommons.ExceptionResponse;
import cz.maku.mommons.Mommons;
import cz.maku.mommons.Response;
import cz.maku.mommons.server.Server;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class HousingServer extends Server {

    public HousingServer(Server server) {
        super(server.getId(), server.getCachedData(), server.getLocalData());
    }

    public List<Plot> getLoadedPlots() {
        Object raw = getCloudValue(HousingConfiguration.HOUSING_SERVER_CLOUD_PLOTS);
        if (raw == null) {
            return Lists.newArrayList();
        }
        return Mommons.GSON.fromJson(
                (String) raw,
                new TypeToken<List<Plot>>() {
                }.getType()
        );
    }

    public void transactionLoadedPlotsAsync(Function<List<Plot>, List<Plot>> transaction) {
        CompletableFuture.runAsync(() -> {
            List<Plot> loadedPlots = getLoadedPlots();
            loadedPlots = transaction.apply(loadedPlots);
            setCloudValue(HousingConfiguration.HOUSING_SERVER_CLOUD_PLOTS, Mommons.GSON.toJson(loadedPlots)).thenAccept(response -> {
                if (!Response.isValid(response)) {
                    ExceptionResponse exceptionResponse = Response.getExceptionResponse(response);
                    if (exceptionResponse != null) {
                        exceptionResponse.getException().printStackTrace();
                    }
                }
            });
        });
    }

    public void addLoadedPlotsAsync(Plot plot) {
        transactionLoadedPlotsAsync(plots -> {
            plots.add(plot);
            return plots;
        });
    }

    public void removeLoadedPlotsAsync(Plot plot) {
        transactionLoadedPlotsAsync(plots -> {
            plots.remove(plot);
            return plots;
        });
    }
}
