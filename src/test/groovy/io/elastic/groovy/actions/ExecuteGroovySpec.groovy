package io.elastic.groovy.actions

import com.github.restdriver.clientdriver.ClientDriverRequest
import com.github.restdriver.clientdriver.ClientDriverRule
import io.elastic.api.EventEmitter
import io.elastic.api.ExecutionParameters
import io.elastic.api.Message
import io.elastic.sailor.impl.HttpUtils
import org.junit.Rule
import spock.lang.Specification

import javax.json.Json

import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo
import static org.hamcrest.Matchers.equalToIgnoringCase

class ExecuteGroovySpec extends Specification {


    @Rule
    public ClientDriverRule driver = new ClientDriverRule(12345);

    def emitter
    def emittedMessages = []

    def message = new Message.Builder().build()
    def errorCallback = Mock(EventEmitter.Callback)
    def snapshotCallback = Mock(EventEmitter.Callback)
    def reboundCallback = Mock(EventEmitter.Callback)
    def updateKeysCallback = Mock(EventEmitter.Callback)
    def httpReplyCallback = Mock(EventEmitter.Callback)
    def dataCallback = new EventEmitter.Callback () {

        @Override
        void receive(Object data) {
            emittedMessages << data
        }
    }

    def setup() {
        emitter = new EventEmitter.Builder()
                .onError(errorCallback)
                .onData(dataCallback)
                .onSnapshot(snapshotCallback)
                .onRebound(reboundCallback)
                .onUpdateKeys(updateKeysCallback)
                .onHttpReplyCallback(httpReplyCallback)
                .build()
    }

    def "should send requests using JAX-RS successfully"() {

        setup:
        def component = new ExecuteGroovy()
        def code = """
        JsonObject pet = ClientBuilder.newClient()
                .target('http://localhost:12345')
                .path('v1/exec/result/55e5eeb460a8e2070000001e')
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(JsonObject.class)
                
        new Message.Builder().body(pet).build()
        """
        def configuration = Json.createObjectBuilder().add("code", code).build()
        def parameters = new ExecutionParameters.Builder(message, emitter).configuration(configuration).build()

        driver.addExpectation(
                onRequestTo("/v1/exec/result/55e5eeb460a8e2070000001e")
                        .withMethod(ClientDriverRequest.Method.GET),
                giveResponse('{"status":"done"}', 'application/json')
                        .withStatus(200));


        when:
        component.execute(parameters)

        then:
        emittedMessages.size == 1
        emittedMessages[0].getBody().getString("status") == "done"
    }

    def "should emit multiple messages using EventEmitter"() {

        setup:
        def component = new ExecuteGroovy()
        def code = """
        def body = Json.createObjectBuilder().add("id", "123").build()
        def msg = new Message.Builder().body(body).build()
        parameters.getEventEmitter().emitData(msg);
        
        body = Json.createObjectBuilder().add("id", "456").build()
        msg = new Message.Builder().body(body).build()
        parameters.getEventEmitter().emitData(msg);
                
        """

        def configuration = Json.createObjectBuilder().add("code", code).build()
        def parameters = new ExecutionParameters.Builder(message, emitter).configuration(configuration).build()


        when:
        component.execute(parameters)

        then:
        emittedMessages.size == 2
        emittedMessages[0].getBody().getString("id") == "123"
        emittedMessages[1].getBody().getString("id") == "456"
    }
}
