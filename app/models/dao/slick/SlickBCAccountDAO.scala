package models.dao.slick

import javax.inject.{Inject, Singleton}
import models.BCAccount
import models.dao.BCAccountDAO
import models.dao.slick.table.BCAccountTable
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickBCAccountDAO @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
	extends BCAccountDAO with BCAccountTable with SlickCommontDAO {

	import dbConfig.profile.api._

	import scala.concurrent.Future.{successful => future}

	private val queryById = Compiled(
		(id: Rep[String]) => table.filter(_.id === id))

	def _createBCAccount(id: String,
											 blockId: String,
											 blockHeight: Long,
											 name: Option[String],
											 amount: Long) = {
		val t = BCAccount(
			id,
			blockId,
			blockHeight,
			name,
			amount)
		(table += t).map(_ => t)
	}

	def _findBCAccountById(id: String) =
		queryById(id).result.headOption

	def _removeById(id: String) =
		queryById(id).delete.map(_ == 1)


	def _updateBCAccount(id: String,
											 blockId: String,
											 blockHeight: Long,
											 name: Option[String],
											 amount: Long) =
		table
			.filter(_.id === id)
			.map(t => (t.blockId,
				t.blockHeight,
				t.name,
				t.amount))
			.update(blockId,
				blockHeight,
				name,
				amount)
			.map(_ =>
				BCAccount(id,
					blockId,
					blockHeight,
					name,
					amount)
			)

	def _getBCAccountStateById(id: String) =
		_findBCAccountById(id)
			.map(t => t.getOrElse(BCAccount(id, "", 0, None, 0)))

	override def updateBCAccount(id: String,
															 blockId: String,
															 blockHeight: Long,
															 name: Option[String],
															 amount: Long): Future[BCAccount] =
		db.run(_updateBCAccount(id,
			blockId,
			blockHeight,
			name,
			amount).transactionally)

	override def removeById(id: String): Future[Boolean] =
		db.run(_removeById(id).transactionally)

	override def findBCAccountById(id: String): Future[Option[BCAccount]] =
		db.run(_findBCAccountById(id))

	override def createBCAccount(id: String,
															 blockId: String,
															 blockHeight: Long,
															 name: Option[String],
															 amount: Long): Future[BCAccount] =
		db.run(_createBCAccount(id,
			blockId,
			blockHeight,
			name,
			amount).transactionally)

	override def getBCAccountStateById(id: String): Future[BCAccount] =
		db.run(_getBCAccountStateById(id))

	override def close: Future[Unit] =
		future(db.close())

}
