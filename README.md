# Jackson Extension
[![Build Status](https://travis-ci.org/klausboeing/jackson-extension.svg?branch=master)](https://travis-ci.org/klausboeing/jackson-extension)

Module for Jackson adds utilities for serialization and deserialization.

## Getting Started

### @JsonPropertyGroup and @JsonUsePropertyGroup

#### Sample 1

```java
public class Customer {

    private String name;
    private String email;
    private String phone;
    private Address address;
    
    // .. more
}
```

```java
public class Address {

    private String street;
    private String city;
    private String state;
    private String zip;
    private List<Contact> contacts;

    // .. more
}
```

```java
public class Contact {

    private String name;
    private String email;
    private String phone;
    
    // .. more
}

```

The JSON result of serialization:

```json
{
  "name" : "Adam Levine",
  "email" : "adam.levine@maroon5.com",
  "phone" : "(877) 609-2233",
  "address" : {
    "street" : "1444 S. Alameda Street",
    "city" : "Los Angeles",
    "state" : "Califórnia",
    "zip" : "90021",
    "contacts" : [ {
      "name" : "James Valentine",
      "email" : "james.valentine@maroon5.com",
      "phone" : "(877) 609-2244"
    }, {
      "name" : "Jesse Carmichael",
      "email" : "jesse.carmichael@maroon5.com",
      "phone" : "(877) 609-2255"
    } ]
  }
}
```

Use @JsonUsePropertyGroup and @JsonPropertyGroup to reduce serialization of relationships. Note the relationships with @JsonUsePropertyGroup.

```java
public class Customer {

    private String name;
    private String email;
    private String phone;
    
    @JsonUsePropertyGroup
    private Address address;
    
    // .. more
}
```

Note the attributes of the relationship with @JsonPropertyGroup.


```java
public class Address {

    private String street;
    @JsonPropertyGroup
    private String city;
    @JsonPropertyGroup
    private String state;
    private String zip;
    private List<Contact> contacts;

   // .. more
}
```

The JSON result of serialization:

```json
{
  "name" : "Adam Levine",
  "email" : "adam.levine@maroon5.com",
  "phone" : "(877) 609-2233",
  "address" : {
    "city" : "Los Angeles",
    "state" : "Califórnia"
  }
}
```

### Prerequisities

- Maven
- Java 8

### Installing

Configure the project repository:

```xml
        <repositories>
            <repository>
                <id>klausboeing-mvn-repo</id>
                <url>https://raw.github.com/klausboeing/jackson-extension/mvn-repo/</url>
                <snapshots>
                    <enabled>true</enabled>
                    <updatePolicy>always</updatePolicy>
                </snapshots>
            </repository>
        </repositories>
```
Add the following dependence on project:
```xml
        <dependency>
          <groupId>com.klausboeing</groupId>
          <artifactId>jackson-extension</artifactId>
          <version>0.0.2-SNAPSHOT</version>
        </dependency>
```

Configure the module in ObjectMapper:

```java
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JacksonExtensionModule());
```


## Versioning

We use [SemVer](http://semver.org/) for versioning.

## Authors

* **Klaus Boeing** - *Initial work*
