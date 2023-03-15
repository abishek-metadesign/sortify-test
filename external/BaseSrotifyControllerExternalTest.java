package uk.co.metadesignsolutions.javachallenge.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import de.cronn.testutils.h2.H2Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.metadesignsolutions.javachallenge.repositories.ArtistRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
@Import(H2Util.class)
public class BaseSrotifyControllerExternalTest {

    final static String SONG_URL = "/song/";
    final static String ARTIST_URL = "/artist/";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected H2Util h2Util;

    @Autowired
    protected ArtistRepository artistRepository;


    protected void validateResponseAsPerSchema(String json, String schema) throws IOException, ProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode responseJsonNode = objectMapper.readTree(json);
        JsonNode schemaJsonNode = JsonLoader.fromString(schema);
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.byDefault();
        JsonSchema jsonSchema = jsonSchemaFactory.getJsonSchema(schemaJsonNode);
        ProcessingReport validate = jsonSchema.validate(responseJsonNode);
        if (!validate.isSuccess()){
            throw  new RuntimeException();
        }
    }


    protected String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    Map<String, Object> getArtistRequestMap() {
        Map<String,Object> map = new HashMap<>();
        map.put("name","kana");
        return map;
    }


}
