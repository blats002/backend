package com.divroll.webdash.server.service;

import com.divroll.webdash.client.shared.Value;
import com.divroll.webdash.client.shared.Values;
import com.divroll.webdash.server.service.exception.ValidationException;

/**
 * Created by Kerby on 1/5/2016.
 */
public interface ValueService {
    public Value save(Value user) throws ValidationException;
    public Value read(Value blogId) throws ValidationException;
    public Value update(Value user) throws ValidationException;
    public void delete(Long blogId) throws ValidationException;
    public Values list(String cursor) throws ValidationException;

}
