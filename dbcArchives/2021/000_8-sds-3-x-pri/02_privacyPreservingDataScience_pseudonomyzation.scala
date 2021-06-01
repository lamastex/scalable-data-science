// Databricks notebook source
// MAGIC %md
// MAGIC ScaDaMaLe Course [site](https://lamastex.github.io/scalable-data-science/sds/3/x/) and [book](https://lamastex.github.io/ScaDaMaLe/index.html)

// COMMAND ----------

// DBTITLE 1,Privacy Preserving Data Science Part 2: Pseudonymization
// MAGIC %md
// MAGIC 
// MAGIC ### [Christoffer Långström](https://www.linkedin.com/in/christoffer-l%C3%A5ngstr%C3%B6m-7a460514b/)
// MAGIC This section aims to recreate & elaborate on the material presented in the following talk at the 2018 Spark AI summit by Sim Semeonov & Slater Victoroff. Following the advent of the General Data Protection Regulation in the EU, many data scientists are concerned about how this legislation will impact them & what steps they should take in order to protect their data and themselves (as holders of the data) in terms of privacy. Here we focus on two vital aspects of GDPR compliant learning, keeping only pseudonymous identifiers and ensuring that subjects can ask what data is being kept about them. 
// MAGIC 
// MAGIC Watch the video below to see the full talk:  
// MAGIC ##[Great Models with Great Privacy: Optimizing ML and AI Under GDPR by Sim Simeonov & Slater Victoroff](https://databricks.com/session/great-models-with-great-privacy-optimizing-ml-ai-under-gdpr)
// MAGIC [![XX](https://img.youtube.com/vi/DMzhTY891io/0.jpg)](https://www.youtube.com/watch?v=DMzhTY891io)
// MAGIC 
// MAGIC 
// MAGIC ### Why Do We Need Pseudonymization?
// MAGIC We want to have identifiers for our data, but personal identifiers (name, social numbers) are to sensitive to store. By storing (strong) pseudonyms we satisfy this requirement. However, the GDPR stipulates that we must be able to answer the question "What data do you have on me?" from any individual in the data set (The Right To Know)

// COMMAND ----------

// DBTITLE 1,Pseudonymity Checklist: 
// MAGIC %md
// MAGIC 
// MAGIC #### What we need:
// MAGIC * Consistent serialization of data 
// MAGIC * Assign a unique ID to each subject in the database
// MAGIC * Be able to identify individuals in the set, given their identifiers 
// MAGIC * Ensure that adversarial parties may not decode these IDs
// MAGIC 
// MAGIC Below is a flowchart showing the process at a high level. Each step is explained later in the guide. 

// COMMAND ----------

// DBTITLE 1,PII Processing Flowchart: 
// MAGIC %md
// MAGIC <img src="http://i.imgur.com/jSfzKzq.png" alt="drawing" width="1000"/>

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### 1) Data serialization & Randomization 
// MAGIC All data is not formatted equally, and if we wish any processing procedure to be consistent it is important to ensure proper formating. The formating employed here is for a row with fields firstname, lastname, gender, date of birth which is serialized according to the rule: firstname, lastname, gender, dob --> Firstname|Lastname|M/F|YYYY-MM-DD. All of this information will be used in generating the pseudonymous ID, which will help prevent collisions caused by people having the same name, etc. 
// MAGIC 
// MAGIC An important point to be made here is that there should also be some random shuffling of the rows, to decrease further the possibility of adversaries using additional information to divulge sensitive attributes from your table. If the original data set is in alphabetical order, and the output (even thought it has been treated) remains in the same order, we would be vulnerable to attacks if someone compared alphabetical lists of names run through common hash functions. 
// MAGIC 
// MAGIC ###### Note: 
// MAGIC It is often necessary to "fuzzy" the data to some extent in the sense that we decrease the level of accuracy by, for instance, reducing a day of birth to year of birth to decrease the power of quasi-identifiers. As mentioned in part 1, quasi-identifiers are fields that do not individually provide sensitive information but can be utilized together to identify a member of the set. This is discussed further in the next section, for now we implement a basic version of distilling a date of birth to a year of birth. Similar approaches would be to only store initials of names, cities instead of adresses, or dropping information all together (such as genders). 

// COMMAND ----------

import org.apache.spark.sql.functions.rand

// Define the class for pii
case class pii(firstName: String, lastName: String, gender: String, dob: String)

"""
firstName,lastName,gender,dob
Howard,Philips,M,1890-08-20
Edgar,Allan,M,1809-01-19
Arthur,Ignatius,M,1859-05-22
Mary,Wollstonecraft,F,1797-08-30
"""
// Read the data from a csv file, then convert to dataset // /FileStore/tables/demo_pii.txt was uploaded
val IDs = spark.read.option("header", "true").csv("/FileStore/tables/demo_pii.txt").orderBy(rand())
val ids = IDs.as[pii]
ids.show
 

// COMMAND ----------

// DBTITLE 1,1.1) Load some data
import org.apache.spark.sql.functions.rand

// Define the class for pii
case class pii(firstName: String, lastName: String, gender: String, dob: String)

"""
firstName,lastName,gender,dob
Howard,Philips,M,1890-08-20
Edgar,Allan,M,1809-01-19
Arthur,Ignatius,M,1859-05-22
Mary,Wollstonecraft,F,1797-08-30
"""
// Read the data from a csv file, then convert to dataset // /FileStore/tables/demo_pii.txt was uploaded
val IDs = spark.read.option("header", "true").csv("/FileStore/tables/demo_pii.txt").orderBy(rand())
val ids = IDs.as[pii]
ids.show
 

// COMMAND ----------

// MAGIC %md 
// MAGIC 
// MAGIC ## 2) Hashing
// MAGIC 
// MAGIC [Hash functions](https://en.wikipedia.org/wiki/Cryptographic_hash_function) apply mathematical algorithms to map abitrary size inputs to fixed length bit string (referred to as "the hash"),
// MAGIC and cryptographic hash functions do so in a one-way fashion, i.e it is not possible to reverse this procedure. This makes them good candidates for generating pseudo-IDs since they cannot be reverse engineered but we may easily hash any given identifier and search for it in the database. However, the procedure is not completely satisfactory in terms of safety. Creating good hash functions is very hard, and so commonly a standard hash function is used such as the SHA series. This means unfortunately that anyone can hash common names, passwords, etc using these standard hash functions and then search for them in our database in what is known as a [rainbow attack](https://en.wikipedia.org/wiki/Rainbow_table). 
// MAGIC 
// MAGIC A realistic way of solving this problem is then to encrypt the resulting hashes using a strong encryption algorithm, making them safe for storage by the database holder. 
// MAGIC 
// MAGIC It is important to choose a secure destructive hash function, MD5, SHA-1 and SHA-2 are all vulnerable to [length extension attacks](https://en.wikipedia.org/wiki/Length_extension_attack),
// MAGIC for a concrete example see [here](https://blog.skullsecurity.org/2012/everything-you-need-to-know-about-hash-length-extension-attacks).

// COMMAND ----------

// DBTITLE 1,2.1) Hashing (SHA-256)
import java.security.MessageDigest
import java.util.Base64
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{IntegerType}

// Perform SHA-256 hashin
def sha_256(in: String): String = {
  val md: MessageDigest = MessageDigest.getInstance("SHA-256")  // Instantiate MD with algo SHA-256
  new String(Base64.getEncoder.encode(md.digest(in.getBytes)),"UTF-8") // Encode the resulting byte array as a base64 string
}

// Generate UDFs from the above functions (not directly evaluated functions) 
// If you wish to call these functions directly then use the names in parenthesis, if you wish to use them in a transform use the udf() names. 
val sha__256 = udf(sha_256 _)


// COMMAND ----------

// DBTITLE 1,2.2) Example of SHA-256 Hashing
val myStr = "Hello!"  // Input is a string
val myHash = sha_256(myStr)
println(myHash) // The basic function takes a strings and outputs a string of bits in base64 ("=" represents padding becuse of the block based hashes)


// COMMAND ----------

// DBTITLE 1,Data Serialization Rule
// Concantination with a pipe character
val p = lit("|")

// Define our serialization rules as a column
val rules: Column = {
    //Use all of the pii for the pseudo ID   
    concat(upper('firstName), p, upper('lastName), p, upper('gender), p,'dob)
}

// COMMAND ----------

// MAGIC %md
// MAGIC ### 3) Encryption
// MAGIC 
// MAGIC Encrypting our hashed IDs via a symmetric key cipher ensures that the data holder can encrypt the hashed identifiers for storage and avoid the hash table attacks discussed above, and can decrypt the values at will to lookup entries. The choice of encryption standard for these implementations is AES-128. When provided with a key (a 128 bit string in our case), the AES algorithm encrypts the data into a sequence of bits, traditionally formated in base 64 binary to text encoding. The key should be pseudorandomly generated, which is a standard functionality in Java implementions. 

// COMMAND ----------

// DBTITLE 1,3.1) AES-128 Encryption Functions 
import javax.crypto.KeyGenerator
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

var myKey: String = ""

// Perform AES-128 encryption
def encrypt(in: String): String = {
  val raw = KeyGenerator.getInstance("AES").generateKey.getEncoded()      //Initiates a key generator object of type AES, generates the key, and encodes & returns the key 
  myKey = new String(Base64.getEncoder.encode(raw),"UTF-8")       //String representation of the key for decryption
  val skeySpec = new SecretKeySpec(raw, "AES")      //Creates a secret key object from our generated key
  val cipher = Cipher.getInstance("AES")      //Initate a cipher object of type AES
  cipher.init(Cipher.ENCRYPT_MODE, skeySpec)  // Initialize the cipher with our secret key object, specify encryption
  new String(Base64.getEncoder.encode(cipher.doFinal(in.getBytes)),"UTF-8")  // Encode the resulting byte array as a base64 string
}

// Perform AES-128 decryption
def decrypt(in: String): String = {
  val k = new SecretKeySpec(Base64.getDecoder.decode(myKey.getBytes), "AES")    //Decode the key from base 64 representation
  val cipher = Cipher.getInstance("AES")       //Initate a cipher object of type AES
  cipher.init(Cipher.DECRYPT_MODE, k)       // Initialize the cipher with our secret key object, specify decryption
  new String((cipher.doFinal(Base64.getDecoder.decode(in))),"UTF-8")      // Encode the resulting byte array as a base64 string
}

val myEnc = udf(encrypt _)
val myDec = udf(decrypt _)


// COMMAND ----------

// DBTITLE 1,3.2) Example of AES-128 Encryption

val secretStr = encrypt(myStr) //Output is a bit array encoded as a string in base64
println("Encrypted: ".concat(secretStr))  
println("Decrypted: ".concat(decrypt(secretStr)))


// COMMAND ----------

// MAGIC %md 
// MAGIC ### 4) Define Mappings 
// MAGIC Now we are ready to define the mappings that make up our pseudonymization procedure:

// COMMAND ----------

// DBTITLE 1,4.1) Define pseudonymous ID columns
val psids = {
  //Serialize -> Hash -> Encrypt
    myEnc(
       sha__256(
        rules)
  ).as(s"pseudoID")       
}

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ##### Next, we define what we mean by a quasi-id column, and "fuzzy" the data by reducing the date of birth into a year of birth

// COMMAND ----------

// DBTITLE 1,4.2) Define quasi-identifier columns
val quasiIdCols: Seq[Column] = Seq(
  //Fuzzify data
  'gender,
  'dob.substr(1, 4).cast(IntegerType).as("yob")
)

// COMMAND ----------

// DBTITLE 1,4.3) Create PII removal transformation
// Here we define the PII transformation to be applied to our dataset, which outputs a dataframe modified according to our specifications. 

def removePII(ds: Dataset[_]): DataFrame = 
  ds.toDF.select(quasiIdCols ++ Seq(psids): _*)

// COMMAND ----------

// MAGIC %md 
// MAGIC ### 5) Pseudonymize the Data
// MAGIC Here we perform the actual transformation, saving the output in a new dataframe. This is the data we should be storing in our databases for future use. 

// COMMAND ----------

// DBTITLE 1,5.1) Process PII & Display Results
val masterIds = ids.transform(removePII)
display(masterIds)

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC ### 6) Partner Specific Encoding 
// MAGIC 
// MAGIC In practice it may be desirable to, as the trusted data holder, send out partial data sets to different partners. Here, the main concern is that if we send two partial data sets to two different partners, those two partners may merge their data sets together without our knowledge, and obtain more information than we intended. This issue is discussed further in the next part, where attacks to divulge sensitive information from pseudonymized data sets is handled, but for now we will show how to limit the dangers using partner specific encoding. 

// COMMAND ----------

// If we do not need partner specific passwords, it is sufficient to simply call the transform twice since the randomized key will be different each time. 

val partnerIdsA = ids.transform(removePII)
val partnerIdsB = ids.transform(removePII)

display(partnerIdsB)

// COMMAND ----------

display(partnerIdsA)

// COMMAND ----------

// DBTITLE 1,Notes: 
// MAGIC %md
// MAGIC 
// MAGIC The data set above is now pseudonymized, but is still vulnerable to other forms of privacy breaches. For instance, since there is only one female in the set, an adversary who only knows that their target is in the set and is a woman, her information is still subject to attacks. For more information see [K-anonymity](https://en.wikipedia.org/wiki/K-anonymity), see also the [original paper](https://epic.org/privacy/reidentification/Sweeney_Article.pdf)