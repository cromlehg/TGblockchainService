package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import javax.inject.{Inject, Singleton}
import models.dao.{BDBlockDAO, BDTxDAO, MenuDAO, OptionDAO}
import play.api.{Configuration, Logger}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext
import controllers.AuthRequestToAppContext.ac

@Singleton
class TxsController @Inject()(txDAO: BDTxDAO,
															blockDAO: BDBlockDAO,
															cc: ControllerComponents,
															config: Configuration,
															deadbolt: DeadboltActions
														 )(implicit ec: ExecutionContext, optionDAO: OptionDAO, menuDAO: MenuDAO)
	extends CommonAbstractController(optionDAO, cc)
		with I18nSupport with JSONSupport with LoggerSupport {

	import scala.concurrent.Future.{successful => future}

	def viewTx(id: String) = deadbolt.WithAuthRequest()() { implicit request =>
		checkModifierId(id) {
			txDAO.findById(id).flatMap(_.fold (future(Ok(views.html.app.viewTx(id, None, None)))) { tx =>
				blockDAO.findById((tx.blockId)).map(_.fold (NotFound("Not found block with id " + tx.blockId + "  for transaction with id " + id)) { block =>
					Ok(views.html.app.viewTx(id, Some(tx), Some(block)))
				})
			})
		}
	}

	def txsInc = deadbolt.WithAuthRequest()(parse.json) { implicit request =>
		txDAO.txsListPage(
			AppConstants.DEFAULT_PAGE_SIZE,
			1,
			Seq.empty,
			None
		) map { txs => Ok(jsonResultOk(Json.toJson(txs))) }
	}

	def txsListPage = deadbolt.WithAuthRequest()(parse.json) { implicit request =>
		fieldIntOpt("page_id")(pageIdOpt => fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			txDAO.txsListPage(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				pageIdOpt.getOrElse(0),
				Seq.empty,
				filterOpt) map { txs =>
				Ok(views.html.app.components.pagparts.txsListPage(txs))
			}
		}))
	}

	def txsListPagesCount = deadbolt.WithAuthRequest()(parse.json) { implicit request =>
		fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			txDAO.txsListPagesCount(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				filterOpt) map { count => Ok(count.toString) }
		})
	}

	def txs = deadbolt.WithAuthRequest()() { implicit request =>
		future(Ok(views.html.app.txs()))
	}

	def latestTxs = deadbolt.WithAuthRequest()() { implicit request =>
		txDAO.txsListPage(
			AppConstants.DEFAULT_PAGE_SIZE,
			1,
			Seq.empty,
			None
		) map { txs => Ok(views.html.app.components.latestTransactions(txs)) }
	}

}
