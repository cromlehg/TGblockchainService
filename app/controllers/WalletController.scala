package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.dao.{BCAccountDAO, BDBlockDAO, MenuDAO, OptionDAO}
import play.api.{Configuration, Logger}
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping, nonEmptyText}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import services.MBService

import scala.concurrent.ExecutionContext

@Singleton
class WalletController @Inject()(blockDAO: BDBlockDAO,
																 bcAccountDAO: BCAccountDAO,
																 mbService: MBService,
																 cc: ControllerComponents,
																 config: Configuration,
																 deadbolt: DeadboltActions
																)(implicit ec: ExecutionContext, optionDAO: OptionDAO, menuDAO: MenuDAO)
	extends AbstractController(cc)
		with I18nSupport with JSONSupport with LoggerSupport {


	val faucetAddr = config.get[String]("techgen.faucet.address")

	val faucetPk = config.get[String]("techgen.faucet.pk")

	val faucetAmount = config.get[Long]("techgen.faucet.amount")

	case class KeyPairData(val address: String)

	case class TransferData(val address: String, val pk: String, val to: String, val amount: Long)

	val keyPairForm = Form(
		mapping(
			"address" -> AppConstants.bcAddressVerifying)(KeyPairData.apply)(KeyPairData.unapply))

	val transferForm = Form(
		mapping(
			"address" -> AppConstants.bcAddressVerifying,
			"pk" -> AppConstants.pkVerifying,
			"to" -> AppConstants.bcAddressVerifying,
			"amount" -> longNumber(0, Long.MaxValue))(TransferData.apply)(TransferData.unapply))


	import scala.concurrent.Future.{successful => future}

	def generateKeyPair = deadbolt.WithAuthRequest()() { implicit request =>
		mbService.getGenerateKeyPair.map { keyPair =>
			keyPair.fold(err => BadRequest(jsonResultError(err)), t => Ok(jsonResultOk(Json.toJson(t))))
		}
	}

	def generateWallet = deadbolt.WithAuthRequest()() { implicit request =>
		future(Ok(views.html.app.generateWallet()))
	}

	def openWallet = deadbolt.WithAuthRequest()() { implicit request =>
		future(Ok(views.html.app.openWallet(keyPairForm)))
	}

	def processOpenWallet = deadbolt.WithAuthRequest()() { implicit request =>
		keyPairForm.bindFromRequest.fold(formWithErrors => future(BadRequest(views.html.app.openWallet(formWithErrors))), { kpData =>
			bcAccountDAO.getBCAccountStateById(kpData.address) map { bcAccount =>
				Ok(views.html.app.showWallet(transferForm.fill(TransferData(kpData.address, "", "", 0)), bcAccount))
			}
		})
	}

	def faucet = deadbolt.WithAuthRequest()() { implicit request =>
		bcAccountDAO.getBCAccountStateById(faucetAddr) map { bcAccount =>
			Ok(views.html.app.faucet(keyPairForm, bcAccount, faucetAmount))
		}
	}

	def processFaucet = deadbolt.WithAuthRequest()() { implicit request =>
		bcAccountDAO.getBCAccountStateById(faucetAddr) flatMap { bcAccount =>
			keyPairForm.bindFromRequest.fold(formWithErrors => future(BadRequest(views.html.app.faucet(formWithErrors, bcAccount, faucetAmount))), { transferData =>
				if (bcAccount.amount >= faucetAmount)
					mbService.transfer(faucetAddr, transferData.address, faucetAmount, faucetPk).map(_ match {
						case Right(hash) =>
							Logger.error(hash)
							Redirect(routes.TxsController.viewTx(hash))
							//Ok(views.html.app.transactionSent(hash))
						case Left(msg) =>
							val formWithErrors = keyPairForm.fill(transferData).withGlobalError(msg)
							BadRequest(views.html.app.faucet(formWithErrors, bcAccount, faucetAmount))
					})
				else {
					val formWithErrors = keyPairForm.fill(transferData).withGlobalError("Sorry, faucet is empty=( Try later.")
					future(Ok(views.html.app.faucet(formWithErrors, bcAccount, faucetAmount)))
				}
			})
		}
	}

	def processTransfer = deadbolt.WithAuthRequest()() { implicit request =>
		transferForm.bindFromRequest.fold(formWithErrors => {
			val address = formWithErrors.data("address")
			bcAccountDAO.getBCAccountStateById(address) map { bcAccount =>
				BadRequest(views.html.app.showWallet(formWithErrors, bcAccount))
			}
		}, { transferData =>
			mbService.transfer(transferData.address, transferData.to, transferData.amount, transferData.pk).flatMap(_ match {
				case Right(hash) =>
					future(Redirect(routes.TxsController.viewTx(hash)))
					//future(Ok(views.html.app.transactionSent(hash)))
				case Left(msg) =>
					val formWithErrors = transferForm.fill(transferData).withGlobalError(msg)
					bcAccountDAO.getBCAccountStateById(transferData.address) map { bcAccount =>
						BadRequest(views.html.app.showWallet(formWithErrors, bcAccount))
					}
			})
		})
	}


}
