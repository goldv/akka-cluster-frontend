package cluster

import akka.actor.{ActorSystem, Props}

/**
 * Created by goldv on 14/04/2015.
 */
object ProducerLauncher extends App{

  val system = ActorSystem("ClusterSystem" )
  system.actorOf( Props[Producer] )

}
