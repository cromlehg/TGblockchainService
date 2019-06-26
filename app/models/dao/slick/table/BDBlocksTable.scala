package models.dao.slick.table

import models.BDBlock

trait BDBlocksTable extends CommonTable {

	import dbConfig.profile.api._

	class InnerCommonTable(tag: Tag) extends Table[BDBlock](tag, "blocks") with DynamicSortBySupport.ColumnSelector {
		def id = column[String]("id")

		def height = column[Long]("height")

		def version = column[Int]("version")

		def timestamp = column[Long]("timestamp")

		def parentId = column[String]("parent_id")

		def generator = column[String]("generator")

		def signature = column[String]("signature")

		def * = (id,
			height,
			version,
			timestamp,
			parentId,
			generator,
			signature) <>[BDBlock](t =>
			BDBlock(t._1,
				t._2,
				t._3,
				t._4,
				t._5,
				t._6,
				t._7), t => Some(
				(t.id,
				t.height,
				t.version,
				t.timestamp,
				t.parentId,
				t.generator,
				t.signature)))

		override val select = Map(
			"id" -> (this.id),
			"height" -> (this.height),
			"parent_id" -> (this.parentId),
			"generator" -> (this.generator),
			"signature" -> (this.signature))
	}

	val table = TableQuery[InnerCommonTable]

}
