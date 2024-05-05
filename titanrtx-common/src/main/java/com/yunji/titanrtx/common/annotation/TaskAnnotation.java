package com.yunji.titanrtx.common.annotation;

import com.yunji.titanrtx.common.enums.TaskType;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Component
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE })
public @interface TaskAnnotation {

    TaskType type();

}
