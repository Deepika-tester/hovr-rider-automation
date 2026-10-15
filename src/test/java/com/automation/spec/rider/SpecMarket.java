package com.automation.spec.rider;

import com.automation.utils.ConfigReader;

import java.util.Map;

/**
 * The marketplace a run is in. The feature files are written with Toronto places ("Union Station"); an India run
 * substitutes Mohali/Chandigarh equivalents, a Canada run (emulator GPS in Toronto) uses them as written.
 * Selected by the platform config's `market` key (emulator-canada / driver-canada set it to "canada").
 */
public final class SpecMarket {

    private SpecMarket() {}

    private static final Map<String, String> INDIA_EQUIVALENTS = Map.of(
            "Union Station", "Chandigarh Railway Station",
            "CN Tower", "Rock Garden Chandigarh");

    public static boolean canada() {
        return "canada".equalsIgnoreCase(ConfigReader.get("market", "india"));
    }

    /** A place named in a feature file, as it should be searched in this marketplace. */
    public static String place(String specName) {
        return canada() ? specName : INDIA_EQUIVALENTS.getOrDefault(specName, specName);
    }

    /** A destination that reliably has search results and a drivable route from the test location. */
    public static String destination() {
        return canada() ? "Union Station" : "Chandigarh Railway Station";
    }

    /** The first words of {@link #destination()}, for typing a partial query. */
    public static String destinationPrefix() {
        return canada() ? "Union Stat" : "Chandigarh Railway";
    }

    /** An address to save as a place. */
    public static String savedAddress() {
        return canada() ? "CN Tower" : "CP67 Mall Mohali";
    }
}
