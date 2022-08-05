package cz.maku.housing;

import cz.maku.mommons.worker.annotation.Plugin;
import cz.maku.mommons.worker.plugin.WorkerPlugin;

import java.util.List;

@Plugin(
        name = "Housing",
        main = "cz.maku.housing.HousingApplication",
        authors = "itIsMaku",
        apiVersion = "1.19"
)
public class HousingApplication extends WorkerPlugin {

    @Override
    public List<Class<?>> registerServices() {
        //todo: register services
        return null;
    }

    @Override
    public List<Class<?>> registerSpecialServices() {
        return null;
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onUnload() {
    }
}
