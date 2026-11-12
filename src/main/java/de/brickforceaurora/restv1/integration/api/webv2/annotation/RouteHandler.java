package de.brickforceaurora.restv1.integration.api.webv2.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(SOURCE)
@Target(METHOD)
public @interface RouteHandler {

    /**
     * Defines the id that this route handler handles
     * 
     * @return the id
     */
    String id();

}
