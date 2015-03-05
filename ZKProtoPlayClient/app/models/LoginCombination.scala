package models

/**
 * Created by alper on 26/11/14.
 */
case class LoginCombination(pin:String,pwd:String, fp:String, card:String, face:String)

object LoginCombination{

   def fromParser(
                  pin:String = "",
                  pwd:String = "",
                  fp:String = "",
                  card:String = "",
                  face:String = ""
                   ):LoginCombination = {
      new LoginCombination(
      pin,
      pwd,
      fp,
      card,
      face
      )
   }
}
