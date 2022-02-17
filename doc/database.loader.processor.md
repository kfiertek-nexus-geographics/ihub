# Database loader processor

## :neutral_face: What does it do?

The purpose of this loader is to load data from databases and then parse it's data to Json Node collection 

For know supported databases are

 - H2
 - PostgreSQL

## :dizzy_face: How does it do?

To understand how does it do, I need to first explain you the processor's configuration

<details>
    <summary>Configurations</summary>

- **db.url** (REQUIRED)
    
    Database url

- **source.username** (REQUIRED)
  
    User token identification 

- **source.auth**
  
    Represents the mechanism to authenticate to remote host. By default it uses *Basic auth*. If you don't define this parameter or you define as BASIC, you **must** define *source.user* and *source.password*.

- **source.password**
- 
    User password



</details>

Let's list the proces that logic commits. At each step if it fails, it returns an empty collection

- Try to load remote excel file and save data to a temporally file
- Transform temporal file data into Json Node collection
- Closes temporal file
- Finally, it returns an iterator of the built Json Node collection