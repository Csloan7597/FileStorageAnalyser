package utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 * Created by conor on 05/10/2014.
 */
public class JsonFileLoadHelper {

    private JsonFileLoadHelper() {
        // Prevent instantiation
    }

    public static Map<String, Object> loadJsonFile(String path) throws FileNotFoundException, IOException {
        return new ObjectMapper().readValue(
                new File(path),
                new TypeReference<Map<String, Object>>() {
                });
    }

}



