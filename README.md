# Groovy component

This component for the [elastic.io platform](https://www.elastic.io "elastic.io platform") may be used to execute an 
arbitrary Groovy code inside your integration flow.


## Available Variables
Here the list of the available variables within the context of execution. 

### Elastic.io Specific Functionality
- `parameters` - an instance of [ExecutionParameters](https://javadoc.io/doc/io.elastic/sailor-jvm/latest/io/elastic/api/ExecutionParameters.html) used to access the incoming message, step's configuration, step's snapshot or an instance of [EventEmitter](https://javadoc.io/doc/io.elastic/sailor-jvm/latest/io/elastic/api/EventEmitter.html).
- `logger` - an instance of [org.slf4j.Logger](http://www.slf4j.org/apidocs/org/slf4j/Logger.html) used to log inside the component

### Available Libraries

Here is the list of available libraries within the context of execution. 

- [The JAX-RS client API](https://docs.oracle.com/javaee/7/api/javax/ws/rs/client/package-summary.html)

## Emitting an empty object

The following code creates an empty [Message](https://javadoc.io/doc/io.elastic/sailor-jvm/latest/io/elastic/api/Message.html) to be sent to the next step in the integration flow.

````java
new Message.Builder().build()
````


## Emitting multiple messages

The following code emits two messages.

````java
1.upto(2) {
    def body = Json.createObjectBuilder().add("id", "${it}").build()
    def msg = new Message.Builder().body(body).build()
    parameters.getEventEmitter().emitData(msg);
} 
````

### Calling an external REST API

The following code sends a request to an external REST API using [The JAX-RS client API](https://docs.oracle.com/javaee/7/api/javax/ws/rs/client/package-summary.html). 
The response is packed into a [Message](https://javadoc.io/doc/io.elastic/sailor-jvm/latest/io/elastic/api/Message.html) and emitted.

````java
def token = "${System.getenv('ELASTICIO_API_USERNAME')}:${System.getenv('ELASTICIO_API_KEY')}"
JsonObject result = ClientBuilder.newClient()
    .target('http://localhost:12345')
    .path('v1/objects/1')
    .request(MediaType.APPLICATION_JSON_TYPE)
    .header("Authorization", "Basic " + Base64.getEncoder().encodeToString(token.getBytes("UTF-8")))
    .get(JsonObject.class)
                
new Message.Builder().body(result).build()
````
