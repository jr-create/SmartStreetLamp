package com.wjr.spark.utils

import com.wjr.spark.bean.{DeviceCountInfo, DeviceInfo, LampSensor}
import com.wjr.spark.sink.MyKafkaSink
import org.apache.spark.sql.catalyst.expressions.TypeOf
import org.apache.spark.sql.execution.streaming.FileStreamSource.Timestamp
import org.json4s
import org.json4s.{DefaultFormats, DynamicJValue, Extraction, JArray, JBool, JInt, JObject, JString}
import org.json4s.jackson.JsonMethods
import org.json4s.jackson.JsonMethods.compact

import java.text.SimpleDateFormat
import java.util.Date
import javax.xml.crypto.Data
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object JsonUtils {
  /**
   * 获取样例类的属性名
   *
   * @tparam T 样例类
   * @return Array(属性名)
   */

  import scala.reflect.runtime.universe._

  System.setProperty("HADOOP_USER_NAME", "root")

  def getFields[T: TypeTag] = typeOf[T].members.collect {
    case m: MethodSymbol if m.isCaseAccessor =>
      m.toString.split(" ")(1)
  }.toArray

  def getObjectValue(data: Any): Array[AnyRef] = {
    JsonUtils.getFields[DeviceCountInfo]
      .map(x=> FieldAndValue.getFieldValueByName(x, data))
      .reverse
  }

  /**
   * bean对象转sparkSQL的cast
   *
   * @tparam T
   * @return
   */
  def ObjectToCastStr[T: TypeTag] = getFields[T].map(field => {
    val prefix = s"cast(${
      field match {
        case "timestamp" => "from_unixtime("
        case _ => ""
      }
    }get_json_object(v,'${"$"}."
    s"${prefix}${field}') ${
      field match {
        case "timestamp" => ")as timestamp"
        case _ => "as string"
      }
    }) as ${field}"
  })

  /**
   * json转sparkSQL的Cast
   */
  def JsonListToCastStr(jsonList: List[Any]): List[String] = {
    List[String]()
  }

  /**
   * bean对象转json字符串
   *
   * @param data
   * @return
   */
  private[spark] def ObjectToJson(data: Any): String = {
    try {
      //import org.json4s._
      implicit val formats = DefaultFormats // TODO: 需要放在函数内，来防止NotSerializableException
      val str = compact(Extraction.decompose(data))
      str
    } catch {
      case e: Exception =>
        println(e.printStackTrace())
        ""
    }
  }

  /**
   * 万能json解析成List((名称 类型 值),(名称 类型 值))
   * 嵌套的名称则为  一级名称.二级名称
   *
   * @param json
   * @return
   */
  def jsonToList(json: String): List[Any] = {
    implicit val formats = DefaultFormats
    val jValue = JsonMethods.parse(json)
    var list = List[Any](Nil)
    var parentObjectName = ""
    for (JObject(child) <- jValue) {
      val tempList = child.map(_ match {
        case (x, y: JString) => parentObjectName + x + " String " + y.extract[String]
        case (x, y: JInt) => x + " Int " + y.extract[Int]
        case (x, y: JBool) => x + " Boolean " + y.extract[Boolean]
        case (x, y: JArray) =>
          val vs = y.children.map(v => {
            if (!v.isInstanceOf[JObject]) {
              v.extract[String]
            }
          }).filter(_.isInstanceOf[String])
          x + " Array<string> ('" + vs.mkString("','") + "')"
        case (x, y: JObject) =>
          parentObjectName = x + "."
          Nil

      })
      list ++= tempList
    }
    list.filter(_.isInstanceOf[String])
  }

  /**
   * 将List(名称，类型，值)转为建表语句
   *
   * @return
   */
  def jsonListToHSql(tbType: String, dbName: String, tbName: String, list: List[Any]): (String, String, List[String]) = {
    val valuesMap = ArrayBuffer[ArrayBuffer[String]]()
    val nowDate: String = new SimpleDateFormat("yyyy-MM-dd").format(new Date())
    var road_id = ""
    // TODO: sqlCast语句
    val strings = list.map(x => {
      val schema = x.toString.split(" ")
      val attribute = if (schema(0).split("\\.").length > 1) {
        schema(0).split("\\.")(1)
      } else schema(0)
      s"""
         |${
        if (schema(1).toLowerCase.contains("array")) {
          s"cast(get_json_array(get_json_object(v,'${"$"}.${schema(0)}'))as array<string>) as ${attribute}"
        } else if (schema(0).equals("road_id")) {
          "null"
        } else {
          s"cast(get_json_object(v,'${"$"}.${schema(0)}')as ${schema(1).toLowerCase} ) as ${attribute}"
        }
      }
         |""".stripMargin
    }).filter(!_.contains("null"))
    (
      // TODO: 建表语句
      s"""
         |create EXTERNAL table  if not exists  $dbName.$tbName(
         |${
        list.filter(x => {
          val bool = x.toString.contains("road_id")
          if (bool) {
            road_id = x.toString.split(" ")(2)
          }
          !bool
        }).map(x => {
          val schema = x.toString.split(" ")
          if (schema.length > 2) {
            valuesMap += ArrayBuffer(schema(1).toLowerCase(), schema(2)) //todo 将类型，值保存
            //todo 进行属性切分 object.att=> att
            val attribute = if (schema(0).split("\\.").length > 1) {
              schema(0).split("\\.")(1)
            } else schema(0)

            val str = attribute match {
              case "timestamp" => "`timestamp`"
              case x => x
            }
            str + " " + schema(1).toLowerCase
          }
        }).mkString(",")
      }
         |)PARTITIONED BY (`dt` string,road_id string)
         |STORED As parquet
         |LOCATION 'hdfs://hadoop01:8020/warehouse/smartStreetLamp/${tbType}/${tbName}'
         |""".stripMargin,
      // TODO: 插入语句
      s"""
         |insert into $dbName.$tbName  partition(dt="$nowDate" ,road_id="$road_id")values(
         |${
        valuesMap.map(x => {
          x(0) match {
            case "string" => s"'${x(1)}'"
            case "array<string>" => s"array${x(1)}"
            case _ => x(1)
          }
        }).mkString(",")
      }
         |)
         |""".stripMargin,

      strings :+ "cast(from_unixtime(cast(t as long),'yyyy-MM-dd')as string) as dt" :+ "cast(get_json_object(v,'$.road_id')as int ) as road_id"
    )
  }

  def main(args: Array[String]): Unit = {


    var json = ""

    for (i <- 0 until 100) {
      val random = new Random().nextInt(20 + 1) // TODO: 生成n+1的随机生成数
      val typeNum = new Random().nextInt(7 + 1) // TODO: 生成n+1的随机生成数
      val number = new Random().nextInt(9) + 1 // TODO: 生成n+1的随机生成数
      val device_id = new Random().nextInt(1000 + 1) // TODO: 生成n+1的随机生成数
      val error_code = 0 // TODO: 生成0/1的随机生成数
      val vol = Math.round((Random.nextGaussian() + 4) * 100) / 100 // TODO: 生成n+1的随机生成数
      json =
        s"""
           |{"reason":"success","device_id":"my_00$device_id","type_id":${typeNum},"timestamp":${System.currentTimeMillis() / 1000},"error_code":$error_code,"road_id":${random},"longitude":"11${number}.${number}820083778303","latitude":"3${number}.${number}242552469552","values":{"voltage":"$vol","temperature":"2${vol}","humidity":"${vol * 25}","lighting":"${vol * 25}","PM2_5":"${vol * 25}","CO_2":"${vol * 25}","info":"yin阴","direct":"xibeifeng西北风","power":"${number}级"},"test":["a1",2]}
           |""".stripMargin
      // if (error_code == 1) {
      //   MyKafkaSink.send("dwd_error_device", json)
      // }
      // else {
      //   MyKafkaSink.send("dwd_normal_device", json)
      // }
      // println(error_code)
      MyKafkaSink.send("ods_lamp_log", json.trim)
    }
    println(json)
    //import org.json4s._
    //implicit val formats = DefaultFormats
    //
    //val jValue = JsonMethods.parse(json)
    //val jsonList = jsonToList(json)
    //
    //println("List::" + jsonList)
    //// TODO: json转换的List拼接createSQL
    //val tuple = jsonListToHSql("dwd", "lamp", "dwd_device_index", jsonList)
    //println("建表语句" + tuple._1)
    //println("插入语句" + tuple._2)
    //println("json转cast" + tuple._3)

    //println((jValue \ "timestamp").extract[String])

    //val jsonGet = jValue.extract[DeviceInfo]
    //println("样例类Str：" + jsonGet.toString)
    //val className = jsonGet.toString.split("""\(""").head
    //println(s"${className}样例类指标：" + getFields[DeviceInfo].toBuffer)
    //println("sql拼接后Array：" + ObjectToCastStr[DeviceInfo].toBuffer)
  }


}
