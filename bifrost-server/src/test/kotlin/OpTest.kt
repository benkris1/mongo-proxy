import com.mongodb.MongoClient
import org.junit.Before
import com.mongodb.ServerAddress
import com.mongodb.MongoCredential
import org.junit.Test
import com.mongodb.client.MongoCollection
import org.bson.types.MaxKey
import org.bson.types.MinKey
import org.bson.BsonTimestamp
import org.bson.BsonDbPointer
import org.bson.Document
import org.bson.types.Binary
import org.bson.types.ObjectId
import java.util.*
import java.util.function.Consumer
import java.util.regex.Pattern


/**
 * Created by.
 * User: ben
 * Date: 31/07/2017
 * Time: 6:41 PM
 * Email:benkris1@126.com
 *
 */
class OpTest {
  private val ADDRESS = "localhost"
  //private val ADDRESS = "10.10.0.6"
  private lateinit var mgoClient:MongoClient
  private val dbName = "geo_blocks"
  private val collectionName = "bifrost_kotlin"
  @Before
  fun before(){
    val mgoCredentials = ArrayList<MongoCredential>()
    mgoCredentials.add(MongoCredential.createCredential("maxleap", dbName, "maxleap10086".toCharArray()))
    this.mgoClient = MongoClient(ServerAddress(ADDRESS, 27017), mgoCredentials)
  }



  @Test
  fun insert(){
    val collection = mgoClient.getDatabase(dbName).getCollection(collectionName)
    val newOne = insert(collection)
    val loadOne = findOne(collection, Document("uuid",newOne.get("uuid")))
    assert(loadOne!= null)
  }

  @Test
  fun update(){
    val collection = mgoClient.getDatabase(dbName).getCollection(collectionName)
    val newOne = insert(collection)
    val updateOne = collection.updateOne(Document("uuid", newOne.get("uuid")), Document("\$set",Document("name", "ben")))
    val loadOne = findOne(collection, Document("uuid",newOne.get("uuid")))
    assert(loadOne?.getString("name").equals("ben"))
  }

  @Test
  fun findMore() {
    val collection = mgoClient.getDatabase(dbName).getCollection(collectionName)
    var count = collection.count()
    collection.find().forEach({  count -- })
    assert(!count.equals(0))
  }

  @Test
  fun delete(){
    val collection = mgoClient.getDatabase(dbName).getCollection(collectionName)
    val newOne = insert(collection)
    val deleteResult = collection.deleteOne(Document("uuid", newOne.get("uuid")))
    val loadOne = findOne(collection, Document("uuid",newOne.get("uuid")))
    assert(loadOne ==null)
  }

  private fun insert(mgoCollection: MongoCollection<Document>): Document {
    val document = Document()
    document.put("double", 111.0)
    document.put("string", "string")
    val obj = Document()
    obj.put("hello", "hello")
    obj.put("double", 222.0)
    document.put("object", obj)
    val array = ArrayList<Any>()
    array.add(obj)
    array.add(111f)
    document.put("array", array)
    document.put("binData", Binary("Binary赵静".toByteArray()))
    document.put("bool", true)
    document.put("null", null)
    document.put("regex", Pattern.compile("123"))
    document.put("dbPointer", BsonDbPointer("test", ObjectId()))
    document.put("int", 111)
    document.put("timestamp", BsonTimestamp())
    document.put("long", 1231L)
    document.put("minKey", MinKey())
    document.put("maxKey", MaxKey())
    document.put("uuid",UUID.randomUUID().toString())
    mgoCollection.insertOne(document)
    return document
  }

  private fun findOne(mgoCollection: MongoCollection<Document>,document:Document):Document?{
    return mgoCollection.find(document).limit(1)?.first()
  }
}