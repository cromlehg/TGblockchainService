package models

import models.BDTransactionKinds.intToKind
import play.api.libs.json._

case class BDTransaction(val id: String,
												 val blockId: String,
												 val blockHeight: Long,
												 val nonce: Long,
												 val signer: String,
												 val signature: String,
												 val kind: BDTransactionKinds.BDTransactionKind,
												 val recepient: Option[String],
												 val amount: Option[Long],
												 val timestamp: Long) {


	lazy val timestampPrettyTime =
		controllers.TimeConstants.prettyTime.format(new java.util.Date(timestamp))

	override def equals(obj: Any) = obj match {
		case t: BDTransaction => t.id == id
		case _ => false
	}


}

object BDTransactionKinds extends Enumeration() {

	type BDTransactionKind = Value

	val TX_CHANGE_PERMISSIONS = Value("change_permissions")

	val TX_TRANSFER_ASSETS = Value("transfer_assets")

	val TX_IDENTIFY = Value("identify")

	val TX_VOTE = Value("vote")

	val INT_TX_CHANGE_PERMISSIONS = 0

	val INT_TX_TRANSFER_ASSETS = 1

	val INT_TX_IDENTIFY = 2

	val INT_TX_VOTE = 3

	val map = Map(INT_TX_CHANGE_PERMISSIONS -> TX_CHANGE_PERMISSIONS,
		INT_TX_TRANSFER_ASSETS -> TX_TRANSFER_ASSETS,
		INT_TX_IDENTIFY -> TX_IDENTIFY,
		INT_TX_VOTE -> TX_VOTE)

	def intToKind(intKind: Int): Option[BDTransactionKind] =
		map.get(intKind)

	implicit val reads: Reads[BDTransactionKind] =
		new Reads[BDTransactionKind] {
			override def reads(json: JsValue) = {
				intToKind(json.as[Int]) match {
					case Some(t) => JsSuccess(t)
					case _ => JsError(s"Unknown transaction type")
				}
			}
		}

}


object BDTransaction {

	implicit val reads1: Reads[BDTransaction] =
		new Reads[BDTransaction] {
			override def reads(json: JsValue) = {
				intToKind((json \ "type").as[Int]) match {
					case Some(kind) =>
						val tx = BDTransaction((json \ "id").as[String],
							"",
							0,
							(json \ "nonce").as[Long],
							(json \ "signer").as[String],
							(json \ "signature").as[String],
							kind,
							(json \ "recipient").asOpt[String],
							(json \ "amount").asOpt[Long],
							0)
						JsSuccess(kind match {
							case BDTransactionKinds.TX_TRANSFER_ASSETS => tx
							case _ => tx
						})
					case _ => JsError(s"Unknown transaction type")
				}
			}
		}

	implicit val writes = new Writes[BDTransaction] {
		def writes(target: BDTransaction) = Json.obj(
			"id" -> target.id,
			"amount" -> target.amount,
			"blockId" -> target.blockId,
			"nonce" -> target.nonce,
			"recipient" -> target.recepient,
			"signer" -> target.signer,
			"signature" -> target.signature,
			"timestamp" -> target.timestamp
		)
	}


	def apply(id: String,
						blockId: String,
						blockHeight: Long,
						nonce: Long,
						signer: String,
						signature: String,
						kind: BDTransactionKinds.BDTransactionKind,
						recpient: Option[String],
						amount: Option[Long],
					 timestamp: Long) =
		new BDTransaction(id,
			blockId,
			blockHeight,
			nonce,
			signer,
			signature,
			kind,
			recpient,
			amount,
			timestamp)


}

