package org.yonside.aurora.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This annotation when applied to a class will cause a {@link java.util.ServiceLoader} compatible configuration
 * to be generated.
 * <br><br>
 * This implementation class must:
 * <ul>
 *     <li>Be a concrete public or public static inner class</li>
 *     <li>Have a public no-arg constructor</li>
 * </ul>
 */
@Target(ElementType.TYPE)
public @interface ServiceProvider {
    Class<?>[] value() default {};
}
