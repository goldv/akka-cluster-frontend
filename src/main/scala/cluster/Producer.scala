package cluster

import akka.actor._
import akka.contrib.pattern.ClusterSingletonProxy
import scala.concurrent.duration._
import Protocol._

/**
 * Created by goldv on 13/04/2015.
 */
class Producer extends Actor with ActorLogging{

  implicit val ec = context.dispatcher
  case object Tick

  var count = 0;

  val masterProxy = context.actorOf(ClusterSingletonProxy.props(
    singletonPath = "/user/master/active",
    role = None),
    name = "masterProxy")

  override def preStart = context.system.scheduler.scheduleOnce(1 second, self, Tick)

  def receive = {
    case Tick => {
      println(s"sending $count")
      masterProxy ! UpdateCommand(count)
      count += 1
      context.system.scheduler.scheduleOnce(1 second, self, Tick)
    }
    case Terminated(s) => println(s"actor terminated $s")
  }
}


