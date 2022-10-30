import com.typesafe.config.{Config, ConfigFactory}
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.model.GetObjectRequest
import org.joda.time.LocalDateTime

import java.io.{BufferedReader, InputStreamReader}
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.math.BigInteger
import java.time.LocalTime
import scala.collection.mutable.ListBuffer
import java.util.regex.Pattern


class Lambda {

}

/**Lambda Object
 *
 * This is the Lambda function which access the S3 bucket to match the pattern of the logs.
 *
 *
 */
object Lambda{
  val NINE = 9
  val FIVE = 5
  val TWO = 2
  val ZERO = 0
  val ONE = 1
  val MD5 = "MD5"
  val FALSE = "false"
  val TIMESTAMP_FULL = "yyyy:MM:dd:hh:mm:ss:sss"
  val TIMESTAMP_MILLI = "HH:mm:ss.SSS"


  /**Lambda Function
   *
   * This is the Lambda function checks the log files in the time interval for pattern matching logs
   *
   * @param interval: Time interval, format:"00:01:00"
   * @param time : Sepecific Time for search, format : "2022-10-28-19-22-00:000"
   * @param pattern : Specific regex pattern to search for logs, format : [a-zA-Z0-9_]
   *
   */
  def lambdaFunction(interval:String, time:String, pattern:String): String={
    val Timeinput = time.replace("-",":")
    val format = new java.text.SimpleDateFormat(TIMESTAMP_FULL)
    val DateTime = format.parse(Timeinput)
    val localDate = LocalDateTime.fromDateFields(DateTime)
    val interval_time = LocalTime.parse(interval)
    //To find Start Time and End Time for the given Time
    val start = localDate.minusHours(interval_time.getHour).minusMinutes(interval_time.getMinute).minusSeconds(interval_time.getSecond())
    val end = localDate.plusHours(interval_time.getHour).plusMinutes(interval_time.getMinute).plusSeconds(interval_time.getSecond())
    // To find the starting/ending hour and minutes of the interval
    val starting_hr = start.getHourOfDay
    val starting_min = start.getMinuteOfHour
    val ending_hr = end.getHourOfDay
    val ending_min = end.getMinuteOfHour
    val startingTime = start.toLocalTime.toString()
    val endingTime = end.toLocalTime.toString()

    //Loading the S3 creds and Data
    val dateFormatter = new SimpleDateFormat(TIMESTAMP_MILLI)
    val user_config: Config = ConfigFactory.load("S3.conf")
    val bucket = (user_config.getString("S3Conf.Bucket"))
    val key = (user_config.getString("S3Conf.Key"))
    val secret = (user_config.getString("S3Conf.Secret"))
    val creds = new BasicAWSCredentials(key, secret)
    val s3: AmazonS3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(creds)).withRegion(Regions.US_EAST_2).build()
    val files = new ListBuffer[String]()
    val list = s3.listObjects(bucket);
    val objSummary = list.getObjectSummaries

    /**
     * Code to find the list of files which fall inside our time interval
     * Example :
     * TIME : 21:00:00, Interval : 00:05:00
     * Files which will have logs of 20:55:00 to 21:05:00 will be picked in the list
     */
    objSummary.iterator().forEachRemaining(x=>{
      val timeTemp = x.getKey.takeRight(NINE).take(FIVE)
      val hr = timeTemp.take(TWO).toInt
      val min = timeTemp.takeRight(TWO).toInt
      if(hr.compare(starting_hr)>=ZERO & hr.compare(ending_hr)<ONE){
        if(min.compare(starting_min)>=ZERO & min.compare(ending_min)<ONE)
        files += x.getKey
      }
    })

    /**
     * The time in the log files is used to create an array of Interger time,
     * to find the time splice for the interval.
     * This search of the time splice is done by Binary Search, leading to search
     * time of index to O(log(n))
     */
    files.foreach(x=>{
      val file = s3.getObject(new GetObjectRequest(bucket, x))
      val reader = new BufferedReader(new InputStreamReader(file.getObjectContent()))
      val lines = reader.lines().toArray().map(_.asInstanceOf[String])
      val timeStmpArray = lines.map(x=>{
        val spt = x.split(" ")
        val date = ((dateFormatter.parse(spt.head)).getTime)
        date.toInt})
      val startTime = (dateFormatter.parse(startingTime)).getTime
      val endTime = (dateFormatter.parse(endingTime)).getTime
      val startIndex = RecursiveBinarySearch(timeStmpArray,startTime.toInt)()
      val endIndex = RecursiveBinarySearch(timeStmpArray,endTime.toInt)()
      val spliced =lines.slice(startIndex,endIndex)

      /**
       * Pattern matching to find the pattern in the log messages
       */
      val patternReg = Pattern.compile(pattern)
      spliced.foreach(x=>{
        val splitArray = x.split(" ")
        val matcher = patternReg.matcher(splitArray.last)
        if(matcher.find()){
          return md5(splitArray.last)
        }
      })
    })
    return FALSE
  }

  /**Recursive Binary Search
   *
   * This is a recursive binary search to find the closest value in the array to the target value
   *
   * @param arr: Input Array
   * @param target : the target value to search in the array
   * @param low : not to be used
   * @param high : not to be used
   *
   * @return Int : Index of the Target
   *
   */
  def RecursiveBinarySearch(arr: Array[Int],
                            target: Int)
                           (low: Int = 0,
                            high: Int = arr.length - 1): Int =
  {

    //left-side case
    if (target <= arr(0))
      return 0;
    //right-side case
    if (target >= arr(high - 1))
      return high - 1;

    val mid = (low + high) / 2;

    if (arr(mid) == target)
      return mid;

    /* If target is less than array element,
        then search in left */
    if (target < arr(mid)) {
      // to mid, return closest of two
      if (mid > 0 && target > arr(mid - 1)) {
        return getClosest(arr,mid - 1,
          mid, target)
      };
      else
        RecursiveBinarySearch(arr,target)(0,mid.toInt)
    }
    /* Repeat for left half */

    // If target is greater than mid
    else {
      if (mid < high && target < arr(mid + 1))
        return getClosest(arr,mid,
          mid + 1, target);
      else
        RecursiveBinarySearch(arr,target)(mid+1,high)
    }
    return mid;
  }
  /**Closest Value
   *
   * This is a function which gets the index of the closes value of the target
   *
   * @param arr: Input Array
   * @param target : the target value to search in the array
   * @param low : not to be used
   * @param high : not to be used
   *
   * @return Index of the value closest to target
   */
  def getClosest(arr: Array[Int], val1: Int, val2: Int, target: Int): Int = if (target - arr(val1) >= arr(val2) - target) val2
  else val1

  /**MD5 Hash Function
   *
   * This function returns MD5 encoded hash value of a String
   *
   * @param s: String input
   *
   * @return String : Hashed Value
   */
  def md5(s: String) = {
    val digest = MessageDigest.getInstance(MD5).digest(s.getBytes)
    val bigInt = new BigInteger(1, digest)
    val hashedString = bigInt.toString(16)
    hashedString
  }
}
