package cz.maku.housing.plot;

import cz.maku.housing.Rests;
import cz.maku.housing.shared.HousingConfiguration;
import cz.maku.mommons.Mommons;
import cz.maku.mommons.player.CloudPlayer;
import cz.maku.mommons.token.NetworkTokenService;
import cz.maku.mommons.worker.annotation.Initialize;
import cz.maku.mommons.worker.annotation.Load;
import cz.maku.mommons.worker.annotation.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class PlotServerService {

    @Load
    private NetworkTokenService networkTokenService;
    @Load
    private PlotContainersService plotContainersService;
    @Load
    private PlotWorldService plotWorldService;

    @Initialize
    public void tokenActions() {
        networkTokenService.addAction(HousingConfiguration.HOUSING_TOKEN_ACTION_LOAD, networkTokenAction -> {
            Map<String, String> data = networkTokenAction.getData();
            String plotId = data.get(HousingConfiguration.HOUSING_TOKEN_DATA_PLOT_ID);
            String playerConnect = data.get(HousingConfiguration.HOUSING_TOKEN_DATA_CONNECT_PLAYER);
            plotContainersService.load();

            //String id = data.get(HousingConfiguration.HOUSING_TOKEN_DATA_PLOT_ID);
            //plotContainersService.load(id);
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
