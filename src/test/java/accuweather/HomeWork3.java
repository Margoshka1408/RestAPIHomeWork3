package accuweather;
import accuweather.AccuweatherAbstractTest;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import seminar.accuweather.location.Location;
import seminar.accuweather.weather.Weather;
import seminar.accuweather.error.Error;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.lessThan;
public class HomeWork3 extends AccuweatherAbstractTest {

    @Test
    void testGetLocationsWithNewAssertions() {
        Map<String, String> mapQuery = new HashMap<>();

        mapQuery.put("apikey", getApiKey());
        mapQuery.put("q", "Moscow");

        List<Location> list = given().queryParams(mapQuery)
                .when().get(getBaseUrl() + "/locations/v1/cities/autocomplete")
                .then().statusCode(200).time(lessThan(3000L))
                .extract().body().jsonPath().getList(".", Location.class);

        Assertions.assertAll(() -> Assertions.assertEquals(10,
                        list.size()),
                () -> Assertions.assertEquals("Moscow",
                        list.get(5).getLocalizedName()),
                () -> Assertions.assertEquals("City",
                        list.get(0).getType()),
                () -> Assertions.assertEquals("294021",
                        list.get(0).getKey()),
                () -> Assertions.assertEquals("RU",
                        list.get(0).getCountry().getId()),
                () -> Assertions.assertEquals("Russia",
                        list.get(0).getCountry().getLocalizedName()),
                () -> Assertions.assertEquals("MOW",
                        list.get(0).getAdministrativeArea().getId()),
                () -> Assertions.assertEquals("Moscow", list.get(0).
                        getAdministrativeArea().getLocalizedName()));
    }

    @Test
    void testGetResponseLocations() {

        Response response = given().queryParams("apikey", getApiKey(), "q", "Moscow")
                .when().request(Method.GET, getBaseUrl() + "/locations/v1/cities/autocomplete");

        int statusCode = response.getStatusCode();
        List locationList = response.getHeaders().asList();
        String header = response.getHeader("Content-Encoding");
        String contentType = response.getContentType();

        Assertions.assertEquals(200, statusCode);
        Assertions.assertEquals(25, locationList.size());
        Assertions.assertEquals("gzip", header);
        Assertions.assertEquals(ContentType.JSON.withCharset(StandardCharsets.UTF_8).
                toLowerCase(Locale.ROOT), contentType);

    }

    @Test
    void testGetResponseForecastOneDay() {
        Weather weather = given().queryParam("apikey", getApiKey()).pathParam("locationkey", 100)
                .when().get(getBaseUrl() + "/forecasts/v1/daily/1day/{locationkey}")
                .then().statusCode(200).time(lessThan(2000L))
                .extract().response().body().as(Weather.class);

        int size = weather.getDailyForecasts().size();
        String unit = weather.getDailyForecasts().get(0).getTemperature().getMinimum().getUnit();

        Assertions.assertEquals(1, size);
        Assertions.assertEquals("F", unit);
    }

    @Test
    void testGetResponseForecastTenDays() {
        Error weather = given().queryParam("apikey", getApiKey()).pathParam("locationkey", 100)
                .when().get(getBaseUrl() + "/forecasts/v1/daily/10day/{locationkey}")
                .then().statusCode(401).time(lessThan(2000L))
                .extract().response().body().as(Error.class);

        Assertions.assertEquals("Unauthorized", weather.getCode());
        Assertions.assertEquals("Api Authorization failed", weather.getMessage());
        Assertions.assertEquals("/forecasts/v1/daily/10day/" +
                        "100?apikey=4MNqhWdUGXFe8WIzjRrXVz9u6iibdP9o",
                weather.getReference());
    }

    @Test
    void testGetResponseForecastFifteenDays() {
        Response response = given().queryParam("apikey", getApiKey()).pathParam("locationkey", 100)
                .when().request(Method.GET, getBaseUrl() + "/forecasts/v1/daily/15day/{locationkey}");

        Assertions.assertAll(() -> Assertions.assertEquals(401, response.statusCode()),
                () -> Assertions.assertEquals("Unauthorized", response.body().as(Error.class).getCode()),
                () -> Assertions.assertEquals("Api Authorization failed",
                        response.body().as(Error.class).getMessage()),
                () -> Assertions.assertEquals("/forecasts/v1/daily/15day/" +
                                "100?apikey=4MNqhWdUGXFe8WIzjRrXVz9u6iibdP9o",
                        response.body().as(Error.class).getReference()));

    }

}
