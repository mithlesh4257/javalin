/*
 * Copyright 2017 David Åse
 * Licensed under Apache 2.0: https://github.com/tipsy/javalin/blob/master/LICENSE
 *
 */

package javalin;

import java.util.HashMap;

import org.apache.velocity.app.VelocityEngine;
import org.junit.Test;

import javalin.util.TestObject_NonSerializable;
import javalin.util.TestObject_Serializable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.HttpResponse;

import static javalin.ResponseMapper.TemplateUtil.model;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TestResponseMapper extends _UnirestBaseTest {

    @Test
    public void test_responseBuilder_json() throws Exception {
        app.get("/hello", (req, res) -> res.status(200).json(new TestObject_Serializable()));
        String expected = new ObjectMapper().writeValueAsString(new TestObject_Serializable());
        assertThat(GET_body("/hello"), is(expected));
    }

    @Test
    public void test_responseBuilder_json_haltsForBadObject() throws Exception {
        app.get("/hello", (req, res) -> res.status(200).json(new TestObject_NonSerializable()));
        HttpResponse<String> response = call(HttpMethod.GET, "/hello");
        assertThat(response.getStatus(), is(500));
        assertThat(response.getBody(), is("Failed to write object as JSON"));
    }

    @Test
    public void test_renderVelocity_works() throws Exception {
        app.get("/hello", (req, res) -> res.renderVelocity("/templates/velocity/test.vm", model("message", "Hello Velocity!")));
        assertThat(GET_body("/hello"), is("<h1>Hello Velocity!</h1>"));
    }

    @Test
    public void test_velocity_customEngine_works() throws Exception {
        app.get("/hello", (req, res) -> res.renderVelocity("/templates/velocity/test.vm", model()));
        assertThat(GET_body("/hello"), is("<h1>$message</h1>"));
        ResponseMapper.Velocity.setEngine(strictVelocityEngine());
        assertThat(GET_body("/hello"), is("Internal server error"));
    }

    private static VelocityEngine strictVelocityEngine() {
        VelocityEngine strictEngine = new VelocityEngine();
        strictEngine.setProperty("runtime.references.strict", true);
        strictEngine.setProperty("resource.loader", "class");
        strictEngine.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        return strictEngine;
    }

}