package xdean.junit.ex.power.handler;

import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtField.Initializer;
import javassist.Modifier;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.annotation.Annotation;
import xdean.jex.extra.Either;
import xdean.junit.ex.power.PowerUpHandler;
import xdean.junit.ex.power.PowerUpResult;

public interface PowerRule extends PowerUpHandler {
  public static final String POWER_RULE = "$PowerRule$";

  @Override
  default PowerUpResult powerup(Class<?> testClass) throws Exception {
    int code = System.identityHashCode(this);
    Class<?> ruleClass = getRuleClass().unify(a -> a, b -> b);
    ClassPool pool = ClassPool.getDefault();
    CtClass ruleCC = pool.get(ruleClass.getName());
    CtClass cc = pool.get(testClass.getName());
    String newName;
    if (cc.isFrozen()) {
      cc.defrost();
      newName = testClass.getName().substring(0, testClass.getName().lastIndexOf(POWER_RULE)) + POWER_RULE + code;
    } else {
      newName = testClass.getName() + POWER_RULE + code;
    }
    cc.setName(newName);
    CtField field = new CtField(ruleCC, "rule" + code, cc);
    ConstPool constPool = cc.getClassFile().getConstPool();
    FieldInfo fieldInfo = field.getFieldInfo();
    AnnotationsAttribute annos = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
    annos.addAnnotation(new Annotation(Rule.class.getName(), constPool));
    fieldInfo.addAttribute(annos);
    field.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
    cc.addField(field, Initializer.byExpr(String.format("new %s()", ruleCC.getName())));
    Class<?> newClass = cc.toClass();
    return PowerUpResult.justClass(newClass);
  }

  Either<Class<? extends TestRule>, Class<? extends MethodRule>> getRuleClass();
}
