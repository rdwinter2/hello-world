<!--- to transclude content use the following syntax at the beginning of a line "transclude::file_name.ext::[an optional regular expression]" -->
<!--- Never directly edit transcluded content, always edit the source file -->
Hello World
=============

## <a name="overview"></a>Overview

This is a simple Hello World self-contained system (SCS) to demonstrate the use of the Lagom framework. It has a simple CRUD interface as well as methods for doing a proper Domain Driven Design (DDD) ubiquitious language (UL).

## Table of Contents
1. [Overview](#overview)
2. [Description](#description)
3. [System Design](#systemdesign)
3. [Domain Model](#domainmodel)
   * [External Event Flow](#externaleventflow)
   * [Internal Event Flow](#internaleventflow)
   * [Conceptual Data Model](#conceptualdatamodel)
2. [Microservices](#microservices)
   * [Hello World](#helloworldmicroservice)
   * [Tag](#tagmicroservice)
   * [Tag Hello World Saga](#taghelloworldsagamicroservice)
   * [Authorization](#authorizationmicroservice)
   * [Authentication](#authenticationmicroservice)
2. [Glossary - Domain](#glossarydomain)
2. [Glossary - Technical](#glossarytechnical)
3. [References](#references)
4. [Notes](#notes)

## <a name="description"></a>Description

<!--- transclude::api/HelloWorldService.scala::[override final def descriptor = {] cjqnn2ydl00002iyayttq5w4j -->

```scala
  override final def descriptor = {
    import Service._
    // @formatter:off
    named("hello-world").withCalls(
      // CRUDy Bulk Data Administration
      restCall(Method.POST,   "/api/hello-worlds/data-administration/bulk-creation",     bulkCreateHelloWorld _),
      restCall(Method.POST,   "/api/hello-worlds/data-administration/bulk-replacement",  bulkReplaceHelloWorld _),
      restCall(Method.POST,   "/api/hello-worlds/data-administration/bulk-mutation",     bulkMutateHelloWorld _),
      restCall(Method.POST,   "/api/hello-worlds/data-administration/bulk-deactivation", bulkDeactivateHelloWorld _),
      restCall(Method.POST,   "/api/hello-worlds/data-administration/bulk-reactivation", bulkReactivateHelloWorld _),
      restCall(Method.POST,   "/api/hello-worlds/data-administration/bulk-excision",     bulkExciseHelloWorld _),
      // CRUDy REST
      restCall(Method.POST,   "/api/hello-worlds",     postHelloWorld1 _),
      restCall(Method.POST,   "/api/hello-worlds/:id", postHelloWorld2 _),
      restCall(Method.PUT,    "/api/hello-worlds/:id", putHelloWorld _),
      restCall(Method.PATCH,  "/api/hello-worlds/:id", patchHelloWorld _),
      restCall(Method.DELETE, "/api/hello-worlds/:id", deleteHelloWorld _),
      restCall(Method.GET,    "/api/hello-worlds/:id", getHelloWorld _),
      restCall(Method.GET,    "/api/hello-worlds",     getAllHelloWorlds _),
      // CRUDy DDDified REST without a proper ubiquitious language
      // Create
      restCall(Method.POST, "/api/hello-worlds/creation",                                createHelloWorld1 _),
      restCall(Method.POST, "/api/hello-worlds/:id/creation",                            createHelloWorld2 _),
      restCall(Method.POST, "/api/hello-worlds/creation/:creationId",                    createHelloWorld3 _),
      restCall(Method.POST, "/api/hello-worlds/:id/creation/:creationId",                createHelloWorld4 _),
      restCall(Method.GET,  "/api/hello-worlds/:id/creation/:creationId",                getCreationHelloWorld _),
      pathCall(             "/api/hello-worlds/:id/creation/:creationId/stream",         streamCreationHelloWorld _),
      // Read
      // Update
      restCall(Method.POST, "/api/hello-worlds/:id/replacement",                         replaceHelloWorld1 _),
      restCall(Method.POST, "/api/hello-worlds/:id/replacement/:replacementId",          replaceHelloWorld2 _),
      restCall(Method.GET,  "/api/hello-worlds/:id/replacement/:replacementId",          getReplacementHelloWorld _),
      pathCall(             "/api/hello-worlds/:id/replacement/:replacementId/stream",   streamReplacementHelloWorld _),
      restCall(Method.POST, "/api/hello-worlds/:id/mutation",                            mutateHelloWorld1 _),
      restCall(Method.POST, "/api/hello-worlds/:id/mutation/:mutationId",                mutateHelloWorld2 _),
      restCall(Method.GET,  "/api/hello-worlds/:id/mutation/:mutationId",                getMutationHelloWorld _),
      pathCall(             "/api/hello-worlds/:id/mutation/:mutationId/stream",         streamMutationHelloWorld _),
      // Delete
      restCall(Method.POST, "/api/hello-worlds/:id/deactivation",                        deactivateHelloWorld1 _),
      restCall(Method.POST, "/api/hello-worlds/:id/deactivation/:deactivationId",        deactivateHelloWorld2 _),
      restCall(Method.GET,  "/api/hello-worlds/:id/deactivation/:deactivationId",        getDeactivationHelloWorld _),
      pathCall(             "/api/hello-worlds/:id/deactivation/:deactivationId/stream", streamDeactivationHelloWorld _),
      // Undelete
      restCall(Method.POST, "/api/hello-worlds/:id/reactivation",                        reactivateHelloWorld1 _),
      restCall(Method.POST, "/api/hello-worlds/:id/reactivation/:reactivationId",        reactivateHelloWorld2 _),
      restCall(Method.GET,  "/api/hello-worlds/:id/reactivation/:reactivationId",        getReactivationHelloWorld _),
      pathCall(             "/api/hello-worlds/:id/reactivation/:reactivationId/stream", streamReactivationHelloWorld _),
      // DDDified REST using the bounded context's ubiquitious language
      //restCall(Method.POST, "/api/hello-worlds/:id/description-enhancement/:enhancementId", enhanceDescriptionHelloWorld _),
//      pathCall("/api/ff hello-worlds/stream", streamHelloWorlds _),
    )
      .withAutoAcl(true)
      .withExceptionSerializer(new DefaultExceptionSerializer(Environment.simple(mode = Mode.Prod)))
      .withTopics(
        topic("helloWorld-HelloWorldMessageBrokerEvent", this.helloWorldMessageBrokerEvents)
      )
    // @formatter:on
  }
```

<!--- transclude cjqnn2ydl00002iyayttq5w4j -->
NOTE: For naming resources in a domain driven design (DDD) manner, focus on domain events not low-level create, read, update, and delete (CRUD) operations.

From [Roy Fielding's dissertation](https://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_2_1_1):
> The key abstraction of information in REST is a resource. Any information that can be named can be a resource: a document or image, a temporal service (e.g. "today's weather in Los Angeles"), a collection of other resources, a non-virtual object (e.g. a person), and so on. In other words, any concept that might be the target of an author's hypertext reference must fit within the definition of a resource. A resource is a conceptual mapping to a set of entities, not the entity that corresponds to the mapping at any particular point in time.
From [REST API Design - Resource Modeling](https://www.thoughtworks.com/insights/blog/rest-api-design-resource-modeling):
> The way to escape low-level CRUD is to create business operation or business process resources, or what we can call as "intent" resources that express a business/domain level "state of wanting something" or "state of the process towards the end result". But to do this you need to ensure you identify the true owners of all your state. In a world of four-verb (AtomPub-style) CRUD, it's as if you allow random external parties to mess around with your resource state, through PUT and DELETE, as if the service were just a low-level database. PUT puts too much internal domain knowledge into the client. The client shouldn't be manipulating internal representation; it should be a source of user intent.
> HTTP verb PUT can be used for idempotent resource updates (or resource creations in some cases) by the API consumer. However, use of PUT for complex state transitions can lead to synchronous cruddy CRUD. It also usually throws away a lot of information that was available at the time the update was triggered - what was the real business domain event that triggered this update? With “[REST without PUT](https://www.thoughtworks.com/radar/techniques/rest-without-put)” technique, the idea is that consumers are forced to post new 'nounified' request resources.
From HTTP/1.1 Spec:
> The POST method is used to request that the origin server accept the entity enclosed in the request as a new subordinate of the resource identified by the Request-URI in the Request-Line.
From [DDD & REST - Domain-Driven APIs for the web](https://speakerdeck.com/olivergierke/ddd-and-rest-domain-driven-apis-for-the-web?slide=42)
> Prefer explicit state transitions over poking at your resources using PATCH.
> |-----------|----------|
> | Aggregate Root / Repository | Collection / Item Resources |
> | Relations | Links |
> | IDs | URIs |
> | @Version | ETags |
> | Last Modified Property | Last Modified Header |
The algebraic data type for Hello World is defined as:

<!--- transclude::api/HelloWorldService.scala::[Hello World algebraic data type {] cjqnn2yob00012iyasowb9vtl -->

```scala
// Hello World algebraic data type {
//
// An algebraic data type is a kind of composite type.
// They are built up from Product types and Sum types.
//
// Product types - a tuple or record (this and that)
//   class ScalaPerson(val name: String, val age: Int)
//
// Sum types - a disjoint union or variant type (this or that)
//   sealed trait Pet
//   case class Cat(name: String) extends Pet
//   case class Fish(name: String, color: String) extends Pet
//   case class Squid(name: String, age: Int) extends Pet
case class HelloWorld(
  name: String,
  description: Option[String])
object HelloWorld {
  implicit val format: Format[HelloWorld] = Jsonx.formatCaseClass
  val helloWorldValidator: Validator[HelloWorld] =
    validator[HelloWorld] { helloWorld =>
      helloWorld.name is notEmpty
      helloWorld.name should matchRegexFully(Matchers.Name)
      helloWorld.description.each should matchRegexFully(Matchers.Description)
    }
}
// }
```

<!--- transclude cjqnn2yob00012iyasowb9vtl -->

With regular expression validation matchers:

<!--- transclude::api/HelloWorldService.scala::[object Matchers {] cjqnn2yyp00022iyaoxbsvs0q -->

```scala
object Matchers {
  val Email =
    """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"""
  val Id = """^[a-zA-Z0-9\-\.\_\~]{1,64}$"""
  val Name = """^[a-zA-Z0-9\-\.\_\~]{1,128}$"""
  val Description = """^.{1,2048}$"""
  val Motivation = """^.{1,2048}$"""
  val Op = """^add|remove|replace|move|copy|test$"""
}
```

<!--- transclude cjqnn2yyp00022iyaoxbsvs0q -->

And supporting algebraic data types:

<!--- transclude::api/HelloWorldService.scala::[Supporting algebraic data types {] cjqnn2z9h00032iyap5q87pwr -->

```scala
// Supporting algebraic data types {
case class Identity(
  identifier: String,
  revision: Option[Int],    // a monotonically increasing counter of changes
  hash: Option[String])
object Identity {
  implicit val format: Format[Identity] = Jsonx.formatCaseClass
  val identityValidator: Validator[Identity] =
    validator[Identity] { identity =>
      identity.identifier is notEmpty
      identity.identifier should matchRegexFully(Matchers.Id)
      // need Option[Int]
      //identity.revision should be >= 0
    }
}
case class HypertextApplicationLanguage(
  halLinks: Seq[HalLink]
  )
object HypertextApplicationLanguage {
  implicit val format: Format[HypertextApplicationLanguage] = Jsonx.formatCaseClass
}
case class HalLink(
  rel: String,
  href: String,
  deprecation: Option[String] = None,
  name: Option[String] = None,
  profile: Option[String] = None,
  title: Option[String] = None,
  hreflang: Option[String] = None,
  `type`: Option[String] = None,
  templated: Boolean = false) {
  def withDeprecation(url: String) = this.copy(deprecation = Some(url))
  def withName(name: String) = this.copy(name = Some(name))
  def withProfile(profile: String) = this.copy(profile = Some(profile))
  def withTitle(title: String) = this.copy(title = Some(title))
  def withHreflang(lang: String) = this.copy(hreflang = Some(lang))
  def withType(mediaType: String) = this.copy(`type` = Some(mediaType))
}
object HalLink {
  implicit val format: Format[HalLink] = Jsonx.formatCaseClass
}
case class Mutation(
  op: String,
  path: String,
  value: Option[String]
  )
object Mutation {
  implicit val format: Format[Mutation] = Jsonx.formatCaseClass
  val mutationValidator: Validator[Mutation] =
    validator[Mutation] { mutation =>
      mutation.op is notEmpty
      mutation.path is notEmpty
      mutation.op should matchRegexFully(Matchers.Op)
    }
}
// }
```

<!--- transclude cjqnn2z9h00032iyap5q87pwr -->

The REST resource for Hello World is defined as:

<!--- transclude::api/HelloWorldService.scala::[case class HelloWorldResource(] cjqnn2zk700042iyavwhtlyap -->

```scala
case class HelloWorldResource(
  helloWorld: HelloWorld
)
```

<!--- transclude cjqnn2zk700042iyavwhtlyap -->

The DDD aggregate for Hello World is defined as:

<!--- transclude::impl/HelloWorldServiceImpl.scala::[case class HelloWorldAggregate(] cjqnn2zut00052iyadv72dtj5 -->

```scala
case class HelloWorldAggregate(
  helloWorldIdentity: Identity,
  helloWorldResource: HelloWorldResource
)
```

<!--- transclude cjqnn2zut00052iyadv72dtj5 -->

The state for Hello World is defined as:

<!--- transclude::impl/HelloWorldServiceImpl.scala::[override type State = .*] cjqnn305600062iyaa5fz0oh3 -->

```scala
  override type State = Option[HelloWorldState]
```

<!--- transclude cjqnn305600062iyaa5fz0oh3 -->

And uses the following HelloWorldState algebraic data type:

<!--- transclude::impl/HelloWorldServiceImpl.scala::[case class HelloWorldState(] cjqnn30g600072iya1y9yjgiu -->

```scala
case class HelloWorldState(
  helloWorldAggregate: HelloWorldAggregate,
  status: HelloWorldStatus.Status = HelloWorldStatus.NONEXISTENT
)
```

<!--- transclude cjqnn30g600072iya1y9yjgiu -->

The initial state for all DDD aggregates is:

<!--- transclude::impl/HelloWorldServiceImpl.scala::[override def initialState: .*] cjqnn30qt00082iya2vcfqu6s -->

```scala
  override def initialState: Option[HelloWorldState] = None
```

<!--- transclude cjqnn30qt00082iya2vcfqu6s -->

The possible statuses for the Hello World aggregate are defined to be:

<!--- transclude::impl/HelloWorldServiceImpl.scala::[object HelloWorldStatus extends Enumeration {] cjqnn310o00092iyay9y5wunu -->

```scala
object HelloWorldStatus extends Enumeration {
  val NONEXISTENT, ACTIVE, ARCHIVED, UNKNOWN = Value
  type Status = Value
  implicit val format: Format[Value] = enumFormat(this)
//  implicit val pathParamSerializer: PathParamSerializer[Status] =
//    PathParamSerializer.required("helloWorldStatus")(withName)(_.toString)
}
```

<!--- transclude cjqnn310o00092iyay9y5wunu -->

The finite state machine (FSM) for the DDD aggregate is defined as:

<!--- transclude::impl/HelloWorldServiceImpl.scala::[override def behavior: Behavior = {] cjqnn31az000a2iyahafrcvnm -->

```scala
  override def behavior: Behavior = {
    case None => nonexistentHelloWorld
    case Some(state) if state.status == HelloWorldStatus.ACTIVE => activeHelloWorld
    case Some(state) if state.status == HelloWorldStatus.ARCHIVED => archivedHelloWorld
    case Some(state) => unknownHelloWorld
  }
```

<!--- transclude cjqnn31az000a2iyahafrcvnm -->

The persistent entity for Hello World is defined as:

<!--- transclude::impl/HelloWorldServiceImpl.scala::[final class HelloWorldEntity extends PersistentEntity {] cjqnn31lm000b2iya3a9t563z -->

```scala
final class HelloWorldEntity extends PersistentEntity {
  //private val publishedHelloWorldCreatedEvent = pubSubRegistry.refFor(TopicId[HelloWorldCreatedEvent])
  override type Command = HelloWorldCommand[_]
  override type Event = HelloWorldEvent
  override type State = Option[HelloWorldState]
  type OnCommandHandler[M] = PartialFunction[(Command, CommandContext[M], State), Persist]
  type ReadOnlyHandler[M] = PartialFunction[(Command, ReadOnlyCommandContext[M], State), Unit]
  override def initialState: Option[HelloWorldState] = None
  // Finite State Machine (FSM)
  override def behavior: Behavior = {
    case None => nonexistentHelloWorld
    case Some(state) if state.status == HelloWorldStatus.ACTIVE => activeHelloWorld
    case Some(state) if state.status == HelloWorldStatus.ARCHIVED => archivedHelloWorld
    case Some(state) => unknownHelloWorld
  }
  private val nonexistentHelloWorld = {
    getHelloWorldAction orElse {
      Actions()
        .onCommand[CreateHelloWorldCommand, Either[ServiceError, HelloWorldAggregate]] { createHelloWorldCommand }
        .onCommand[ReplaceHelloWorldCommand, Either[ServiceError, HelloWorldAggregate]] { replyHelloWorldDoesNotExist }
        .onEvent {
          case (HelloWorldCreatedEvent(helloWorldAggregate), state) => HelloWorldState(Some(helloWorldAggregate), HelloWorldStatus.ACTIVE, 1)
          case (_, state) => state
        }
    }
  }
  private val activeHelloWorld = {
    getHelloWorldAction orElse {
      Actions()
        .onCommand[CreateHelloWorldCommand, Either[ServiceError, HelloWorldAggregate]] { replyConflict }
        .onCommand[ReplaceHelloWorldCommand, Either[ServiceError, HelloWorldAggregate]] { replaceHelloWorldCommand }
        .onEvent {
          case (HelloWorldReplacedEvent(helloWorldId, replacementHelloWorldResource, motivation), state) =>
            HelloWorldState(Some(HelloWorldAggregate(helloWorldId, replacementHelloWorldResource)), HelloWorldStatus.ACTIVE, 1)
          case (_, state) => state
        }
    }
  }
  private val archivedHelloWorld = {
    getHelloWorldAction orElse {
      Actions()
        .onCommand[CreateHelloWorldCommand, Either[ServiceError, HelloWorldAggregate]] { replyConflict }
        .onEvent {
          case (_, state) => state
        }
    }
  }
  private val unknownHelloWorld = {
    getHelloWorldAction orElse {
      Actions()
        .onCommand[CreateHelloWorldCommand, Either[ServiceError, HelloWorldAggregate]] { replyConflict }
        .onCommand[ReplaceHelloWorldCommand, Either[ServiceError, HelloWorldAggregate]] { replyConflict }
    }
  }
  private def getHelloWorldAction = Actions()
    .onReadOnlyCommand[GetHelloWorldQuery.type, HelloWorldState] {
      case (GetHelloWorldQuery, ctx, state) => ctx.reply(state)
    }
  private def createHelloWorldCommand: OnCommandHandler[Either[ServiceError, HelloWorldAggregate]] = {
    case (CreateHelloWorldCommand(helloWorldAggregate), ctx, state) =>
      ctx.thenPersist(HelloWorldCreatedEvent(helloWorldAggregate)) { evt =>
        ctx.reply(Right(helloWorldAggregate))
      }
  }
  private def replaceHelloWorldCommand: OnCommandHandler[Either[ServiceError, HelloWorldAggregate]] = {
    case (ReplaceHelloWorldCommand(replaceHelloWorldRequest), ctx, state) =>
      ctx.thenPersist(HelloWorldReplacedEvent(replaceHelloWorldRequest)) { evt =>
        ctx.reply(Right(HelloWorldAggregate("3",replaceHelloWorldRequest.replacementHelloWorldResource)))
      }
  }
  private val notCreated = {
    getHelloWorldAction orElse {
    Actions()
      .onCommand[CreateHelloWorldCommand, Either[ServiceError, HelloWorldAggregate]] {
        case (CreateHelloWorldCommand(helloWorldAggregate), ctx, state) =>
          ctx.thenPersist(HelloWorldCreatedEvent(helloWorldAggregate)) { evt =>
            ctx.reply(Right(helloWorldAggregate))
          }
      }
    }
  }
  private def replyConflict[R]: OnCommandHandler[Either[ServiceError, R]] = {
    case (_, ctx, _) =>
      ctx.reply(Left(HelloWorldConflict))
      ctx.done
  }
  private def created(helloWorldAggregate: HelloWorldAggregate) = {
    getHelloWorldAction orElse {
    Actions()
//      .onCommand[DestroyHelloWorldCommand.type, Done] {
//        case (DestroyHelloWorldCommand, ctx, Some(u)) =>
//          ctx.thenPersist(HelloWorldDestroyedEvent(u.id))(_ => ctx.reply(Done))
//      }
//      .onCommand[ImproveHelloWorldDescripionCommand.type, Done] {
//        case (ImproveHelloWorldDescripionCommand, ctx, Some(u)) =>
//          ctx.thenPersist(HelloWorldDescripionImprovedEvent(improveHelloWorldDescripionRequest))(_ => ctx.reply(Done))
//      }
//      .onEvent {
//        case (HelloWorldDestroyedEvent(_), Some(u)) =>
//          None
//      }
//      .onEvent {
//        case (HelloWorldDescripionImprovedEvent(_), Some(u)) =>
//          None
//      }
    }
  }
  private def replyHelloWorldDoesNotExist[R]: OnCommandHandler[Either[ServiceError, R]] = {
    case (_, ctx, _) =>
      ctx.reply(Left(HelloWorldDoesNotExist))
      ctx.done
  }
}
```

<!--- transclude cjqnn31lm000b2iya3a9t563z -->

For CRUDy operations the following subordinate, nounified, resources are created:
*   Creation
*   Replacement
*   Mutation: [JSON Patch](http://jsonpatch.com/)
*   Deactivation
*   Reactivation

Creation
--------
A Creation request takes a desired HelloWorld algebraic data type and responds with the created HelloWorldResource plus the supporing algebraic data types of Identity and HypertextApplicationLanguage. If the Hello World resource is not created the service responses with an ErrorResponse. The following REST calls can be used. Identifiers are optional. If specified all identifiers must adhere to the Matcher for Id. Otherwise, the service will create and use a collision resistant unique identifier.

<!--- transclude::api/HelloWorldService.scala::[Hello World Creation Calls {] cjqnn31w9000c2iyajicry7j6 -->

```scala
// Hello World Creation Calls {
  /**
    * Rest api allowing an authenticated user to create a "Hello World" aggregate.
    *
    * @param  helloWorldId  Optional unique identifier of the "Hello World"
    *         creationId    Optional unique identifier of the creation subordinate resource
    *
    * @return HTTP 201 Created               if the "Hello World" was created successfully
    *         HTTP 202 Accepted              if the request has been accepted, but the processing is not complete
    *         HTTP 400 Bad Request           if domain validation of the [[CreateHelloWorldRequest]] failed
    *         HTTP 401 Unauthorized          if JSON Web Token is missing
    *         HTTP 403 Forbidden             if authorization failure
    *         HTTP 404 Not Found             if requested resource doesn't exist, or so as to not reveal a 401 or 403
    *         HTTP 409 Conflict              if the "Hello World" already exists with the same unique identity
    *         HTTP 413 Payload Too Large     if request size exceeds a defined limit
    *         HTTP 422 Unprocessable Entity  if the aggregate is not in the proper state to perform this action.
    *
    * REST POST endpoints:
    *   /api/hello-worlds
    *   /api/hello-worlds/:id
    *   /api/hello-worlds/creation
    *   /api/hello-worlds/:id/creation
    *   /api/hello-worlds/creation/:creationId
    *   /api/hello-worlds/:id/creation/:creationId
    *
    * Examples:
    * CT="Content-Type: application/json"
    * DATA='{"helloWorld": {"name": "test", "description": "test description"}}'
    * curl -H $CT -X POST -d $DATA http://localhost:9000/api/hello-worlds
    */
  def postHelloWorld1:                                             ServiceCall[CreateHelloWorldRequest, Either[ErrorResponse, CreateHelloWorldResponse]]
  def postHelloWorld2(helloWorldId: String):                       ServiceCall[CreateHelloWorldRequest, Either[ErrorResponse, CreateHelloWorldResponse]]
  def createHelloWorld1:                                           ServiceCall[CreateHelloWorldRequest, Either[ErrorResponse, CreateHelloWorldResponse]]
  def createHelloWorld2(helloWorldId: String):                     ServiceCall[CreateHelloWorldRequest, Either[ErrorResponse, CreateHelloWorldResponse]]
  def createHelloWorld3(creationId: String):                       ServiceCall[CreateHelloWorldRequest, Either[ErrorResponse, CreateHelloWorldResponse]]
  def createHelloWorld4(helloWorldId: String, creationId: String): ServiceCall[CreateHelloWorldRequest, Either[ErrorResponse, CreateHelloWorldResponse]]
  // Retrieve status of creation request
  def getCreationHelloWorld(helloWorldId: String, creationId: String):    ServiceCall[NotUsed, Either[ErrorResponse, CreationHelloWorldResponse]]
  def streamCreationHelloWorld(helloWorldId: String, creationId: String): ServiceCall[NotUsed, Source[CreationHelloWorldResponse, NotUsed]]
// }
```

<!--- transclude cjqnn31w9000c2iyajicry7j6 -->

The Matcher for identifiers is defined to be:

<!--- transclude::api/HelloWorldService.scala::[val Id = .*] cjqnn327d000d2iya4r7l3hix -->

```scala
  val Id = """^[a-zA-Z0-9\-\.\_\~]{1,64}$"""
```

<!--- transclude cjqnn327d000d2iya4r7l3hix -->

The create Hello World request is defined as:

<!--- transclude::api/HelloWorldService.scala::[Create Hello World Request payload {] cjqnn32it000e2iya6ldq5fka -->

```scala
// Create Hello World Request payload {
//type CreateHelloWorldRequest = String
//implicit val createHelloWorldRequestValidator
//    : Validator[CreateHelloWorldRequest] { r =>
//    r has size > 0
//    r has size <= maxRequestSize
//    }
case class ValidCreateHelloWorldRequest(
    helloWorld: HelloWorld
) {}
case object ValidCreateHelloWorldRequest {
  implicit val format: Format[ValidCreateHelloWorldRequest] = Jsonx.formatCaseClass
  implicit val validCreateHelloWorldRequestValidator
    : Validator[ValidCreateHelloWorldRequest] =
    validator[ValidCreateHelloWorldRequest] { createHelloWorldRequest =>
      createHelloWorldRequest.helloWorld is valid(HelloWorld.helloWorldValidator)
    }
}
// }
```

<!--- transclude cjqnn32it000e2iya6ldq5fka -->

And the create Hello World response is defined as:

<!--- transclude::api/HelloWorldService.scala::[case class CreateHelloWorldResponse(] cjqnn32ts000f2iya8p8rgfn7 -->

```scala
case class CreateHelloWorldResponse(
    helloWorldId: Identity,
    helloWorld: HelloWorld,
    helloWorldHal: Option[HypertextApplicationLanguage]
)
```

<!--- transclude cjqnn32ts000f2iya8p8rgfn7 -->

<!--- transclude::impl/HelloWorldServiceImpl.scala::[Hello World Creation Calls {] cjqnn334n000g2iyaab6m4ou5 -->

```scala
// Hello World Creation Calls {
  override def postHelloWorld
    : ServiceCall[CreateHelloWorldRequest, Either[ErrorResponse, CreateHelloWorldResponse]] =
    authenticated { (tokenContent, _) =>
      ServerServiceCall { (requestHeader, createHelloWorldRequest) =>
        val helloWorldId = Cuid.createCuid()
        val creationId = Cuid.createCuid()
        logger.info(
          s"Posting 'Hello World' with identifier $helloWorldId...")
        this
          .createHelloWorldInternal(helloWorldId, creationId)
          .handleRequestHeader(requestHeader => requestHeader)
          .invokeWithHeaders(requestHeader, createHelloWorldRequest).map {
            case (responseHeader, response) => (Ok, Right(response))
          }
      }
    }
  override def createHelloWorld1
    : ServiceCall[CreateHelloWorldRequest, Either[ErrorResponse, CreateHelloWorldResponse]] =
    authenticated { (tokenContent, _) =>
      ServerServiceCall { (requestHeader, createHelloWorldRequest) =>
        val helloWorldId = Cuid.createCuid()
        val creationId = Cuid.createCuid()
        this
          .createHelloWorldInternal(helloWorldId, creationId)
          .handleRequestHeader(requestHeader => requestHeader)
          .invokeWithHeaders(requestHeader, createHelloWorldRequest).map {
            case (responseHeader, response) => (Ok, Right(response))
          }
      }
    }
  override def createHelloWorld2(helloWorldId: String)
    : ServiceCall[CreateHelloWorldRequest, Either[ErrorResponse, CreateHelloWorldResponse]] =
    authenticated { (tokenContent, _) =>
      ServerServiceCall { (requestHeader, createHelloWorldRequest) =>
        val creationId = Cuid.createCuid()
        this
          .createHelloWorldInternal(helloWorldId, creationId)
          .handleRequestHeader(requestHeader => requestHeader)
          .invokeWithHeaders(requestHeader, createHelloWorldRequest).map {
            case (responseHeader, response) => (Ok, Right(response))
          }
      }
    }
  override def createHelloWorld3(creationId: String)
    : ServiceCall[CreateHelloWorldRequest, Either[ErrorResponse, CreateHelloWorldResponse]] =
    authenticated { (tokenContent, _) =>
      ServerServiceCall { (requestHeader, createHelloWorldRequest) =>
        val helloWorldId = Cuid.createCuid()
        this
          .createHelloWorldInternal(helloWorldId, creationId)
          .handleRequestHeader(requestHeader => requestHeader)
          .invokeWithHeaders(requestHeader, createHelloWorldRequest).map {
            case (responseHeader, response) => (Ok, Right(response))
          }
      }
    }
  override def createHelloWorld4(helloWorldId: String, creationId: String)
    : ServiceCall[CreateHelloWorldRequest, Either[ErrorResponse, CreateHelloWorldResponse]] =
    authenticated { (tokenContent, _) =>
      ServerServiceCall { (requestHeader, createHelloWorldRequest) =>
        this
          .createHelloWorldInternal(helloWorldId, creationId)
          .handleRequestHeader(requestHeader => requestHeader)
          .invokeWithHeaders(requestHeader, createHelloWorldRequest).map {
            case (responseHeader, response) => (Ok, Right(response))
          }
      }
    }
  def createHelloWorldInternal(helloWorldId: String, creationId: String)
    : ServerServiceCall[CreateHelloWorldRequest, CreateHelloWorldResponse] =
    authenticated { (tokenContent, _) =>
      ServerServiceCall { createHelloWorldRequest =>
        val username = tokenContent.username
        logger.info(s"User $username is creating a Hello World ")
        logger.info(
          s"Creating 'Hello World' with input $createHelloWorldRequest...")
        val validationResult = validate(createHelloWorldRequest)
        validationResult match {
          case failure: Failure =>
            throw new TransportException(TransportErrorCode.BadRequest,
                                         "request validation failure")
          case _ =>
        }
        val helloWorldAggregate =
          HelloWorldAggregate(helloWorldId, HelloWorldResource(createHelloWorldRequest.helloWorld))
        val helloWorldResource =
          HelloWorldResource(createHelloWorldRequest.helloWorld)
        val helloWorldEntityRef =
          registry.refFor[HelloWorldEntity](helloWorldId.toString)
        logger.info(s"Publishing event $helloWorldAggregate")
        val topic = pubSubRegistry.refFor(TopicId[HelloWorldResource])
        topic.publish(helloWorldResource)
        helloWorldEntityRef
          .ask(CreateHelloWorldCommand(helloWorldAggregate))
          .map { _ =>
            mapToCreateHelloWorldResponse(helloWorldId, helloWorldResource)
          }
      }
    }
  private def mapToCreateHelloWorldResponse(
      helloWorldId: String,
      helloWorldResource: HelloWorldResource): CreateHelloWorldResponse = {
    CreateHelloWorldResponse(helloWorldId,
                             helloWorldResource.helloWorld)
  }
  private def mapToCreateHelloWorldResponse(
      helloWorldState: HelloWorldState): CreateHelloWorldResponse = {
    CreateHelloWorldResponse(helloWorldState.helloWorldAggregate map { _.helloWorldId } getOrElse "No identifier",
                             helloWorldState.helloWorldAggregate map { _.helloWorldResource.helloWorld} getOrElse HelloWorld("No name", Some("No description")))
  }
// }
```

<!--- transclude cjqnn334n000g2iyaab6m4ou5 -->

<!--- transclude::impl/HelloWorldServiceImpl.scala::[The create Hello World command {] cjqnn33f8000h2iya90ekcdlv -->

```scala
// The create Hello World command {
case class CreateHelloWorldCommand(
  helloWorldAggregate: HelloWorldAggregate)
    extends HelloWorldCommand[Either[ServiceError, CreateHelloWorldReply]]
object CreateHelloWorldCommand {
  implicit val format: Format[CreateHelloWorldCommand] = Json.format
}
// }
```

<!--- transclude cjqnn33f8000h2iya90ekcdlv -->

<!--- transclude::impl/HelloWorldServiceImpl.scala::[The create Hello World reply {] cjqnn33pv000i2iya4hy7he4k -->

```scala
// The create Hello World reply {
case class CreateHelloWorldReply(
  helloWorldAggregate: HelloWorldAggregate)
object CreateHelloWorldReply {
  implicit val format: Format[CreateHelloWorldReply] = Json.format
}
// }
```

<!--- transclude cjqnn33pv000i2iya4hy7he4k -->

<!--- transclude::impl/HelloWorldServiceImpl.scala::[private def createHelloWorldCommand: OnCommandHandler[Either[ServiceError, HelloWorldAggregate]] = {] cjqnn3415000j2iyap03suly6 -->

```scala
```

<!--- transclude cjqnn3415000j2iyap03suly6 -->

<!--- transclude::impl/HelloWorldServiceImpl.scala::[private def createHelloWorldCommand: OnCommandHandler[Either[ServiceError, HelloWorldAggregate]] = {] cjqnn34du000k2iyaa8yj9079 -->

```scala
```

<!--- transclude cjqnn34du000k2iyaa8yj9079 -->

Replacement
-----------
A Replacement request takes the new desired HelloWorld algebraic data type and responds with the replaced HelloWorldResource plus the supporing algebraic data types of Identity and HypertextApplicationLanguage. If the Hello World resource is not replaced the service responses with an ErrorResponse. The following REST calls can be used. The Hello World identifier is required, but the replacementId is optional. If specified all identifiers must adhere to the Matcher for Id.

<!--- transclude::api/HelloWorldService.scala::[Hello World Replacement Calls {] cjqnn34nu000l2iyasm6ucn7y -->

```scala
// Hello World Replacement Calls {
  /**
    * Rest api allowing an authenticated user to replace a "Hello World".
    *
    * @param  helloWorldId   The unique identifier of the "Hello World"
    *         replacementId  Optional unique identifier of the replacement subordinate resource
    *
    * @return HTTP 200 OK                    if the "Hello World" was replaced successfully
    *         HTTP 202 Accepted              if the request has been accepted, but the processing is not complete
    *         HTTP 400 Bad Request           if domain validation of the [[ReplaceHelloWorldRequest]] failed
    *         HTTP 401 Unauthorized          if JSON Web Token is missing
    *         HTTP 403 Forbidden             if authorization failure (use 404 if authz failure shouldn't be revealed)
    *         HTTP 404 Not Found             if requested resource doesn't exist, or so as to not reveal a 401 or 403
    *         HTTP 413 Payload Too Large     if request size exceeds a defined limit
    *         HTTP 422 Unprocessable Entity  if the aggregate is not in the proper state to perform this action
    *
    * REST PUT endpoint:
    *   /api/hello-worlds/:id
    * REST POST endpoints:
    *   /api/hello-worlds/:id/mutation
    *   /api/hello-worlds/:id/mutation/:mutationId
    *
    * Example:
    * CT="Content-Type: application/json"
    * DATA='{"helloWorld": {"name": "test", "description": "different description"}}'
    * curl -H $CT -X PUT -d $DATA http://localhost:9000/api/hello-worlds/cjq5au9sr000caqyayo9uktss
    */
  def putHelloWorld(helloWorldId: String):                             ServiceCall[ReplaceHelloWorldRequest, Either[ErrorResponse, ReplaceHelloWorldResponse]]
  def replaceHelloWorld1(helloWorldId: String):                        ServiceCall[ReplaceHelloWorldRequest, Either[ErrorResponse, ReplaceHelloWorldResponse]]
  def replaceHelloWorld2(helloWorldId: String, replacementId: String): ServiceCall[ReplaceHelloWorldRequest, Either[ErrorResponse, ReplaceHelloWorldResponse]]
  // Retrieve status of replacement request
  def getReplacementHelloWorld(helloWorldId: String, replacementId: String):    ServiceCall[NotUsed, Either[ErrorResponse, ReplacementHelloWorldResponse]]
  def streamReplacementHelloWorld(helloWorldId: String, replacementId: String): ServiceCall[NotUsed, Source[ReplacementHelloWorldResponse, NotUsed]]
// }
```

<!--- transclude cjqnn34nu000l2iyasm6ucn7y -->

The replace Hello World request is defined as:

<!--- transclude::api/HelloWorldService.scala::[case class ReplaceHelloWorldRequest(] cjqnn34y0000m2iya0nihiwwv -->

```scala
case class ReplaceHelloWorldRequest(
    replacementHelloWorld: HelloWorld,
    motivation: Option[String]
)
```

<!--- transclude cjqnn34y0000m2iya0nihiwwv -->

And the replace Hello World response is defined as:

<!--- transclude::api/HelloWorldService.scala::[case class ReplaceHelloWorldResponse(] cjqnn3594000n2iya7gm27hyw -->

```scala
case class ReplaceHelloWorldResponse(
    helloWorldId: Identity,
    helloWorld: HelloWorld,
    helloWorldHal: Option[HypertextApplicationLanguage]
)
```

<!--- transclude cjqnn3594000n2iya7gm27hyw -->

Mutation
--------

<!--- transclude::api/HelloWorldService.scala::[Hello World Mutation Calls {] cjqnn35ke000o2iyai8nwyybp -->

```scala
// Hello World Mutation Calls {
  /**
    * Rest api allowing an authenticated user to mutate a "Hello World".
    *
    * @param  helloWorldId  The unique identifier of the "Hello World"
    *         mutationId    Optional unique identifier of the mutation subordinate resource
    *
    * @return HTTP 200 OK                    if the "Hello World" was mutated successfully
    *         HTTP 202 Accepted              if the request has been accepted, but the processing is not complete
    *         HTTP 400 Bad Request           if domain validation of the [[MutateHelloWorldRequest]] failed
    *         HTTP 401 Unauthorized          if JSON Web Token is missing
    *         HTTP 403 Forbidden             if authorization failure (use 404 if authz failure shouldn't be revealed)
    *         HTTP 404 Not Found             if requested resource doesn't exist, or so as to not reveal a 401 or 403
    *         HTTP 413 Payload Too Large     if request size exceeds a defined limit
    *         HTTP 422 Unprocessable Entity  if the aggregate is not in the proper state to perform this action
    *
    * REST PATCH endpoint:
    *   /api/hello-worlds/:id
    * REST POST endpoints:
    *   /api/hello-worlds/:id/replacement
    *   /api/hello-worlds/:id/replacement/:replacementId
    *
    * Example:
    * CT="Content-Type: application/json"
    * DATA='[{"op": "replace", "path": "/name", "value": "new name"}]'
    * curl -H $CT -X PATCH -d $DATA http://localhost:9000/api/hello-worlds/cjq5au9sr000caqyayo9uktss
    */
  def patchHelloWorld(helloWorldId: String):                       ServiceCall[MutateHelloWorldRequest, Either[ErrorResponse, MutateHelloWorldResponse]]
  def mutateHelloWorld1(helloWorldId: String):                     ServiceCall[MutateHelloWorldRequest, Either[ErrorResponse, MutateHelloWorldResponse]]
  def mutateHelloWorld2(helloWorldId: String, mutationId: String): ServiceCall[MutateHelloWorldRequest, Either[ErrorResponse, MutateHelloWorldResponse]]
  // Retrieve status of mutation request
  def getMutationHelloWorld(helloWorldId: String, mutationId: String):    ServiceCall[NotUsed, Either[ErrorResponse, MutationHelloWorldResponse]]
  def streamMutationHelloWorld(helloWorldId: String, mutationId: String): ServiceCall[NotUsed, Source[MutationHelloWorldResponse, NotUsed]]
// }
```

<!--- transclude cjqnn35ke000o2iyai8nwyybp -->

Deactivation
------------

<!--- transclude::api/HelloWorldService.scala::[Hello World Deactivation Calls {] cjqnn35vh000p2iyalkhte7xz -->

```scala
// Hello World Deactivation Calls {
  /**
    * Rest api allowing an authenticated user to deactivate a "Hello World".
    *
    * @param  helloWorldId    The unique identifier of the "Hello World"
    *         deactivationId  Optional unique identifier of the deactivation subordinate resource
    *
    * @return HTTP 200 OK                    if the "Hello World" was deactivated successfully
    *         HTTP 202 Accepted              if the request has been accepted, but the processing is not complete
    *         HTTP 400 Bad Request           if domain validation of the [[DeactivateHelloWorldRequest]] failed
    *         HTTP 401 Unauthorized          if JSON Web Token is missing
    *         HTTP 403 Forbidden             if authorization failure (use 404 if authz failure shouldn't be revealed)
    *         HTTP 404 Not Found             if requested resource doesn't exist, or so as to not reveal a 401 or 403
    *         HTTP 413 Payload Too Large     if request size exceeds a defined limit
    *         HTTP 422 Unprocessable Entity  if the aggregate is not in the proper state to perform this action
    *
    * REST DELETE endpoint:
    *   /api/hello-worlds/:id
    * REST POST endpoints:
    *   /api/hello-worlds/:id/deactivation
    *   /api/hello-worlds/:id/deactivation/:deactivationId
    *
    * Example:
    * CT="Content-Type: application/json"
    * curl -H $CT -X DELETE http://localhost:9000/api/hello-worlds/cjq5au9sr000caqyayo9uktss
    */
  def patchHelloWorld(helloWorldId: String):                               ServiceCall[DeactivateHelloWorldRequest, Either[ErrorResponse, DeactivateHelloWorldResponse]]
  def deactivateHelloWorld1(helloWorldId: String):                         ServiceCall[DeactivateHelloWorldRequest, Either[ErrorResponse, DeactivateHelloWorldResponse]]
  def deactivateHelloWorld2(helloWorldId: String, deactivationId: String): ServiceCall[DeactivateHelloWorldRequest, Either[ErrorResponse, DeactivateHelloWorldResponse]]
  // Retrieve status of deactivation request
  def getDeactivationHelloWorld(helloWorldId: String, deactivationId: String):    ServiceCall[NotUsed, Either[ErrorResponse, DeactivationHelloWorldResponse]]
  def streamDeactivationHelloWorld(helloWorldId: String, deactivationId: String): ServiceCall[NotUsed, Source[DeactivationHelloWorldResponse, NotUsed]]
// }
```

<!--- transclude cjqnn35vh000p2iyalkhte7xz -->

Reactivation
------------

<!--- transclude::api/HelloWorldService.scala::[Hello World Reactivation Calls {] cjqnn365w000q2iyaupxakres -->

```scala
// Hello World Reactivation Calls {
  /**
    * Rest api allowing an authenticated user to reactivate a "Hello World".
    *
    * @param  helloWorldId    The unique identifier of the "Hello World"
    *         reactivationId  Optional unique identifier of the reactivation subordinate resource
    *
    * @return HTTP 200 OK                    if the "Hello World" was reactivated successfully
    *         HTTP 202 Accepted              if the request has been accepted, but the processing is not complete
    *         HTTP 400 Bad Request           if domain validation of the [[ReactivateHelloWorldRequest]] failed
    *         HTTP 401 Unauthorized          if JSON Web Token is missing
    *         HTTP 403 Forbidden             if authorization failure (use 404 if authz failure shouldn't be revealed)
    *         HTTP 404 Not Found             if requested resource doesn't exist, or so as to not reveal a 401 or 403
    *         HTTP 413 Payload Too Large     if request size exceeds a defined limit
    *         HTTP 422 Unprocessable Entity  if the aggregate is not in the proper state to perform this action
    *
    * REST POST endpoints:
    *   /api/hello-worlds/:id/reactivation
    *   /api/hello-worlds/:id/reactivation/:reactivationId
    *
    * Example:
    * CT="Content-Type: application/json"
    * curl -H $CT -X POST http://localhost:9000/api/hello-worlds/cjq5au9sr000caqyayo9uktss/reactivation
    */
  def patchHelloWorld(helloWorldId: String):                               ServiceCall[ReactivateHelloWorldRequest, Either[ErrorResponse, ReactivateHelloWorldResponse]]
  def reactivateHelloWorld1(helloWorldId: String):                         ServiceCall[ReactivateHelloWorldRequest, Either[ErrorResponse, ReactivateHelloWorldResponse]]
  def reactivateHelloWorld2(helloWorldId: String, reactivationId: String): ServiceCall[ReactivateHelloWorldRequest, Either[ErrorResponse, ReactivateHelloWorldResponse]]
  // Retrieve status of reactivation request
  def getReactivationHelloWorld(helloWorldId: String, reactivationId: String):    ServiceCall[NotUsed, Either[ErrorResponse, ReactivationHelloWorldResponse]]
  def streamReactivationHelloWorld(helloWorldId: String, reactivationId: String): ServiceCall[NotUsed, Source[ReactivationHelloWorldResponse, NotUsed]]
// }
```

<!--- transclude cjqnn365w000q2iyaupxakres -->

Read
----

<!--- transclude::api/HelloWorldService.scala::[Hello World Get Calls {] cjqnn36ft000r2iya148g8due -->

```scala
// Hello World Get Calls {
  /**
    * Rest api allowing an authenticated user to get a "Hello World" with the given surrogate key.
    *
    * @param helloWorldId    The unique identifier of the "Hello World"
    *
    * @return HTTP 200 OK                    if the "Hello World" was retrieved successfully
    *
    * Example:
    * CT="Content-Type: application/json"
    * curl -H $CT http://localhost:9000/api/hello-worlds/cjq5au9sr000caqyayo9uktss
    */
  def getHelloWorld(helloWorldId: String): ServiceCall[NotUsed, Either[ErrorResponse, GetHelloWorldResponse]]
  /**
    * Get all "Hello Worlds".
    *
    * @return A list of "Hello World" resources.
    *
    * Example:
    * curl http://localhost:9000/api/hello-worlds
    */
  def getAllHelloWorlds(page: Option[String]): ServiceCall[NotUsed, utils.PagingState[GetAllHelloWorldsResponse]]
  def getAllHelloWorlds:                       ServiceCall[NotUsed, GetAllHelloWorldsResponse]
// }
```

<!--- transclude cjqnn36ft000r2iya148g8due -->

```bash
-- With Bearer Auth Token
export AT=`./get-auth-token.sh`
sed 's/\r//'  hello-worlds.csv | perl -MText::CSV -MJSON::MaybeXS=encode_json -lne '$c=Text::CSV->new;$c->parse($_);@C=$c->fields if $.==1;@F=$c->fields;@L{@C}=@F;$J{helloWorld}=\%L;$l=encode_json \%J;`curl --show-error --header \"Authorization: Bearer ${'AT'}\" -H \"Content-Type: application/json\" -X POST -d \047$l\047 http://localhost:9000/api/hello-worlds/$F[0]/create-hello-world`unless $.==1;'
```

For REST calls with DDD/CQRS/ES only use GET and POST
GET for queries
  pagination and expand for large resources
 POST for commands
   "Use POST APIs to create new subordinate resources" [HTTP Methods](https://restfulapi.net/http-methods/)
   A DDD command can be thought of as a subordinate resource to the DDD aggregate entity
   The command "could" have an identity and be queryable, for instance an async req/resp.
   A Saga needs to be implemented in this manner
   Command body should include a unique identifier, can be a span id

Distributed tracing
-------------------

[Zipkin](https://zipkin.io/pages/instrumenting.html)

Annotation

An Annotation is used to record an occurrence in time. There’s a set of core annotations used to define the beginning and end of an RPC request:

cs - Client Send. The client has made the request. This sets the beginning of the span.
sr - Server Receive: The server has received the request and will start processing it. The difference between this and cs will be combination of network latency and clock jitter.
ss - Server Send: The server has completed processing and has sent the request back to the client. The difference between this and sr will be the amount of time it took the server to process the request.
cr - Client Receive: The client has received the response from the server. This sets the end of the span. The RPC is considered complete when this annotation is recorded.
When using message brokers instead of RPCs, the following annotations help clarify the direction of the flow:

ms - Message Send: The producer sends a message to a broker.
mr - Message Receive: A consumer received a message from a broker.
Unlike RPC, messaging spans never share a span ID. For example, each consumer of a message is a different child span of the producing span.

Trace Id

The overall 64 or 128-bit ID of the trace. Every span in a trace shares this ID.

Span Id

The ID for a particular span. This may or may not be the same as the trace id.

Parent Id

This is an optional ID that will only be present on child spans. That is the span without a parent id is considered the root of the trace.

HTTP Tracing

HTTP headers are used to pass along trace information.

The B3 portion of the header is so named for the original name of Zipkin: BigBrotherBird.

Ids are encoded as hex strings:

X-B3-TraceId: 128 or 64 lower-hex encoded bits (required)
X-B3-SpanId: 64 lower-hex encoded bits (required)
X-B3-ParentSpanId: 64 lower-hex encoded bits (absent on root span)
X-B3-Sampled: Boolean (either “1” or “0”, can be absent)
X-B3-Flags: “1” means debug (can be absent)
For more information on B3, please see its specification.

Timestamps are microseconds

[b3-propagation](https://github.com/openzipkin/b3-propagation)

Single Header
A single header named b3 standardized in late 2018 for use in JMS and w3c tracestate. Design and rationale are captured here. Check or update our support page for adoption status.

In simplest terms b3 maps propagation fields into a hyphen delimited string.

b3={TraceId}-{SpanId}-{SamplingState}-{ParentSpanId}, where the last two fields are optional.

For example, the following state encoded in multiple headers:

X-B3-TraceId: 80f198ee56343ba864fe8b2a57d3eff7
X-B3-ParentSpanId: 05e3ac9a4f6e3b90
X-B3-SpanId: e457b5a2e4d86bd1
X-B3-Sampled: 1
Becomes one b3 header, for example:

b3: 80f198ee56343ba864fe8b2a57d3eff7-e457b5a2e4d86bd1-1-05e3ac9a4f6e3b90
[play-zipkin-tracing](https://github.com/bizreach/play-zipkin-tracing)

[OpenTracing for the Lagom Framework](https://github.com/deltaprojects/lagom-opentracing)

libraryDependencies += "com.deltaprojects" % "lagom-opentracing_2.12" % "0.2.3"

JWT
----

*   User gets authenticated and gets a ticket of some type (JWT, Kerberos, ...)
*   User wants to use your Self-Contained System (SCS) and exchanges any token that verifies their identity with a JWT for your SCS that adds the roles they have withing your SCS.
*   The JWT has a

Versioning
-----------

Semantic versioning: [Semantic Versioning 2.0.0](https://semver.org/)
1. MAJOR version when you make incompatible API changes,
2. MINOR version when you add functionality in a backwards-compatible manner, and
3. PATCH version when you make backwards-compatible bug fixes.

RESTful versioning:
1. MAJOR: The resource has changed so much it is considered a different resource, thus it has a different URI. Version 1 of `http://myservice/helloworld/123` becomes  `http://example.com/helloworld/v2/123` in version 2.
2. MINOR: Http Accept header encodes minor version number. Requesting major version 1 and minor version 0 of a resource should have an "Accept" header of "application/vnd.helloworld.hal+json;version=1.0" and use URI http://myservice/helloworld/123. Requesting major version 3 and minor version 2 of a resource should have an "Accept" header of "application/vnd.helloworld.hal+json;version=3.2" and use URI `http://myservice/helloworld/v3/123`. A mismatch between the major version in the header and in the URI will return an error.
3. PATCH: Requests will not specify patch version, but the full semantic version information is avaliable for every request.

Deprecation: A request to a prior version that is still supported will include information on what the latest version is and when the requested version will be decomissioned.

HATEOAS
-------

[How a RESTful API represents resources](https://www.oreilly.com/ideas/how-a-restful-api-represents-resources)
[HAL Primer](https://phlyrestfully.readthedocs.io/en/latest/halprimer.html)
[Getting started with JSON Hyper-Schema](https://blog.apisyouwonthate.com/getting-started-with-json-hyper-schema-184775b91f)
[Getting started with JSON Hyper-Schema: Part 2](https://blog.apisyouwonthate.com/getting-started-with-json-hyper-schema-part-2-ca9d7ffdf6f6)

Hypermedia Application Language (HAL)

```json
{"_links":{"self":{"href":"/"},"curies": [...]
```
```yaml
_embedded:
_links:
  self:
    href:
  first:
  prev:
  next:
  last:
  # DDD commands allowed
  archive:
    href: ""
    targetMediaType: "vnd.helloworld.hal+json;version=3.2"
    targetSchema: { "$ref": "#"} # point to the root document or {"$ref": "https://schemas.dashron.com/users"}
    methods: [POST]
```
targetSchema - request body is identical to the target representation
submissionSchema - request body does not match the target representation
headerSchema -
```yaml
    targetSchema:
      id:
        type: String
      name:
        type: String
  # Curies for templating hrefs
  curies: [{
    name:
    href:
    templated: true
  }],
```
```json
{
  "_links": {
    "self": { "href": "/orders" },
    "curies": [{
      "name": "acme",
      "href": "http://docs.acme.com/relations/{rel}",
      "templated": true
    }],
    "acme:widgets": { "href": "/widgets" }
  }
}
```
Need links to stable self that includes transactionClock in identity as well as current.
`http://myservice/helloworld/123/c12341895a3`
[JSON Hyper-Schema: A Vocabulary for Hypermedia Annotation of JSON](https://json-schema.org/latest/json-schema-hypermedia.html)
[JSON Schema](http://json-schema.org/)
JSON Hyper-Schema document
```json
{
    "type": "object",
    "properties": {
        "id": {
            "type": "number"
        },
        "title": {
            "type": "string"
        },
        "urlSlug": {
            "type": "string"
        },
        "post": {
            "type": "string"
        }
    },
    "required": ["id"],
    "base": "http://api.dashron.com/",
    "links": [{
        "rel": "self",
        "href": "posts/{id}",
        "templateRequired": ["id"]
    }]
}
```
Example application that uses schema on the client side: A React component for building Web forms from JSON Schema.
[react-jsonschema-form](https://github.com/mozilla-services/react-jsonschema-form)
[DDD & REST — Domain-Driven APIs for the web](https://speakerdeck.com/olivergierke/ddd-and-rest-domain-driven-apis-for-the-web?slide=42)
ETags
Aggregate Root -> Collection/Item Resource
Relations -> Links
IDs -> URIs
@Version -> ETags
Last Modified Property -> Last Modified Header
According to the HTTP/1.1 Spec:
The POST method is used to request that the origin server accept the entity enclosed in the request as a new subordinate of the resource identified by the Request-URI in the Request-Line
In other words, POST is used to create.
The PUT method requests that the enclosed entity be stored under the supplied Request-URI. If the Request-URI refers to an already existing resource, the enclosed entity SHOULD be considered as a modified version of the one residing on the origin server. If the Request-URI does not point to an existing resource, and that URI is capable of being defined as a new resource by the requesting user agent, the origin server can create the resource with that URI."
That is, PUT is used to create or update.
So, which one should be used to create a resource? Or one needs to support both?
[REST and DDD: incompatible?](http://dontpanic.42.nl/2012/04/rest-and-ddd-incompatible.html)
Representational State Transfer is a software architectural style that defines a set of constraints to be used for creating web services. Web services that conform to the REST architectural style, termed RESTful web services, provide interoperability between computer systems on the Internet. Wikipedia
### License
MIT
### Author Information
| Author                | E-mail                        |
|-----------------------|-------------------------------|
|  |   |