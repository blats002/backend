package com.divroll.backend.resource.jee;

import com.google.common.io.ByteStreams;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Scanner;

public class JeeVersionServerResource extends BaseServerResource {

    @Get
    public Representation getProperties() {
        return new JsonRepresentation(gitProperties().toString());
    }

    protected JSONObject gitProperties() {
        InputStream is = getContext().getClass().getResourceAsStream("/git.properties");
        String text = null;
        try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
            text = scanner.useDelimiter("\\A").next();
        }
        JSONObject jsonObject = new JSONObject(text);
        return jsonObject;
    }

}
