# Database loader processor

## :neutral_face: What does it do?

The purpose of this loader is to load data from databases and then parse it's data to Json Node collection 

For know supported databases are

 - H2
 - PostgreSQL
 - SQL Server

## :dizzy_face: How does it do?

To understand how does it do, I need to first explain you the processor's configuration

<details>
    <summary>Configurations</summary>

- **db.url** (REQUIRED)
    
    Database url

- **db.username** (REQUIRED)
  
    User token identification 

- **db.password** (REQUIRED)
    
    User password

- **db.driver** (REQUIRED)
  
    Which driver should jdbc use for connecting to remote database

    Available:
    -  org.h2.Driver
    -  org.postgresql.Driver
    -  microsoft.sqlserver.jdbc

- **db.query** (REQUIRED)
  
    SQL query to get content from remote database

- db.request.timeout

    Timeout for database response query request

</details>

Let's list the proces that logic commits. At each step if it fails, it returns an empty collection

- Try to load data from remote database using configured query
- Transform data to Json Collection
- Finally, it returns an iterator of the built Json Node collection