package models.dao.slick.table

import models.{BDTransaction, BDTransactionKinds}

trait BDTransactionsTable extends CommonTable {

	import dbConfig.profile.api._

	implicit val TxKindsMapper = enum2String(BDTransactionKinds)

	class InnerCommonTable(tag: Tag) extends Table[BDTransaction](tag, "txs") with DynamicSortBySupport.ColumnSelector {
		def id = column[String]("id")

		def blockId = column[String]("block_id")

		def blockHeight = column[Long]("block_h")

		def nonce = column[Long]("nonce")

		def signer = column[String]("signer")

		def recepient = column[Option[String]]("recepient")

		def amount = column[Option[Long]]("amount")

		def signature = column[String]("signature")

		def kind = column[BDTransactionKinds.BDTransactionKind]("kind")

		def timestamp = column[Long]("timestamp")

		def * = (id,
			blockId,
			blockHeight,
			nonce,
			signer,
			signature,
			kind,
			recepient,
			amount,
			timestamp) <>[BDTransaction](t =>
			BDTransaction(t._1,
				t._2,
				t._3,
				t._4,
				t._5,
				t._6,
				t._7,
				t._8,
				t._9,
				t._10), t => Some(
			(t.id,
				t.blockId,
				t.blockHeight,
				t.nonce,
				t.signer,
				t.signature,
				t.kind,
				t.recepient,
				t.amount,
				t.timestamp)))

		override val select = Map(
			"id" -> (this.id),
			"block_id" -> (this.blockId),
			"nonce" -> (this.nonce),
			"signer" -> (this.signer),
			"signature" -> (this.signature))
	}

	val table = TableQuery[InnerCommonTable]

}
