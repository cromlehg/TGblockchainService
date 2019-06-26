package models.dao.slick

import javax.inject.{Inject, Singleton}
import models.BDBlock
import models.dao.BDBlockDAO
import models.dao.slick.table.BDBlocksTable
import play.api.db.slick.DatabaseConfigProvider
import slick.ast.Ordering.{Asc, Desc, Direction}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickBDBlockDAO @Inject()(val dbConfigProvider: DatabaseConfigProvider,
																txDAO: SlickBDTxDAO
															 )(implicit ec: ExecutionContext)
	extends BDBlockDAO with BDBlocksTable with SlickCommontDAO {

	import dbConfig.profile.api._

	import scala.concurrent.Future.{successful => future}

	private val queryById = Compiled(
		(id: Rep[String]) => table.filter(_.id === id))

	private val queryByHeight = Compiled(
		(height: Rep[Long]) => table.filter(_.height === height))


	def _findById(id: String) =
		queryById(id).result.headOption

	def _findByIdFull(id: String) =
		for {
			blockOpt <- _findById(id)
			txs <- maybeOptActionSeqR(blockOpt)(t => txDAO._findTxsByBlockId(t.id))
		} yield blockOpt.map(_.copy(transactions = txs))

	def _findByHeight(height: Long) =
		queryByHeight(height).result

	def _blocks(from: Long, to: Long) =
		for {
			blocks <- table.filter(t => t.height >= from && t.height <= to).sortBy(_.height.asc).result
			txs <- txDAO.table.filter(_.blockId inSet blocks.map(_.id)).result
		} yield blocks map { b => b.copy(transactions = txs.filter(_.blockId == b.id)) }

	def _blocksListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String]) =
		table
			.dynamicSortBy(if (sortsBy.isEmpty) Seq(("height", Desc)) else sortsBy)
			.page(pSize, pId)

	def _blocksListPagesCount(pSize: Int, filterOpt: Option[String]) =
		table
			.size

	def _create(id: String,
							height: Long,
							version: Int,
							timestamp: Long,
							parentId: String,
							generator: String,
							signature: String) = {
		val block = BDBlock(
			id,
			height,
			version,
			timestamp,
			parentId,
			generator,
			signature)
		(table += block).map(_ => block)
	}

	def _height =
		table.map(_.height).max

	def _lastBlock =
		table.sortBy(_.height.desc).result.headOption

	override def create(id: String,
											height: Long,
											version: Int,
											timestamp: Long,
											parentId: String,
											generator: String,
											signature: String): Future[BDBlock] =
		db.run(_create(id,
			height,
			version,
			timestamp,
			parentId,
			generator,
			signature).transactionally)

	override def lastBlock: Future[Option[BDBlock]] =
		db.run(_lastBlock)

	override def blocks(from: Long, to: Long): Future[Seq[BDBlock]] =
		db.run(_blocks(from, to))

	override def blocksListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String]): Future[Seq[BDBlock]] =
		db.run(_blocksListPage(pSize, pId, sortsBy.map(t => (t._1, if (t._2) Asc else Desc)), filterOpt).result)

	override def blocksListPagesCount(pSize: Int, filterOpt: Option[String]): Future[Int] =
		db.run(_blocksListPagesCount(pSize, filterOpt).result).map(t => pages(t, pSize))

	override def findById(id: String): Future[Option[BDBlock]] =
		db.run(_findById(id))

	override def findByIdFull(id: String): Future[Option[BDBlock]] =
		db.run(_findByIdFull(id))

	override def findByHeight(height: Long): Future[Seq[BDBlock]] =
		db.run(_findByHeight(height))

	override def height: Future[Option[Long]] =
		db.run(_height.result)

	override def close: Future[Unit] =
		future(db.close())

}
