package models.dao.slick

import javax.inject.{Inject, Singleton}
import models.BDTransaction
import models.BDTransactionKinds.BDTransactionKind
import models.dao.BDTxDAO
import models.dao.slick.table.BDTransactionsTable
import play.api.db.slick.DatabaseConfigProvider
import slick.ast.Ordering.{Asc, Desc, Direction}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickBDTxDAO @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
	extends BDTxDAO with BDTransactionsTable with SlickCommontDAO {

	import dbConfig.profile.api._

	import scala.concurrent.Future.{successful => future}

	private val queryById = Compiled(
		(id: Rep[String]) => table.filter(_.id === id))

	private val queryByBlockId = Compiled(
		(blockId: Rep[String]) => table.filter(_.blockId === blockId))

	def _findById(id: String) =
		queryById(id).result.headOption

	def _txsListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String]) =
		table
			.dynamicSortBy(sortsBy)
			.page(pSize, pId)


	def _findTxsByBlockId(blockId: String) =
		queryByBlockId(blockId).result

	def _findByBlockId(blockId: String) =
		table
			.filter(_.blockId === blockId)

	def _txsListPagesCount(pSize: Int, filterOpt: Option[String]) =
		table
			.size

	def _create(id: String,
							blockId: String,
							blockHeight: Long,
							nonce: Long,
							signer: String,
							signature: String,
							kind: BDTransactionKind,
							recepient: Option[String],
							amount: Option[Long],
							timestamp: Long) = {
		val tx = BDTransaction(
			id,
			blockId,
			blockHeight,
			nonce,
			signer,
			signature,
			kind,
			recepient,
			amount,
			timestamp)
		(table += tx).map(_ => tx)
	}


	override def create(id: String,
											blockId: String,
											blockHeight: Long,
											nonce: Long,
											signer: String,
											signature: String,
											kind: BDTransactionKind,
											recepient: Option[String],
											amount: Option[Long],
											timestamp: Long): Future[BDTransaction] =
		db.run(_create(id,
			blockId,
			blockHeight,
			nonce,
			signer,
			signature,
			kind,
			recepient,
			amount,
			timestamp).transactionally)

	override def txsListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String]): Future[Seq[BDTransaction]] =
		db.run(_txsListPage(pSize, pId, sortsBy.map(t => (t._1, if (t._2) Asc else Desc)), filterOpt).result)

	override def txsListPagesCount(pSize: Int, filterOpt: Option[String]): Future[Int] =
		db.run(_txsListPagesCount(pSize, filterOpt).result).map(t => pages(t, pSize))

	override def findById(id: String): Future[Option[BDTransaction]] =
		db.run(_findById(id))

	override def close: Future[Unit] =
		future(db.close())

}
