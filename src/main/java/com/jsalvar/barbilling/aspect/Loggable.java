package com.jsalvar.barbilling.aspect;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)      // applies to methods
@Retention(RetentionPolicy.RUNTIME) // available at runtime
public @interface Loggable {

}
