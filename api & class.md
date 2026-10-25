# SOME AGREEMENTS

## PRE
1. For time,consistently use ISO 8601 formate with timezone.

## About Classes

- account:
1. id (_The administrator account should be admin.And this is the primary key of this class._)[string <=20]
2. telephone[string <=13>]
3. password[string <=13>]
4. isManager(_Used to detemine a staff member.True marks an permission_)[bool]
- category:
(_to classify items independently of their date_)
1. id(_use `by default as identity` to generate automatically_)[int]
2. name[string <=50]
3. life(_to mark the length of shelf life.The unit is day.PostgreSQL will use interval to solve_)[int]
4. totalInve(_the total number of items in the category_)[int]
- goods:
1. id[int]
2. categoryId[int]
3. batch[int]
4. inbound(_to mark the inbound time._)[time]
5. outbound(_to mark the outbound time._)[time]
6. singleDimen(_length,width,and height included_)[array[int*3]]
7. quantity(_only refer to one time_)[int]
8. productionTime[time]
9. picture(_the array of address.Stored by bucket_)[array[string]]
- shelf:
1. shelfId[int]
2. gridNum[int]
3. goodsId[int]
4. number[int]
- flow:
1. id[int]
2. time[time]
3. goodsId[int]
4. shelfId(_goodsId and gridId should be arrays and correspond one-to-one._)[array[int]]
5. gridId[array[int]]
6. number[int]
7. isIn[bool]

## About Data Storage
Docker is used to simulate bucket and PostgreSQL.

## About apis

### GET
1. 


### POST
- **URL**: '/api/employee'
- **body**: 
