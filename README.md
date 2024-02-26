# sunbird-cb-network

## Neo4j Script

1. CREATE CONSTRAINT ON  (n:userV2) ASSERT n.id IS UNIQUE

2. CREATE INDEX ON :userV2(id)

## Database Requirements
This project requires the following databases:
- Cassandra
- Elasticsearch
- Neo4j

## Network Hub APIs Endpoints

### 1. /connections/add
Description: Send connection request to a user.

```shell
curl --location 'http://localhost:3013/connections/add' \
--header 'Content-Type: application/json' \
--data-raw '{
"userIdFrom": "",
"userNameFrom": "",
"userDepartmentFrom": "",
"userIdTo": "",
"userNameTo": "",
"userDepartmentTo": ""
}'
```
### 2. /connections/update
Description: Accept the connection request received.

```shell
curl --location 'http://localhost:3013/connections/update' \
--header 'Content-Type: application/json' \
--data-raw '{
"userIdFrom": "",
"userNameFrom": "",
"userDepartmentFrom": "",
"userIdTo": "",
"userNameTo": "",
"userDepartmentTo": "",
"status": "Approved"
}'
```
### 3. /connections/profile/fetch/requests/received
Description: Get all the connection requests received by a particular user.

```shell
curl --location 'http://localhost:3013/connections/profile/fetch/requests/received' \
--header 'userId: <user-id>'
```
### 4. /connections/profile/fetch/requested
Description: Get all the connection requests sent by a user.

```shell
curl --location 'http://localhost:3013/connections/profile/fetch/requested' \
--header 'userId: <user-id>'
```
### 5. /connections/profile/fetch/established
Description: Get the list of approved connections.

```shell
curl --location 'http://localhost:3013/connections/profile/fetch/established' \
--header 'userId: <user-id>'
```
### 6. /connections/profile/find/suggests
Description: Get the list of suggested connections.

```shell
curl --location 'http://localhost:3013/connections/profile/find/suggests' \
--header 'userId: <user-id>'
```

### 7. /connections/profile/find/recommended
Description: Get the recommended connections list based on filter.

```shell
curl --location 'http://localhost:3013/connections/profile/find/recommended' \
--header 'userId: <user-id>' \
--header 'Content-Type: application/json' \
--data '{
    "size": 50,
    "offset": 0,
    "search": [
          {
            "field": "employmentDetails.departmentName",
            "values": [
                "<departmentName>"
            ]
        }
    ]
}'
```
### 8. /v1/user/autocomplete/?searchString=<name or email>
Description: Search for users based on name or email.

```shell
curl --location 'http://localhost:3013/v1/user/autocomplete/?searchString=<search-string>'
```