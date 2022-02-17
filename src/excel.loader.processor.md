# Excel loader processor

## :neutral_face: What does it do?

The purpose of this loader is to load Excel binary files from remote host. 

For know source protocols supported are.

 - HTTP
 - FTP

## :dizzy_face: How does it do?

To understand how does it do, I need to first explain you the processor's configuration

<details>
    <summary>Configurations</summary>

- **source.has.headers** (REQUIRED)
   
    This property notice logic if loaded excel file has it's default headers. This parameter is required, because there is no control of type to know if first line is header and the rest is the data. In case this property is false, logic autogenerate headers

-  **source.protocol** (REQUIRED)
  
    Represents the way the logic connect to remote host in order to rescue the binary file. For now *HTTP* and *FTP* protocols are available.

- **source.auth**
  
    Represents the mechanism to authenticate to remote host. By default it uses *Basic auth*. If you don't define this parameter or you define as BASIC, you **must** define *source.user* and *source.password*.

- **source.password**
- 
    User password

- **source.user**
  
    User token identification

- **source.hostname** (REQUIRED)
  
    IP or DNS to connect to remote host

- **source.port**
  
    Socket connection port, if it is defined, overrides default protocol socket connection

- **source.uri**

    Remote target element

- **source.param.names**
  
    Thougth to be used in HTTP protocols, it allows you add query params to HTTP request. Add how much you like, but separate it using semicolom.

- **source.param.values**
  
    Values related to names params, separated them using semicoloms

- **source.local**
  
    Path of temporally file rescued from remote
</details>
  