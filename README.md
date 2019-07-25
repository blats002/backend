![Divroll Logo](https://avatars1.githubusercontent.com/u/30984522?s=200&v=4)

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
<br/>   

## Welcome to backend

backend is an Open Source, RESTful Backend as a Service for mobile apps, web apps and IoT devices.
Self-contained and does not need any external database. It is built on top of [Jetbrains Xodus](https://github.com/JetBrains/xodus). 

**Requirements:** JDK 8, Maven

- [Features](#features)
- [Installation](#installation)
- [How to use](#how-to-use)

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

If you are deploying to a Jetty server or Tomcat server, please refer to their website on how to deploy.
Alternatively, you can deploy to Heroku:

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy)

How to use
---

#### Create a new Application

```
curl -X POST \
  http://localhost:8080/divroll/applications/app-name
```
This will output access keys that can be used in the front-end.  

```
{
    "application": {
        apiKey: "a87ee2497fb643e88c60f1402538501c",
        appId: "0d26c5084dc44b289b61eb152048dd1f",
        appName: "app-name",
        masterKey: "b94a2a04c1894282a0bb01ebb60a12b4"
    }
}
```

You should take note of the response as it cannot be retrieved again.

You can also create an application through the Backend Dashboard accessible via:

```
http://localhost:8080/app/
``` 

Similarly, access keys cannot be retrieved again but are stored in the browser local storage. 

Checkout these examples for [Java](https://github.com/divroll/Backend-SDK-Java/tree/master/src/test/java/com/divroll/backend/sdk) and [GWT](https://github.com/divroll/Backend-SDK-GWT/tree/master/src/test/java/com/divroll/backend/sdk) 

Client integrations
---

- [GWT](https://github.com/divroll/Backend-SDK-GWT)
- [Java](https://github.com/divroll/Backend-SDK-Java)
- [Java/CustomCode]()

License
---

AGPL 3.0
