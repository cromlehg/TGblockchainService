package models.dao

import javax.inject.Inject
import models.BDBlock
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

trait BDBlockDAO {

	def create(id: String,
						 height: Long,
						 version: Int,
						 timestamp: Long,
						 parentId: String,
						 generator: String,
						 signature: String): Future[BDBlock]

	def findById(id: String): Future[Option[BDBlock]]

	def findByIdFull(id: String): Future[Option[BDBlock]]

	def lastBlock: Future[Option[BDBlock]]

	def blocks(from: Long, to: Long): Future[Seq[BDBlock]]

	def findByHeight(height: Long): Future[Seq[BDBlock]]

	def blocksListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String]): Future[Seq[BDBlock]]

	def blocksListPagesCount(pSize: Int, filterOpt: Option[String]): Future[Int]

	def height: Future[Option[Long]]

	def close: Future[Unit]

}

class BDBlockDAOCloseHook @Inject()(dao: BDBlockDAO, lifecycle: ApplicationLifecycle) {
	lifecycle.addStopHook { () =>
		Future.successful(dao.close)
	}
}
