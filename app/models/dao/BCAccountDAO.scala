package models.dao

import javax.inject.Inject
import models.BCAccount
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

trait BCAccountDAO {

	def close: Future[Unit]

	def findBCAccountById(id: String): Future[Option[BCAccount]]

	def getBCAccountStateById(id: String): Future[BCAccount]

	def createBCAccount(id: String,
											blockId: String,
											blockHeight: Long,
											name: Option[String],
											amount: Long): Future[BCAccount]

	def updateBCAccount(id: String,
											blockId: String,
											blockHeight: Long,
											name: Option[String],
											amount: Long): Future[BCAccount]

	def removeById(id: String): Future[Boolean]

}

class BCAccountDAOCloseHook @Inject()(dao: BCAccountDAO, lifecycle: ApplicationLifecycle) {
	lifecycle.addStopHook { () =>
		Future.successful(dao.close)
	}
}
