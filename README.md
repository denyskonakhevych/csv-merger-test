# CSV merger (test project)

Service enriches provided trade data from csv with product name.
In current implementation it was decided no to store any data in temp file when implementing API from task and all processing was done in pass-through mode.
For test perspective it was also added method in service that uses temp file, but it would require client to delete file after receiving it.

Its worth mentioning, that csv procession is done in the way, it does not require holding the entire data set in memory.
There are some concerns regarding how file is passed to API, it would be covered Things to improve section.

## Prerequisites

Make sure you have the following installed:

- Java Development Kit (JDK)
- Apache Maven

To build the project, follow these steps:

1. Clone this repository to your local machine:
   ```shell
   git clone https://github.com/denyskonakhevych/csv-merger-test.git
   ```

2. Build project
   ```shell
   mvn clean package
   ```

3. Run service
   ```shell
   java -jar target/csv-merger.jar
   ```

4. Verify 
   ```shell
   curl --request POST --data-binary @trade.csv --header 'Content-Type: text/csv' --header 'Accept: text/csv' http://localhost:8080/api/v1/enrich
   ```

## Notes

In task description provided command:

   ```shell
   curl --request POST --data @trade.csv --header 'Content-Type: text/csv' --header 'Accept: text/csv' http://server.com/api/v1/enrich
   ```

ignores all new-line character that results in malformatted input

   ```text
   date,product_id,currency,price20160101,1,EUR,10.020161301,1,EUR,42.020160101,2,EUR,20.120160101,3,EUR,30.3420160101,11,EUR,35.34
   ```

It was decided not to try to separate price from date (since it could be invalid date) and use instead of `--data` parameter `--data-binary` 

## Things to improve

1. Instead of providing csv file as request body it is better to use multipart upload (especially in case of big files).
2. In case of big files and/or complex enrichment logic, it would be better to make process asynchronous. First file should be handled and safely uploaded to reliable storage. Then async job can start processing it, resulting new file to be created. In this case file processing could be parallelized by multiple jobs (by assigning some range of line, or product id hash, etc.). When processed file is ready, original file is not needed and can be removed and client should be notified (long/short polling/notification through email/ws/etc.). After file is received by user, it also can be removed (or it can be removed automatically by ttl). Also, it worth considering using some big data tools (Spark) to implement enrichment operations, depending on file size.
3. Add code coverage, metrics, etc.