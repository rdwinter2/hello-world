package com.example.helloworld.impl

import com.example.helloworld.api._
import com.example.helloworld.api.HelloWorldService
import com.example.common.authentication.TokenContent
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtJson}
import com.typesafe.config.ConfigFactory
import java.util.UUID
import java.net.URI
import play.api.libs.json.{Format, Json}
import com.lightbend.lagom.scaladsl.api.transport.{ Method, MessageProtocol }
//import com.lightbend.lagom.scaladsl.api.transport.RequestHeader.RequestHeaderImpl

import akka.{ Done, NotUsed }
import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import akka.stream.testkit.scaladsl.TestSink
import akka.testkit.TestKit
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.{ RequestHeader, ResponseHeader }
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.server.{ LagomApplication, LocalServiceLocator, ServerServiceCall }
import com.lightbend.lagom.scaladsl.testkit.{ PersistentEntityTestDriver, ServiceTest, TestTopicComponents }
import cool.graph.cuid._
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, Matchers }
import play.api.http.Status
import play.api.libs.ws.ahc.AhcWSComponents
import scala.concurrent.duration._
//import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future, Promise }

class HelloWorldServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {


  val secret = ConfigFactory.load().getString("jwt.secret")
  val algorithm = JwtAlgorithm.HS512
  val authExpiration = ConfigFactory.load().getInt("jwt.token.auth.expirationInSeconds")
  val tokenContent = TokenContent(
            clientId = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            username = "testuser"
          )
  val authClaim = JwtClaim(Json.toJson(tokenContent).toString())
    .expiresIn(authExpiration)
    .issuedNow
  val authToken = JwtJson.encode(authClaim, secret, algorithm)
  //println(authToken)
  val requestHeader = RequestHeader(Method.POST, URI.create("/"), MessageProtocol.empty, Nil, None, Nil)

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup.withCassandra(true)) { ctx =>
    new LagomApplication(ctx) with HelloWorldComponents with LocalServiceLocator with AhcWSComponents with TestTopicComponents
  }

  val helloWorldService: HelloWorldService = server.serviceClient.implement[HelloWorldService]

  override protected def afterAll(): Unit = server.stop()

  "Hello World service" should {
    "allow creating an Hello World" in {
      val helloWorld = HelloWorld("name", Some("description"))
      val createHelloWorldRequest = CreateHelloWorldRequest(helloWorld)
      for {
        createdHelloWorld <- helloWorldService.createHelloWorldWithSystemGeneratedId.handleRequestHeader(requestHeader => requestHeader.withHeader("Authorization", "Bearer " + authToken.toString)).invoke(createHelloWorldRequest)
      } yield {
        createdHelloWorld.id should not be null
        createdHelloWorld.helloWorld.name should be("name")
        createdHelloWorld.helloWorld.description should be(Some("description"))
      }
    }

    "allow looking up a created Hello World" in {
      val helloWorld = HelloWorld("name", Some("description"))
      val createHelloWorldRequest = CreateHelloWorldRequest(helloWorld)
      for {
        createdHelloWorld <- helloWorldService.createHelloWorldWithSystemGeneratedId.handleRequestHeader(requestHeader => requestHeader.withHeader("Authorization", "Bearer " + authToken.toString)).invoke(createHelloWorldRequest)
        lookupHelloWorld <- helloWorldService.getHelloWorld(createdHelloWorld.id).handleRequestHeader(requestHeader => requestHeader.withHeader("Authorization", "Bearer " + authToken.toString)).invoke
      } yield {
        createdHelloWorld.id should ===(lookupHelloWorld.id)
        createdHelloWorld.helloWorld should ===(lookupHelloWorld.helloWorld)
      }
    }

    "allow looking up all created Hello World" in {
      val id1 = Cuid.createCuid()
      val id2 = Cuid.createCuid()
      val id3 = Cuid.createCuid()
      val helloWorld1 = HelloWorld("name", Some("description1"))
      val helloWorld2 = HelloWorld("name", Some("description2"))
      val helloWorld3 = HelloWorld("name", Some("description3"))
      (for {
        createdHelloWorld1 <- helloWorldService.createHelloWorld(id1).handleRequestHeader(requestHeader => requestHeader.withHeader("Authorization", "Bearer " + authToken.toString)).invoke(CreateHelloWorldRequest(helloWorld1))
        createdHelloWorld2 <- helloWorldService.createHelloWorld(id2).handleRequestHeader(requestHeader => requestHeader.withHeader("Authorization", "Bearer " + authToken.toString)).invoke(CreateHelloWorldRequest(helloWorld2))
        createdHelloWorld3 <- helloWorldService.createHelloWorld(id3).handleRequestHeader(requestHeader => requestHeader.withHeader("Authorization", "Bearer " + authToken.toString)).invoke(CreateHelloWorldRequest(helloWorld3))
      } yield {
        awaitSuccess() {
          for {
            lookupHelloWorldsResponse <- helloWorldService.getAllHelloWorlds.handleRequestHeader(requestHeader => requestHeader.withHeader("Authorization", "Bearer " + authToken.toString)).invoke
          } yield {
            lookupHelloWorldsResponse.helloWorlds should contain allOf(HelloWorldResource(id1, createdHelloWorld1.helloWorld), HelloWorldResource(id2, createdHelloWorld2.helloWorld), HelloWorldResource(id3, createdHelloWorld3.helloWorld))
          }
        }
      }).flatMap(identity)
    }

    "publish helloWorld events on the Kafka topic" in {
      implicit val system = server.actorSystem
      implicit val mat = server.materializer
      val helloWorld = HelloWorld("name", Some("description"))
      val createHelloWorldRequest = CreateHelloWorldRequest(helloWorld)
      val id = Cuid.createCuid()

      for {
        createdHelloWorld <- helloWorldService.createHelloWorld(id).handleRequestHeader(requestHeader => requestHeader.withHeader("Authorization", "Bearer " + authToken.toString)).invoke(createHelloWorldRequest)
        events <- helloWorldService.helloWorldMessageBrokerEvents.subscribe.atMostOnceSource
          .filter(_.id == createdHelloWorld.id)
          .take(1)
          .runWith(Sink.seq)
      } yield {
        events.size shouldBe 1
        events.head shouldBe an[HelloWorldCreated]
        val event = events.head.asInstanceOf[HelloWorldCreated]
        event.helloWorld.name should be("name")
        event.helloWorld.description should be(Some("description"))
      }
    }

//    "publish newly created Hello Worlds on the PubSub topic" in {
//      helloWorldService.streamHelloWorlds.handleRequestHeader(requestHeader => requestHeader.withHeader("Authorization", "Bearer " + authToken.toString)).invoke.map { source =>
//        implicit val system = server.actorSystem
//        implicit val mat = server.materializer
//
//        val id1 = Cuid.createCuid()
//        val id2 = Cuid.createCuid()
//        val id3 = Cuid.createCuid()
//        val helloWorld1 = HelloWorld("name", Some("description"))
//        val helloWorld2 = HelloWorld("name2", Some("description2"))
//        val helloWorld3 = HelloWorld("name3", Some("description3"))
//println("before create")
//        val probe = source.runWith(TestSink.probe)
//        probe.request(3)
//
//        helloWorldService.createHelloWorld(id1).handleRequestHeader(requestHeader => requestHeader.withHeader("Authorization", "Bearer " + authToken.toString)).invoke(CreateHelloWorldRequest(helloWorld1))
//        helloWorldService.createHelloWorld(id2).handleRequestHeader(requestHeader => requestHeader.withHeader("Authorization", "Bearer " + authToken.toString)).invoke(CreateHelloWorldRequest(helloWorld2))
//        helloWorldService.createHelloWorld(id3).handleRequestHeader(requestHeader => requestHeader.withHeader("Authorization", "Bearer " + authToken.toString)).invoke(CreateHelloWorldRequest(helloWorld3))
//
//println("after create")
//        probe.expectNextUnordered(HelloWorldResource(id1, helloWorld1), HelloWorldResource(id1, helloWorld2), HelloWorldResource(id1, helloWorld3))
//println("after probe")
//        probe.cancel()
//        succeed
//      }
//    }
  }

  def awaitSuccess[T](maxDuration: FiniteDuration = 10.seconds, checkEvery: FiniteDuration = 100.milliseconds)(block: => Future[T]): Future[T] = {
    val checkUntil = System.currentTimeMillis() + maxDuration.toMillis

    def doCheck(): Future[T] = {
      block.recoverWith {
        case recheck if checkUntil > System.currentTimeMillis() =>
          val timeout = Promise[T]()
          server.application.actorSystem.scheduler.scheduleOnce(checkEvery) {
            timeout.completeWith(doCheck())
          }(server.executionContext)
          timeout.future
      }
    }

    doCheck()
  }
}