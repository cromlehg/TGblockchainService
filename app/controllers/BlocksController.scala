package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import javax.inject.{Inject, Singleton}
import models.dao.{BDBlockDAO, MenuDAO, OptionDAO}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import controllers.AuthRequestToAppContext.ac

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BlocksController @Inject()(blockDAO: BDBlockDAO,
																 cc: ControllerComponents,
																 config: Configuration,
																 deadbolt: DeadboltActions
																)(implicit ec: ExecutionContext, optionDAO: OptionDAO, menuDAO: MenuDAO)
	extends CommonAbstractController(optionDAO, cc)
		with JSONSupport {

	import scala.concurrent.Future.{successful => future}

	def viewBlock(id: String) = deadbolt.WithAuthRequest()() { implicit request =>
		checkModifierId(id) {
			blockDAO.findByIdFull((id)).map(t => Ok(views.html.app.viewBlock(id, t)))
		}
	}

	def blocksInc = deadbolt.WithAuthRequest()(parse.json) { implicit request =>
		val COUNT = AppConstants.DEFAULT_PAGE_SIZE.toLong
		val fromOpt = (request.body \ "from").asOpt[Long]
		val toOpt = (request.body \ "to").asOpt[Long]
		val count = (request.body \ "count").asOpt[Long].getOrElse(COUNT)
		blockDAO.height map {
			case Some(height) => (0L, height)
			case _ => (0L, 0L)
		} map { case (validFrom, validTo) =>
			(fromOpt, toOpt) match {
				case (Some(from), Some(to)) if from > to => Left("\"from\" could not be greater than \"to\"")
				case (Some(from), _) if from < validFrom => Left("invalid \"from\" value")
				case (_, Some(to)) if to < validFrom => Left("invalid \"to\" value")
				case (Some(from), Some(to)) => Right((from, to))
				case (Some(from), None) => Right((from, validTo))
				case (None, Some(to)) => Right((validFrom, to))
				case (_, _) => Right((if (validTo > count) validTo - count else 0L, validTo))
			}
		} flatMap {
			case Left(error) => Future.successful(BadRequest(jsonResultError(400, error)))
			case Right((from, to)) => blockDAO.blocks(from, to) map { blocks => Ok(jsonResultOk(Json.toJson(blocks))) }
		}
	}

	def blocksListPage = deadbolt.WithAuthRequest()(parse.json) { implicit request =>
		fieldIntOpt("page_id")(pageIdOpt => fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			blockDAO.blocksListPage(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				pageIdOpt.getOrElse(0),
				Seq.empty,
				filterOpt) map { blocks =>
				Ok(views.html.app.components.pagparts.blocksListPage(blocks))
			}
		}))
	}

	def blocksListPagesCount = deadbolt.WithAuthRequest()(parse.json) { implicit request =>
		fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			blockDAO.blocksListPagesCount(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				filterOpt) map { count => Ok(count.toString) }
		})
	}

	def blocks = deadbolt.WithAuthRequest()() { implicit request =>
		future(Ok(views.html.app.blocks()))
	}

	def latestBlocks = deadbolt.WithAuthRequest()() { implicit request =>
		blockDAO.height map {
			case Some(height) =>
				val from = height - AppConstants.DEFAULT_PAGE_SIZE
				(if (from > 0) from else 0L, height)
			case _ =>
				(0L, 0L)
		} flatMap { case (from, to) => blockDAO.blocks(from, to) map { blocks =>
			Ok(views.html.app.components.latestBlocks(blocks.sortBy(_.timestamp).reverse))
		}}
	}



}
