package utils

import java.math.BigInteger

/**
 * Created by alper on 19/11/14.
 */
object HashFactory {

  final val HASH_LONG: Int = 32

  def hash(text:String): String = {
    val digest:java.security.MessageDigest = java.security.MessageDigest.getInstance("MD5")
    digest.update(text.getBytes,0,text.length)
    var md5 = new BigInteger(1, digest.digest()).toString(16)
    if(md5.length != HASH_LONG){
        md5 = padLeft(md5, "0", HASH_LONG)
    }
    md5
  }

  def padLeft(str:String,padding:String,size:Int):String = {
      val shortBy:Int = size - str.length
      val buff:StringBuilder = new StringBuilder()
      for(i <- 0 to shortBy){
           buff.append(padding)
      }
      buff.append(str)
      buff.toString()
  }

  def check(toCheck: String, hashAndSalt: String): Boolean = {
    val auxPwd = hash(toCheck)
    val hashedStr = hashAndSalt.substring(0, HASH_LONG)
    auxPwd.equals(hashedStr)
  }

  def generateToken(): String = {
    val key = java.util.UUID.randomUUID.toString
    new sun.misc.BASE64Encoder().encode(key.getBytes)
  }
}