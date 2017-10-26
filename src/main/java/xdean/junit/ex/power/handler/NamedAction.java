package xdean.junit.ex.power.handler;

import io.reactivex.functions.Action;

public interface NamedAction extends Action {

  static NamedAction create(String name, Action action) {
    return new NamedAction() {
      @Override
      public Action getAction() {
        return action;
      }

      @Override
      public String getName() {
        return name;
      }
    };
  }

  Action getAction();

  String getName();

  @Override
  default void run() throws Exception {
    getAction().run();
  }
}