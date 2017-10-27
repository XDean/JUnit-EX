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
  @Override
  default PowerUpResult powerup(Class<?> testClass) throws Exception {
    int code = System.identityHashCode(this);
    Object rule = getRule().unify(a -> a, b -> b);
    ClassPool pool = ClassPool.getDefault();
    CtClass ruleClass = pool.get(rule.getClass().getName());
    CtClass cc = pool.get(testClass.getName());
    cc.defrost();
    cc.setName(testClass.getName() + "$" + code);
    CtField field = new CtField(ruleClass, "rule" + code, cc);
    ConstPool constPool = cc.getClassFile().getConstPool();
    FieldInfo fieldInfo = field.getFieldInfo();
    AnnotationsAttribute annos = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
    annos.addAnnotation(new Annotation(Rule.class.getName(), constPool));
    fieldInfo.addAttribute(annos);
    field.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
    cc.addField(field, Initializer.byExpr(String.format("new %s()", ruleClass.getName())));
    Class<?> newClass = cc.toClass();
    return PowerUpResult.justClass(newClass);
  }

  Either<TestRule, MethodRule> getRule();
}
