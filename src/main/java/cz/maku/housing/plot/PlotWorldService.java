package cz.maku.housing.plot;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import cz.maku.housing.AddonData;
import cz.maku.housing.rest.Rests;
import cz.maku.housing.shared.HousingConfiguration;
import cz.maku.housing.shared.PlotsService;
import cz.maku.mommons.ExceptionResponse;
import cz.maku.mommons.Response;
import cz.maku.mommons.worker.annotation.Initialize;
import cz.maku.mommons.worker.annotation.Load;
import cz.maku.mommons.worker.annotation.Service;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class PlotWorldService {

    @Load
    private PlotsService plotsService;

    @Getter
    private SlimePlugin slimePlugin;
    @Getter
    private SlimeLoader sqlLoader;

    @Initialize
    private void registerLoader() {
        slimePlugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
        sqlLoader = slimePlugin.getLoader("mysql");
    }

    @SneakyThrows
    public void loadWorld(Plot plot) {
        slimePlugin.createEmptyWorld(sqlLoader, plot.getId(), false, new SlimePropertyMap());
        loadSchematic(plot);
    }

    public void unloadWorld(String id) {
        plotsService.getPlotAsync(id).thenAccept(optPlot -> {
            if (optPlot.isEmpty()) {
                return;
            }
            Plot plot = optPlot.get();
            saveSchematic(plot);
            try {
                sqlLoader.deleteWorld(id);
            } catch (UnknownWorldException | IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void loadSchematic(Plot plot) {
        AddonData data = plot.getData();
        int minX = Integer.parseInt(data.get("min-x"));
        int minY = Integer.parseInt(data.get("min-y"));
        int minZ = Integer.parseInt(data.get("max-z"));
        CompletableFuture.supplyAsync(() -> {
            try {
                return Rests.download(HousingConfiguration.STORAGE_UPLOAD_ENDPOINT, plot.getId());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }).thenAccept(schematicFile -> {
            if (schematicFile == null) {
                return;
            }
            Clipboard clipboard;

            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile.getValue());
            try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile.getValue()))) {
                clipboard = reader.read();
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(new BukkitWorld(Bukkit.getWorld(plot.getId())))) {
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(BlockVector3.at(minX, minY, minZ))
                            .build();
                    Operations.complete(operation);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @SneakyThrows
    public void saveSchematic(Plot plot) {
        AddonData data = plot.getData();
        int maxX = Integer.parseInt(data.get("max-x"));
        int maxY = Integer.parseInt(data.get("max-y"));
        int maxZ = Integer.parseInt(data.get("max-z"));
        int minX = Integer.parseInt(data.get("min-x"));
        int minY = Integer.parseInt(data.get("min-y"));
        int minZ = Integer.parseInt(data.get("max-z"));
        CuboidRegion region = new CuboidRegion(
                BlockVector3.at(minX, minY, minZ),
                BlockVector3.at(maxX, maxY, maxZ)
        );
        region.setWorld(new BukkitWorld(Bukkit.getWorld(plot.getId())));
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                region.getWorld(), region, clipboard, region.getMinimumPoint()
        );
        Operations.complete(forwardExtentCopy);

        File file = File.createTempFile("temp-" + plot.getId(), ".schematic");

        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file))) {
            writer.write(clipboard);
            CompletableFuture.runAsync(() -> {
                try {
                    Response upload = Rests.upload(HousingConfiguration.STORAGE_DOWNLOAD_ENDPOINT, file);
                    if (!Response.isValid(upload)) {
                        ExceptionResponse exceptionResponse = Response.getExceptionResponse(upload);
                        if (exceptionResponse != null) {
                            exceptionResponse.getException().printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

}
