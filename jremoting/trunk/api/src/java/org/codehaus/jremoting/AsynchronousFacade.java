package org.codehaus.jremoting;
/*****************************************************************************
 * Copyright (C) JRemoting Committers. All rights reserved.                  *
 * ------------------------------------------------------------------------- *
 * The software in this class is published under the terms of the BSD        *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE-BSD.txt file.                                                 *
 *****************************************************************************/

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AsynchronousFacade {
}
