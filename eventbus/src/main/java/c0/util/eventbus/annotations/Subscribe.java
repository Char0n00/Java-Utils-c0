package c0.util.eventbus.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark methods which will be called on events. The only
 * parameter for these methods should be the event of the type that this method
 * acts as a subscriber to. 
 * 
 * Subscribers can set a priority of type int for themselves in the annotation, for example "@Subscriber(priority = 5)". Higher value = lower priority.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subscribe {
	int priority() default 0;
}