// Databricks notebook source
// MAGIC %md
// MAGIC 
// MAGIC #[Great Models with Great Privacy: Optimizing ML and AI Under GDPR by Sim Simeonov & Slater Victoroff](https://databricks.com/session/great-models-with-great-privacy-optimizing-ml-ai-under-gdpr)
// MAGIC ## [Christoffer Långström](https://www.linkedin.com/in/christoffer-l%C3%A5ngstr%C3%B6m-7a460514b/)
// MAGIC [![XX](https://img.youtube.com/vi/DMzhTY891io/0.jpg)](https://www.youtube.com/watch?v=DMzhTY891io)

// COMMAND ----------

// DBTITLE 1,User functions (SHA-256 and AES-128)
import java.security.MessageDigest
import javax.crypto.KeyGenerator
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.util.Base64
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{IntegerType}

var myKey: String = ""

def sha_256(in: String): String = {
  val bytes = in.getBytes
  val md: MessageDigest = MessageDigest.getInstance("SHA-256")
  new String(Base64.getEncoder.encode(md.digest(bytes)),"UTF-8")
}

def encrypt(in: String): String = {
  val raw = KeyGenerator.getInstance("AES").generateKey.getEncoded()
  myKey = new String(Base64.getEncoder.encode(raw),"UTF-8")
  val skeySpec = new SecretKeySpec(raw, "AES")
  val cipher = Cipher.getInstance("AES")
  cipher.init(Cipher.ENCRYPT_MODE, skeySpec)
  new String(Base64.getEncoder.encode(cipher.doFinal(in.getBytes)),"UTF-8")
}

def decrypt(in: String): String = {
  val k = new SecretKeySpec(Base64.getDecoder.decode(myKey.getBytes), "AES")
  val c = Cipher.getInstance("AES")
  c.init(Cipher.DECRYPT_MODE, k)
  new String((c.doFinal(Base64.getDecoder.decode(in))),"UTF-8")
}

val sha__256 = udf(sha_256 _)
val myEnc = udf(encrypt _)
val myDec = udf(decrypt _)

// COMMAND ----------

// DBTITLE 1,Import Data
case class pii(firstName: String, lastName: String, gender: String, dob: String)
val hp = pii("Howard","Philips","M","1890-08-20")
val ea = pii("Edgar","Allan","M","1809-01-19")
val ai = pii("Arthur","Ignatius","M","1859-05-22")
val ids = spark.createDataset(Seq(hp,ea,ai))
ids.show

// COMMAND ----------

// DBTITLE 1,Data Serialization Rule
val p = lit("|")
val rules: Column = {
    //Use all of the pii for the pseudo ID   
    concat(upper('firstName), p, upper('lastName), p,'gender, p,'dob)
}

// COMMAND ----------

// DBTITLE 1,Define pseudonymous ID columns
val psids = {
  //Serialize -> Hash -> Encrypt
    myEnc(
       sha__256(
        rules)
  ).as(s"pseudoID")       
}

// COMMAND ----------

// DBTITLE 1,Define quasi-identifier columns
val quasiIdCols: Seq[Column] = Seq(
  //Fuzzify data
  'gender,
  'dob.substr(1, 4).cast(IntegerType).as("yob")
)

// COMMAND ----------

// DBTITLE 1,Create PII removal transformation
def removePII(ds: Dataset[_]): DataFrame = 
  ds.toDF.select(quasiIdCols ++ Seq(psids): _*)

// COMMAND ----------

// DBTITLE 1,Process PII
val masterIds = ids.transform(removePII)

// COMMAND ----------

// DBTITLE 1,Display result
display(masterIds)

// COMMAND ----------

// DBTITLE 1,Partner specific encoding
// val partnerPasswords: String = ""

// def costumizeForPartner(name: String)(df: DataFrame) = {
//   val colName = s"pseudoID"  
//   psids(colName, base64(myEnc(col(colName))))
// }

// display(
//   masterIds.transform(
//     costumizeForPartner("A")
//   )
// )
