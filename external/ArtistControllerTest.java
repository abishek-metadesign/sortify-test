package uk.co.metadesignsolutions.javachallenge.external;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import uk.co.metadesignsolutions.javachallenge.external.testlogger.Position;
import uk.co.metadesignsolutions.javachallenge.external.testlogger.TestPrinter;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class ArtistControllerTest extends BaseSrotifyControllerExternalTest{

    private static TestPrinter testPrinter;

    private static final String ARTIST_CREATION_SCHEMA = "{\"$schema\":\"http://json-schema.org/draft-04/schema#\",\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\"},\"name\":{\"type\":\"string\"}},\"required\":[\"id\",\"name\"]}";

    @BeforeAll
    public static void setup(){
        testPrinter = new TestPrinter(" Artist Creation Test");
    }

    @Test
    public void  shouldReturn2xx(){
        testPrinter.print(()->{
            Map<String,Object>  artistMap = getArtistRequestMap();
            String artistJson = this.asJsonString(artistMap);
            try {
                mockMvc.perform(
                        post(ARTIST_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(artistJson)
                ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10, Position.JUNIOR);
    }


    @Test
    public void shouldReturn201(){
        testPrinter.print(()->{
            Map<String,Object>  artistMap = getArtistRequestMap();
            String artistJson = this.asJsonString(artistMap);
            try {
                mockMvc.perform(
                        post(ARTIST_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(artistJson)
                ).andExpect(MockMvcResultMatchers.status().isCreated());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10, Position.MID);
    }

    @Test
    public void shouldReturnCreatedArtistInProperFormat(){
        testPrinter.print(()->{
            Map<String, Object> artistMap = getArtistRequestMap();
            String content = this.asJsonString(artistMap);
            try {
                mockMvc.perform(
                        post(ARTIST_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                ).andExpect(mvcResult -> {
                    MockHttpServletResponse response = mvcResult.getResponse();
                    if (response.getStatus()==201 || response.getStatus()==200){
                        String contentAsString = response.getContentAsString();
                        validateResponseAsPerSchema(contentAsString, ARTIST_CREATION_SCHEMA);
                    }else{
                        throw  new RuntimeException("did not have proper format");
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10,Position.JUNIOR);
    }

    @Test
    public void shouldAddArtistToDatabase(){
        testPrinter.print(()->{
            h2Util.resetDatabase();
            Map<String, Object> eventMap = getArtistRequestMap();
            String content = this.asJsonString(eventMap);
            try {
                mockMvc.perform(
                        post(ARTIST_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            long count = artistRepository.count();
            Assertions.assertEquals(1,count,()->"Event should be added to api");
        },10,Position.JUNIOR);
    }

    @Test
    public void shouldReturn4xxForNullNameValue(){
        testPrinter.print(()->{
            Map<String,Object>  artistMap = getArtistRequestMap();
            artistMap.put("name",null);
            String artistJson = this.asJsonString(artistMap);
            try {
                mockMvc.perform(
                        post(ARTIST_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(artistJson)
                ).andExpect(MockMvcResultMatchers.status().is4xxClientError());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10, Position.MID);

    }

    @Test
    public void shouldReturn422ForNullNameValue(){
        testPrinter.print(()->{
            Map<String,Object>  artistMap = getArtistRequestMap();
            artistMap.put("name",null);
            String artistJson = this.asJsonString(artistMap);
            try {
                mockMvc.perform(
                        post(ARTIST_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(artistJson)
                ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10, Position.MID);
    }

    @Test
    public void shouldReturn4xxForEmptyNameValue(){
        testPrinter.print(()->{
            Map<String,Object>  artistMap = getArtistRequestMap();
            artistMap.put("name","");
            String artistJson = this.asJsonString(artistMap);
            try {
                mockMvc.perform(
                        post(ARTIST_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(artistJson)
                ).andExpect(MockMvcResultMatchers.status().is4xxClientError());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10, Position.JUNIOR);

    }

    @Test
    public void shouldReturn422ForEmptyNameValue(){
        testPrinter.print(()->{
            Map<String,Object>  artistMap = getArtistRequestMap();
            artistMap.put("name","");
            String artistJson = this.asJsonString(artistMap);
            try {
                mockMvc.perform(
                        post(ARTIST_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(artistJson)
                ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10, Position.MID);
    }

}
