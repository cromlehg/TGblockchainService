package controllers

import controllers.AuthRequestToAppContext.ac
import be.objectify.deadbolt.scala.DeadboltActions
import javax.inject.{Inject, Singleton}
import models.dao.{BCAccountDAO, BDBlockDAO, BDTxDAO, MenuDAO, OptionDAO}
import play.api.{Configuration, Logger}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class ModifiersController @Inject()(txDAO: BDTxDAO,
																		blockDAO: BDBlockDAO,
																		bcAccountDAO: BCAccountDAO,
																		cc: ControllerComponents,
																		config: Configuration,
																		deadbolt: DeadboltActions
														 )(implicit ec: ExecutionContext, optionDAO: OptionDAO, menuDAO: MenuDAO)
	extends CommonAbstractController(optionDAO, cc)
		with I18nSupport with JSONSupport with LoggerSupport {

	import scala.concurrent.Future.{successful => future}

	def modidier(id: String) = deadbolt.WithAuthRequest()() { implicit request =>
		checkModifierOrAddressId(id) {
			txDAO.findById(id).flatMap(_.fold {
				blockDAO.findById(id).flatMap(_.fold {
					Logger.warn("Check address and name")
					future(Redirect(controllers.routes.BCAccountsController.viewBCAccount(id)))
				} { block =>
					future(Redirect(controllers.routes.BlocksController.viewBlock(block.id)))
				})
			} { tx =>
				future(Redirect(controllers.routes.TxsController.viewTx(tx.id)))
			})
		}
	}

}
