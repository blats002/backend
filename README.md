## Domino - a Key-Value BaaS (Backend as a Service) Server

Domino is a Key-Value "Backend-as-a-Service" Server  
built on top of [Jetbrains Xodus](https://github.com/JetBrains/xodus). 

It is still in a very early stage but basic features are available. 

The usage is very simple and requires no configuration. 

### Deploy 

To run your own Domino Server, simply execute these commands:

```$xslt
mvn jetty:run
```


If you are deploying to a Jetty server or Tomcat server, please refer to their website on how to deploy

### Usage

#### Create a new Application

In order to store key/values to Domino, create a application:

```$xslt
curl -X GET \
  http://localhost:8080/domino/applications
```

Take note of the response as it cannot be retrieved again, response
looks like this:

```$xslt
{
    "application": {
        "appId": "aec05bcb1cf245123fb6ca95a169e55b",
        "apiKey": "897d706ca9d123cd879c563214284f67",
        "masterKey": "cb95cc6105844b5e1237f3a8cd1f4caa"
    }
}
```

These keys will be required to "put" and "get" objects from the server.
The `apiKey` and `masterKey` can be changed later but the `appId` cannot 
be changed (by design for performance.)


#### Create an Entity

```$xslt
curl -X POST \
  http://localhost:8080/domino/entities/messages/hello \
  -H 'X-Domino-Api-Key: 897d706ca9d123cd879c563214284f67' \
  -H 'X-Domino-App-Id: aec05bcb1cf245123fb6ca95a169e55b' \
  -H 'X-Domino-ACL-Read: ['\''*'\'']' \
  -H 'X-Domino-ACL-Write: ['\''*'\'']' \
  -d world
```

#### Update and Entity

```$xslt
curl -X PUT \
  http://localhost:8080/domino/entities/messages/hello \
  -H 'X-Domino-Api-Key: 897d706ca9d123cd879c563214284f67' \
  -H 'X-Domino-App-Id: aec05bcb1cf245123fb6ca95a169e55b' \
  -H 'X-Domino-ACL-Read: ['\''*'\'']' \
  -H 'X-Domino-ACL-Write: ['\''*'\'']' \
  -d world again
```

#### Get an Entity
```$xslt
curl -X GET \
  http://localhost:8080/domino/entities/messages/hello \
  -H 'X-Domino-Api-Key: 897d706ca9d123cd879c563214284f67' \
  -H 'X-Domino-App-Id: aec05bcb1cf245123fb6ca95a169e55b'
```

#### Changing the masterKey and apiKey

```$xslt
curl -X PUT \
  https://domino-server.appspot.com/domino/applications \
  -H 'Content-Type: application/json' \
  -H 'X-Domino-App-Id: aec05bcb1cf245123fb6ca95a169e55b' \
  -H 'X-Domino-Master-Key: cb95cc6105844b5e1237f3a8cd1f4caa' \
  -d '{
    "application": {
        "apiKey": "NEW_API_KEY",
        "masterKey": "NEW_MASTER_KEY"
    }
}'
```

### Users

#### Create a User

```$xslt
curl -X POST \
  http://localhost:8080/domino/entities/users \
  -H 'Content-Type: application/json' \
  -H 'X-Domino-Api-Key: 897d706ca9d123cd879c563214284f67' \
  -H 'X-Domino-App-Id: aec05bcb1cf245123fb6ca95a169e55b' \
  -d '{
        "username" : "user",
        "password" : "pass"
      }'
```

Response will be a token that can be used as `X-Domino-Auth-Token` for future
HTTP request:

```
{
    "webToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6ImRlbW8ifQ.bIoJS9WJcr3gg5KIkkK9ZEf1XXXaUCyePdFbU9sKhw0"
}
```

#### Update a User
```$xslt
curl -X POST \
  http://localhost:8080/domino/entities/users \
  -H 'Content-Type: application/json' \
  -H 'X-Domino-Auth-Token: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9' \
  -H 'X-Domino-Api-Key: 897d706ca9d123cd879c563214284f67' \
  -H 'X-Domino-App-Id: aec05bcb1cf245123fb6ca95a169e55b' \
  -d '{
        "username" : "newuser",
        "password" : "newpass"
      }'
```

Reponse will be like this:

```
{
    "webToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6ImRlbW8xMjMifQ.1WE2ZvCGnAcrMDuaIehDAeunpoM2oniRtgPAX_iZ_Rw"
}
```
#### Delete a User


### Features

Any type of entity can be stored to Domino, texts, images or anything
that can be saved. 

Each entity stored contains a ACL (Access-Control-List) to control which 
Users can access the entity. ACL's are set during POST and PUT method (Creation or update of entity).

