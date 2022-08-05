package cz.maku.housing.plot;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cz.maku.housing.HousingApplication;
import cz.maku.housing.shared.HousingServer;
import cz.maku.mommons.worker.annotation.BukkitEvent;
import cz.maku.mommons.worker.annotation.Initialize;
import cz.maku.mommons.worker.annotation.Load;
import cz.maku.mommons.worker.annotation.Service;
import cz.maku.spigotcontainers.api.PlayerJoinContainerEvent;
import cz.maku.spigotcontainers.api.PlayerLeaveContainerEvent;
import cz.maku.spigotcontainers.data.Container;
import cz.maku.spigotcontainers.data.ContaineredPlayer;
import cz.maku.spigotcontainers.data.WorldRestrictedContainer;
import cz.maku.spigotcontainers.service.ContainersService;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service(listener = true)
public class PlotContainersService {

    @Load
    private PlotWorldService plotWorldService;
    @Load
    private PlotContainersService plotContainersService;
    @Load
    private ContainersService containersService;

    @Getter
    private List<Plot> loadedPlots;
    @Getter
    private Map<String, String> queue;

    @Initialize
    private void init() {
        loadedPlots = Lists.newArrayList();
        queue = Maps.newHashMap();
    }

    private Optional<Plot> getLoadedPlot(String id) {
        return loadedPlots.stream().filter(plot -> plot.getId().equals(id)).findAny();
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
        HousingServer.local().addLoadedPlotsAsync(plot.getId());
    }

    public void unload(String id) {
        int index = -1;
        Plot plot = null;
        for (int i = 0; i < loadedPlots.size(); i++) {
            Plot element = loadedPlots.get(i);
            if (element.getId().equals(id)) {
                index = i;
                plot = element;
                break;
            }
        }
        if (index < 0) {
            return;
        }
        HousingServer.local().removeLoadedPlotsAsync(plot.getId());
        plotWorldService.unloadWorld(plot.getId());
        containersService.unregisterContainer(plot.getContainer().getId());
        plot.setContainer(null);
        loadedPlots.remove(index);
    }

    public void queue(String player, String plot) {
        queue.put(player, plot);
    }

    @BukkitEvent(PlayerJoinEvent.class)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        String name = player.getName();
        if (queue.containsKey(name)) {
            String plotId = queue.get(name);
            Plot plot = getLoadedPlot(plotId).get();
            WorldRestrictedContainer container = plot.getContainer();
            container.join(new ContaineredPlayer(player, container.getId()));
            player.teleport(container.getRestrictedWorlds().get(0).getSpawnLocation());
            queue.remove(name);
        }
    }

    @BukkitEvent(PlayerJoinContainerEvent.class)
    public void onPlayerJoinContainer(PlayerJoinContainerEvent e) {
        ContaineredPlayer containeredPlayer = e.getContaineredPlayer();
        Container container = e.getContainer();
        for (ContaineredPlayer cp : container.getPlayers()) {
            cp.getBukkit().sendMessage("§aParcela -> §f" + containeredPlayer.getNickname() + " §7nás přišel navštívit.");
        }
        Optional<Plot> loadedPlot = getLoadedPlot(container.getId());
        loadedPlot.ifPresent(plot -> {
            BukkitTask task = plot.getUnloadTask();
            if (task != null && task.isCancelled()) {
                task.cancel();
                plot.setUnloadTask(null);
            }
        });
    }

    @BukkitEvent(PlayerLeaveContainerEvent.class)
    public void onPlayerLeaveContainer(PlayerLeaveContainerEvent e) {
        ContaineredPlayer containeredPlayer = e.getContaineredPlayer();
        Container container = e.getContainer();
        for (ContaineredPlayer cp : container.getPlayers()) {
            cp.getBukkit().sendMessage("§cParcela -> §f" + containeredPlayer.getNickname() + " §7nás opustil.");
        }
        if (container.getPlayers().size() < 1) {
            Optional<Plot> loadedPlot = getLoadedPlot(container.getId());
            loadedPlot.ifPresent(plot -> {
                BukkitTask task = new BukkitRunnable() {
                    @Override
                    public void run() {
                        unload(container.getId());
                    }
                }.runTaskLater(HousingApplication.getPlugin(HousingApplication.class), 20 * 30);
                plot.setUnloadTask(task);
            });
        }
    }
}
