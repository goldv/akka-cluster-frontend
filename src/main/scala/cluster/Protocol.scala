package cluster

/**
 * Created by goldv on 13/04/2015.
 */
object Protocol {

  case class UpdateCommand(count: Int)
  case class UpdateEvent(count: Int)

}
