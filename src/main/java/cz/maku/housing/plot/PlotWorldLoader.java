package cz.maku.housing.plot;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.CorruptedWorldException;
import com.grinderwolf.swm.api.exceptions.NewerFormatException;
import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.exceptions.WorldInUseException;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimeProperties;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import cz.maku.housing.shared.HousingConfiguration;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
public class PlotWorldLoader implements SlimeLoader {

    private final PlotWorldService plotWorldService;

    @Override
    public byte[] loadWorld(String s, boolean b) throws UnknownWorldException, WorldInUseException, IOException {
        return null;
    }

    @Override
    public boolean worldExists(String s) throws IOException {
        return false;
    }

    @Override
    public List<String> listWorlds() throws IOException {
        return null;
    }

    @Override
    public void saveWorld(String s, byte[] bytes, boolean b) throws IOException {

    }

    @Override
    public void unlockWorld(String s) throws UnknownWorldException, IOException {

    }

    @Override
    public boolean isWorldLocked(String s) throws UnknownWorldException, IOException {
        return false;
    }

    @Override
    public void deleteWorld(String s) throws UnknownWorldException, IOException {

    }

    public void create(String name) throws CorruptedWorldException, NewerFormatException, WorldInUseException, UnknownWorldException, IOException {
        SlimePlugin plugin = plotWorldService.getSlimePlugin();
        SlimeLoader loader = plugin.getLoader(HousingConfiguration.HOUSING_PLOT_LOADER_KEY);
        SlimePropertyMap slimePropertyMap = new SlimePropertyMap();
        slimePropertyMap.setString(SlimeProperties.DIFFICULTY, "normal");

        SlimeWorld slimeWorld = plugin.loadWorld(loader, name, false, slimePropertyMap);
        plugin.generateWorld(slimeWorld);
    }
}
