package atdd.station;

import atdd.station.usecase.LineDTO;
import atdd.station.usecase.LineUsecase;
import atdd.station.usecase.StationDTO;
import atdd.station.usecase.StationUseCase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import reactor.core.publisher.Mono;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class LineAcceptanceTest {

  private static final Logger logger = LoggerFactory.getLogger(StationAcceptanceTest.class);

  @Autowired
  private LineUsecase lineService;

  @Autowired
  private StationUseCase stationService;

  @Autowired
  private WebTestClient webTestClient;

  @Test
  public void createLine() {
    //Given

    //When
    String lineName = "2호선";
    String startTime = "05:00";
    String endTime = "23:50";
    String intervalTime = "10";
    String extraFare = "0";
    String inputJSON = "{\"name\":\"" + lineName
        + "\",\"startTime\":\"" + startTime
        + "\",\"lastTime\":\"" + endTime
        + "\",\"timeInterval\":" + intervalTime
        + ",\"extra_fare\":" + extraFare
        + "}";

    ResponseSpec responseSpec = webTestClient.post().uri("/lines")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(inputJSON), String.class)
        .exchange();

    //Then
    responseSpec
        .expectStatus().isCreated()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectHeader().valueMatches("Location", ".*/lines/[0-9]*$")
        .expectBody().jsonPath("$.name").isEqualTo(lineName)
        .jsonPath("$.startTime").isEqualTo(startTime)
        .jsonPath("$.lastTime").isEqualTo(endTime)
        .jsonPath("$.timeInterval").isEqualTo(intervalTime);

  }

  @Test
  public void getLines() {
    //Given
    String lineName = "2호선";
    String startTime = "05:00";
    String endTime = "23:50";
    int intervalTime = 10;
    int extraFare = 0;

    LineDTO lineDTO = LineDTO.builder()
        .name(lineName)
        .startTime(startTime)
        .lastTime(endTime)
        .timeInterval(intervalTime)
        .extra_fare(extraFare)
        .build();

    LineDTO lineInsertResult = lineService.addLine(lineDTO);

    //When
    String inputJSON = "{\"name\":\"" + lineName + "\"}";

    ResponseSpec responseSpec = webTestClient.get().uri("/lines")
        .exchange();

    //Then
    responseSpec
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody().jsonPath("$.totalResults").isEqualTo(1)
        .jsonPath("$.results[0].name").isEqualTo(lineInsertResult.getName());
  }


  @Test
  public void getLine() {
    //Given
    String lineName = "2호선";
    String startTime = "05:00";
    String endTime = "23:50";
    int intervalTime = 10;
    int extraFare = 0;

    LineDTO lineDTO = LineDTO.builder()
        .name(lineName)
        .startTime(startTime)
        .lastTime(endTime)
        .timeInterval(intervalTime)
        .extra_fare(extraFare)
        .build();

    LineDTO lineInsertResult = lineService.addLine(lineDTO);

    //When
    ResponseSpec responseSpec = webTestClient.
        get().uri(
        "/lines/" + lineInsertResult.getId().toString()
    ).exchange();

    responseSpec
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody().jsonPath("$.id").isEqualTo(lineInsertResult.getId())
        .jsonPath("$.name").isEqualTo(lineInsertResult.getName());
  }

  @Test
  public void removeLine() {
    //Given
    String lineName = "2호선";
    String startTime = "05:00";
    String endTime = "23:50";
    int intervalTime = 10;
    int extraFare = 0;

    LineDTO lineDTO = LineDTO.builder()
        .name(lineName)
        .startTime(startTime)
        .lastTime(endTime)
        .timeInterval(intervalTime)
        .extra_fare(extraFare)
        .build();

    LineDTO lineInsertResult = lineService.addLine(lineDTO);

    //when
    ResponseSpec responseSpec = webTestClient.
        delete().uri(
        "/lines/" + lineInsertResult.getId().toString()
    ).exchange();

    //Then
    responseSpec
        .expectStatus().isNoContent();
  }

  @Test
  public void addStationIntoLine() {
    //Given
    StationDTO station1 = stationService.addStation(new StationDTO("강남역"));
    StationDTO station2 = stationService.addStation(new StationDTO("역삼역"));
    StationDTO station3 = stationService.addStation(new StationDTO("선릉역"));
    String lineName = "2호선";
    String startTime = "05:00";
    String endTime = "23:50";
    int intervalTime = 10;
    int extraFare = 0;
    LineDTO line = LineDTO.builder()
        .name(lineName)
        .startTime(startTime)
        .lastTime(endTime)
        .timeInterval(intervalTime)
        .extra_fare(extraFare)
        .build();
    LineDTO lineInsertResult = lineService.addLine(line);

    //When
    String inputJSON = "{"
        + "\"lineId\":\"" + line.getId().toString()
        + "\",\"elapsedTime\":\"" + "2"
        + "\",\"distance\":\"" + "12"
        + "\",\"sourceStationID\":" + station1.getId().toString()
        + ",\"targetStationID\":" + station2.getId().toString()
        + "}";

    ResponseSpec responseSpec = webTestClient.post().uri("/lines/" + line.getId() +"/edge")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(inputJSON), String.class)
        .exchange();

    //Then
    responseSpec
        .expectStatus().isCreated()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectHeader().valueMatches("Location", ".*/lines/[0-9]*/edge/[0-9]*$")
        .expectBody().jsonPath("$.sourceStationID").isEqualTo(station1.getId())
        .jsonPath("$.targetStationID").isEqualTo(station2.getId());
  }
}
