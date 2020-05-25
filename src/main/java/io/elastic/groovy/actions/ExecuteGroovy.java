package io.elastic.groovy.actions;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import io.elastic.api.ExecutionParameters;
import io.elastic.api.Function;
import io.elastic.api.JSON;
import io.elastic.api.Message;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonObject;
import javax.json.JsonString;
import java.io.StringReader;

/**
 * Action to create a pet.
 */
public class ExecuteGroovy implements Function {
    private static final Logger logger = LoggerFactory.getLogger(ExecuteGroovy.class);

    /**
     * Executes the io.elastic.groovy.actions's logic by sending a request to the Petstore API and emitting response to the platform.
     *
     * @param parameters execution parameters
     */
    @Override
    public void execute(final ExecutionParameters parameters) {
        logger.info("About to execute Groovy code");

        // contains action's configuration
        final JsonObject configuration = parameters.getConfiguration();

        // access the value of the mapped value into name field of the in-metadata
        final JsonString code = configuration.getJsonString("code");
        if (code == null) {
            throw new IllegalStateException("Code is required");
        }
        final Binding binding = new Binding();
        binding.setProperty("parameters", parameters);

        final ImportCustomizer importCustomizer = new ImportCustomizer();
        importCustomizer.addStarImports(
                "io.elastic.api",
                "org.slf4j",
                "javax.json",
                "javax.ws.rs.client",
                "javax.ws.rs.core"
        );

        final CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.addCompilationCustomizers(importCustomizer);

        final GroovyShell shell = new GroovyShell(getClass().getClassLoader(), binding, compilerConfiguration);

        final Script script = shell.parse(new StringReader(code.getString()));

        final Object result = script.run();

        if (result instanceof Message) {

            logger.info("Emitting data");

            // emitting the message to the platform
            parameters.getEventEmitter().emitData((Message) result);
        }
    }
}
