package com.example.helloworld.impl

import com.example.helloworld.api._

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import java.util.UUID
import cool.graph.cuid._
import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, WordSpec}

class HelloWorldEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll with OptionValues {

  private val system = ActorSystem("HelloWorldEntitySpec",
    JsonSerializerRegistry.actorSystemSetupFor(HelloWorldSerializerRegistry))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private val id = Cuid.createCuid()
  private val helloWorld = HelloWorld("name", Some("description"))
  private val createHelloWorldRequest = CreateHelloWorldRequest(helloWorld)
  private val helloWorldAggregate = HelloWorldAggregate(id, helloWorld)
  private val helloWorldCreatedEvent = HelloWorldCreatedEvent(helloWorldAggregate)

  private def withTestDriver[T](block: PersistentEntityTestDriver[HelloWorldCommand, HelloWorldEvent, Option[HelloWorldAggregate]] => T): T = {
    val driver = new PersistentEntityTestDriver(system, new HelloWorldEntity, id)
    try {
      block(driver)
    } finally {
//      driver.getAllHelloWorlds shouldBe empty
    }
  }

  "Hello World entity" should {

    "allow creating an Hello World" in withTestDriver { driver =>
      val outcome = driver.run(CreateHelloWorldCommand(helloWorldAggregate))
      outcome.events should contain only helloWorldCreatedEvent
      outcome.state should ===(Some(helloWorldAggregate))
    }

    "allow looking up an Hello World" in withTestDriver { driver =>
      driver.run(CreateHelloWorldCommand(helloWorldAggregate))
      val outcome = driver.run(GetHelloWorldQuery)
      outcome.events shouldBe empty
      outcome.replies should contain only Some(helloWorldAggregate)
      outcome.state should ===(Some(helloWorldAggregate))
    }
  }
}
