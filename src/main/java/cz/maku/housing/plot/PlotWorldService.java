package cz.maku.housing.plot;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.CorruptedWorldException;
import com.grinderwolf.swm.api.exceptions.NewerFormatException;
import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.exceptions.WorldInUseException;
import cz.maku.housing.shared.HousingConfiguration;
import cz.maku.mommons.worker.annotation.Initialize;
import cz.maku.mommons.worker.annotation.Service;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.io.IOException;

@Service
public class PlotWorldService {

    @Getter
    private SlimePlugin slimePlugin;
    @Getter
    private PlotWorldLoader plotWorldLoader;

    @Initialize
    private void registerLoader() {
        slimePlugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
        plotWorldLoader = new PlotWorldLoader(this);
        slimePlugin.registerLoader(HousingConfiguration.HOUSING_PLOT_LOADER_KEY, plotWorldLoader);
    }

    public void loadWorld(Plot plot) {
        try {
            plotWorldLoader.loadWorld(plot.getId(), false);
        } catch (UnknownWorldException | WorldInUseException | IOException e) {
            if (e instanceof UnknownWorldException) {
                try {
                    plotWorldLoader.create(plot.getId());
                    return;
                } catch (CorruptedWorldException | NewerFormatException | UnknownWorldException | WorldInUseException | IOException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        }
    }
}
