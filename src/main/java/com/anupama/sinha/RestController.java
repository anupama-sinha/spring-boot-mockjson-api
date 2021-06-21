package com.anupama.sinha;

import static com.anupama.sinha.DataConstants.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.List;

@org.springframework.web.bind.annotation.RestController
public class RestController {

    Logger LOGGER = LoggerFactory.getLogger(RestController.class);

    @Autowired
    ObjectMapper objectMapper;

    @GetMapping("/generateJson")
    List fetchAllJsons() throws URISyntaxException, IOException {
        Stream<Path> walk = Files.walk(Paths.get(ClassLoader.getSystemResource(DataConstants.CLASS_PATH).toURI()));

        List<String> allAPIs = walk.map(Path::toString)
                .filter(file -> file.endsWith(JSON_EXTENSION))
                .map(str -> str.replace(FOLDER_PATH, ""))
                .map(str -> str.replace(JSON_EXTENSION, ""))
                .collect(Collectors.toList());

        return allAPIs;
    }

    @PostMapping(value = "/mockService/{serviceName}")
    ResponseEntity createMockService(@PathVariable("serviceName") String serviceName,
                                     @RequestBody JsonNode body) throws IOException {

        LOGGER.info("createMockService called for serviceName={}", serviceName);

        File mockServiceJsonFile = getFile(serviceName);
        objectMapper.writeValue(mockServiceJsonFile, body);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put(DataConstants.MESSAGE, "Mock data created for " + serviceName);
        return new ResponseEntity(responseBody, HttpStatus.CREATED);
    }

    @GetMapping("/generateJson/{type1}")
    ResponseEntity<Object> fetchMockJson(@PathVariable("type1") String type1) throws IOException {
        File mockJsonFile = getFile(type1);
        ResponseEntity<Object> responseEntity = null;
        if(mockJsonFile.exists()){
            JsonNode jsonNode = objectMapper.readValue(mockJsonFile, JsonNode.class);
            responseEntity = new ResponseEntity<>(jsonNode, HttpStatus.OK);
        }
        else{
            Map responseMap = new HashMap<>();
            responseMap.put(DataConstants.MESSAGE,"No JSON for " + type1);
            responseEntity = new ResponseEntity<>(responseMap,HttpStatus.NOT_FOUND);
        }
        return responseEntity;
     }

    private File getFile(String type1) throws FileNotFoundException {
        return ResourceUtils.getFile(DataConstants.FOLDER_PATH + type1 + DataConstants.JSON_EXTENSION);
    }
}
