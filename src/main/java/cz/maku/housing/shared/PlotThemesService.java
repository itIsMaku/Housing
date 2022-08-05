package cz.maku.housing.shared;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import cz.maku.mommons.storage.database.type.MySQL;
import cz.maku.mommons.worker.annotation.Initialize;
import cz.maku.mommons.worker.annotation.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PlotThemesService {

    private Map<String, PlotTheme> themes;

    @Initialize
    private void init() {
        themes = Maps.newHashMap();
        List<PlotTheme> plotThemes = MySQL.getApi().query(HousingConfiguration.HOUSING_SQL_THEMES_TABLE, "select * from {table}").stream()
                .map(PlotTheme::from)
                .toList();
        for (PlotTheme plotTheme : plotThemes) {
            themes.put(plotTheme.getId(), plotTheme);
        }
    }

    public ImmutableMap<String, PlotTheme> getThemes() {
        return ImmutableMap.copyOf(themes);
    }

    public Optional<PlotTheme> getTheme(String id) {
        PlotTheme plotTheme = themes.get(id);
        return !themes.containsKey(id) || plotTheme == null ? Optional.empty() : Optional.of(plotTheme);
    }

}
