package services

import com.typesafe.config.Config
import javax.inject.{Inject, Singleton}
import models.BDBlock
import play.api.Logger
import play.api.libs.concurrent.Futures._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}


trait MBService {

	def getState(id: String): Future[Either[String, BCAccountState]]

	def getBlock(id: String): Future[Either[String, BDBlock]]

	def getGenerateKeyPair: Future[Either[String, KeyPair]]

	def transfer(from: String, to: String, amount: Long, pk: String): Future[Either[String, String]]

}

case class BCAccountState(val target: String, name: Option[String], value: Long)

object BCAccountState {

	implicit val reads: Reads[BCAccountState] = (
		(JsPath \ "target").read[String] and
			(JsPath \ "name").readNullable[String] and
			(JsPath \ "value").read[Long]
		) (BCAccountState.apply _)

	implicit val writes = new Writes[BCAccountState] {
		def writes(t: BCAccountState) = {
			Json.obj(
				"target" -> t.target,
				"name" -> t.name,
				"value" -> t.value)
		}
	}

}

case class KeyPair(val address: String, val pk: String)

case class TransferAsset(val amount: Long, val pk: String, val recipient: String)

object TransferAsset {

	implicit val reads: Reads[TransferAsset] = (
		(JsPath \ "amount").read[Long] and
			(JsPath \ "recipient").read[String] and
			(JsPath \ "pk").read[String]
		) (TransferAsset.apply _)

	implicit val writes = new Writes[TransferAsset] {
		def writes(t: TransferAsset) = {
			Json.obj(
				"amount" -> t.amount,
				"recipient" -> t.recipient,
				"pk" -> t.pk)
		}
	}


}


object KeyPair {

	implicit val reads: Reads[KeyPair] = (
		(JsPath \ "address").read[String] and
			(JsPath \ "pk").read[String]
		) (KeyPair.apply _)

	implicit val writes = new Writes[KeyPair] {
		def writes(t: KeyPair) = {
			Json.obj(
				"address" -> t.address,
				"pk" -> t.pk)
		}
	}


}

case class GetBlockResponse(val contains: Boolean, val block: Option[BDBlock])

case class GetTransferResponse(val tx: String)

object GetBlockResponse {

	implicit val reads: Reads[GetBlockResponse] = (
		(JsPath \ "contains").read[Boolean] and
			(JsPath \ "block").readNullable[BDBlock]
		) (GetBlockResponse.apply _)

}

object GetTransferResponse {

	implicit val reads: Reads[GetTransferResponse] =
		(JsPath \ "tx").read[String].map(t => GetTransferResponse(t))

}


@Singleton
class MBServiceImpl @Inject()(config: Config, ws: WSClient)(implicit executionContext: ExecutionContext)
	extends MBService {


	import ServiceAktorSystem.actorSystem

	import scala.concurrent.Future.{successful => future}

	val nodeAddr: String = config.getString("techgen.node")

	val blockURL = nodeAddr + "/bd/blocks/"

	val genPkURL = nodeAddr + "/bd/genpk"

	val transferURL = nodeAddr + "/bd/transfer"

	val stateURL = nodeAddr + "/bd/state/"

	import play.api.libs.concurrent.Futures.actorSystemToFutures

	override def getState(id: String): Future[Either[String, BCAccountState]] =
		ws
			.url(stateURL + id)
			.addHttpHeaders("Accept" -> "application/json")
			.addHttpHeaders("Content-Type" -> "application/json")
			.get()
			.withTimeout(10.seconds).flatMap { response: WSResponse =>
			response.status match {
				case 200 =>
					response.json.validate[BCAccountState].fold(invalid => future(Left(invalid.toString())), r => future(Right(r)))
				case status =>
					val msg = "Error during execution: " + status + ", response text " + response.statusText + ", " + response.json
					Logger.error(msg)
					future(Left(msg))
			}
		}.recover {
			case e: Exception =>
				val msg = "Exception occurs: " + e.toString
				Logger.error(msg)
				Left(msg)
		}

	override def getGenerateKeyPair: Future[Either[String, KeyPair]] =
		ws
			.url(genPkURL)
			.addHttpHeaders("Accept" -> "application/json")
			.get()
			.withTimeout(10.seconds).flatMap { response: WSResponse =>
			response.status match {
				case 200 =>
					response.json.validate[KeyPair].fold(invalid => future(Left(invalid.toString())), r => future(Right(r)))
				case status =>
					val msg = "Error during execution: " + status + ", response text " + response.statusText + ", " + response.json
					Logger.error(msg)
					future(Left(msg))
			}
		}.recover {
			case e: Exception =>
				val msg = "Exception occurs: " + e.toString
				Logger.error(msg)
				Left(msg)
		}

	override def getBlock(id: String): Future[Either[String, BDBlock]] =
		ws
			.url(blockURL + id)
			.addHttpHeaders("Accept" -> "application/json")
			.addHttpHeaders("Content-Type" -> "application/json")
			.get()
			.withTimeout(10.seconds).flatMap { response: WSResponse =>
			response.status match {
				case 200 =>
					response.json.validate[GetBlockResponse].fold(invalid => future(Left(invalid.toString())), r => {
						if (r.contains)
							r.block match {
								case Some(block) => future(Right(block))
								case _ => future(Left("Wrong JSON format"))
							}
						else
							future(Left(""))
					})
				case status =>
					val msg = "Error during execution: " + status + ", response text " + response.statusText + ", " + response.json
					Logger.error(msg)
					future(Left(msg))
			}
		}.recover {
			case e: Exception =>
				val msg = "Exception occurs: " + e.toString
				Logger.error(msg)
				Left(msg)
		}

	override def transfer(from: String, to: String, amount: Long, pk: String): Future[Either[String, String]] = {
		ws
			.url(transferURL)
			.addHttpHeaders("Accept" -> "application/json")
			.addHttpHeaders("Content-Type" -> "application/json")
			.post(Json.toJson(new TransferAsset(amount, pk, to)))
			.withTimeout(10.seconds).flatMap { response: WSResponse =>
			response.status match {
				case 200 =>
					response.json.validate[GetTransferResponse].fold(invalid => future(Left(invalid.toString())), r => future(Right(r.tx)))
				case status =>
					val msg = "Error during execution: " + status + ", response text " + response.statusText + ", " + response.json
					Logger.error(msg)
					future(Left(msg))
			}
		}.recover {
			case e: Exception =>
				val msg = "Exception occurs: " + e.toString
				Logger.error(msg)
				Left(msg)
		}
	}


}
