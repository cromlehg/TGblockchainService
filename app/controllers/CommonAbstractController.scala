package controllers

import javax.inject.{Inject, Singleton}
import models.dao.OptionDAO
import play.api.i18n.I18nSupport
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CommonAbstractController @Inject()(optionDAO: OptionDAO,
																				 cc: ControllerComponents)(implicit ec: ExecutionContext)
	extends AbstractController(cc) with I18nSupport with LoggerSupport {

	import scala.concurrent.Future.{successful => future}

	def checkModifierOrAddressId[T](id: String)(f: => Future[Result])(implicit request: Request[T]): Future[Result] =
		if(id.matches(AppConstants.BC_ID_REGEXP_STRING) || id.matches(AppConstants.BC_ADDRESS_REGEXP_STRING)) f else asyncErrorRedirect("Wrong search id: " + id)

	def checkModifierId[T](id: String)(f: => Future[Result])(implicit request: Request[T]): Future[Result] =
		if(id.matches(AppConstants.BC_ID_REGEXP_STRING)) f else asyncErrorRedirect("Wrong modifier id: " + id)

	def checkAddress[T](id: String)(f: => Future[Result])(implicit request: Request[T]): Future[Result] =
		if(id.matches(AppConstants.BC_ADDRESS_REGEXP_STRING)) f else asyncErrorRedirect("Wrong address: " + id)

	def checkedOwner(targetOwnerId: Long, anyPermission: String, ownPermission: String)(f: => Future[Result])(implicit ac: AppContext): Future[Result] =
		if (ac.actor.containsPermission(anyPermission) || (ac.actor.containsPermission(ownPermission) && ac.actor.id == targetOwnerId))
			f
		else
			future(BadRequest("You are not authorized to this action!"))

	def errorRedirect[T](msg: String, call: Call = routes.AppController.index)(implicit request: Request[T]) =
		request.headers.get("referer")
			.fold {
				Redirect(call).flashing("error" -> (msg))
			} { url =>
				Redirect(url).flashing("error" -> (msg))
			}

	def asyncErrorRedirect[T](msg: String, call: Call = routes.AppController.index)(implicit request: Request[T]) =
		future(errorRedirect(msg, call))

	def successRedirect[T](msg: String, call: Call = routes.AppController.index)(implicit request: Request[T]) =
		request.headers.get("referer")
			.fold {
				Redirect(call).flashing("success" -> (msg))
			} { url =>
				Redirect(url).flashing("success" -> (msg))
			}

	def asyncSuccessRedirect[T](msg: String, call: Call = routes.AppController.index)(implicit request: Request[T]) =
		future(successRedirect(msg, call))

	def booleanOptionFold(name: String)(ifFalse: Future[Result])(ifTrue: Future[Result]): Future[Result] =
		optionDAO.getOptionByName(name) flatMap {
			_.fold(future(BadRequest("Not found option " + name)))(option => if (option.toBoolean) ifTrue else ifFalse)
		}

}

