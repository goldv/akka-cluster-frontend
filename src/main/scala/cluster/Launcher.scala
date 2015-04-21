package cluster

import akka.actor._
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster._
import akka.contrib.pattern.ClusterSingletonManager
import akka.io.IO
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import spray.can.Http
import akka.pattern.ask
import spray.httpx.SprayJsonSupport
import spray.httpx.marshalling.Marshaller
import spray.json._
import scala.collection.SortedSet
import scala.concurrent.Future
import scala.concurrent.duration._
import spray.routing.HttpService

/**
 * Created by goldv on 13/04/2015.
 */
object Launcher extends App{

  println(args.mkString(","))

  if(args.length != 2) throw new RuntimeException("Illegal number of arguments")

  val port = args(0).toInt
  val httpPort = args(1).toInt

  val localConfig = ConfigFactory.parseString(s"akka.remote.netty.tcp.port = $port")

  implicit val system = ActorSystem("ClusterSystem", localConfig.withFallback( ConfigFactory.load() ) )

  // create and start our service actor
  val service = system.actorOf(Props[ClusterServiceActor], "demo-service")

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ! Http.Bind(service, "localhost", port = httpPort)

}

class ClusterServiceActor extends Actor with ClusterService {


  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  val cluster = Cluster(context.system)

  // this actor only runs our route, but you could add
  // other things here, like request stream processing,
  // timeout handling or alternative handler registration
  def receive = runRoute(appRoute)
}

case class Test(name: String, count: Int)

object JsonImplicits extends DefaultJsonProtocol with SprayJsonSupport{

  implicit val tfmt = jsonFormat2(Test)

  implicit object AddressFormat extends RootJsonFormat[Address] {
    def write(obj: Address): JsValue = {
      JsObject(
        "protocol" ->  JsString(obj.protocol),
        "system" -> JsString(obj.system),
        "host" -> JsString(obj.host.getOrElse("")),
        "port" -> JsNumber(obj.port.getOrElse(0))
      )
    }

    def read(json: JsValue): Address = json match {
      case _ => deserializationError("Not a Record")
    }
  }

  implicit object UniqueAddressFormat extends RootJsonFormat[UniqueAddress] {
    def write(obj: UniqueAddress): JsValue = {
      JsObject(
        "address" ->  obj.address.toJson,
        "uid" -> JsNumber(obj.uid)
      )
    }

    def read(json: JsValue): UniqueAddress = json match {
      case _ => deserializationError("Not a Record")
    }
  }

  implicit object MemberFormat extends RootJsonFormat[Member] {
    def write(obj: Member): JsValue = {
      JsObject(
        "status" ->  JsString(obj.status.toString),
        "uniqueAddress" -> obj.uniqueAddress.toJson,
        "address" -> obj.address.toJson
      )
    }

    def read(json: JsValue): Member = json match {
      case _ => deserializationError("Not a Record")
    }
  }

  implicit object ClusterStateFormat extends RootJsonFormat[CurrentClusterState]{
    def write(obj: CurrentClusterState): JsValue = {
      JsObject(
        "members" ->  JsArray( obj.members.map(_.toJson).toList:_* ),
        "unreachable" -> JsArray( obj.unreachable.map(_.toJson).toList:_* ),
        "leader" -> obj.leader.map(_.toJson).getOrElse(JsNull)
      )
    }

    def read(json: JsValue): CurrentClusterState = json match {
      case _ => deserializationError("Not a Record")
    }
  }
}

trait ClusterService extends HttpService {

  implicit val timeout = Timeout(5 second)
  implicit def executionContext = actorRefFactory.dispatcher

  import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
  import JsonImplicits._

  def cluster: Cluster

  //val clusterListener = actorRefFactory.actorOf(ClusterSingletonManager.props(Props[ClusterListener], "active", PoisonPill, None), "master")

  val clusterListener = actorRefFactory.actorOf(Props[ClusterListener], "master")

  val appRoute = {

    get{
      path("state"){
        complete(
          cluster.state
          //(clusterListener ? ApplicationRequestClusterState).mapTo[CurrentClusterState]
        )
      }
    }
  }

}