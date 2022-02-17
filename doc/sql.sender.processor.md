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

- **db.query.check** 
    
    In case you want to check if a register exist by a key or combination of keys

- **db.query.update**

    Used in case you had defined *db.query.check*, if it finds just one register that matches it updates it

- **db.query.insert** (REQUIRED)

    SQL to add register in target table

- **db.query.requires**

    Columns that **must** appear in processed object to add in target table

- **db.query.optionals**

    Columns that **may** appear in processed object to add in target table

- *+db.request.timeout**

    Timeout for database response query request

</details>

Let's list the proces that logic commits. At each step if it fails, returns false, indicating that the process were wrong

- Create maps of parameters to add to SQL query
- Check if it should add or update register
- Execute SQL query