package com.am.scheduler;

import com.am.scheduler.dto.Timer;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import java.text.MessageFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class MainVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

  private static final String ENDPOINT = "/v1/timer";
  private static final String CONTENTTYPE = "application/json";

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    LOGGER.info("Started MainVerticle");
    Router router = Router.router(vertx);
    router.mountSubRouter("/api/", getAPISubSrouter(vertx));
    HttpServer server = vertx.createHttpServer();
    router.route().handler(StaticHandler.create().setCachingEnabled(false));

    server.requestHandler(router).listen(config().getInteger("http.port"),
      http -> {
      if(http.succeeded()) {
        startFuture.complete();
        LOGGER.info(("HTTP Server started on port: " + config().getInteger("http.port"))); }
          else {
            startFuture.fail(http.cause());
            LOGGER.error(MessageFormat
              .format("HTTP Server Failed: {0}", http.cause()));
          }
        });
  }

  @Override
  public void stop() throws Exception {
    LOGGER.info("Stopped MainVerticle");
  }

  public Router getAPISubSrouter(Vertx vertx) {
    io.vertx.reactivex.ext.web.Router apiSubRouter = Router.router(vertx);
    apiSubRouter.route(ENDPOINT).handler(BodyHandler.create());
    apiSubRouter.post(ENDPOINT).handler(this::createTimer);
    return apiSubRouter;
  }

  private Timer generateNewTimer(Long delay) {
    final String id = UUID.randomUUID().toString();
    Timer result = new Timer(id, delay);
    persistTimer(result);
    return result;
  }

  private void persistTimer(Timer timer) {
    //TODO
  }

  private void createTimer(RoutingContext routingContext)  {
    JsonObject jsonBody = routingContext.getBodyAsJson();
    Timer timer = generateNewTimer(jsonBody.getLong("delay"));

    vertx.timerStream(timer.getDelay()).toObservable()
      .observeOn(Schedulers.computation())
      .subscribe(new Observer<Long>() {
        private Disposable sub;
        @Override
        public void onSubscribe(Disposable disposable) {
          sub = disposable;
          LOGGER.info(MessageFormat
            .format("Timer with id {0} is started", timer.getId()));
        }
        @Override
        public void onNext(Long aLong) {

        }
        @Override
        public void onError(Throwable throwable) {
          LOGGER.error(MessageFormat
            .format("Timer with id: {0} finished with error{1}", timer.getId(), throwable.getMessage()));
        }
        @Override
        public void onComplete() {
          LOGGER.info(MessageFormat
            .format("Timer with id: {0}, delay: {1} is fired ", timer.getId(), timer.getDelay()));
          routingContext
            .response()
            .setStatusCode(200)
            .putHeader("content-type", CONTENTTYPE)
            .end("Timer with id: " + timer.getId() + ", delay: " + timer.getDelay() + " is fired");
        }
      } );
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    ConfigRetriever configRetriever = ConfigRetriever.create(vertx);
    configRetriever.getConfig(config -> {
      if(config.succeeded()) {
        JsonObject configJson = config.result();
        DeploymentOptions options = new DeploymentOptions().setConfig(configJson);
        vertx.deployVerticle(new MainVerticle(), options);
      }
    });
  }
}
