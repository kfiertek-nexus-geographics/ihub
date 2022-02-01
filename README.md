
# BIMROCKET IHUB
Framework based on Apache Kafka and spring boot to synchronize the information of multiple object inventory databases.


## Development
### Code format
You should use [Allman code style](https://en.wikipedia.org/wiki/Indentation_style#Allman_style)

**In Eclipse**
Go to Window > Preferences > Java > Code Style > Formatter and import [allman_formatter_eclipse.xml](allman_formatter_eclipse.xml)
Inside class you can use shortcut CTRL + SHIFT + F to format automatically all objects in class.

### API Documentation
Run application and documentation is available http://localhost:8080/swagger-ui.html
Swagger also allows execution/test of endpoints very useful to create connectors or check available processors.

### Logging
We use org.slf4j.Logger
Declare field on start of class :
```  
private static final Logger log = LoggerFactory
      .getLogger(NAME_OF_CLASS.class);
```
Use following syntax:
```
log.info("methodName@className - message")
```
Inside processors please define connector name running for easier debugging like this:
```
log.info("methodName@className - Connector::{} message", this.getConnector.getName());
```
**Logging levels are following:**
* **TRACE** - Use to output variables content in order to trace which data is generating unexcepted behaviour. Excepted to be very verbose.
```
log.trace("VERY VERBOSE INCLUDE AS MUCH INFORMATION AS YOU CAN");
```
* **DEBUG** - Use to add more information about specified step in order to diagnose issues in code.
```
log.debug("VERBOSE NOTIFY STEP BY STEP CODE EXECUTION");
```
* **INFO** - Standard logging level, use it to notify changes in state of application like for example start of an connector.
```
log.info("INDICATE SOMETHING IS HAPPENING");
```
* **ERROR** - Use when something unexcepted has occured or when developer made a configuration mistake so it's easy to know which step of configuration failed.
```
log.error("SOMETHING WENT WRONG, WITH OBJECT {} INITIALIZATION FAILED", object.toString());
```
* **FATAL** - Datastore connection is failing or crucial part of app went wrong.
```
log.fatal("USE ONLY IF SOMETHING TERRIBLE HAPPEND");
```

### Datastore
Actually you can use one datastore **mongo** which is recommended for production environments.
We are currently working **on-fly** which internally uses h2 with JPA (no need for any configuration h2 is created and configured on-fly)

To set datastore you can find property in application properties it's called **data.store**

* Mongo Configuration properties:
```

Datastore
data.store=mongo
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration

Mongodb
spring.data.mongodb.host=127.0.0.1
spring.data.mongodb.port=27017
spring.data.mongodb.database=ihub
```

### API Documentation
Run application and documentation is available http://localhost:8080/swagger-ui.html
Swagger also allows execution/test of endpoints.

### Connectors
You can use connector (create it via API POST /connectors with ConnectorSetup object) to bind various processors sequentially
### Processors

### Using @ConfigProperty
While developing Processor we can be in need of specific configuration which varies with every connector (can be Consumer or Producer or both)