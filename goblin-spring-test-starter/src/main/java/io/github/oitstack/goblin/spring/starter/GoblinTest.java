package io.github.oitstack.goblin.spring.starter;


import java.lang.annotation.*;

/**
 * Mark this annotation on the test class to start goblin.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface GoblinTest {

}
