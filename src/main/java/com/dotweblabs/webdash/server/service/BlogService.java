package com.divroll.webdash.server.service;

import com.divroll.webdash.shared.Blog;
import com.divroll.webdash.shared.Blogs;
import com.divroll.webdash.server.service.exception.ValidationException;

/**
 * Created by Kerby on 1/5/2016.
 */
public interface BlogService {
    public Blog save(Blog user) throws ValidationException;
    public Blog read(Long blogId) throws ValidationException;
    public Blog update(Blog user) throws ValidationException;
    public void delete(Long blogId) throws ValidationException;
    public Blogs list(String cursor) throws ValidationException;
}
