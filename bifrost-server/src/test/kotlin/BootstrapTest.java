import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.io.InputStreamReader;
import java.util.Map;

/**
 *bifrost test server
 */
@RunWith(VertxUnitRunner.class)
public class BootstrapTest{
  private Logger logger = LoggerFactory.getLogger(BootstrapTest.class);
  private Vertx vertx;
  @Before
  public void before(TestContext testContext) throws Exception{
    Async async = testContext.async();
    this.vertx = Vertx.vertx();

    JsonObject config = new JsonObject(Json.mapper.readValue(new InputStreamReader(BootstrapTest.class.getClassLoader().getResourceAsStream("config.json")),Map.class));

    deployVerticle("com.maxleap.bifrost.kotlin.BifrostServer", config).subscribe( result -> logger.info("start success"));
    async.complete();
  }

  protected Observable<Boolean> deployVerticle(final String className, final JsonObject config) {

    return Observable.create(subscriber -> {
      DeploymentOptions options = new DeploymentOptions();
      options.setConfig(config);
      vertx.deployVerticle(className, options, event -> {
        if (event.succeeded()) {
          logger.info("Deploy [" + className + "] success.");
          subscriber.onNext(Boolean.TRUE);
        } else {
          subscriber.onError(event.cause());
        }
        subscriber.onCompleted();
      });
    });
  }

  @Test(timeout = 1000000000l)
  public void start(TestContext testContext){
    Async async = testContext.async();
    async.await();
  }

}
