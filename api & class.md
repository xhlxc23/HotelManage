# SOME AGREEMENTS

## PRE
1. For time,consistently use ISO 8601 formate with timezone.

## About Classes

- account:
1. id (_The administrator account should be Admin.This is the primary key of this class._)[string <=20]
2. vipId(_the mark of employees included_)[int]
3. telephone[string <=13]
4. password[string <=13]
5. exp[int]


- vip:
(_to classify the rate of the clients_)
1. id[int]
2. name[string <=10]
3. icon(_the url of icon_)[string]

- discount:
1. vipId[int]
2. categoryId[string]
3. vipDiscount[float]

- room:
1. id[string]
2. name[string <=10]
3. status[int]
4. categoryId[string]


- category:
1. id[string]
2. name[sting]
3. basicPrice[int]
4. maxNumber[int]
5. picture[array[string]]
6. description[string]

- occupation:
1. id[string]
2. orderId[string]
3. stayData[time]
4. roomId[string]


- order:
1. id[string]
2. accountId[string <=20]
3. roomId[string]
3. orderTime[time]
4. status[int]
5. startTime[time]
6. endTime[time]
7. releaseTime[time]
8. price[int]
9. remark[string]
## About Data Storage
Docker is used to simulate bucket and PostgreSQL.

## About apis

### GET
1. 


### POST
- **URL**: '/api/employee'
- **body**: 
