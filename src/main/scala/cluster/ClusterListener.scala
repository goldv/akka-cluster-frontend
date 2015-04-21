package cluster

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.persistence.{PersistentActor, SnapshotOffer}
import cluster.Protocol._


/**
 * Created by goldv on 13/04/2015.
 */
case class IncrementState(value: Int){
  def update(event: UpdateEvent) = IncrementState(value + event.count)
}

object ApplicationRequestClusterState

class ClusterListener extends Actor with ActorLogging {

  //override def persistenceId = "master"

  case object SnapshotRequest

  val cluster = Cluster(context.system)

  var state = IncrementState(0)

  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[UnreachableMember])
  }

  override def postStop(): Unit = {
    //super.postStop()
    cluster.unsubscribe(self)
  }

  def updateState(evt: UpdateEvent) = {
    state = state.update(evt)
  }

  override def receive: Receive = {
    case MemberUp(member) => log.info(s"Member is Up: ${member.address}")
    case UnreachableMember(member) => log.info(s"Member detected as unreachable: ${member}")
    case MemberRemoved(member, previousStatus) => log.info(s"Member is Removed: ${member.address} after ${previousStatus}")
    case ApplicationRequestClusterState => clusterState
    case m: MemberEvent => log.info(s"Received unknown member event")
  }

  def clusterState = sender ! cluster.state

}


