package com.divroll.webdash.server.service.gae;

import com.divroll.webdash.shared.Blog;
import com.divroll.webdash.shared.Blogs;
import com.divroll.webdash.server.repository.gae.GaeBlogRepository;
import com.divroll.webdash.server.service.BlogService;
import com.divroll.webdash.server.service.exception.ValidationException;
import com.google.inject.Inject;

import java.util.logging.Logger;

/**
 * Created by Kerby on 1/5/2016.
 */
public class GaeBlogService implements BlogService {

    private static final Logger LOG
            = Logger.getLogger(GaeBlogService.class.getName());

    @Inject
    GaeBlogRepository blogRepository;

    @Override
    public Blog save(Blog user) throws ValidationException {
        return null;
    }

    @Override
    public Blog read(Long blogId) throws ValidationException {
        return null;
    }

    @Override
    public Blog update(Blog user) throws ValidationException {
        return null;
    }

    @Override
    public void delete(Long blogId) throws ValidationException {

    }

    @Override
    public Blogs list(String cursor) throws ValidationException {
        return null;
    }
}
