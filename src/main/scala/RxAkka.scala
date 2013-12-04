import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import rx.lang.scala.{ Observable, Subscription }
import rx.lang.scala.subjects.BehaviorSubject
import scala.concurrent.duration._

case object Init
case class Subscribe()
case class Subscribed(connect: () ⇒ Subscription, observable: Observable[String])
case class Result(value: String)

class ActorA(target: ActorRef) extends Actor {
  def receive: Receive = {
    case Init ⇒
      target ! Subscribe()
      context.become(waitingObservable)
  }

  def waitingObservable: Receive = {
    case Subscribed(connect, observable) ⇒
      observable.subscribe((string: String) ⇒ self ! Result(string))
      context.become(waitingResult(connect()))
  }

  def waitingResult(subscription: Subscription): Receive = {
    case Result(value) ⇒
      println(s"ReceivedA :: $value")
      subscription.unsubscribe()
      context.become(receive)

    case m @ _ ⇒
      println(s"Invalid message: $m")
  }

  context.system.scheduler.scheduleOnce(5.seconds)(
    self ! Init
  )(context.system.dispatcher)

  context.system.scheduler.scheduleOnce(15.seconds)(
    self ! Init
  )(context.system.dispatcher)

  context.system.scheduler.scheduleOnce(25.seconds)(
    self ! Init
  )(context.system.dispatcher)
}

class ActorB(target: ActorRef) extends Actor {
  def receive: Receive = {
    case Init ⇒
      target ! Subscribe()
      context.become(waitingObservable)
  }

  def waitingObservable: Receive = {
    case Subscribed(connect, observable) ⇒
      observable.subscribe((string: String) ⇒ self ! Result(string))
      context.become(waitingResult(connect()))
  }

  def waitingResult(subscription: Subscription): Receive = {
    case Result(value) ⇒
      println(s"ReceivedB :: $value")
      subscription.unsubscribe()
      context.become(receive)

    case m @ _ ⇒
      println(s"Invalid message: $m")
  }

  context.system.scheduler.scheduleOnce(5.seconds)(
    self ! Init
  )(context.system.dispatcher)
  context.system.scheduler.scheduleOnce(35.seconds)(
    self ! Init
  )(context.system.dispatcher)
}

class ActorC() extends Actor {
  def receive: Receive = {
    case Subscribe() ⇒
      sender ! (Subscribed.apply _).tupled(subject.publish)
  }

  val subject: BehaviorSubject[String] = BehaviorSubject[String]("seed")

  context.system.scheduler.scheduleOnce(10.seconds)(
    subject.onNext("10 seconds elapsed!")
  )(context.system.dispatcher)

  context.system.scheduler.scheduleOnce(20.seconds)(
    subject.onNext("20 seconds elapsed!")
  )(context.system.dispatcher)

  context.system.scheduler.scheduleOnce(30.seconds)(
    subject.onNext("30 seconds elapsed!")
  )(context.system.dispatcher)

  context.system.scheduler.scheduleOnce(30.seconds)(
    subject.onNext("40 seconds elapsed!")
  )(context.system.dispatcher)
}

object RxAkka extends App {
  val system: ActorSystem = ActorSystem("test")
  val actorC: ActorRef = system.actorOf(Props(classOf[ActorC]))
  val actorB: ActorRef = system.actorOf(Props(classOf[ActorB], actorC))
  val actorA: ActorRef = system.actorOf(Props(classOf[ActorA], actorC))
}
