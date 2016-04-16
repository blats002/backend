package com.divroll.webdash.server.service.gae;

import com.divroll.webdash.shared.Value;
import com.divroll.webdash.shared.Values;
import com.divroll.webdash.server.repository.gae.GaeValueRepository;
import com.divroll.webdash.server.service.ValueService;
import com.divroll.webdash.server.service.exception.ValidationException;
import com.google.inject.Inject;

import java.util.logging.Logger;

/**
 * Created by Kerby on 1/5/2016.
 */
public class GaeValueService implements ValueService {

    private static final Logger LOG
            = Logger.getLogger(GaeValueService.class.getName());

    @Inject
    GaeValueRepository valueRepository;

    @Override
    public Value save(Value user) throws ValidationException {
        return null;
    }

    @Override
    public Value read(Value blogId) throws ValidationException {
        return null;
    }

    @Override
    public Value update(Value user) throws ValidationException {
        return null;
    }

    @Override
    public void delete(Long blogId) throws ValidationException {

    }

    @Override
    public Values list(String cursor) throws ValidationException {
        return null;
    }
}
