package uk.co.metadesignsolutions.javachallenge.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import uk.co.metadesignsolutions.javachallenge.enums.Genre;
import uk.co.metadesignsolutions.javachallenge.exceptions.GenericException;
import uk.co.metadesignsolutions.javachallenge.external.testlogger.Position;
import uk.co.metadesignsolutions.javachallenge.external.testlogger.TestPrinter;
import uk.co.metadesignsolutions.javachallenge.models.Artist;
import uk.co.metadesignsolutions.javachallenge.models.PlayHistory;
import uk.co.metadesignsolutions.javachallenge.models.Song;
import uk.co.metadesignsolutions.javachallenge.repositories.PlayHistoryRepository;
import uk.co.metadesignsolutions.javachallenge.repositories.SongRepository;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class SongControllerTest extends BaseSrotifyControllerExternalTest{
    private static TestPrinter testPrinter;
    private static final String SONG_CREATION_SCHEMA = "{\"$schema\":\"http://json-schema.org/draft-04/schema#\",\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"},\"artists\":{\"type\":\"array\",\"items\":[{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\"},\"name\":{\"type\":\"string\"}},\"required\":[\"id\",\"name\"]}]},\"genres\":{\"type\":\"array\",\"items\":[{\"type\":\"string\"},{\"type\":\"string\"}]},\"length\":{\"type\":\"string\"},\"url\":{\"type\":\"string\"}},\"required\":[\"name\",\"artists\",\"genres\",\"length\",\"url\"]}";

    @Autowired
    private  SongRepository songRepository;

    @Autowired
    private PlayHistoryRepository playHistoryRepository;

    @Autowired
    private ObjectMapper objectMapper;


    @BeforeAll
    public static void setup(){
        testPrinter = new TestPrinter(" Artist Creation Test");
    }

    @BeforeEach
    public void setupTest(){
        h2Util.resetDatabase();
    }

    @AfterEach
    public void cleanTest(){
        h2Util.resetDatabase();
    }

    @Test
    public void  shouldReturn2xx(){
        testPrinter.print(()->{
            Set<Long> artistIds = setUpArtist();
            Map<String, Object> songRequestDto = getStringRequestMap(artistIds);
            String content = this.asJsonString(songRequestDto);
            try {
                mockMvc.perform(
                        post(SONG_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        },10, Position.JUNIOR);
    }



    @Test
    public void  shouldReturn201(){
        testPrinter.print(()->{
            Set<Long> artistIds = setUpArtist();
            Map<String, Object> songRequestDto = getStringRequestMap(artistIds);
            String content = this.asJsonString(songRequestDto);
            try {
                mockMvc.perform(
                        post(SONG_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                ).andExpect(MockMvcResultMatchers.status().isCreated());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        },10, Position.MID);
    }

    @Test
    public void shouldReturnCreatedSongInProperFormat(){
        testPrinter.print(()->{
            Set<Long> artistIds = setUpArtist();
            Map<String, Object> songRequestDto = getStringRequestMap(artistIds);
            String content = this.asJsonString(songRequestDto);
            try {
                mockMvc.perform(
                        post(SONG_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                ).andExpect(mvcResult -> {
                    MockHttpServletResponse response = mvcResult.getResponse();
                    if (response.getStatus()==201 || response.getStatus()==200){
                        String contentAsString = response.getContentAsString();
                        validateResponseAsPerSchema(contentAsString, SONG_CREATION_SCHEMA);
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
    public void  shouldReturn4xxWhenThereIsNoName(){
        testPrinter.print(()->{
            Set<Long> artistIds = setUpArtist();
            Map<String, Object> songRequestDto = getStringRequestMap(artistIds);
            songRequestDto.put("name",null);
            String content = this.asJsonString(songRequestDto);
            try {
                mockMvc.perform(
                        post(SONG_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                ).andExpect(MockMvcResultMatchers.status().is4xxClientError());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        },10, Position.JUNIOR);
    }

    @Test
    public void  shouldReturn422WhenThereIsNoName(){
        testPrinter.print(()->{
            Set<Long> artistIds = setUpArtist();
            Map<String, Object> songRequestDto = getStringRequestMap(artistIds);
            songRequestDto.put("name",null);
            String content = this.asJsonString(songRequestDto);
            try {
                mockMvc.perform(
                        post(SONG_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        },10, Position.MID);
    }

    @Test
    public void  shouldReturn4xxWhenThereIsNoArtist(){
        testPrinter.print(()->{
            Set<Long> artistIds = null;
            Map<String, Object> songRequestDto = getStringRequestMap(artistIds);
            String content = this.asJsonString(songRequestDto);
            try {
                mockMvc.perform(
                        post(SONG_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                ).andExpect(MockMvcResultMatchers.status().is4xxClientError());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10, Position.JUNIOR);
    }

    @Test
    public void  shouldReturn422WhenThereIsNoArtist(){
        testPrinter.print(()->{
            Set<Long> artistIds = null;
            Map<String, Object> songRequestDto = getStringRequestMap(artistIds);
            String content = this.asJsonString(songRequestDto);
            try {
                mockMvc.perform(
                        post(SONG_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        },10, Position.MID);
    }


    @Test
    public void  shouldReturn4xxWhenThereIsNoGenre(){
        testPrinter.print(()->{
            Set<Long> artistIds = setUpArtist();
            Map<String, Object> songRequestDto = getStringRequestMap(artistIds);
            songRequestDto.remove("genres");
            String content = this.asJsonString(songRequestDto);
            try {
                mockMvc.perform(
                        post(SONG_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                ).andExpect(MockMvcResultMatchers.status().is4xxClientError());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10, Position.JUNIOR);
    }

    @Test
    public void  shouldReturn422WhenThereIsNoGenre(){
        testPrinter.print(()->{
            Set<Long> artistIds = setUpArtist();
            Map<String, Object> songRequestDto = getStringRequestMap(artistIds);
            songRequestDto.remove("genres");
            String content = this.asJsonString(songRequestDto);
            try {
                mockMvc.perform(
                        post(SONG_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        },10, Position.MID);
    }

    @Test
    public void  shouldReturn4xxWhenFormatForLengthIsWrong(){
        testPrinter.print(()->{
            Set<Long> artistIds = setUpArtist();
            Map<String, Object> songRequestDto = getStringRequestMap(artistIds);
            songRequestDto.put("length","12:12:12");
            String content = this.asJsonString(songRequestDto);
            try {
                mockMvc.perform(
                        post(SONG_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        },10, Position.MID);
    }

    @Test
    public void  shouldReturn422WhenFormatForLengthIsWrong(){
        testPrinter.print(()->{
            Set<Long> artistIds = setUpArtist();
            Map<String, Object> songRequestDto = getStringRequestMap(artistIds);
            songRequestDto.put("length","12:12:12");
            String content = this.asJsonString(songRequestDto);
            try {
                mockMvc.perform(
                        post(SONG_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        },10, Position.MID);
    }


    @Test
    public void  shouldReturn2xxOnListen(){
        Set<Long> artistIds = setUpArtist();
       Set<Artist> artists =artistIds
                .stream()
                .map(i -> artistRepository.findById(i)
                        .orElseThrow(()-> new RuntimeException("could not find"))
                ).collect(Collectors.toSet());

        Song song = new Song();
        song.setArtists(artists);
        song.setGenres(Arrays.asList(Genre.ROCK));
        song.setUrl("http://kanaboom.con");
        song.setLength(Duration.ofMinutes(2).plusSeconds(10));
        song.setName("silhoute");
        Song savedSong = songRepository.save(song);

        testPrinter.print(()->{
            try {
                mockMvc.perform(
                        get(SONG_URL+savedSong.getId()+"/listen")
                ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        },10, Position.JUNIOR);

    }


    @Test
    public void shouldReturn200OnListen(){
        Set<Long> artistIds = setUpArtist();
        Set<Artist> artists =artistIds
                .stream()
                .map(i -> artistRepository.findById(i)
                        .orElseThrow(()-> new RuntimeException("could not find"))
                ).collect(Collectors.toSet());

        Song song = new Song();
        song.setArtists(artists);
        song.setGenres(Arrays.asList(Genre.ROCK));
        song.setUrl("http://kanaboom.con");
        song.setLength(Duration.ofMinutes(2).plusSeconds(10));
        song.setName("silhoute");
        Song savedSong = songRepository.save(song);

        testPrinter.print(()->{
            try {
                mockMvc.perform(
                        get(SONG_URL+savedSong.getId()+"/listen")
                ).andExpect(MockMvcResultMatchers.status().isOk());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        },10, Position.JUNIOR);
    }

    @Test
    public void shouldSavePlayHistoryInDataBase(){
        Set<Long> artistIds = setUpArtist();
        Set<Artist> artists =artistIds
                .stream()
                .map(i -> artistRepository.findById(i)
                        .orElseThrow(()-> new RuntimeException("could not find"))
                ).collect(Collectors.toSet());

        Song song = new Song();
        song.setArtists(artists);
        song.setGenres(Arrays.asList(Genre.ROCK));
        song.setUrl("http://kanaboom.con");
        song.setLength(Duration.ofMinutes(2).plusSeconds(10));
        song.setName("silhoute");
        Song savedSong = songRepository.save(song);
        long initialCount = playHistoryRepository.count();
        testPrinter.print(()->{
            try {
                mockMvc.perform(
                        get(SONG_URL+savedSong.getId()+"/listen")
                ).andExpect(MockMvcResultMatchers.status().isOk());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            long finalCount = playHistoryRepository.count();
            Assertions.assertNotEquals(initialCount,finalCount);
        },10, Position.JUNIOR);
    }

    @Test
    public void shouldReturn2xxTrending(){
        Set<Long> artistIds = setUpArtist();
        Set<Artist> artists =artistIds
                .stream()
                .map(i -> artistRepository.findById(i)
                        .orElseThrow(()-> new RuntimeException("could not find"))
                ).collect(Collectors.toSet());
        Song song = new Song();
        song.setArtists(artists);
        song.setGenres(Arrays.asList(Genre.ROCK));
        song.setUrl("http://kanaboom.con");
        song.setLength(Duration.ofMinutes(2).plusSeconds(10));
        song.setName("silhoute");
        Song savedSong = songRepository.save(song);

        PlayHistory playHistory = new PlayHistory();
        playHistory.setSong(savedSong);
        playHistory.setListenedAt(LocalDateTime.now());
        playHistoryRepository.save(playHistory);

        LocalDate startTime =  LocalDate.now().minusDays(1);
        LocalDate endTime = LocalDate.now().plusDays(1);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");


        Map<String,String> data = new HashMap<>();
        data.put("startTime",startTime.format(timeFormatter));
        data.put("endTime",endTime.format(timeFormatter));

        String content = this.asJsonString(data);

        testPrinter.print(()->{
            try {
                mockMvc.perform(
                        post(SONG_URL+"/trending")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10, Position.JUNIOR);

    }


    @Test
    public void shouldReturn200OnTrending(){

        Set<Long> artistIds = setUpArtist();
        Set<Artist> artists =artistIds
                .stream()
                .map(i -> artistRepository.findById(i)
                        .orElseThrow(()-> new RuntimeException("could not find"))
                ).collect(Collectors.toSet());
        Song song = new Song();
        song.setArtists(artists);
        song.setGenres(Arrays.asList(Genre.ROCK));
        song.setUrl("http://kanaboom.con");
        song.setLength(Duration.ofMinutes(2).plusSeconds(10));
        song.setName("silhoute");
        Song savedSong = songRepository.save(song);

        PlayHistory playHistory = new PlayHistory();
        playHistory.setSong(savedSong);
        playHistory.setListenedAt(LocalDateTime.now());
        playHistoryRepository.save(playHistory);

        LocalDate startTime =  LocalDate.now().minusDays(1);
        LocalDate endTime = LocalDate.now().plusDays(1);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");


        Map<String,String> data = new HashMap<>();
        data.put("startTime",startTime.format(timeFormatter));
        data.put("endTime",endTime.format(timeFormatter));

        String content = this.asJsonString(data);

        testPrinter.print(()->{
            try {
                mockMvc.perform(
                        post(SONG_URL+"/trending")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                ).andExpect(MockMvcResultMatchers.status().isOk());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10, Position.MID);
    }

    @Test
    public void shouldThrow4xxOnStartDateIsAfterEndDate(){
        Set<Long> artistIds = setUpArtist();
        Set<Artist> artists =artistIds
                .stream()
                .map(i -> artistRepository.findById(i)
                        .orElseThrow(()-> new RuntimeException("could not find"))
                ).collect(Collectors.toSet());
        Song song = new Song();
        song.setArtists(artists);
        song.setGenres(Arrays.asList(Genre.ROCK));
        song.setUrl("http://kanaboom.con");
        song.setLength(Duration.ofMinutes(2).plusSeconds(10));
        song.setName("silhoute");
        Song savedSong = songRepository.save(song);

        PlayHistory playHistory = new PlayHistory();
        playHistory.setSong(savedSong);
        playHistory.setListenedAt(LocalDateTime.now());
        playHistoryRepository.save(playHistory);

        LocalDate startTime =  LocalDate.now().plusDays(1);
        LocalDate endTime = LocalDate.now().minusDays(1);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");


        Map<String,String> data = new HashMap<>();
        data.put("startTime",startTime.format(timeFormatter));
        data.put("endTime",endTime.format(timeFormatter));

        String content = this.asJsonString(data);

        testPrinter.print(()->{
            try {
                mockMvc.perform(
                        post(SONG_URL+"/trending")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                ).andExpect(MockMvcResultMatchers.status().is4xxClientError());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10, Position.JUNIOR);
    }

    @Test
    public void shouldThrow422OnStartDateIsAfterEndDate(){
        Set<Long> artistIds = setUpArtist();
        Set<Artist> artists =artistIds
                .stream()
                .map(i -> artistRepository.findById(i)
                        .orElseThrow(()-> new RuntimeException("could not find"))
                ).collect(Collectors.toSet());
        Song song = new Song();
        song.setArtists(artists);
        song.setGenres(Arrays.asList(Genre.ROCK));
        song.setUrl("http://kanaboom.con");
        song.setLength(Duration.ofMinutes(2).plusSeconds(10));
        song.setName("silhoute");
        Song savedSong = songRepository.save(song);

        PlayHistory playHistory = new PlayHistory();
        playHistory.setSong(savedSong);
        playHistory.setListenedAt(LocalDateTime.now());
        playHistoryRepository.save(playHistory);

        LocalDate startTime =  LocalDate.now().plusDays(1);
        LocalDate endTime = LocalDate.now().minusDays(1);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");


        Map<String,String> data = new HashMap<>();
        data.put("startTime",startTime.format(timeFormatter));
        data.put("endTime",endTime.format(timeFormatter));

        String content = this.asJsonString(data);

        testPrinter.print(()->{
            try {
                mockMvc.perform(
                        post(SONG_URL+"/trending")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10, Position.JUNIOR);
    }

    @Test
    public void shouldReturnCorrectOrderOnTrendingRequest(){
        Set<Long> artistIds = setUpArtist();
        Set<Artist> artists =artistIds
                .stream()
                .map(i -> artistRepository.findById(i)
                        .orElseThrow(()-> new RuntimeException("could not find"))
                ).collect(Collectors.toSet());
        Song sihoute = new Song();
        sihoute.setArtists(artists);
        sihoute.setGenres(Arrays.asList(Genre.ROCK));
        sihoute.setUrl("http://kanaboom.com/silhoute");
        sihoute.setLength(Duration.ofMinutes(2).plusSeconds(10));
        sihoute.setName("silhoute");
        Song savedSilhouteSong = songRepository.save(sihoute);

        Song danceWithMe = new Song();
        danceWithMe.setArtists(artists);
        danceWithMe.setGenres(Arrays.asList(Genre.ROCK));
        danceWithMe.setUrl("http://kanaboom.com/dance");
        danceWithMe.setLength(Duration.ofMinutes(2).plusSeconds(10));
        danceWithMe.setName("DanceWithMe");
        Song savedDanceWithMe = songRepository.save(danceWithMe);

        for (int i=0;i<3;i++){
            PlayHistory danceHistory = new PlayHistory();
            danceHistory.setSong(savedDanceWithMe);
            danceHistory.setListenedAt(LocalDateTime.now());
            playHistoryRepository.save(danceHistory);
        }

        Song twinkle = new Song();
        twinkle.setArtists(artists);
        twinkle.setGenres(Arrays.asList(Genre.ROCK));
        twinkle.setUrl("http://kanaboom.com/twinkle");
        twinkle.setLength(Duration.ofMinutes(2).plusSeconds(10));
        twinkle.setName("twinkle");
        Song savedTwinkleSong = songRepository.save(twinkle);

        for (int i=0;i<3;i++){
            PlayHistory twinkleHistory = new PlayHistory();
            twinkleHistory.setSong(savedTwinkleSong);
            twinkleHistory.setListenedAt(LocalDateTime.now());
            playHistoryRepository.save(twinkleHistory);
        }

        PlayHistory playHistory = new PlayHistory();
        playHistory.setSong(savedSilhouteSong);
        playHistory.setListenedAt(LocalDateTime.now());
        playHistoryRepository.save(playHistory);

        LocalDate startTime =  LocalDate.now().minusDays(1);
        LocalDate endTime = LocalDate.now().plusDays(1);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");


        Map<String,String> data = new HashMap<>();
        data.put("startTime",startTime.format(timeFormatter));
        data.put("endTime",endTime.format(timeFormatter));

        String content = this.asJsonString(data);

        testPrinter.print(()->{
            try {
                mockMvc.perform(
                        post(SONG_URL+"/trending")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                ).andExpect(mvcResult -> {
                    MockHttpServletResponse response = mvcResult.getResponse();

                    String danceWithMeText = getNameFromJson(response, 0);
                    String twinkleText = getNameFromJson(response, 1);
                    Assertions.assertEquals("twinkle",twinkleText);
                    Assertions.assertEquals("DanceWithMe",danceWithMeText);




                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10, Position.JUNIOR);
    }


    @Test
    public void shouldReturnTrendingResultForSongsPlayedInCorrectRange(){

        Set<Long> artistIds = setUpArtist();
        Set<Artist> artists =artistIds
                .stream()
                .map(i -> artistRepository.findById(i)
                        .orElseThrow(()-> new RuntimeException("could not find"))
                ).collect(Collectors.toSet());
        Song sihoute = new Song();
        sihoute.setArtists(artists);
        sihoute.setGenres(Arrays.asList(Genre.ROCK));
        sihoute.setUrl("http://kanaboom.com/silhoute");
        sihoute.setLength(Duration.ofMinutes(2).plusSeconds(10));
        sihoute.setName("silhoute");
        Song savedSilhouteSong = songRepository.save(sihoute);

        PlayHistory playHistory = new PlayHistory();
        playHistory.setSong(savedSilhouteSong);
        playHistory.setListenedAt(LocalDateTime.now().minusDays(2));

        playHistoryRepository.save(playHistory);
        Song danceWithMe = new Song();
        danceWithMe.setArtists(artists);
        danceWithMe.setGenres(Arrays.asList(Genre.ROCK));
        danceWithMe.setUrl("http://kanaboom.com/dance");
        danceWithMe.setLength(Duration.ofMinutes(2).plusSeconds(10));
        danceWithMe.setName("DanceWithMe");
        Song savedDanceWithMe = songRepository.save(danceWithMe);

        for (int i=0;i<3;i++){
            PlayHistory danceHistory = new PlayHistory();
            danceHistory.setSong(savedDanceWithMe);
            danceHistory.setListenedAt(LocalDateTime.now());
            playHistoryRepository.save(danceHistory);
        }

        Song twinkle = new Song();
        twinkle.setArtists(artists);
        twinkle.setGenres(Arrays.asList(Genre.ROCK));
        twinkle.setUrl("http://kanaboom.com/twinkle");
        twinkle.setLength(Duration.ofMinutes(2).plusSeconds(10));
        twinkle.setName("twinkle");
        Song savedTwinkleSong = songRepository.save(twinkle);

        for (int i=0;i<3;i++){
            PlayHistory twinkleHistory = new PlayHistory();
            twinkleHistory.setSong(savedTwinkleSong);
            twinkleHistory.setListenedAt(LocalDateTime.now());
            playHistoryRepository.save(twinkleHistory);
        }


        LocalDate startTime =  LocalDate.now().minusDays(1);
        LocalDate endTime = LocalDate.now().plusDays(1);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");


        Map<String,String> data = new HashMap<>();
        data.put("startTime",startTime.format(timeFormatter));
        data.put("endTime",endTime.format(timeFormatter));

        String content = this.asJsonString(data);

        testPrinter.print(()->{
            try {
                mockMvc.perform(
                        post(SONG_URL+"/trending")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                ).andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString("silhoute"))));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10, Position.MID);
    }

    @Test
    public void shouldReturnSongWithSameGenreWhenSearchingSimilarSong(){
        Artist artist = new Artist();
        artist.setName("kana");
        Artist savedArtist = artistRepository.save(artist);

        Song sihoute = new Song();
        sihoute.setArtists(Collections.singleton(savedArtist));
        sihoute.setGenres(Arrays.asList(Genre.ROCK));
        sihoute.setUrl("http://kanaboom.com/silhoute");
        sihoute.setLength(Duration.ofMinutes(2).plusSeconds(10));
        sihoute.setName("silhoute");
        Song savedSilhouteSong = songRepository.save(sihoute);

        Artist artist1 = new Artist();
        artist1.setName("kana");
        Artist savedArtist1 = artistRepository.save(artist1);

        Song twinkle = new Song();
        twinkle.setArtists(Collections.singleton(savedArtist1));
        twinkle.setGenres(Arrays.asList(Genre.ROCK));
        twinkle.setUrl("http://kanaboom.com/twinkle");
        twinkle.setLength(Duration.ofMinutes(2).plusSeconds(10));
        twinkle.setName("twinkle");
        Song savedTwinkleSong = songRepository.save(twinkle);

        testPrinter.print(()->{
            try {
                mockMvc.perform(
                        get(SONG_URL+savedTwinkleSong.getId()+"/similar")
                ).andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("silhoute")));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10, Position.MID);

    }

    @Test
    public void shouldNotReturnSongWhereGenreDoesNotMatchForSimilarSongSearch(){
        Artist artist = new Artist();
        artist.setName("kana");
        Artist savedArtist = artistRepository.save(artist);

        Song sihoute = new Song();
        sihoute.setArtists(Collections.singleton(savedArtist));
        sihoute.setGenres(Arrays.asList(Genre.ROCK));
        sihoute.setUrl("http://kanaboom.com/silhoute");
        sihoute.setLength(Duration.ofMinutes(2).plusSeconds(10));
        sihoute.setName("silhoute");
        Song savedSilhouteSong = songRepository.save(sihoute);

        Artist artist1 = new Artist();
        artist1.setName("kana");
        Artist savedArtist1 = artistRepository.save(artist1);

        Song twinkle = new Song();
        twinkle.setArtists(Collections.singleton(savedArtist1));
        twinkle.setGenres(Arrays.asList(Genre.PUNK));
        twinkle.setUrl("http://kanaboom.com/twinkle");
        twinkle.setLength(Duration.ofMinutes(2).plusSeconds(10));
        twinkle.setName("twinkle");
        Song savedTwinkleSong = songRepository.save(twinkle);

        testPrinter.print(()->{
            try {
                mockMvc.perform(
                        post(SONG_URL+"/"+savedTwinkleSong.getId()+"/similar")
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString("silhoute"))));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10, Position.MID);
    }


    @Test
    public void shouldReturnSongWithSameArtistForSimilarSongSearch(){

        Artist artist = new Artist();
        artist.setName("kana");
        Artist savedArtist = artistRepository.save(artist);

        Song sihoute = new Song();
        sihoute.setArtists(Collections.singleton(savedArtist));
        sihoute.setGenres(Arrays.asList(Genre.ROCK));
        sihoute.setUrl("http://kanaboom.com/silhoute");
        sihoute.setLength(Duration.ofMinutes(2).plusSeconds(10));
        sihoute.setName("silhoute");
        Song savedSilhouteSong = songRepository.save(sihoute);

        Song twinkle = new Song();
        twinkle.setArtists(Collections.singleton(artist));
        twinkle.setGenres(Arrays.asList(Genre.ROCK));
        twinkle.setUrl("http://kanaboom.com/twinkle");
        twinkle.setLength(Duration.ofMinutes(2).plusSeconds(10));
        twinkle.setName("twinkle");
        Song savedTwinkleSong = songRepository.save(twinkle);

        testPrinter.print(()->{
            try {
                mockMvc.perform(
                        get(SONG_URL+savedTwinkleSong.getId()+"/similar")
                ).andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("silhoute")));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10, Position.MID);
    }

    @Test
    public void shouldNotReturnSongWhereArtistDoesNotMatch(){

        Artist artist = new Artist();
        artist.setName("kana");
        Artist savedArtist = artistRepository.save(artist);

        Song sihoute = new Song();
        sihoute.setArtists(Collections.singleton(savedArtist));
        sihoute.setGenres(Arrays.asList(Genre.ROCK));
        sihoute.setUrl("http://kanaboom.com/silhoute");
        sihoute.setLength(Duration.ofMinutes(2).plusSeconds(10));
        sihoute.setName("silhoute");
        Song savedSilhouteSong = songRepository.save(sihoute);

        Artist artist1 = new Artist();
        artist1.setName("lara");
        Artist savedArtist1 = artistRepository.save(artist1);

        Song twinkle = new Song();
        twinkle.setArtists(Collections.singleton(savedArtist1));
        twinkle.setGenres(Arrays.asList(Genre.PUNK));
        twinkle.setUrl("http://kanaboom.com/twinkle");
        twinkle.setLength(Duration.ofMinutes(2).plusSeconds(10));
        twinkle.setName("twinkle");
        Song savedTwinkleSong = songRepository.save(twinkle);

        testPrinter.print(()->{
            try {
                mockMvc.perform(
                        post(SONG_URL+savedTwinkleSong.getId()+"/similar")
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString("silhoute"))));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10, Position.JUNIOR);

    }

    @Test
    public void shouldReturnSongsWhenSearchingWithNameFilter(){

        Artist artist = new Artist();
        artist.setName("kana");
        Artist savedArtist = artistRepository.save(artist);

        Song sihoute = new Song();
        sihoute.setArtists(Collections.singleton(savedArtist));
        sihoute.setGenres(Arrays.asList(Genre.ROCK));
        sihoute.setUrl("http://kanaboom.com/silhoute");
        sihoute.setLength(Duration.ofMinutes(2).plusSeconds(10));
        sihoute.setName("silhoute");
        Song savedSilhouteSong = songRepository.save(sihoute);

        Map<String,Object> searchRequestMap = new HashMap<>();
        searchRequestMap.put("name","ho");
        String content = this.asJsonString(searchRequestMap);

        testPrinter.print(()->{
            try {
                mockMvc.perform(
                        post(SONG_URL+"/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                ).andExpect(MockMvcResultMatchers.content().string((Matchers.containsString("silhoute"))));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10, Position.JUNIOR);



    }

    @Test
    public void shouldReturnSongsWhenSearchingWithIncludeArtistFilter(){
        Artist artist = new Artist();
        artist.setName("kana");
        Artist savedArtist = artistRepository.save(artist);

        Song sihoute = new Song();
        sihoute.setArtists(Collections.singleton(savedArtist));
        sihoute.setGenres(Arrays.asList(Genre.ROCK));
        sihoute.setUrl("http://kanaboom.com/silhoute");
        sihoute.setLength(Duration.ofMinutes(2).plusSeconds(10));
        sihoute.setName("silhoute");
        Song savedSilhouteSong = songRepository.save(sihoute);

        Map<String,Object> searchRequestMap = new HashMap<>();
        searchRequestMap.put("includeArtist",Arrays.asList(savedArtist.getName()));
        String content = this.asJsonString(searchRequestMap);

        testPrinter.print(()->{
            try {
                mockMvc.perform(
                        post(SONG_URL+"/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                ).andExpect(MockMvcResultMatchers.content().string((Matchers.containsString("silhoute"))));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10, Position.JUNIOR);
    }

    @Test
    public void shouldReturnSongsWhenSearchingWithExcludeArtistFilter(){
        Artist artist = new Artist();
        artist.setName("kana");
        Artist savedArtist = artistRepository.save(artist);

        Song sihoute = new Song();
        sihoute.setArtists(Collections.singleton(savedArtist));
        sihoute.setGenres(Arrays.asList(Genre.ROCK));
        sihoute.setUrl("http://kanaboom.com/silhoute");
        sihoute.setLength(Duration.ofMinutes(2).plusSeconds(10));
        sihoute.setName("silhoute");
        Song savedSilhouteSong = songRepository.save(sihoute);

        Artist artist1 = new Artist();
        artist1.setName("rana");
        Artist savedArtist1 = artistRepository.save(artist1);

        Song twinkle = new Song();
        twinkle.setArtists(Collections.singleton(savedArtist1));
        twinkle.setGenres(Arrays.asList(Genre.PUNK));
        twinkle.setUrl("http://ranaboom.com/twinkle");
        twinkle.setLength(Duration.ofMinutes(2).plusSeconds(10));
        twinkle.setName("twinkle");
        Song savedTwinkleSong = songRepository.save(twinkle);

        Map<String,Object> searchRequestMap = new HashMap<>();
        searchRequestMap.put("excludeArtist",Arrays.asList(savedArtist1.getName()));
        String content = this.asJsonString(searchRequestMap);

        testPrinter.print(()->{
            try {
                mockMvc.perform(
                        post(SONG_URL+"/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                ).andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString("silhoute"))))
                        .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("twinkle")));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10, Position.JUNIOR);
    }

    @Test
    public void shouldReturnSongsWhenSearchingWithIncludeGenreFilter(){

        Artist artist = new Artist();
        artist.setName("kana");
        Artist savedArtist = artistRepository.save(artist);

        Song sihoute = new Song();
        sihoute.setArtists(Collections.singleton(savedArtist));
        sihoute.setGenres(Arrays.asList(Genre.ROCK));
        sihoute.setUrl("http://kanaboom.com/silhoute");
        sihoute.setLength(Duration.ofMinutes(2).plusSeconds(10));
        sihoute.setName("silhoute");
        Song savedSilhouteSong = songRepository.save(sihoute);

        Artist artist1 = new Artist();
        artist1.setName("kana");
        Artist savedArtist1 = artistRepository.save(artist1);

        Song twinkle = new Song();
        twinkle.setArtists(Collections.singleton(savedArtist1));
        twinkle.setGenres(Arrays.asList(Genre.PUNK));
        twinkle.setUrl("http://kanaboom.com/twinkle");
        twinkle.setLength(Duration.ofMinutes(2).plusSeconds(10));
        twinkle.setName("twinkle");
        Song savedTwinkleSong = songRepository.save(twinkle);

        Map<String,Object> searchRequestMap = new HashMap<>();
        searchRequestMap.put("includeGenres",Arrays.asList("rock"));
        String content = this.asJsonString(searchRequestMap);

        testPrinter.print(()->{
            try {
                mockMvc.perform(
                                post(SONG_URL+"/search")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(content)
                        ).andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString("twinkle"))))
                        .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("silhoute")));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10, Position.JUNIOR);


    }

    @Test
    public void shouldReturnSongsWhenSearchingWithExcludeGenreFilter(){
        Artist artist = new Artist();
        artist.setName("kana");
        Artist savedArtist = artistRepository.save(artist);

        Song sihoute = new Song();
        sihoute.setArtists(Collections.singleton(savedArtist));
        sihoute.setGenres(Arrays.asList(Genre.ROCK));
        sihoute.setUrl("http://kanaboom.com/silhoute");
        sihoute.setLength(Duration.ofMinutes(2).plusSeconds(10));
        sihoute.setName("silhoute");
        Song savedSilhouteSong = songRepository.save(sihoute);

        Artist artist1 = new Artist();
        artist1.setName("kana");
        Artist savedArtist1 = artistRepository.save(artist1);

        Song twinkle = new Song();
        twinkle.setArtists(Collections.singleton(savedArtist1));
        twinkle.setGenres(Arrays.asList(Genre.PUNK));
        twinkle.setUrl("http://kanaboom.com/twinkle");
        twinkle.setLength(Duration.ofMinutes(2).plusSeconds(10));
        twinkle.setName("twinkle");
        Song savedTwinkleSong = songRepository.save(twinkle);

        Map<String,Object> searchRequestMap = new HashMap<>();
        searchRequestMap.put("excludeGenres",Arrays.asList("rock"));
        String content = this.asJsonString(searchRequestMap);

        testPrinter.print(()->{
            try {
                mockMvc.perform(
                                post(SONG_URL+"/search")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(content)
                        ).andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString("silhoute"))))
                        .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("twinkle")));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10, Position.JUNIOR);
    }

    @Test
    public void shouldReturnSongsWhenApplyingAllFilter(){
        Artist artist = new Artist();
        artist.setName("kana");
        Artist savedArtist = artistRepository.save(artist);

        Song sihoute = new Song();
        sihoute.setArtists(Collections.singleton(savedArtist));
        sihoute.setGenres(Arrays.asList(Genre.ROCK));
        sihoute.setUrl("http://kanaboom.com/silhoute");
        sihoute.setLength(Duration.ofMinutes(2).plusSeconds(10));
        sihoute.setName("silhoute");
        Song savedSilhouteSong = songRepository.save(sihoute);

        Artist artist1 = new Artist();
        artist1.setName("haka");
        Artist savedArtist1 = artistRepository.save(artist1);

        Song twinkle = new Song();
        twinkle.setArtists(Collections.singleton(savedArtist1));
        twinkle.setGenres(Arrays.asList(Genre.PUNK));
        twinkle.setUrl("http://kanaboom.com/twinkle");
        twinkle.setLength(Duration.ofMinutes(2).plusSeconds(10));
        twinkle.setName("twinkle");
        Song savedTwinkleSong = songRepository.save(twinkle);



        Song danceWithMe = new Song();
        danceWithMe.setArtists(Collections.singleton(savedArtist));
        danceWithMe.setGenres(Arrays.asList(Genre.J_POP));
        danceWithMe.setUrl("http://kanaboom.com/dance_with_me");
        danceWithMe.setLength(Duration.ofMinutes(2).plusSeconds(10));
        danceWithMe.setName("dancehour");
        Song saveDancedWithMe = songRepository.save(danceWithMe);

        Map<String,Object> searchRequestMap = new HashMap<>();
        searchRequestMap.put("name","ho");
        searchRequestMap.put("excludeGenres",Arrays.asList("rock"));
        searchRequestMap.put("includeGenres",Arrays.asList("j_pop"));
        searchRequestMap.put("excludeArtist",Arrays.asList(savedArtist.getName()));
        searchRequestMap.put("includeArtist",Arrays.asList(savedArtist1.getName()));
        String content = this.asJsonString(searchRequestMap);

        testPrinter.print(()->{
            try {
                mockMvc.perform(
                                post(SONG_URL+"/search")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(content)
                        ).andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString("moko"))))
                        .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("dancehour")));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },10, Position.JUNIOR);


    }


    private String getNameFromJson(MockHttpServletResponse response, int i) throws UnsupportedEncodingException, JsonProcessingException {
        String contentAsString = response.getContentAsString();
        if (contentAsString==null){
            throw new GenericException("Should return valid response", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        JsonNode jsonNode = objectMapper.readTree(contentAsString);
        JsonNode dataNode = jsonNode.get(i);
        if (dataNode==null){
            throw new GenericException("Should return valid response", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        JsonNode name = dataNode.get("name");
        if (name==null){
            throw new GenericException("Should return valid response", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String songName = name.asText();
        return songName;
    }



    private Set<Long> setUpArtist() {
        Set<Long> artists = new HashSet<>();
        Artist artist = new Artist();
        artist.setName("kana");
        Artist savedArtist = artistRepository.save(artist);
        artists.add(savedArtist.getId());

        Artist artist1 = new Artist();
        artist1.setName("loca");
        Artist savedArtist1 = artistRepository.save(artist1);
        artists.add(savedArtist1.getId());

        Artist artist2 = new Artist();
        artist2.setName("moca");
        Artist savedArtist2 = artistRepository.save(artist2);
        artists.add(savedArtist2.getId());

        return artists;
    }


    private static Map<String, Object> getStringRequestMap(Set<Long> artistIds) {
        Map<String,Object> songRequestDto = new HashMap<>();
        songRequestDto.put("name","silhoute");
        songRequestDto.put("artists", artistIds);
        songRequestDto.put("genres",Arrays.asList("rock","punk"));
        songRequestDto.put("length","12:13");
        songRequestDto.put("url","http://www.kanaboo.com");
        return songRequestDto;
    }








}
