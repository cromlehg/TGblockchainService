package controllers

import controllers.AuthRequestToAppContext.ac
import be.objectify.deadbolt.scala.DeadboltActions
import javax.inject.{Inject, Singleton}
import models.dao.{BCAccountDAO, MenuDAO, OptionDAO}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class BCAccountsController @Inject()(bcAccountDAO: BCAccountDAO,
																			cc: ControllerComponents,
																			config: Configuration,
																			deadbolt: DeadboltActions
																		)(implicit ec: ExecutionContext, optionDAO: OptionDAO, menuDAO: MenuDAO)
	extends CommonAbstractController(optionDAO, cc)
		with I18nSupport with JSONSupport with LoggerSupport {

	def viewBCAccount(id: String) = deadbolt.WithAuthRequest()() { implicit request =>
		checkAddress(id) {
			bcAccountDAO.getBCAccountStateById(id).map(t => Ok(views.html.app.viewBCAccount(id, t)))
		}
	}

}
