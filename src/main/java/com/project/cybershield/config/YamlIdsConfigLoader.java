package com.project.cybershield.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;

public class YamlIdsConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(YamlIdsConfigLoader.class);

    public static IdsConfig load() {
        try(InputStream is = YamlIdsConfigLoader.class.getResourceAsStream("/config/ids-config.yaml")){
            LoaderOptions options = new LoaderOptions();
            Constructor constructor = new Constructor(IdsConfig.class, options);

            Yaml yaml = new Yaml(constructor);
            return yaml.load(is);
        }catch (Exception ex){
            logger.error("Error with loading configuration for Ids", ex);
            throw new RuntimeException();
        }
    }
}
