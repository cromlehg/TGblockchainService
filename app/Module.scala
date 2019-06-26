
import be.objectify.deadbolt.scala.cache.HandlerCache
import com.google.inject.AbstractModule
import models.dao._
import models.dao.slick._
import play.api.{Configuration, Environment}
import security.BaseHandlerCache
import services.{MBService, MBServiceImpl, MailGunMailer, Mailer}

class Module(environment: Environment,
						 configuration: Configuration) extends AbstractModule {

	override def configure(): Unit = {
		bind(classOf[HandlerCache]).to(classOf[BaseHandlerCache])

		bind(classOf[BDBlockDAO]).to(classOf[SlickBDBlockDAO])
		bind(classOf[BDBlockDAOCloseHook]).asEagerSingleton()

		bind(classOf[BDTxDAO]).to(classOf[SlickBDTxDAO])
		bind(classOf[BDTxDAOCloseHook]).asEagerSingleton()

		bind(classOf[AccountDAO]).to(classOf[SlickAccountDAO])
		bind(classOf[AccountDAOCloseHook]).asEagerSingleton()

		bind(classOf[RoleDAO]).to(classOf[SlickRoleDAO])
		bind(classOf[RoleDAOCloseHook]).asEagerSingleton()

		bind(classOf[PermissionDAO]).to(classOf[SlickPermissionDAO])
		bind(classOf[PermissionDAOCloseHook]).asEagerSingleton()

		bind(classOf[TagDAO]).to(classOf[SlickTagDAO])
		bind(classOf[TagDAOCloseHook]).asEagerSingleton()

		bind(classOf[SessionDAO]).to(classOf[SlickSessionDAO])
		bind(classOf[SessionDAOCloseHook]).asEagerSingleton()

		bind(classOf[SNAccountDAO]).to(classOf[SlickSNAccountDAO])
		bind(classOf[SNAccountDAOCloseHook]).asEagerSingleton()

		bind(classOf[BCAccountDAO]).to(classOf[SlickBCAccountDAO])
		bind(classOf[BCAccountDAOCloseHook]).asEagerSingleton()

		bind(classOf[OptionDAO]).to(classOf[SlickOptionDAO])
		bind(classOf[OptionDAOCloseHook]).asEagerSingleton()

		bind(classOf[PostDAO]).to(classOf[SlickPostDAO])
		bind(classOf[PostDAOCloseHook]).asEagerSingleton()

		bind(classOf[MenuDAO]).to(classOf[SlickMenuDAO])
		bind(classOf[MenuDAOCloseHook]).asEagerSingleton()

		bind(classOf[CommentDAO]).to(classOf[SlickCommentDAO])
		bind(classOf[CommentDAOCloseHook]).asEagerSingleton()

		bind(classOf[Mailer]).to(classOf[MailGunMailer])

		bind(classOf[MBService]).to(classOf[MBServiceImpl])
	}

}
