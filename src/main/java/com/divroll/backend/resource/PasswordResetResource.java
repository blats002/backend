package com.divroll.backend.resource;

import com.divroll.backend.model.PasswordResetDTO;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

public interface PasswordResetResource {
    @Get("json")
    void validateResetPassword(PasswordResetDTO entity);
    @Post("json")
    void resetPassword(PasswordResetDTO entity);
}
