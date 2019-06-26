package models.dao

import javax.inject.Inject
import models.BDTransaction
import models.BDTransactionKinds.BDTransactionKind
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

trait BDTxDAO {

	def create(id: String,
						 blockId: String,
						 blockHeight: Long,
						 nonce: Long,
						 signer: String,
						 signature: String,
						 kind: BDTransactionKind,
						 recepient: Option[String],
						 amount: Option[Long],
						 timestamp: Long): Future[BDTransaction]

	def findById(id: String): Future[Option[BDTransaction]]

	def txsListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String]): Future[Seq[BDTransaction]]

	def txsListPagesCount(pSize: Int, filterOpt: Option[String]): Future[Int]

	def close: Future[Unit]

}

class BDTxDAOCloseHook @Inject()(dao: BDTxDAO, lifecycle: ApplicationLifecycle) {
	lifecycle.addStopHook { () =>
		Future.successful(dao.close)
	}
}
