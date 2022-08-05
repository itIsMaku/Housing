package cz.maku.housing.shared;

import cz.maku.housing.plot.Plot;
import cz.maku.mommons.ExceptionResponse;
import cz.maku.mommons.Response;
import cz.maku.mommons.player.CloudPlayer;
import cz.maku.mommons.token.NetworkTokenService;
import cz.maku.mommons.token.Token;
import cz.maku.mommons.worker.annotation.Initialize;
import cz.maku.mommons.worker.annotation.Load;
import cz.maku.mommons.worker.annotation.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class HousingConnectionService {

    @Load
    private NetworkTokenService networkTokenService;
    @Load
    private HousingServersService housingServersService;

    @Initialize
    private void tokenActions() {
        networkTokenService.addAction(HousingConfiguration.HOUSING_TOKEN_ACTION_CONNECT_RESPONSE, networkTokenAction -> {
            Map<String, String> data = networkTokenAction.getData();
            String targetServer = data.get("server");
            String player = data.get(HousingConfiguration.HOUSING_TOKEN_DATA_CONNECT_PLAYER);
            CloudPlayer cloudPlayer = CloudPlayer.getInstance(player);
            if (cloudPlayer != null) {
                cloudPlayer.connect(targetServer);
            }
        });
    }

    public void connect(CloudPlayer cloudPlayer, Plot plot) {
        CompletableFuture.runAsync(() -> {
            List<HousingServer> housingServers = housingServersService.getHousingServers();
            HousingServer plotServer = null;
            for (HousingServer housingServer : housingServers) {
                if (housingServer.getLoadedPlots().stream().map(Plot::getId).collect(Collectors.toList()).contains(plot.getId())) {
                    plotServer = housingServer;
                }
            }
            if (plotServer != null) {
                networkTokenService.sendToken(
                        plotServer.getId(),
                        Token.of(
                                HousingConfiguration.HOUSING_TOKEN_ACTION_CONNECT,
                                Map.of(
                                        HousingConfiguration.HOUSING_TOKEN_DATA_PLOT_ID, plot.getId(),
                                        HousingConfiguration.HOUSING_TOKEN_DATA_CONNECT_PLAYER, cloudPlayer.getNickname()
                                )
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
                return;
            }
            Optional<HousingServer> optionalHousingServer = housingServersService.getAvailableServer();
            if (optionalHousingServer.isEmpty()) {
                return;
            }
            HousingServer housingServer = optionalHousingServer.get();
            cloudPlayer.setLocalValue("connect", housingServer.getId());
            networkTokenService.sendToken(
                    housingServer.getId(),
                    Token.of(
                            HousingConfiguration.HOUSING_TOKEN_ACTION_LOAD,
                            Map.of(
                                    HousingConfiguration.HOUSING_TOKEN_DATA_PLOT_ID, plot.getId(),
                                    HousingConfiguration.HOUSING_TOKEN_DATA_CONNECT_PLAYER, cloudPlayer.getNickname()
                            )
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
        });
    }

}
