package com.project.cybershield.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.cybershield.ids.Signature;
import com.project.cybershield.ids.SignaturePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.InputStream;
import java.util.List;

public class JsonSignatureLoader {
    private static final Logger logger = LoggerFactory.getLogger(JsonSignatureLoader.class);


    public static List<SignaturePayload> load() {

        try(InputStream is = JsonSignatureLoader.class
                .getResourceAsStream("/config/payload-signatures.json")){

            return new ObjectMapper().readValue(is, new TypeReference<List<SignaturePayload>>() {});
        }
        catch (Exception ex){
            logger.error("Cannot load signatures-payload.json", ex.getMessage(), ex.getCause());
            throw new RuntimeException("Cannot load signatures-paylod.json");
        }
    }
}
