package cz.maku.housing.plot;

import com.google.common.collect.Lists;
import cz.maku.mommons.worker.annotation.Initialize;
import cz.maku.mommons.worker.annotation.Load;
import cz.maku.mommons.worker.annotation.Service;
import cz.maku.spigotcontainers.data.WorldRestrictedContainer;
import cz.maku.spigotcontainers.service.ContainersService;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.List;

@Service
public class PlotContainersService {

    @Load
    private PlotWorldService plotWorldService;
    @Load
    private ContainersService containersService;

    @Getter
    private List<Plot> loadedPlots;

    @Initialize
    private void init() {
        loadedPlots = Lists.newArrayList();
    }

    public void load(Plot plot) {
        plotWorldService.loadWorld(plot);
        WorldRestrictedContainer container = new WorldRestrictedContainer(
                plot.getId(),
                Lists.newArrayList(),
                Lists.newArrayList(Bukkit.getWorld(plot.getId()))
        );
        containersService.registerContainer(container);
        plot.setContainer(container);
        loadedPlots.add(plot);
    }

}
