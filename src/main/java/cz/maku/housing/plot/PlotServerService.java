package cz.maku.housing.plot;

import cz.maku.housing.rest.Rests;
import cz.maku.housing.shared.HousingConfiguration;
import cz.maku.housing.shared.PlotsService;
import cz.maku.mommons.Mommons;
import cz.maku.mommons.player.CloudPlayer;
import cz.maku.mommons.server.Server;
import cz.maku.mommons.token.NetworkTokenService;
import cz.maku.mommons.token.Token;
import cz.maku.mommons.worker.annotation.Initialize;
import cz.maku.mommons.worker.annotation.Load;
import cz.maku.mommons.worker.annotation.Service;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

@Service
public class PlotServerService {

    @Load
    private NetworkTokenService networkTokenService;
    @Load
    private PlotContainersService plotContainersService;
    @Load
    private PlotWorldService plotWorldService;
    @Load
    private PlotsService plotsService;

    @Initialize
    public void tokenActions() {
        networkTokenService.addAction(HousingConfiguration.HOUSING_TOKEN_ACTION_LOAD, networkTokenAction -> {
            Map<String, String> data = networkTokenAction.getData();
            String plotId = data.get(HousingConfiguration.HOUSING_TOKEN_DATA_PLOT_ID);
            String playerConnect = data.get(HousingConfiguration.HOUSING_TOKEN_DATA_CONNECT_PLAYER);
            plotsService.getPlotAsync(plotId).thenAcceptAsync(optionalPlot -> {
                if (optionalPlot.isEmpty()) {
                    return;
                }
                Plot plot = optionalPlot.get();
                plotContainersService.load(plot);
                if (playerConnect != null) {
                    CloudPlayer cloudPlayer = CloudPlayer.getInstanceOrDownload(playerConnect);
                    if (cloudPlayer == null) {
                        return;
                    }
                    plotContainersService.queue(playerConnect, plotId);
                    networkTokenService.sendToken(
                            cloudPlayer.getConnectedServer().getId(),
                            Token.of(
                                    HousingConfiguration.HOUSING_TOKEN_ACTION_CONNECT_RESPONSE,
                                    Map.of(
                                            "server", Server.local().getId(),
                                            HousingConfiguration.HOUSING_TOKEN_DATA_CONNECT_PLAYER, cloudPlayer.getNickname()
                                    )
                            ),
                            10,
                            ChronoUnit.SECONDS
                    );
                }
            });
        });
        networkTokenService.addAction(HousingConfiguration.HOUSING_TOKEN_ACTION_CONNECT, networkTokenAction -> {
            Map<String, String> data = networkTokenAction.getData();
            String plotId = data.get(HousingConfiguration.HOUSING_TOKEN_DATA_PLOT_ID);
            String playerConnect = data.get(HousingConfiguration.HOUSING_TOKEN_DATA_CONNECT_PLAYER);
            Optional<Plot> optionalPlot = plotContainersService.getLoadedPlots().stream().filter(p -> p.getId().equals(plotId)).findAny();
            if (optionalPlot.isEmpty()) {
                return;
            }
            CloudPlayer.getInstanceOrDownloadAsync(playerConnect).thenAcceptAsync(cloudPlayer -> {
                if (cloudPlayer == null) {
                    return;
                }
                plotContainersService.queue(playerConnect, plotId);
                networkTokenService.sendToken(
                        cloudPlayer.getConnectedServer().getId(),
                        Token.of(
                                HousingConfiguration.HOUSING_TOKEN_ACTION_CONNECT_RESPONSE,
                                Map.of(
                                        "server", Server.local().getId(),
                                        HousingConfiguration.HOUSING_TOKEN_DATA_CONNECT_PLAYER, cloudPlayer.getNickname()
                                )
                        ),
                        10,
                        ChronoUnit.SECONDS
                );
            });
        });
        networkTokenService.addAction(HousingConfiguration.HOUSING_TOKEN_ACTION_DESTROY, networkTokenAction -> {
            //todo: save data
            try {
                Rests.post(HousingConfiguration.DYNAMIC_SERVERS_DELETE_ENDPOINT, Mommons.GSON.toJson(Map.of("name", networkTokenAction.getTargetServer())));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //todo: handle response, complete 3.8
        });
    }

}
