/*
*
* Copyright (c) 2017 Kerby Martino and Divroll. All Rights Reserved.
* Licensed under Divroll Commercial License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   https://www.divroll.com/licenses/LICENSE-1.0
*
* Unless required by applicable law or agreed to in writing, software distributed
* under the License is distributed as Proprietary and Confidential to
* Divroll and must not be redistributed in any form.
*
*/
package com.divroll.core.rest.exception;

import org.restlet.resource.Status;
import java.io.IOException;

/**
 * Exception thrown when file cannot be found.
 *
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 1.0
 * @since 1.0
 */
@Status(value = 404, serialize = false)
public class FileNotFoundException extends IOException {
    public FileNotFoundException() {}
    public FileNotFoundException(String message) {
        super(message);
    }
    public FileNotFoundException(String message, Exception e) {
        super(message, e);
    }
}
