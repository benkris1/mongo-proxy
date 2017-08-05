import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import java.util.ArrayList
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import org.apache.commons.collections4.MultiMapUtils.getCollection
import com.mongodb.client.MongoCollection





/**
 * Created by.
 * User: ben
 * Date: 04/08/2017
 * Time: 4:49 PM
 * Email:benkris1@126.com
 *
 */

class Benchmark {
  private val ADDRESS = "localhost"
  //private val ADDRESS = "10.10.0.6"
  private val httpClient:OkHttpClient = OkHttpClient()
  @Before
  fun before(){
  }



  @Test
  fun testConnections() {

    val start = System.currentTimeMillis()

    val countDownLatch1 = CountDownLatch(10000)
    for(i in 1 .. 10000) {
      CompletableFuture.supplyAsync {
        countDownLatch1.countDown()
        val mongoCredentials = ArrayList<MongoCredential>()
        mongoCredentials.add(MongoCredential.createCredential("maxleap", "platform_data", "maxleap10086".toCharArray()))
        var mgoClient = MongoClient(ServerAddress(ADDRESS, 27017),mongoCredentials)
        val collection = mgoClient.getDatabase("platform_data").getCollection("zcloud_application")
        collection.find().limit(100).forEach { it }
        mgoClient.close()
      }.exceptionally {
        it.printStackTrace()
      }
    }
    countDownLatch1.await()
    System.out.println(System.currentTimeMillis() - start);
  }
}