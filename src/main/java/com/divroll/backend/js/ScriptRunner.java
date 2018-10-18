package com.divroll.backend.js;

import org.dynjs.Config;
import org.dynjs.exception.ThrowException;
import org.dynjs.runtime.*;

public class ScriptRunner {
    DynJS dynjs;
    Config config;

    public ScriptRunner() {
        config = new Config();
        dynjs  = new DynJS(config);
    }

    public Object runScript(String source) {
        Runner runner = dynjs.newRunner();
        return runner.withSource(source).execute();
    }

    public Object eval(String code) {
        return dynjs.evaluate(code);
    }

}