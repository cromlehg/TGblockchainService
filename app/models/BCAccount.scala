package models

case class BCAccount(val id: String,
										 val blockId: String,
										 val blockHeight: Long,
										 val name: Option[String],
										 val amount: Long) {

	override def equals(obj: Any) = obj match {
		case t: BCAccount => t.id == id
		case _ => false
	}

  override def hashCode = id.hashCode

}

object BCAccount {

	def apply(id: String,
						blockId: String,
            blockHeight: Long,
            name: Option[String],
            amount: Long): BCAccount =
		new BCAccount(id,
			blockId,
			blockHeight,
			name,
			amount)

}
