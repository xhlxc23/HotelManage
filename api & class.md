# SOME AGREEMENTS

## PRE
1. For time,consistently use ISO 8601 formate with timezone.

## About Classes

- account:
1. id (_The administrator account should be Admin.This is the primary key of this class._)[string <=20]
2. telephone[string <=13>]
3. password[string <=13>]
4. isManager(_Used to detemine a staff member.True marks an permission_)[bool]

- category:
(_to classify the rate of the clients_)
1. id[int]
2. name[string <=10]
3. icon(_the url of icon_)[string]
4. discount[float]

- room:
1. id[int]
2. name[string <=10]
3. status[int]
4. userId[string <=20]
5. startTime[time]
6. endTime[time]
7. picture[array[string]]

- order:
1. id[int]
2. accountId[string <=20]
3. roomId[int]
3. orderTime[time]
4. status[int]
5. startTime[time]
6. endTime[time]
7. releaseTime[time]

## About Data Storage
Docker is used to simulate bucket and PostgreSQL.

## About apis

### GET
1. 


### POST
- **URL**: '/api/employee'
- **body**: 
