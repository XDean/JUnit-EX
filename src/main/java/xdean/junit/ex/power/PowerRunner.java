package xdean.junit.ex.power;

import static xdean.jex.util.function.Predicates.not;
import static xdean.jex.util.lang.ExceptionUtil.*;
import static xdean.jex.util.reflect.AnnotationUtil.*;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import io.reactivex.functions.Action;
import javassist.ClassPool;
import javassist.CtClass;
import xdean.jex.util.log.Logable;
import xdean.junit.ex.power.annotation.ActualRunWith;
import xdean.junit.ex.power.annotation.PowerUp;
import xdean.junit.ex.power.annotation.PowerUps;

public class PowerRunner extends Runner implements Logable, Filterable, Sortable {

  private Class<?> originTestClass;
  private Runner childRunner;
  private PowerUpResult powerUpResult;

  public PowerRunner(Class<?> clz, RunnerBuilder runnerBuilder) throws Exception {
    this.originTestClass = clz;
    this.powerUpResult = PowerUpResult.justClass(clz);
    calcPowerPlugins();
    calcActualRunner(runnerBuilder);
  }

  protected void calcActualRunner(RunnerBuilder runnerBuilder) throws Exception {
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

  protected void calcPowerPlugins() throws Exception {
    getPowerUpHandlers()
        .forEachRemaining(pu -> powerUpResult.mergeAfter(uncheck(() -> pu.powerup(getActualTestClass()))));
    adjustClassName();
  }

  protected void adjustClassName() throws Exception {
    Class<?> clz = getActualTestClass();
    if (clz.getName().equals(originTestClass.getName())) {
      return;
    }
    ClassPool pool = ClassPool.getDefault();
    CtClass cc = pool.get(clz.getName());
    cc.defrost();
    cc.setName(originTestClass.getName());

    ClassLoader cl = new ClassLoader() {
    };
    Class<?> newClass = cc.toClass(cl, null);
    powerUpResult.setNewTestClass(newClass);
  }

  protected Iterator<? extends PowerUpHandler> getPowerUpHandlers() {
    return Stream.concat(
        Arrays.stream(originTestClass.getAnnotations())
            .map(Annotation::annotationType)
            .map(a -> a.getAnnotation(PowerUp.class)),
        Stream.of(originTestClass.getAnnotation(PowerUps.class))
            .filter(not(null))
            .map(PowerUps::value)
            .flatMap(Stream::of))
        .filter(not(null))
        .map(PowerUp::value)
        .map(c -> {
          try {
            return c.newInstance();
          } catch (InstantiationException | IllegalAccessException e) {
            String msg = String.format("%s must have a public no-arg constructor.", c);
            log().error(msg, e);
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
    powerUpResult.getBefore().forEach(p -> run(notifier, p.getLeft(), p.getRight()));
    childRunner.run(notifier);
    powerUpResult.getAfter().forEach(p -> run(notifier, p.getLeft(), p.getRight()));
  }

  private void run(RunNotifier notifier, Description desc, Action run) {
    EachTestNotifier eachNotifier = new EachTestNotifier(notifier, desc);
    eachNotifier.fireTestStarted();
    try {
      run.run();
    } catch (AssumptionViolatedException e) {
      eachNotifier.addFailedAssumption(e);
    } catch (Throwable e) {
      eachNotifier.addFailure(e);
    } finally {
      eachNotifier.fireTestFinished();
    }
  }

  @Override
  public void filter(Filter filter) throws NoTestsRemainException {
    if (childRunner instanceof Filterable) {
      ((Filterable) childRunner).filter(filter);
    }
  }

  @Override
  public void sort(Sorter sorter) {
    if (childRunner instanceof Sortable) {
      ((Sortable) childRunner).sort(sorter);
    }
  }
}
