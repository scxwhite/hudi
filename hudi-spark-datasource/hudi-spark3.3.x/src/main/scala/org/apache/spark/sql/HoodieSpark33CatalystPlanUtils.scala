/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql

import org.apache.spark.sql.catalyst.TableIdentifier
import org.apache.spark.sql.catalyst.expressions.{AttributeSet, Expression, ProjectionOverSchema}
import org.apache.spark.sql.catalyst.plans.logical.{LogicalPlan, TimeTravelRelation}
import org.apache.spark.sql.execution.command.RepairTableCommand
import org.apache.spark.sql.hudi.logical.TableArgumentRelation
import org.apache.spark.sql.types.StructType

object HoodieSpark33CatalystPlanUtils extends HoodieSpark3CatalystPlanUtils {

  override def isRelationTimeTravel(plan: LogicalPlan): Boolean = {
    plan.isInstanceOf[TimeTravelRelation]
  }

  override def getRelationTimeTravel(plan: LogicalPlan): Option[(LogicalPlan, Option[Expression], Option[String])] = {
    plan match {
      case timeTravel: TimeTravelRelation =>
        Some((timeTravel.table, timeTravel.timestamp, timeTravel.version))
      case _ =>
        None
    }
  }

  override def projectOverSchema(schema: StructType, output: AttributeSet): ProjectionOverSchema =
    ProjectionOverSchema(schema, output)

  override def isRepairTable(plan: LogicalPlan): Boolean = {
    plan.isInstanceOf[RepairTableCommand]
  }

  override def getRepairTableChildren(plan: LogicalPlan): Option[(TableIdentifier, Boolean, Boolean, String)] = {
    plan match {
      case rtc: RepairTableCommand =>
        Some((rtc.tableName, rtc.enableAddPartitions, rtc.enableDropPartitions, rtc.cmd))
      case _ =>
        None
    }
  }

  /**
   * if the logical plan is a TableArgumentRelation LogicalPlan.
   */
  override def isRelationTableArgument(plan: LogicalPlan): Boolean = {
    plan.isInstanceOf[TableArgumentRelation]
  }

  /**
   * Get the member of the TableArgumentRelation LogicalPlan.
   */
  override def getRelationTableArgument(plan: LogicalPlan): Option[(LogicalPlan, Map[String, String])] = {
    plan match {
      case tableArg: TableArgumentRelation =>
        Some((tableArg.table, tableArg.args))
      case _ =>
        None
    }
  }
}
