package models.dao.slick.table

import models.BCAccount

trait BCAccountTable extends CommonTable {

  import dbConfig.profile.api._

  class InnerCommonTable(tag: Tag) extends Table[BCAccount](tag, "bc_accounts") with DynamicSortBySupport.ColumnSelector {
    def id = column[String]("id")
    def blockId = column[String]("block_id")
    def blockHeight = column[Long]("block_height")
    def name = column[Option[String]]("name")
    def amount = column[Long]("amount")

		def * = (
			id,
			blockId,
			blockHeight,
			name,
			amount) <> [BCAccount](t => BCAccount(
			t._1,
			t._2,
			t._3,
			t._4,
			t._5), t => Some(
			(t.id,
				t.blockId,
				t.blockHeight,
				t.name,
				t.amount)))

    override val select = Map(
      "name" -> (this.name))
  }

  val table = TableQuery[InnerCommonTable]
  
}
