/*
 * Copyright (C) 2006-2007 Mindquarry GmbH, All Rights Reserved
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 */
package com.mindquarry.desktop.util;


/**
 * Helper classes to work with exceptions.
 * 
 * @author dnaber
 */
public class ExceptionUtilities {

    private ExceptionUtilities() {
      // static methods only, no public constructor
    }
    
    /**
     * Returns true if <code>cause</code> is one of the causes
     * of <code>ex</code>.
     */
    public static boolean hasCause(Throwable ex, Class cause) {
        Throwable throwable = ex.getCause();
        while (throwable != null) {
            if (throwable.getClass() == cause) {
                return true;
            }
            throwable = throwable.getCause();
        }
        return false;
    }
    
}
