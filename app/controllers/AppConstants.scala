package controllers

import play.api.data.Form
import play.api.data.Forms._

object AppConstants {

  val APP_NAME = "TechGen"

  val VERSION = "0.1a"

  val BACKEND_NAME = APP_NAME + " " + VERSION

  val DEFAULT_PAGE_SIZE = 10

  val MAX_PAGE_SIZE = 100

  val SESSION_EXPIRE_TIME: Long = 3 * TimeConstants.DAY

  val PWD_MIN_LENGTH: Long = 12

	val SHORT_TITLE_DESCR = 50

  val DESCRIPTION_SIZE = 300

	val SHORT_DESCRIPTION_SIZE = 150

  val RETURN_URL = "referer"

	val HTTP_USER_AGENT = "User-Agent"

	val BC_ADDRESS_REGEXP_STRING = """[0-9a-fA-F]{74}"""

	val BC_PK_REGEXP_STRING = """[0-9a-fA-F]{64}"""

	val BC_ID_REGEXP_STRING = """[0-9a-fA-F]{64}"""

	val bcAddressVerifying = nonEmptyText.verifying("Address should be 74 length and contains only 0-9 or a-F symbols!", t => t.matches(BC_ADDRESS_REGEXP_STRING))

	val pkVerifying = nonEmptyText.verifying("Address should be 64 length and contains only 0-9 or a-F symbols!", t => t.matches(BC_PK_REGEXP_STRING))

	val idVerifying = nonEmptyText.verifying("Address should be 64 length and contains only 0-9 or a-F symbols!", t => t.matches(BC_ID_REGEXP_STRING))


}
