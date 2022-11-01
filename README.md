#  Dhwanit Sharma - HW2 CS441
## University of Illinois at Chicago

## Introduction
This project is to create  a distributed program for parallel processing of the log files that are generated using 
the LogGenerator Project. The Goals of the project are as follows:

1. Create an algorithm of at most O(log N) complexity for locating messages from a set of N messages that contain some designated pattern-matching string that are timestamped within some delta time interval from the requested time.
2. The input to the client program includes two parameters: the time, T and some time interval, dT, e.g., T = 9:58:55 and dT = 00:01:00. Algorithm will determine if messages exist in the log that are timestamped within the time interval 9:57:55 and 9:59:55.
3. Next, algorithm will determine which of these messages contain strings that match the pattern specified in configuration parameter ```pattern```.

To achieve the above goals, a Lambda function is created that will check to see if a log file contains messages within a given time interval and it will return a boolean status to the client to specify the presence of the desired time interval in the log file.

What is a Lambda Function?

Lambda is a compute service that lets you run code without provisioning or managing servers. Lambda runs your code on a high-availability compute infrastructure and performs all of the administration of the compute resources, including server and operating system maintenance, capacity provisioning and automatic scaling, and logging. With Lambda, you can run code for virtually any type of application or backend service.

Link to the video : <a href="https://youtu.be/hTdQP7JUfa0" target="_blank">Video</a>

Link to the LogGenerator repo <a href="https://github.com/dhwanitsharma/LogFileGenerator" target="_blank">LogGenerator Rep</a> 

## Project Structure
The project structure is as follows:
1. Src
    1. main
        1. resources --- Contains the useful resources and config file
            1. S3.conf
            2. logback.xml
        2. scala --- Contains all the task files and helper files.
            1. Helper
                1. CreateLogger.scala
            2. grpc
               1. grpcClient.scala
               2. grpcServer.scala
            3. Gateway.scala
            4. Lambda.scala
        3. protobuf
           1. Log.proto
    2. test --- Contains all the test files.
        1. scala
            1. TestApplicationConf.scala
            2. TestPattern.scala

## Installation Instructions
### Task 1 : Upload the LogGenerator into a EC2 instance
Create an EC2 instance as shown in the video and run the following commands to setup java and sbt
1. For Java:
```
sudo su –
apt-get update
apt-get upgrade
apt install openjdk-17-jdk openjdk-17-jre
```
2. For sbt:
```
sudo apt-get update
sudo apt-get install apt-transport-https curl gnupg -yqq
echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | sudo tee /etc/apt/sources.list.d/sbt.list
echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | sudo tee /etc/apt/sources.list.d/sbt_old.list
curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | sudo -H gpg --no-default-keyring --keyring gnupg-ring:/etc/apt/trusted.gpg.d/scalasbt-release.gpg --import
sudo chmod 644 /etc/apt/trusted.gpg.d/scalasbt-release.gpg
sudo apt-get update
sudo apt-get install sbt
```

3. Now copy the LogGenerator folder to the EC2 instance using the following command:

`scp -i "ec2-dhwanit.pem" -r path\LogFileGenerator\* ubuntu@ec2-18-216-6-187.us-east-2.compute.amazonaws.com:path`

4. Change the config file to select the correct bucket and rollover time. Then run the `sbt run` command to start the LogGenerator. This will generate the log files
and copy the same to the S3 bucket mentioned in the config file.

### Task 2 : Lambda Function
1. Clone this repository using `git clone` command.
2. Compile and create a jar using the command `sbt clean compile assembly`. This will create the jar file in the following path `target\scala-2.13`.
3. Create a Lambda function as shown in the video, and create an API Gateway
4. In the Lambda function edit the handler to the following `Gateway::handleRequest`.
5. Test the Lambda function, using the testing function in API Gateway test utility using the JSON API template:
```
{
   "interval" : "00:02:00",
   "time" : "2022-10-28-19-22-00-000",
   "pattern" : "Rsxg"
}
```

### Task 3 : gRPC Server
1. Clone this repository using `git clone` command.
2. Go inside the sbt using the `sbt` command in the intellj terminal.
3. To run the server, use the command `runMain grpc.grpcServer`

### Task 4 : gRPC Client
1. Clone this repository using `git clone` command.
2. Edit the config file S3.conf `grpc` settings to setup the JSON call.
3. Go inside the sbt using the `sbt` command in the intellj terminal.
4. To run the client, use the command `runMain grpc.grpcClient`


## Log File Description
The input file for the programs will be a log file with a specific format where each line will have a log file such as:
```"17:47:37.791 [scala-execution-context-global-25] WARN HelperUtils.Parameters$ - Swq;g+6M:?820=Gmd#.p)sFaqo".``` 

Where we have TimeStamp:17:47:37.791, Error Message Type:WARN, Error Message:Swq;g+6M:?820=Gmd#.p)sFaqo in each line.

## Tasks Description
### Task 1 : Upload the LogGenerator into a EC2 instance and save the logs in a S3 bucket.
The LogGenerator generates logs based on the config file. I have updated the LogGenerator File to make a new log file every 5 minutes. To achieve this
I have used a class file which extends the RollingFileAppender class. Example of the file name of the log generated is ```LogFileGenerator-20221028-19-20``` where 
20221028 is the date in YYYYMMDD format, and 19-20 is the time, in HH-MM format.

After the logs are generated, I have created a S3Uploader class which will upload the log directory in the S3 bucket. For the purpose of this project all the logs are generated for a single day.

### Task 2 : Create a Lambda function to detect a certain pattern in the log messages
The Lambda function has two major parts:
1. First it iterates through the file name which on the basis of the timestamp. It calculates the starting time and ending time bases on the 
time and the interval provided in the input. Using this time frame, function iterates through the folder and reads the file name of all the log files. It compares the HH-MM part of the file name with the interval
and makes a ```list of key``` of the logs in the bucket. If the input time range logs are not available, function returns a string ```false```.
2. If the list is populated then we iterate through the file list, the timeStamps are taken as an array. Using the binary search, we find the closest value to the start and end time in the array. Using this we get 
the index of the messages which are in desired time interval. Using this index we slice the original log file and iterate through those logs
to find the ```pattern``` in the log messages. Example:
   1. In the list, we have a log file from index 0 to 10000. The starting time stamp is 19:20:00 and ending time stamp 19:25:00. The target value is 19:22 with interval of 00:01. Now the starting interval is 19:21:00 and ending is 19:23:00. 
   2. We find the starting closest index of 19:21 as 2500 and closest index 19:23 as 7500. Now, we use these indexes 2500 and 7500 to slice the original log file, and return the MD5 hash of the log message which is matched to the input ``pattern``

### Task 3 : Create a gRPC Server
Created a gRPC server, which calls the Lambda function. The gRPC Server uses the protobuf to define the structure for the data. The server uses ``Rest POST`` call to the Lambda function. The example of the JSON used by the server.
```
{
    "interval" : "00:02:00",
    "time" : "2022-10-28-19-22-00-000",
    "pattern" : "Rsxg"
}
```

### Task 4 : Create a gRPC Client
Created a gRPC client, which calls the gRPC server. The gRPC client also uses the protobuf to define the structure for the data. The client uses ```S3.conf``` configuration file for input where the inputs are defined ```grpc``` section of the file.
The client uses the following as inputs:
```
time = "2022-10-28-19-22-00:000"
detect_patter = "Rsxg"
interval = "00:02:00"
```

## Output
The output will be as follows:
1. If the pattern is found in the log files, the console of client will print out the following:

`Log messages are found with hash <MD5 HASHCODE> `

2. If the pattern is not found in the log files, the console of client will print out the following:

`No log statements found for given parameters`

## AWS Deployment
As shown in the video, build the file using ```sbt clean compile assembly``` to build the jar. The jar is then uploaded to the Lambda function. Use the instruction in the video to set
and test the Lambda function. Video for the project <a href="https://youtu.be/hTdQP7JUfa0" target="_blank">Video</a> 