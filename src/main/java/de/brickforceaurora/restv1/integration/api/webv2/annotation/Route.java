package de.brickforceaurora.restv1.integration.api.webv2.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(SOURCE)
@Target({
    TYPE,
    METHOD
})
public @interface Route {

    /**
     * Defines the route this handler is implementing
     * 
     * @return the route
     */
    String value();

}
