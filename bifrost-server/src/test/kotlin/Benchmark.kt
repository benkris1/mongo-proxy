import com.mongodb.MongoClient
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch


/**
 * Created by.
 * User: ben
 * Date: 04/08/2017
 * Time: 4:49 PM
 * Email:benkris1@126.com
 *
 */

class Benchmark {
  //private val ADDRESS = "localhost"
  //private val ADDRESS_PROXY = "10.10.0.6"
  private val ADDRESS_PROXY = "localhost"
  private val ADDRESS = "mongo.userdata.2"
  private val httpClient:OkHttpClient = OkHttpClient()
  @Before
  fun before(){
  }



  @Test
  fun testConnections() {

    val start = System.currentTimeMillis()

    val countDownLatch1 = CountDownLatch(1000)
    for(i in 1 .. 1000) {
      CompletableFuture.supplyAsync {
        countDownLatch1.countDown()
        val mongoCredentials = ArrayList<MongoCredential>()
        mongoCredentials.add(MongoCredential.createCredential("maxleap", "platform_data", "maxleap10086".toCharArray()))
        var mgoClient = MongoClient(ServerAddress(ADDRESS, 27013))
        val collection = mgoClient.getDatabase("platform_data").getCollection("zcloud_application")
        collection.find().limit(1000).forEach { it }
        mgoClient.close()
      }.exceptionally {
        it.printStackTrace()
      }
    }
    countDownLatch1.await()
    System.out.println(System.currentTimeMillis() - start);
  }

  @Test
  fun testProxy() {

    val start = System.currentTimeMillis()

    val countDownLatch1 = CountDownLatch(1000)
    for(i in 1 .. 1000) {
      CompletableFuture.supplyAsync {
        countDownLatch1.countDown()
        val mongoCredentials = ArrayList<MongoCredential>()
        mongoCredentials.add(MongoCredential.createCredential("maxleap", "platform_data", "maxleap10086".toCharArray()))
        var mgoClient = MongoClient(ServerAddress(ADDRESS_PROXY, 27017),mongoCredentials)
        // var mgoClient = MongoClient(ServerAddress(ADDRESS, 27013))
        val collection = mgoClient.getDatabase("platform_data").getCollection("zcloud_application")
        collection.find().limit(1000).forEach { it }
        mgoClient.close()
      }.exceptionally {
        it.printStackTrace()
      }
    }
    countDownLatch1.await()
    System.out.println(System.currentTimeMillis() - start);
  }
}