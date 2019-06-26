package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.dao._
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class AppController @Inject()(
	deadbolt: DeadboltActions,
	bdBlockDAO: BDBlockDAO,
	cc: ControllerComponents,
	postDAO: PostDAO,
	txDAO: BDTxDAO,
	config: Configuration
)(implicit ec: ExecutionContext, optionDAO: OptionDAO, menuDAO: MenuDAO)
	extends AbstractController(cc)
		with I18nSupport with LoggerSupport {

	import scala.concurrent.Future.{successful => future}

	def index = deadbolt.WithAuthRequest()() { implicit request =>

		def default = {
			bdBlockDAO.height map {
				case Some(height) =>
					val from = height - AppConstants.DEFAULT_PAGE_SIZE
					(if (from > 0) from else 0L, height)
				case _ =>
					(0L, 0L)
			} flatMap { case (from, to) => bdBlockDAO.blocks(from, to) flatMap { blocks =>
				txDAO.txsListPage(
					AppConstants.DEFAULT_PAGE_SIZE,
					1,
					Seq.empty,
					None
				) map { txs => Ok(views.html.app.index2(blocks.sortBy(_.timestamp).reverse, txs)) }
			}}
		}

		optionDAO
			.getOptionByName(models.Options.INDEX_PAGE_ID)
			.flatMap(_.fold(default) { option =>
				option.toOptLong.fold(default) { pageId =>
					postDAO.findPostById(pageId).flatMap {
						_.fold(default) { page =>
							future(Ok(views.html.app.viewPage(page)))
						}
					}
				}
			})

	}

	def panel = deadbolt.SubjectPresent()() { implicit request =>
		future(Ok(views.html.admin.panel()))
	}

}
