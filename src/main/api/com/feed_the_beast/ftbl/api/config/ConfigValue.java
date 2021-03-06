package com.feed_the_beast.ftbl.api.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by LatvianModder on 17.09.2016.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigValue
{
    String id();

    String file();

    boolean client() default false;

    boolean isExcluded() default false;

    boolean isHidden() default false;

    boolean canEdit() default true;

    boolean useScrollBar() default false;

    String displayName() default "";

    boolean translateDisplayName() default false;

    String[] info() default { };
}