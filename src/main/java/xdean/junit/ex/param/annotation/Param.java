package xdean.junit.ex.param.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface Param {
  public enum ParamType {
    VALUE("${param}"), ARRAY("${param}[]"), LIST("List<${param}>"), UNDEFINED(
        "${param} or ${param}[] or List<${param}>");
    private final String paramString;

    private ParamType(String paramString) {
      this.paramString = paramString;
    }

    public String getParamString(Type paramType) {
      return paramString.replace("${param}", paramType.toString());
    }
  }

  ParamType value() default ParamType.UNDEFINED;
}
