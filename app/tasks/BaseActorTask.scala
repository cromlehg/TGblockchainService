package tasks

import akka.actor.ActorSystem
import javax.inject.Inject
import models.dao.{BCAccountDAO, BDBlockDAO, BDTxDAO, OptionDAO}
import models.{BDBlock, BDTransactionKinds}
import play.Logger
import play.api.Configuration
import services.MBService

import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

class BaseActorTask @Inject()(blocksDAO: BDBlockDAO,
															txDAO: BDTxDAO,
															bcAccountDAO: BCAccountDAO,
															optionsDAO: OptionDAO,
															actorSystem: ActorSystem,
															config: Configuration,
															mbService: MBService)(implicit executionContext: ExecutionContext) {

	import scala.concurrent.Future.{successful => future}

	val genesisAddr = config.get[String]("techgen.genesis.address")

	val genesisBlockId = config.get[String]("techgen.genesis.block.id")

	actorSystem.scheduler.schedule(initialDelay = 0.minutes, interval = 10000.milliseconds) {

		Logger.debug("Sync blocks task started...")

		def syncBlocksTask = blocksDAO.lastBlock.map(_ match {
			case Some(block) =>
				Logger.debug("Current storage blocks height - " + block.height)
				(block.height + 1).toString
			case _ =>
				Logger.debug("Storage are empty. Try to get genesis block")
				"genesis"
		}).flatMap { id =>

			def getAndSave(targetId: String): Future[Boolean] =
				mbService.getBlock(targetId).flatMap(_ match {
					case Left(t) =>
						Logger.debug("Node can't contains block with height " + id + ". Or may be error occurs")
						future(false)
					case Right(nodeBlock) =>
						blocksDAO.lastBlock.flatMap(_ match {
							case Some(localLastBlock) =>
								Logger.debug("Node block height - " + nodeBlock.height)
								if (localLastBlock.height == nodeBlock.height) {
									Logger.debug("Successfully synced!")
									future(true)
								} else if (localLastBlock.height > nodeBlock.height) {
									Logger.debug("Node younger!")
									future(true)
								} else {
									Logger.debug("Storage younger! Save node block")
									saveBlock(nodeBlock).flatMap { _ =>
										getAndSave((nodeBlock.height + 1).toString)
									}
								}
							case _ =>
								Logger.debug("Storage are empty! Check block height - must be 0 - genesis")
								if (nodeBlock.height == 0) {
									Logger.debug("Genesis block height checked. Save to storage!")
									saveBlock(nodeBlock).flatMap { _ =>
										getAndSave((nodeBlock.height + 1).toString)
									}
								} else {
									Logger.debug("Got genesis block with wrong height " + nodeBlock.height + ", and id " + nodeBlock.id)
									future(true)
								}
						})
				})

			getAndSave(id)

		}

		val result = bcAccountDAO.findBCAccountById(genesisAddr).map {
			case Some(_) =>
				syncBlocksTask
			case _ =>
				mbService.getState(genesisAddr).flatMap(_ match {
					case Left(t) =>
						Logger.debug("Error occurs: " + t)
						future(false)
					case Right(bcAccountState) =>
						bcAccountDAO.createBCAccount(bcAccountState.target,
							genesisBlockId,
							0,
							bcAccountState.name,
							bcAccountState.value).flatMap { _ =>
							syncBlocksTask
						}
				})
		}

		Await.ready(result, Duration.Inf).onComplete {
			case Success(_) => Logger.debug("Sync blocks task success")
			case Failure(error) => Logger.debug("Sync blocks task fail: " + error.printStackTrace)
		}

	}

	def saveBlock(b: BDBlock): Future[BDBlock] = {
		blocksDAO.create(b.id,
			b.height,
			b.version,
			b.timestamp,
			b.parentId,
			b.generator,
			b.signature).flatMap { _ =>

			Future.sequence(b.transactions.map { tx =>
				tx.kind match {
					case BDTransactionKinds.TX_TRANSFER_ASSETS =>
						Logger.debug("Transfer " + tx.amount + " asset from " + tx.signer + " to " + tx.recepient + " transaction fetched.")

						txDAO.create(tx.id,
							b.id,
							b.height,
							tx.nonce,
							tx.signer,
							tx.signature,
							tx.kind,
							tx.recepient,
							tx.amount,
							b.timestamp) flatMap { _ =>

							Logger.debug("Transaction created in db " + tx.id)

							tx.recepient.fold(future(b)) { recipient =>
								tx.amount.fold(future(b)) { amount =>
									bcAccountDAO.findBCAccountById(tx.signer) flatMap (_ match {
										case Some(bcAccount) =>
											bcAccountDAO.updateBCAccount(bcAccount.id,
												b.id,
												b.height,
												bcAccount.name,
												bcAccount.amount - amount).flatMap { _ =>
												bcAccountDAO.findBCAccountById(recipient).flatMap(_ match {
													case Some(inBcAccount) =>
														bcAccountDAO.updateBCAccount(inBcAccount.id,
															b.id,
															b.height,
															inBcAccount.name,
															inBcAccount.amount + amount)
													case _ =>
														bcAccountDAO.createBCAccount(recipient,
															b.id,
															b.height,
															None,
															amount)
												}).map(_ => b)
											}
										case _ =>
											future(b)
									})
								}
							}

						}
					case _ => Future.successful(None)
				}
			}).map(_ => b)
		}
	}

}
