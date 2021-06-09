package com.am.scheduler;

import com.am.scheduler.dto.Timer;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.http.HttpClient;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class TestMainVerticle {

  private io.vertx.reactivex.core.Vertx vertx;
  private HttpClient httpClient;

  @BeforeEach
  public void deploy_verticle(VertxTestContext testContext) {
    vertx = io.vertx.reactivex.core.Vertx.vertx();
    httpClient = vertx.createHttpClient();
    ConfigRetriever configRetriever = ConfigRetriever.create(vertx);
    configRetriever.getConfig(config -> {
      if(config.succeeded()) {
        JsonObject configJson = config.result();
        DeploymentOptions options = new DeploymentOptions().setConfig(configJson);
        vertx.
          deployVerticle(new MainVerticle(), options, testContext.succeeding(id -> testContext.completeNow()));
      }
    });
  }

  @AfterEach
  void tearDown(VertxTestContext testContext) {
    vertx.close(testContext.succeeding(response -> {
      testContext.completeNow();
    }));
  }

  @Test
  void shouldStartHttpServer() throws Throwable {
    VertxTestContext testContext = new VertxTestContext();
    vertx.createHttpServer()
      .requestHandler(req -> req.response().end())
      .listen(8081, event -> {
        event.succeeded();
        testContext.completeNow();
      });
    Assertions.assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
    if (testContext.failed()) {
      throw testContext.causeOfFailure();
    }
  }

  @Test
  @Disabled
  void shouldCreateTimerRequest (VertxTestContext context) throws Exception {
    final String json = Json.encodePrettily(new Timer("1",10L));
    final String length = Integer.toString(json.length());
    httpClient.post("localhost:8081/api/v1/timer")
      .putHeader("content-type", "application/json")
      .putHeader("content-length", length)
      .handler(response ->
        context.succeeding(buffer ->
            context.verify(() -> {
            Assertions.assertEquals(200, response.statusCode());
            context.completeNow();
            })))
      .write(json)
    .end();
  }

  @Test
  void verticle_deployed(Vertx vertx, VertxTestContext testContext) throws Throwable {
    testContext.completeNow();
  }
}
