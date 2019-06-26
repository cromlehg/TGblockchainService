package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}


case class BDBlock(val id: String,
									 val height: Long,
									 val version: Int,
									 val timestamp: Long,
									 val parentId: String,
									 val generator: String,
									 val signature: String,
									 val transactions: Seq[BDTransaction]) {

	lazy val timestampPrettyTime =
		controllers.TimeConstants.prettyTime.format(new java.util.Date(timestamp))

}

object BDBlock {

	implicit val reads: Reads[BDBlock] = (
		(JsPath \ "id").read[String] and
			(JsPath \ "height").read[Long] and
			(JsPath \ "version").read[Int] and
			(JsPath \ "timestamp").read[Long] and
			(JsPath \ "parentId").read[String] and
			(JsPath \ "generatorBox").read[String] and
			(JsPath \ "signature").read[String] and
			(JsPath \ "transactions").read[Seq[BDTransaction]]
		) (BDBlock.applyJSON _)

	implicit val writes = new Writes[BDBlock] {
		def writes(target: BDBlock) = Json.obj(
			"id" -> target.id,
			"height" -> target.height,
			"version" -> target.version,
			"timestamp" -> target.timestamp,
			"parentId" -> target.parentId,
			"generator" -> target.generator,
			"signature" -> target.signature,
			"transactions" -> target.transactions
		)
	}

	def applyJSON(id: String,
								height: Long,
								version: Int,
								timestamp: Long,
								parentId: String,
								generator: String,
								signature: String,
								transactions: Seq[BDTransaction]) =
		new BDBlock(id,
			height,
			version,
			timestamp,
			parentId,
			generator,
			signature,
			transactions)

	def apply(id: String,
						height: Long,
						version: Int,
						timestamp: Long,
						parentId: String,
						generator: String,
						signature: String,
						transactions: Seq[BDTransaction]) =
		new BDBlock(id,
			height,
			version,
			timestamp,
			parentId,
			generator,
			signature,
			transactions)

	def apply(id: String,
						height: Long,
						version: Int,
						timestamp: Long,
						parentId: String,
						generator: String,
						signature: String) =
		new BDBlock(id,
			height,
			version,
			timestamp,
			parentId,
			generator,
			signature,
			Seq.empty)

}
