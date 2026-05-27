package simulations;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.util.Random;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class OrderSimulation extends Simulation {

    private static final int TARGET_RPS = 333;
    private static final int RAMP_DURATION_SECONDS = 30;
    private static final int STEADY_DURATION_SECONDS = 60;

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    ScenarioBuilder scenariu = scenario("Order Processing V2 - Virtual Threads")
            .exec(
                    http("Process Single Order")
                            .post("/gateway/process")
                            .body(StringBody(session -> {
                                int randomId = new Random().nextInt(10000) + 1;
                                return "{\"orderId\": " + randomId + "}";
                            }))
                            .check(status().is(200))
            );

    {
        setUp(
                scenariu.injectOpen(
                        rampUsersPerSec(1).to(TARGET_RPS).during(RAMP_DURATION_SECONDS),
                        constantUsersPerSec(TARGET_RPS).during(STEADY_DURATION_SECONDS)
                )
        ).protocols(httpProtocol);
    }
}
