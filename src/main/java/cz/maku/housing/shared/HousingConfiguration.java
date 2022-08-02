package cz.maku.housing.shared;

public class HousingConfiguration {

    public static final String HOUSING_PLOT_LOADER_KEY = "housing-plot-loader";
    public final static String HOUSING_TOKEN_ACTION_LOAD = "housing-load";
    public final static String HOUSING_TOKEN_ACTION_DESTROY = "housing-destroy";
    public final static String HOUSING_TOKEN_DATA_PLOT_ID = "housing-plot-id";
    public final static String HOUSING_SERVER_CLOUD_PLOTS = "housing-server-plots";
    public final static int HOUSING_SERVER_MAX_PLOTS = 3;
    public final static int HOUSING_READY_SERVERS = 2;

    public final static String DYNAMIC_SERVERS_BOOT_ENDPOINT = "http://dyna.maku.codes/boot-server";
    public final static String DYNAMIC_SERVERS_DELETE_ENDPOINT = "http://dyna.maku.codes/delete-server";
    public final static String DYNAMIC_SERVERS_SERVER_PREFIX = "housing-";
    public final static String DYNAMIC_SERVERS_SERVER_SOURCE = "housing-1.19";
}