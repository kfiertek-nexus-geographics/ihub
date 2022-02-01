
# BIMROCKET IHUB
Framework based on Apache Kafka and spring boot to synchronize the information of multiple object inventory databases.


## Development
### Code format
You should use [Allman code style](https://en.wikipedia.org/wiki/Indentation_style#Allman_style)

**In Eclipse**
Go to Window > Preferences > Java > Code Style > Formatter and import [allman_formatter_eclipse.xml](allman_formatter_eclipse.xml)

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