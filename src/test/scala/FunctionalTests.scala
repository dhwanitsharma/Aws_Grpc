import Lambda.{FIVE, NINE, RecursiveBinarySearch, TIMESTAMP_FULL, TWO, getClosest, lambdaFunction}
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.typesafe.config.{Config, ConfigFactory}
import org.joda.time.LocalDateTime
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalTime

class FunctionalTests extends AnyFlatSpec with Matchers  {

/*  it should "Test1" in {
    val interval = "00:02:00"
    val time = "2022-10-28-19-22-00:000"
    val time2 = "2022-10-28-19-29-00:000"
    val pattern = "Rsxg"
    val resp = lambdaFunction(interval,time,pattern)
    val resp2 = lambdaFunction(interval,time2,pattern)
    val a = 1
  }*/
  it should "Binary Search should return closest value" in{
    val arr = Array(1,2,4,5,6,8,10,13,15,17,20,24,25,27,29)
    val arr2 = Array(150,200,210,250,255,257,276,300,310,320,400)
    val t1 = RecursiveBinarySearch(arr,3)()
    val t2 = RecursiveBinarySearch(arr2,295)()
    assert(t1.equals(2))
    assert(t2.equals(7))
  }

  it should "Starting time and ending time should be correct" in{
    val interval = "00:02:00"
    val Timeinput = "2022-11-01-00-45-00-000".replace("-",":")
    val format = new java.text.SimpleDateFormat(TIMESTAMP_FULL)
    val DateTime = format.parse(Timeinput)
    val localDate = LocalDateTime.fromDateFields(DateTime)
    val interval_time = LocalTime.parse(interval)
    val start = localDate.minusHours(interval_time.getHour).minusMinutes(interval_time.getMinute).minusSeconds(interval_time.getSecond())
    val end = localDate.plusHours(interval_time.getHour).plusMinutes(interval_time.getMinute).plusSeconds(interval_time.getSecond())
    assert(start.getMinuteOfHour.equals(43))
    assert(end.getMinuteOfHour.equals(47))
  }
  it should "File Name calculation is correct" in{
    val file = "LogFileGenerator-20221028-19-17.log"
    val timeTemp = file.takeRight(NINE).take(FIVE)
    val hr = timeTemp.take(TWO).toInt
    val min = timeTemp.takeRight(TWO).toInt
    assert(hr.equals(19))
    assert(min.equals(17))
  }
  it should "Should return closest value" in{
    val arr = Array(1,2,4,5,6,8,10,13,15,17,20,24,25,27)
    val target = 11
    val value1 = 10
    val value2 = 13
    val closest = getClosest(arr,value1,value2, target)
    assert(closest.equals(10))
  }

}
