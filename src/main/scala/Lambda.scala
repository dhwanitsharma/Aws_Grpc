import com.typesafe.config.{Config, ConfigFactory}
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.model.{GetObjectRequest}
import org.joda.time.LocalDateTime

import java.io.{BufferedReader, InputStreamReader}
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.math.BigInteger
import scala.collection.mutable.ListBuffer
import java.util.regex.Pattern


class Lambda {

}

object Lambda{
  val NINE = 9
  val FIVE = 5
  val TWO = 2
  val ZERO = 0
  val ONE = 1
  def main(args: Array[String]): Unit = {
    //val Timeinput = args(0)
    val Timeinput = "2022-10-28-19-22-00-000"
    val Timeinput1 = Timeinput.replace("-",":")
    val format = new java.text.SimpleDateFormat("yyyy:MM:dd:hh:mm:ss:sss")
    val timea = format.parse(Timeinput1)
    val x = LocalDateTime.fromDateFields(timea)
    val start =x.minusMinutes(2)
    val end = x.plusMinutes(2)

    val starting_hr = start.getHourOfDay
    val starting_min = start.getMinuteOfHour
    val ending_hr = end.getHourOfDay
    val ending_min = end.getMinuteOfHour
    val startingTime = start.toLocalTime.toString()
    val endingTime = end.toLocalTime.toString()
    val Interval = "2"
    val dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS")
    //val TimeInterval = args(1)
    val user_config: Config = ConfigFactory.load("S3.conf")
    val bucket = (user_config.getString("S3Conf.Bucket"))
    val key = (user_config.getString("S3Conf.Key"))
    val path = (user_config.getString("S3Conf.File_path"))
    val secret = (user_config.getString("S3Conf.Secret"))

    val creds = new BasicAWSCredentials(key, secret)
    val s3: AmazonS3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(creds)).withRegion(Regions.US_EAST_2).build()
    val f :SimpleDateFormat = new SimpleDateFormat("hh:mm")
    val files = new ListBuffer[String]()
    try {
      val list = s3.listObjects(bucket);
      val obj = list.getObjectSummaries
      obj.iterator().forEachRemaining(x=>{
        val timeTemp = x.getKey.takeRight(NINE).take(FIVE)
        val hr = timeTemp.take(TWO).toInt
        val min = timeTemp.takeRight(TWO).toInt
        if(hr.compare(starting_hr)>=ZERO & hr.compare(ending_hr)<ONE){
          if(min.compare(starting_min)>=ZERO & min.compare(ending_min)<ONE)
          files += x.getKey
        }
      })
    }


    files.foreach(x=>{
      val file = s3.getObject(new GetObjectRequest(bucket, x))
      val reader = new BufferedReader(new InputStreamReader(file.getObjectContent()))
      val lines = reader.lines().toArray().map(_.asInstanceOf[String])
      val timeStmpArray = lines.map(x=>{
        val spt = x.split(" ")
        val date = ((dateFormatter.parse(spt.head)).getTime)
        date.toInt})
      val ss = dateFormatter.parse("19:22:00.000").getTime.toInt
      val startTime = (dateFormatter.parse(startingTime)).getTime
      val endTime = (dateFormatter.parse(endingTime)).getTime
      val startIndex = RecursiveBinarySearch(timeStmpArray,startTime.toInt)()
      val endIndex = RecursiveBinarySearch(timeStmpArray,endTime.toInt)()
      val spliced =lines.slice(startIndex,endIndex)
      val config = ConfigFactory.load()
      val conf = config.getConfig("Patterns")
      val patternReg = Pattern.compile(conf.getString("detect_pattern"))
      var a =1
      spliced.foreach(x=>{
        val splitArray = x.split(" ")
        val matcher = patternReg.matcher(splitArray.last)
        if(matcher.find()){
          a = 2
          return md5(splitArray.last)
        }
      })
    })
  }

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

  def getClosest(arr: Array[Int], val1: Int, val2: Int, target: Int): Int = if (target - arr(val1) >= arr(val2) - target) val2
  else val1


  def md5(s: String) = {
    val digest = MessageDigest.getInstance("MD5").digest(s.getBytes)
    val bigInt = new BigInteger(1, digest)
    val hashedString = bigInt.toString(16)
    hashedString
  }
}
