package com.wjr.spark.utils

import io.searchbox.client.config.HttpClientConfig
import io.searchbox.client.{JestClient, JestClientFactory}
import io.searchbox.core.{Bulk, BulkResult, Get, Index, Search, SearchResult}
import org.elasticsearch.index.query.{BoolQueryBuilder, MatchQueryBuilder, TermQueryBuilder}
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder
import org.elasticsearch.search.sort.SortOrder

import java.util

/**
 *
 * @Package: com.wjr.gmall.realtime.utils
 * @ClassName: MyESUtil
 * @Author: 86157
 * @CreateTime: 2021/8/22 13:42
 */
object MyESUtil {

    //声明客户端工厂
    private var jestClientFactory:JestClientFactory = null

    //提供客户端方法
    def getJestClient(): JestClient ={
        if (jestClientFactory==null){
            //创建工厂对象
            build()
        }
        jestClientFactory.getObject
    }

    def build() = {
        jestClientFactory = new JestClientFactory
        //构造者模式
        jestClientFactory.setHttpClientConfig(new HttpClientConfig
        .Builder("http://hadoop01:9200")//可以是集合
        .multiThreaded(true)
        .maxTotalConnection(20)
        .connTimeout(10000)
        .readTimeout(1000).build())
    }

    //向ES中插入单条数据 方式1
    def putIndex(): Unit ={
        //读取客户端连接
        val jestClient = getJestClient()
        //定义执行的 source
        val source =
            """
              |{
              |"id":3,
              |    "name":"IDEA 红海事件",
              |    "doubanScore":5.0,
              |    "actorList":[
              |        {"id":4,"name":"张三丰"} ]
              |}
              |""".stripMargin
        //创建插入类 Index(相当于put) Builder的参数表示要插入索引的文档，底层会转换成JSON格式，所以可以将文档封装成样例类
        val index : Index = new Index.Builder(source)
                .index("movie_chn_2")
                .`type`("movie")
                .id("1")
                .build()

        //通过客户端对象，操作ES，execute是Action类型，index是Action接口的实现类
        jestClient.execute(index)


        //关闭连接
        jestClient.close()
    }



    //向ES中插入单条数据 方式2
    def putIndex2(): Unit ={
        //读取客户端连接
        val jestClient = getJestClient()

        //封装样例类对象
        val actorList = new util.ArrayList[util.Map[String, Any]]()
        val map = new util.HashMap[String, Any]()
        map.put("id",4)
        map.put("name","乔峰")
        actorList.add(map)
        val movie = Movie(302, "IDEA 天龙八部", 7.2f, actorList)
        //创建实现类
        val index = new Index.Builder(movie)//底层将样例对象转换成json字符串
                .index("movie_chn_5")
                .`type`("movie")
                .id("2")
                .build()

        //执行操作，创建实现类Index
        jestClient.execute(index)
        jestClient.close()
    }
    //根据文档id，从ES中查询出一条记录
    def queryIndexById(): Unit ={
        val jestClient = getJestClient()
        val getById = new Get.Builder("movie_chn_5","2").build()
        val result = jestClient.execute(getById)
        println(result.getJsonString)
        jestClient.close()
    }

    //根据指定查询条件，从ES中查询多个文档 方式1
    def queryIndexByCondition(): Unit ={
        val jestClient = getJestClient()
        val query =
            """
              |{
              |  "query": {
              |    "bool": {
              |       "must": [
              |        {"match": {
              |          "name": "IDEA"
              |        }}
              |      ],
              |      "filter": [
              |        {"term": { "actorList.name.keyword": "乔峰"}}
              |      ]
              |    }
              |  },
              |  "from": 0,
              |  "size": 20,
              |  "sort": [
              |    {
              |      "doubanScore": {
              |        "order": "desc"
              |      }
              |    }
              |  ],
              |  "highlight": {
              |    "fields": {
              |     "name": {}
              |    }
              |  }
              |}
              |""".stripMargin
        val search = new Search.Builder(query)
                .addIndex("movie_chn_5")
                .build()
        val result = jestClient.execute(search)
        val list : util.List[SearchResult#Hit[util.Map[String, Any], Void]] = result.getHits(classOf [util.Map[String, Any]])

        //将Java的 list转成scala的list
        import scala.collection.JavaConverters.asScalaBufferConverter
        val scalaArr = asScalaBufferConverter(list).asScala //类型为ArrayBuffer
        val scalaList = scalaArr.map(_.source).toList
        println(scalaList.mkString("\n"))


        jestClient.close()

    }

    //根据指定查询条件，从ES中查询多个文档 方式2
    def queryIndexByCondition2(): Unit ={
        val jestClient = getJestClient()

        //SearchSourceBuilder用于构建查询json字符串
        val searchSourceBuilder = new SearchSourceBuilder
        val boolQueryBuilder = new BoolQueryBuilder()
        boolQueryBuilder.must(new MatchQueryBuilder("name","IDEA"))
        boolQueryBuilder.filter(new TermQueryBuilder("actorList.name.keyword","乔峰"))
        searchSourceBuilder.query(boolQueryBuilder)
        searchSourceBuilder.from(0)
        searchSourceBuilder.size(20)
        searchSourceBuilder.sort("doubanScore",SortOrder.ASC)
        searchSourceBuilder.highlighter(new HighlightBuilder().field("name"))
        //加入过滤条件
        val query = searchSourceBuilder.toString()
        //println(query)//todo 查询条件
        //创建实现类
        val search = new Search.Builder(query)
                .addIndex("movie_chn_5")
                .build()
        //执行查询操作
        val result = jestClient.execute(search)

        val list : util.List[SearchResult#Hit[util.Map[String, Any], Void]] = result.getHits(classOf [util.Map[String, Any]])

        //将Java的 list转成scala的list
        import scala.collection.JavaConverters.asScalaBufferConverter
        val scalaArr = asScalaBufferConverter(list).asScala //类型为ArrayBuffer
        val scalaList = scalaArr.map(_.source).toList
        println(scalaList.mkString("\n"))


        jestClient.close()
    }
    //向 ES 中批量插入数据
    //参数 1：批量操作的数据   参数 2：索引名称
    def bulkInsert(sourceList:List[(String,Any)],indexName:String): Unit ={
        if(sourceList!=null && sourceList.size>0){
            //获取操作对象
            val jest: JestClient = getJestClient()
            //构造批次操作
            val bulkBuild: Bulk.Builder = new Bulk.Builder
            //对批量操作的数据进行遍历
            for ((id,source) <- sourceList) {
                val index: Index = new Index.Builder(source)
                        .index(indexName)
                        .`type`("_doc")
                        .id(id)
                        .build()
                //将每条数据添加到批量操作中
                bulkBuild.addAction(index)
            }

            //Bulk 是 Action 的实现类，主要实现批量操作
            val bulk: Bulk = bulkBuild.build()
            //执行批量操作  获取执行结果
            val result: BulkResult = jest.execute(bulk)
            //通过执行结果  获取批量插入的数据
            val items: util.List[BulkResult#BulkResultItem] = result.getItems
            println("保存到 ES " + items.size() + "条数据")
            //关闭连接
            jest.close()
        }
    }

    def main(args : Array[String]) : Unit = {
        //putIndex2()
        //queryIndexById
        queryIndexByCondition2()
    }
}
case class Movie(id:Long,name:String,doubanScore:Float,actorList:java.util.List[java.util.Map[String,Any]])
