## Welcome to backend

backend is an Open Source, RESTful Backend as a Service for mobile apps, web apps and IoT devices.
Self-contained and does not need any external database. It is build on top of [Jetbrains Xodus](https://github.com/JetBrains/xodus). 

**Requirements:** JDK 8, Maven

- [Features](#features)
- [Installation](#installation)
- [How to use](#how-to-use)
- [Documentation]()
- [Examples]()

Features
---

- Multi-tenancy (same backend instance can be used for multiple apps)
- User authentication (register, login, reset password)
- Data persistence through 'Entities'
- BeforeSave and AfterSave triggers
- PubSub (created, updated, deleted, linked)
- Custom Codes (server-side code extensions, Java and Javascript)
- (Future) Database storage through Amazon S3 or S3-compatible storage

Installation
---
To run your own backend server, simply execute this commands:

```
mvn jetty:run
```

If you are deploying to a Jetty server or Tomcat server, please refer to their website on how to deploy

How to use
---

#### Create a new Application

```
curl -X POST \
  http://localhost:8080/applications/app-name
```
This will output access keys that can be used in the front-end.  

```
{
    "application": {
        "appId": "aec05bcb1cf245123fb6ca95a169e55b",
        "apiKey": "897d706ca9d123cd879c563214284f67",
        "masterKey": "cb95cc6105844b5e1237f3a8cd1f4caa"
    }
}
```

You should take note of the response as it cannot be retrieved again.

Checkout these examples for [Java](https://github.com/divroll/Backend-SDK-Java/tree/master/src/test/java/com/divroll/backend/sdk) and [GWT](https://github.com/divroll/Backend-SDK-GWT/tree/master/src/test/java/com/divroll/backend/sdk) 

Client integrations
---

- [GWT](https://github.com/divroll/Backend-SDK-GWT)
- [Java](https://github.com/divroll/Backend-SDK-Java)
- [Java/CustomCode]()

License
---

AGPL 3.0
