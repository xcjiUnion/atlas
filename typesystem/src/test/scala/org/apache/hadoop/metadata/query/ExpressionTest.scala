/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.metadata.query

import com.google.common.collect.ImmutableList
import org.apache.hadoop.metadata.BaseTest
import org.apache.hadoop.metadata.types._
import org.junit.{Before, Test}
import Expressions._

class ExpressionTest extends BaseTest {

  @Before
  override def setup {
    super.setup

    QueryTestsUtils.setupTypes

  }

  @Test def testClass: Unit = {
    val e = QueryProcessor.validate(_class("DB"))
    println(e)
  }

  @Test def testFilter: Unit = {
    val e = QueryProcessor.validate(_class("DB").where(id("name").`=`(string("Reporting"))))
    println(e)
  }

  @Test def testSelect: Unit = {
    val e = QueryProcessor.validate(_class("DB").where(id("name").`=`(string("Reporting"))).
    select(id("name"), id("owner")))
    println(e)
  }

  @Test def testNegTypeTest: Unit = {
    try {
      val e = QueryProcessor.validate(_class("DB").where(id("name")))
      println(e)
    } catch {
      case e : ExpressionException if e.getMessage.endsWith("expression: DB where name") => ()
    }
  }

  @Test def testIsTrait: Unit = {
    val e = QueryProcessor.validate(_class("DB").where(isTrait("Jdbc")))
    println(e)
  }

  @Test def testIsTraitNegative: Unit = {
    try {
    val e = QueryProcessor.validate(_class("DB").where(isTrait("Jdb")))
    println(e)
    } catch {
      case e : ExpressionException if e.getMessage.endsWith("not a TraitType, expression:  is Jdb") => ()
    }
  }

  @Test def testhasField: Unit = {
    val e = QueryProcessor.validate(_class("DB").where(hasField("name")))
    println(e)
  }

  @Test def testHasFieldNegative: Unit = {
    try {
      val e = QueryProcessor.validate(_class("DB").where(hasField("nam")))
      println(e)
    } catch {
      case e : ExpressionException if e.getMessage.endsWith("not a TraitType, expression:  is Jdb") => ()
    }
  }

  @Test def testFieldReference: Unit = {
    val e = QueryProcessor.validate(_class("DB").field("Table"))
    println(e)
  }

  @Test def testBackReference: Unit = {
    val e = QueryProcessor.validate(
      _class("DB").as("db").field("Table").where(id("db").field("name").`=`(string("Reporting"))))
    println(e)
  }

  @Test def testArith: Unit = {
    val e = QueryProcessor.validate(_class("DB").where(id("name").`=`(string("Reporting"))).
      select(id("name"), id("createTime") + int(1)))
    println(e)
  }

  @Test def testComparisonLogical: Unit = {
    val e = QueryProcessor.validate(_class("DB").where(id("name").`=`(string("Reporting")).
      and(id("createTime") + int(1) > int(0))))
    println(e)
  }

  @Test def testJoinAndSelect1: Unit = {
    val e = QueryProcessor.validate(
      _class("DB").as("db").field("Table").as("tab").where((id("createTime") + int(1) > int(0))
        .and(id("db").field("name").`=`(string("Reporting")))).select(id("db").field("name").as("dbName"), id("tab").field("name").as("tabName"))
    )
    println(e)
  }

  @Test def testJoinAndSelect2: Unit = {
    val e = QueryProcessor.validate(
      _class("DB").as("db").field("Table").as("tab").where((id("createTime") + int(1) > int(0))
        .or(id("db").field("name").`=`(string("Reporting"))))
        .select(id("db").field("name").as("dbName"), id("tab").field("name").as("tabName"))
    )
    println(e)
  }

  @Test def testJoinAndSelect3: Unit = {
    val e = QueryProcessor.validate(
      _class("DB").as("db").field("Table").as("tab").where((id("createTime") + int(1) > int(0))
        .and(id("db").field("name").`=`(string("Reporting")))
        .or(id("db").hasField("owner")))
        .select(id("db").field("name").as("dbName"), id("tab").field("name").as("tabName"))
    )
    println(e)
  }

  @Test def testJoinAndSelect4: Unit = {
    val e = QueryProcessor.validate(
      _class("DB") as "db" join "Table" as "tab" where (
      id("createTime") + int(1) > int(0) and
        ( id("db") `.` "name" `=` string("Reporting") ) or
        ( id("db") hasField "owner" )
      ) select (
        id("db") `.` "name" as "dbName", id("tab") `.` "name" as "tabName"
      )
    )
    println(e)
  }
}
