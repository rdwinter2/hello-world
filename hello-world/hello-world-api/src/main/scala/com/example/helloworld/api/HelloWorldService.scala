package com.example.helloworld.api

//import com.example.common.regex.Matchers
import com.example.common.response.ErrorResponse
import com.example.common.utils.JsonFormats._
import com.example.common.validation.ValidationViolationKeys._

import ai.x.play.json.Jsonx
import akka.{Done, NotUsed}
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{
  KafkaProperties,
  PartitionKeyStrategy
}
import com.lightbend.lagom.scaladsl.api.deser.{
  DefaultExceptionSerializer,
  PathParamSerializer
}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import com.wix.accord.Validator
import com.wix.accord.dsl._
import com.wix.accord.Descriptions._
import java.time.{Duration, Instant}
import java.util.UUID
import julienrf.json.derived
import play.api.{Environment, Mode}
import play.api.libs.json._

//object HelloWorldService  {
//  val TOPIC_NAME = "agg.event.hello_world"
//}

/**
  * The Hello World service interface.
  * <p>
  * This describes everything that Lagom needs to know about how to serve and
  * consume the HelloWorldService.
  */
trait HelloWorldService extends Service {

  override final def descriptor = {
    import Service._
    // @formatter:off
    named("hello-world").withCalls(
      // Hello World Queries
      restCall(Method.GET,    "/api/hello-worlds/:id", getHelloWorld _),
      //restCall(Method.GET,    "/api/hello-worlds",     getAllHelloWorlds _),
      // CRUDy Bulk Data Administration
      //restCall(Method.POST,   "/api/hello-worlds/data-administration/bulk-creation",        bulkCreateHelloWorld _),
      //restCall(Method.POST,   "/api/hello-worlds/data-administration/bulk-replacement",     bulkReplaceHelloWorld _),
      //restCall(Method.POST,   "/api/hello-worlds/data-administration/bulk-mutation",        bulkMutateHelloWorld _),
      //restCall(Method.POST,   "/api/hello-worlds/data-administration/bulk-deactivation",    bulkDeactivateHelloWorld _),
      //restCall(Method.POST,   "/api/hello-worlds/data-administration/bulk-reactivation",    bulkReactivateHelloWorld _),
      //restCall(Method.POST,   "/api/hello-worlds/data-administration/bulk-distruction",     bulkDistroyHelloWorld _),
      // CRUDy Bulk Data Administration Queries
      //restCall(Method.GET,    "/api/hello-worlds/data-administration/bulk-creation/:id",        getHelloWorldBulkCreation _),
      //restCall(Method.GET,    "/api/hello-worlds/data-administration/bulk-replacement/:id",     getHelloWorldBulkReplacement _),
      //restCall(Method.GET,    "/api/hello-worlds/data-administration/bulk-mutation/:id",        getHelloWorldBulkMutation _),
      //restCall(Method.GET,    "/api/hello-worlds/data-administration/bulk-deactivation/:id",    getHelloWorldBulkDeactivation _),
      //restCall(Method.GET,    "/api/hello-worlds/data-administration/bulk-reactivation/:id",    getHelloWorldBulkReactivation _),
      //restCall(Method.GET,    "/api/hello-worlds/data-administration/bulk-distruction/:id",     getHelloWorldBulkDistruction _),
      // CRUDy plain REST
      //restCall(Method.POST,   "/api/hello-worlds",     postHelloWorld1 _),
      //restCall(Method.POST,   "/api/hello-worlds/:id", postHelloWorld2 _),
      //restCall(Method.PUT,    "/api/hello-worlds/:id", putHelloWorld _),
      //restCall(Method.PATCH,  "/api/hello-worlds/:id", patchHelloWorld _),
      //restCall(Method.DELETE, "/api/hello-worlds/:id", deleteHelloWorld _),
      //restCall(Method.GET,    "/api/hello-worlds/:id", getHelloWorld _),
      //restCall(Method.GET,    "/api/hello-worlds",     getAllHelloWorlds _),
      // Data Administrator bulk data hammer interface
      // request body is an array of Create, Update, Delete (CUD) operations each containing an array
      // Example:
      // {"dataAdminActions": [{"create":[{"id":..,"name":..,"description":..},{..},..]},{"delete":[..]},..]}
      // NOTE: for update you just need to supply the id and the changed fields
      // Service will respond with a 202 Accepted and a link to check the status
      //}
      //restCall(Method.POST,   "/api/hello-worlds/data-administration",                   administerCe _),
      // CRUDy DDDified REST without a proper ubiquitious language
      // Create
      //restCall(Method.POST, "/api/hello-worlds/creation",                                createHelloWorld1 _),
      //restCall(Method.POST, "/api/hello-worlds/:id/creation",                            createHelloWorld2 _),
      //restCall(Method.POST, "/api/hello-worlds/creation/:creationId",                    createHelloWorld3 _),
      //restCall(Method.POST, "/api/hello-worlds/:id/creation/:creationId",                createHelloWorld4 _),
      //restCall(Method.GET,  "/api/hello-worlds/:id/creation/:creationId",                getCreationHelloWorld _),
      //pathCall(             "/api/hello-worlds/:id/creation/:creationId/stream",         streamCreationHelloWorld _),
      // Read
      // Update
      //restCall(Method.POST, "/api/hello-worlds/:id/replacement",                         replaceHelloWorld1 _),
      //restCall(Method.POST, "/api/hello-worlds/:id/replacement/:replacementId",          replaceHelloWorld2 _),
      //restCall(Method.GET,  "/api/hello-worlds/:id/replacement/:replacementId",          getReplacementHelloWorld _),
      //pathCall(             "/api/hello-worlds/:id/replacement/:replacementId/stream",   streamReplacementHelloWorld _),
      //restCall(Method.POST, "/api/hello-worlds/:id/mutation",                            mutateHelloWorld1 _),
      //restCall(Method.POST, "/api/hello-worlds/:id/mutation/:mutationId",                mutateHelloWorld2 _),
      //restCall(Method.GET,  "/api/hello-worlds/:id/mutation/:mutationId",                getMutationHelloWorld _),
      //pathCall(             "/api/hello-worlds/:id/mutation/:mutationId/stream",         streamMutationHelloWorld _),
      // Delete
      //restCall(Method.POST, "/api/hello-worlds/:id/deactivation",                        deactivateHelloWorld1 _),
      //restCall(Method.POST, "/api/hello-worlds/:id/deactivation/:deactivationId",        deactivateHelloWorld2 _),
      //restCall(Method.GET,  "/api/hello-worlds/:id/deactivation/:deactivationId",        getDeactivationHelloWorld _),
      //pathCall(             "/api/hello-worlds/:id/deactivation/:deactivationId/stream", streamDeactivationHelloWorld _),
      // Undelete
      //restCall(Method.POST, "/api/hello-worlds/:id/reactivation",                        reactivateHelloWorld1 _),
      //restCall(Method.POST, "/api/hello-worlds/:id/reactivation/:reactivationId",        reactivateHelloWorld2 _),
      //restCall(Method.GET,  "/api/hello-worlds/:id/reactivation/:reactivationId",        getReactivationHelloWorld _),
      //pathCall(             "/api/hello-worlds/:id/reactivation/:reactivationId/stream", streamReactivationHelloWorld _),
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

// Hello World Service Calls

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
  //def postHelloWorld1:                                             ServiceCall[CreateHelloWorldRequest, Either[ErrorResponse, CreateHelloWorldResponse]]
  //def postHelloWorld2(helloWorldId: String):                       ServiceCall[CreateHelloWorldRequest, Either[ErrorResponse, CreateHelloWorldResponse]]
  //def createHelloWorld1:                                           ServiceCall[CreateHelloWorldRequest, Either[ErrorResponse, CreateHelloWorldResponse]]
  //def createHelloWorld2(helloWorldId: String):                     ServiceCall[CreateHelloWorldRequest, Either[ErrorResponse, CreateHelloWorldResponse]]
  //def createHelloWorld3(creationId: String):                       ServiceCall[CreateHelloWorldRequest, Either[ErrorResponse, CreateHelloWorldResponse]]
  //def createHelloWorld4(helloWorldId: String, creationId: String): ServiceCall[CreateHelloWorldRequest, Either[ErrorResponse, CreateHelloWorldResponse]]
  // Retrieve status of creation request
  //def getCreationHelloWorld(helloWorldId: String, creationId: String):    ServiceCall[NotUsed, Either[ErrorResponse, CreationHelloWorldResponse]]
  //def streamCreationHelloWorld(helloWorldId: String, creationId: String): ServiceCall[NotUsed, Source[CreationHelloWorldResponse, NotUsed]]
// }

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
  //def putHelloWorld(helloWorldId: String):                             ServiceCall[ReplaceHelloWorldRequest, Either[ErrorResponse, ReplaceHelloWorldResponse]]
  //def replaceHelloWorld1(helloWorldId: String):                        ServiceCall[ReplaceHelloWorldRequest, Either[ErrorResponse, ReplaceHelloWorldResponse]]
  //def replaceHelloWorld2(helloWorldId: String, replacementId: String): ServiceCall[ReplaceHelloWorldRequest, Either[ErrorResponse, ReplaceHelloWorldResponse]]
  // Retrieve status of replacement request
  //def getReplacementHelloWorld(helloWorldId: String, replacementId: String):    ServiceCall[NotUsed, Either[ErrorResponse, ReplacementHelloWorldResponse]]
  //def streamReplacementHelloWorld(helloWorldId: String, replacementId: String): ServiceCall[NotUsed, Source[ReplacementHelloWorldResponse, NotUsed]]
// }

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
  //def patchHelloWorld(helloWorldId: String):                       ServiceCall[MutateHelloWorldRequest, Either[ErrorResponse, MutateHelloWorldResponse]]
  //def mutateHelloWorld1(helloWorldId: String):                     ServiceCall[MutateHelloWorldRequest, Either[ErrorResponse, MutateHelloWorldResponse]]
  //def mutateHelloWorld2(helloWorldId: String, mutationId: String): ServiceCall[MutateHelloWorldRequest, Either[ErrorResponse, MutateHelloWorldResponse]]
  // Retrieve status of mutation request
  //def getMutationHelloWorld(helloWorldId: String, mutationId: String):    ServiceCall[NotUsed, Either[ErrorResponse, MutationHelloWorldResponse]]
  //def streamMutationHelloWorld(helloWorldId: String, mutationId: String): ServiceCall[NotUsed, Source[MutationHelloWorldResponse, NotUsed]]
// }

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
  //def patchHelloWorld(helloWorldId: String):                               ServiceCall[DeactivateHelloWorldRequest, Either[ErrorResponse, DeactivateHelloWorldResponse]]
  //def deactivateHelloWorld1(helloWorldId: String):                         ServiceCall[DeactivateHelloWorldRequest, Either[ErrorResponse, DeactivateHelloWorldResponse]]
  //def deactivateHelloWorld2(helloWorldId: String, deactivationId: String): ServiceCall[DeactivateHelloWorldRequest, Either[ErrorResponse, DeactivateHelloWorldResponse]]
  // Retrieve status of deactivation request
  //def getDeactivationHelloWorld(helloWorldId: String, deactivationId: String):    ServiceCall[NotUsed, Either[ErrorResponse, DeactivationHelloWorldResponse]]
  //def streamDeactivationHelloWorld(helloWorldId: String, deactivationId: String): ServiceCall[NotUsed, Source[DeactivationHelloWorldResponse, NotUsed]]
// }

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
  //def patchHelloWorld(helloWorldId: String):                               ServiceCall[ReactivateHelloWorldRequest, Either[ErrorResponse, ReactivateHelloWorldResponse]]
  //def reactivateHelloWorld1(helloWorldId: String):                         ServiceCall[ReactivateHelloWorldRequest, Either[ErrorResponse, ReactivateHelloWorldResponse]]
  //def reactivateHelloWorld2(helloWorldId: String, reactivationId: String): ServiceCall[ReactivateHelloWorldRequest, Either[ErrorResponse, ReactivateHelloWorldResponse]]
  // Retrieve status of reactivation request
  //def getReactivationHelloWorld(helloWorldId: String, reactivationId: String):    ServiceCall[NotUsed, Either[ErrorResponse, ReactivationHelloWorldResponse]]
  //def streamReactivationHelloWorld(helloWorldId: String, reactivationId: String): ServiceCall[NotUsed, Source[ReactivationHelloWorldResponse, NotUsed]]
// }

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
  //def getAllHelloWorlds(page: Option[String]): ServiceCall[NotUsed, utils.PagingState[GetAllHelloWorldsResponse]]
  //def getAllHelloWorlds:                       ServiceCall[NotUsed, GetAllHelloWorldsResponse]
// }

//  def streamHelloWorlds
//    : ServiceCall[NotUsed, Source[HelloWorldResource, NotUsed]]

// Hello World Topic

  def helloWorldMessageBrokerEvents: Topic[HelloWorldMessageBrokerEvent]

}

// Hello World regex matchers

object Matchers {
  val Email =
    """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"""
  val Identifier = """^[a-zA-Z0-9\-\.\_\~]{1,64}$"""
  val Name = """^[a-zA-Z0-9\-\.\_\~]{1,128}$"""
  val Description = """^.{1,2048}$"""
  val Motivation = """^.{1,2048}$"""
  val Op = """^add|remove|replace|move|copy|test$"""
}

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
//   final case class Cat(name: String) extends Pet
//   final case class Fish(name: String, color: String) extends Pet
//   final case class Squid(name: String, age: Int) extends Pet

// Hello World algebraic data type  (ADT)
final case class HelloWorldAdt(
  name: String,
  description: Option[String])

object HelloWorldAdt {
  implicit val helloWorldAdtFormat: OFormat[HelloWorldAdt] = derived.oformat()

  val helloWorldAdtValidator: Validator[HelloWorldAdt] =
    validator[HelloWorldAdt] { a =>
      a.name is notEmpty
      a.name should matchRegexFully(Matchers.Name)
      a.description.each should matchRegexFully(Matchers.Description)
    }
}
// }

// Supporting algebraic data types {

// Identity.identifier and Identity.revision uniquely identify a particular version of an entity
final case class HelloWorldIdentity(
  identifier: String       // a collision resistant unique identifier for the entity which remains constant throughout its lifecycle
)

object HelloWorldIdentity {
  implicit val format: Format[HelloWorldIdentity] = Jsonx.formatCaseClass

  val helloWorldIdentityValidator: Validator[HelloWorldIdentity] =
    validator[HelloWorldIdentity] { i =>
      i.identifier is notEmpty
      i.identifier should matchRegexFully(Matchers.Identifier)
    }
}

// Not so sure if object level metadata is needed or just event level metadata
final case class HelloWorldMetadata(
  revision: Int             // a monotonically increasing count of changes perisited by this entity
//  created: Instant,      // When the 
//  lastModified: Option[Instant], // Last Change Transaction Time: the time assigned by the persistent entity
//                            // it will be different from the time it was persisted in Cassandra or Kafka or other datastore
//                            // the persistent entity is where the real transaction occurs, however it still need to be written to the log to become durable 
//  hash: Option[String],     // SHA256 hash of json representation of  the persistent entity's data
//                            // import java.security.MessageDigest import java.math.BigInteger MessageDigest.getInstance("SHA-256").digest("some string".getBytes("UTF-8")).map("%02x".format(_)).mkString
//  previousHash: Option[String], // SHA256 hash of the prior state of the  persistent entity's data
//  validTimeBegin: Option[Instant],    // "valid time (VT) is the time period during which a database fact is valid in the modeled reality." https://en.wikipedia.org/wiki/Valid_time
//  validTimeEnd: Option[Instant],
//  decisionTimeBegin: Option[Instant],  // "Decision time is the time period during which a fact stored in the database was decided to be valid.'' https://en.wikipedia.org/wiki/Temporal_database
//  decisionTimeEnd: Option[Instant]

)

object HelloWorldMetadata {
  implicit val format: Format[HelloWorldMetadata] = Jsonx.formatCaseClass

  val helloWorldMetadataValidator: Validator[HelloWorldMetadata] = 
  validator[HelloWorldMetadata] { m =>
    m.revision should be >= 0
  }
}

final case class HypertextApplicationLanguage(
  halLinks: Seq[HalLink]
  )

object HypertextApplicationLanguage {
  implicit val format: Format[HypertextApplicationLanguage] = Jsonx.formatCaseClass
}

final case class HalLink(
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

final case class Mutation(
  op: String,
  path: String,
  value: Option[String]
  )

object Mutation {
  implicit val format: Format[Mutation] = Jsonx.formatCaseClass

  val helloWorldMutationValidator: Validator[Mutation] =
    validator[Mutation] { mutation =>
      mutation.op is notEmpty
      mutation.path is notEmpty
      mutation.op should matchRegexFully(Matchers.Op)
    }
}
// }

// Hello World Resource
final case class HelloWorldResource(
  helloWorldAdt: HelloWorldAdt,
  helloWorldMetadata: HelloWorldMetadata
)

object HelloWorldResource {
  implicit val format: Format[HelloWorldResource] = Jsonx.formatCaseClass

  val helloWorldResourceValidator: Validator[HelloWorldResource] =
    validator[HelloWorldResource] { r =>
      r.helloWorldAdt is valid(HelloWorldAdt.helloWorldAdtValidator)
      r.helloWorldMetadata is valid(HelloWorldMetadata.helloWorldMetadataValidator)
    }
}

// Hello World Request

// TODO: include span ID as the unique identity of a CreateHelloWorldRequest

// Create Hello World Request payload {
final case class CreateHelloWorldRequest(
    helloWorldAdt: HelloWorldAdt
) {}

case object CreateHelloWorldRequest {
  implicit val format: Format[CreateHelloWorldRequest] = Jsonx.formatCaseClass

  implicit val createHelloWorldRequestValidator
    : Validator[CreateHelloWorldRequest] =
    validator[CreateHelloWorldRequest] { r =>
      r.helloWorldAdt is valid(HelloWorldAdt.helloWorldAdtValidator)
    }
}
// }

final case class ReplaceHelloWorldRequest(
    replacementHelloWorldAdt: HelloWorldAdt,
    motivation: Option[String]
) {}

case object ReplaceHelloWorldRequest {
  implicit val format: Format[ReplaceHelloWorldRequest] = Jsonx.formatCaseClass

  implicit val replaceHelloWorldRequestValidator
    : Validator[ReplaceHelloWorldRequest] =
    validator[ReplaceHelloWorldRequest] { r =>
      r.replacementHelloWorldAdt is valid(HelloWorldAdt.helloWorldAdtValidator)
      r.motivation.each should matchRegexFully(Matchers.Motivation)
    }
}

final case class MutateHelloWorldRequest(
    mutations: Seq[Mutation],
    motivation: Option[String]
) {}

case object MutateHelloWorldRequest {
  implicit val format: Format[MutateHelloWorldRequest] = Jsonx.formatCaseClass

  implicit val mutateHelloWorldRequestValidator
    : Validator[MutateHelloWorldRequest] =
    validator[MutateHelloWorldRequest] { m =>
      m.mutations.each is valid(Mutation.helloWorldMutationValidator)
      m.motivation.each should matchRegexFully(Matchers.Motivation)
    }
}

// Response

final case class CreateHelloWorldResponse(
    helloWorldIdentity: HelloWorldIdentity,
    helloWorldResource: HelloWorldResource,
    helloWorldHal: Option[HypertextApplicationLanguage]
)

object CreateHelloWorldResponse {
  implicit val format: Format[CreateHelloWorldResponse] = Jsonx.formatCaseClass
}

final case class ReplaceHelloWorldResponse(
    helloWorldIdentity: HelloWorldIdentity,
    helloWorldResource: HelloWorldResource,
    helloWorldHal: Option[HypertextApplicationLanguage]
)

object ReplaceHelloWorldResponse {
  implicit val format: Format[ReplaceHelloWorldResponse] = Json.format
}

final case class GetHelloWorldResponse(
  helloWorldIdentity: HelloWorldIdentity,
  helloWorldResource: HelloWorldResource,
  helloWorldHal: Option[HypertextApplicationLanguage]
)

object GetHelloWorldResponse {
  implicit val format: Format[GetHelloWorldResponse] = Json.format
}

final case class GetAllHelloWorldsResponse(helloWorlds: Seq[HelloWorldResource])

object GetAllHelloWorldsResponse {
  implicit val format: Format[GetAllHelloWorldsResponse] = Json.format
}

// Message Broker Event
// One service to many other services

sealed trait HelloWorldMessageBrokerEvent {
  val helloWorldIdentity: HelloWorldIdentity
}

final case class HelloWorldCreated(
  helloWorldIdentity: HelloWorldIdentity,
  revision: Int,                             // The change number for this entity (berfore or after this persistance?)
  helloWorldResource: HelloWorldResource,
  version: Int = 0                           // The minor version of the HelloWorldCreated event. A change in the event's major requires use of a new event. 
) extends HelloWorldMessageBrokerEvent

object HelloWorldCreated {
  implicit val format: Format[HelloWorldCreated] = Json.format
}

//final case class HelloWorldBrokerEvent(event: HelloWorldEventType,
//                          id: String,
//                          data: Map[String, String] = Map.empty[String, String])

object HelloWorldMessageBrokerEvent {
  implicit val format: Format[HelloWorldMessageBrokerEvent] =
    derived.flat.oformat((__ \ "type").format[String])
}

//object HelloWorldEventTypes extends Enumeration {
//  type HelloWorldEventType = Value
//  val REGISTERED, DELETED, VERIFIED, UNVERIFIED = Value
//
//  implicit val format: Format[HelloWorldEventType] = enumFormat(HelloWorldEventTypes)
//}
