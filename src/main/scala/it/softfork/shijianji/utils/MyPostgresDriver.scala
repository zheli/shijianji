package it.softfork.shijianji.utils

import com.github.tminglei.slickpg._

// See https://github.com/tminglei/slick-pg/commit/3e2248489b342a68fe036c52da2aaaaf6971fae0#diff-8075aef163bbbffd8ec17ac278417407
trait MyPostgresDriver extends ExPostgresProfile
  with PgArraySupport
  with PgDate2Support
  with PgPlayJsonSupport
  with PgNetSupport
  with PgLTreeSupport
  with PgRangeSupport
  with PgHStoreSupport
  with PgSearchSupport {
  override val pgjson = "jsonb"
  override val api = new API with ArrayImplicits
    with DateTimeImplicits
    with PlayJsonImplicits
    with NetImplicits
    with LTreeImplicits
    with RangeImplicits
    with HStoreImplicits
    with SearchImplicits
    with SearchAssistants {}
  }

object MyPostgresDriver extends MyPostgresDriver
