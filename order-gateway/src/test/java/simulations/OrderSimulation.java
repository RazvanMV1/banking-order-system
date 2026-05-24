package simulations;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.util.Random;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class OrderSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    ScenarioBuilder scenariu = scenario("Order Processing")
            .exec(
                    http("Process Order")
                            .post("/gateway/process")
                            .body(StringBody(session -> {
                                int randomId = new Random().nextInt(10000) + 1;
                                return "[" + randomId + "]";
                            }))
                            .check(status().is(200))
            );

    {
        setUp(
                scenariu.injectOpen(
                        rampUsersPerSec(1).to(50).during(10),
                        constantUsersPerSec(50).during(30),
                        rampUsersPerSec(50).to(200).during(30),
                        constantUsersPerSec(200).during(30)
                )
        )
                .protocols(httpProtocol);

    }
}
