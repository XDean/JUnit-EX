package xdean.junit.ex.power;

import static xdean.jex.util.function.Predicates.not;
import static xdean.jex.util.lang.ExceptionUtil.throwAsUncheck;
import static xdean.jex.util.reflect.AnnotationUtil.*;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import xdean.jex.util.log.Logable;
import xdean.junit.ex.power.annotation.ActualRunWith;
import xdean.junit.ex.power.annotation.PowerUpHandler;

public class PowerRunner extends Runner implements Logable {

  private Class<?> originTestClass;
  private Runner childRunner;
  private PowerUpResult powerUpResult;

  public PowerRunner(Class<?> clz, RunnerBuilder runnerBuilder) {
    this.originTestClass = clz;
    this.powerUpResult = PowerUpResult.justClass(clz);
    calcPowerPlugins();
    calcActualRunner(runnerBuilder);
  }

  private void calcActualRunner(RunnerBuilder runnerBuilder) {
    RunWith runWith = originTestClass.getAnnotation(RunWith.class);
    ActualRunWith actualRunWith = originTestClass.getAnnotation(ActualRunWith.class);
    if (actualRunWith == null) {
      removeAnnotation(getActualTestClass(), RunWith.class);
    } else if (runWith == null) {
      addAnnotation(getActualTestClass(),
          createAnnotationFromMap(RunWith.class, Collections.singletonMap("value", actualRunWith.value())));
    } else {
      changeAnnotationValue(runWith, "value", actualRunWith.value());
    }
    childRunner = runnerBuilder.safeRunnerForClass(getActualTestClass());
  }

  private void calcPowerPlugins() {
    getPowerUpHandlers().forEachRemaining(pu -> powerUpResult.mergeAfter(pu.powerup(getActualTestClass())));
  }

  private Iterator<? extends PowerUp> getPowerUpHandlers() {
    return Arrays.stream(originTestClass.getAnnotations())
        .map(Annotation::annotationType)
        .map(a -> a.getAnnotation(PowerUpHandler.class))
        .filter(not(null))
        .map(PowerUpHandler::value)
        .map(c -> {
          try {
            return c.newInstance();
          } catch (InstantiationException | IllegalAccessException e) {
            String msg = String.format("%s must have a default public no-arg constructor.", c);
            log().error(msg);
            return throwAsUncheck(new InitializationError(msg));
          }
        })
        .iterator();
  }

  protected Class<?> getActualTestClass() {
    return powerUpResult.getActualClass();
  }

  @Override
  public Description getDescription() {
    Description childDescription = childRunner.getDescription();
    Description newRoot;
    if (childDescription.isSuite()) {
      newRoot = childDescription.childlessCopy();
      powerUpResult.getBefore().forEach(p -> newRoot.addChild(p.getLeft()));
      childDescription.getChildren().forEach(newRoot::addChild);
      powerUpResult.getAfter().forEach(p -> newRoot.addChild(p.getLeft()));
    } else {
      newRoot = Description.createSuiteDescription("PowerUpTestSuite");
      powerUpResult.getBefore().forEach(p -> newRoot.addChild(p.getLeft()));
      newRoot.addChild(childDescription);
      powerUpResult.getAfter().forEach(p -> newRoot.addChild(p.getLeft()));
    }
    return newRoot;
  }

  @Override
  public void run(RunNotifier notifier) {
    childRunner.run(notifier);
  }
}
