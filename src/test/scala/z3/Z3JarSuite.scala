package z3

import com.microsoft.z3._
import org.scalatest.{FunSuite, Matchers}
import scala.collection.JavaConversions._

trait Z3JarSuite extends FunSuite with Matchers {
  Z3Loader.load();
  val z3 = new Context(Map[String, String]("MODEL" -> "true"))
  Global.ToggleWarningMessages(true)
}
