package cz.maku.housing.shared;

import com.google.common.collect.Maps;
import cz.maku.housing.HousingApplication;
import cz.maku.housing.Rests;
import cz.maku.housing.plot.Plot;
import cz.maku.mommons.ExceptionResponse;
import cz.maku.mommons.Mommons;
import cz.maku.mommons.Response;
import cz.maku.mommons.server.ServerData;
import cz.maku.mommons.token.NetworkTokenService;
import cz.maku.mommons.token.Token;
import cz.maku.mommons.worker.annotation.Async;
import cz.maku.mommons.worker.annotation.Load;
import cz.maku.mommons.worker.annotation.Repeat;
import cz.maku.mommons.worker.annotation.Service;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@Service(scheduled = true)
public class HousingServersService {

    @Load
    private NetworkTokenService networkTokenService;

    public CompletableFuture<Optional<HousingServer>> getAvailableServerAsync() {
        return CompletableFuture.supplyAsync(this::getAvailableServer);
    }

    public List<HousingServer> getHousingServers() {
        return ServerData.getServersByType("housing").values()
                .stream()
                .map(HousingServer::new)
                .toList();
    }

    public List<HousingServer> getHousingServersByCondition(Predicate<? super HousingServer> condition) {
        return ServerData.getServersByType("housing").values()
                .stream()
                .map(HousingServer::new)
                .filter(condition)
                .toList();
    }

    public Optional<HousingServer> getAvailableServer() {
        List<HousingServer> housingServers = getHousingServersByCondition(housingServer -> housingServer.getLoadedPlots().size() < HousingConfiguration.HOUSING_SERVER_MAX_PLOTS);
        if (housingServers.isEmpty()) {
            return Optional.empty();
        }
        List<HousingServer> notEmptyServers = housingServers.stream()
                .filter(housingServer -> !housingServer.getLoadedPlots().isEmpty())
                .toList();
        if (notEmptyServers.size() < 1) {
            return Optional.ofNullable(housingServers.get(HousingConfiguration.RANDOM.nextInt(housingServers.size())));
        }

        housingServers.sort(HousingServer::compareTo);
        return Optional.of(housingServers.get(0));
    }

    @Repeat(delay = 20, period = 20 * 60)
    @Async
    public void serversWorker() {
        List<HousingServer> housingServers = getHousingServers();
        List<HousingServer> emptyServers = housingServers.stream()
                .filter(housingServer -> housingServer.getLoadedPlots().isEmpty())
                .toList();
        housingServers.removeIf(housingServer -> housingServer.getLoadedPlots().size() >= HousingConfiguration.HOUSING_SERVER_MAX_PLOTS);

        int emptyPlots = 0;
        for (HousingServer housingServer : housingServers) {
            List<Plot> loadedPlots = housingServer.getLoadedPlots();
            if (loadedPlots.isEmpty()) {
                emptyPlots = emptyPlots + HousingConfiguration.HOUSING_SERVER_MAX_PLOTS;
                continue;
            }
            emptyPlots = emptyPlots + HousingConfiguration.HOUSING_SERVER_MAX_PLOTS - loadedPlots.size();
        }
        int usedServersWithEmptyPlots = emptyPlots / HousingConfiguration.HOUSING_SERVER_MAX_PLOTS;
        if (usedServersWithEmptyPlots < HousingConfiguration.HOUSING_READY_SERVERS) {
            if (startServers(HousingConfiguration.HOUSING_READY_SERVERS - usedServersWithEmptyPlots)) {
                HousingApplication.getPlugin(HousingApplication.class).getLogger().severe("Servers was not created successfully!");
            }
        }

        if (emptyServers.size() > HousingConfiguration.HOUSING_READY_SERVERS) {
            int more = emptyServers.size() - HousingConfiguration.HOUSING_READY_SERVERS;
            List<HousingServer> serversToDelete = emptyServers.stream().limit(more).toList();
            for (HousingServer housingServer : serversToDelete) {
                networkTokenService.sendToken(
                        housingServer.getId(),
                        Token.of(
                                HousingConfiguration.HOUSING_TOKEN_ACTION_DESTROY,
                                Maps.newHashMap()
                        ),
                        10,
                        ChronoUnit.SECONDS
                ).thenAccept(response -> {
                    if (!Response.isValid(response)) {
                        ExceptionResponse exceptionResponse = Response.getExceptionResponse(response);
                        if (exceptionResponse != null) {
                            exceptionResponse.getException().printStackTrace();
                        }
                    }
                });
            }
        }
    }

    private boolean startServers(int count) {
        boolean success = true;
        for (int i = 0; i < count; i++) {
            Map<String, String> data = Maps.newHashMap();
            data.put("name", HousingConfiguration.DYNAMIC_SERVERS_SERVER_PREFIX + RandomStringUtils.randomAlphabetic(8));
            data.put("source", HousingConfiguration.DYNAMIC_SERVERS_SERVER_SOURCE);
            data.put("onlineMod", "false");
            data.put("parameters", "-javaagent:classmodifier.jar");
            try {
                Response response = Rests.post(HousingConfiguration.DYNAMIC_SERVERS_BOOT_ENDPOINT, Mommons.GSON.toJson(data));
                if (!Response.isValid(response)) {
                    ExceptionResponse exceptionResponse = Response.getExceptionResponse(response);
                    if (exceptionResponse != null) {
                        exceptionResponse.getException().printStackTrace();
                    }
                    success = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            }
        }
        return success;
    }

}