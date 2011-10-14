/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.merger.tests.components

import org.junit._
import org.scalatest.junit.AssertionsForJUnit
import org.kevoree.merger.KevoreeMergerComponent
import org.kevoree.merger.tests.MergerTestSuiteHelper
import org.kevoree.TypeDefinition
import org.kevoree.api.service.core.merger.MergerService

class ComponentsTest extends MergerTestSuiteHelper  {

   var component : MergerService = null

  @Before def initialize() {
    component = new KevoreeMergerComponent
  }

  @Test def verifyComponentTypeAdded() {
     val mergedModel = component.merge(model("artFragments/lib4test-ComponentRemoved.art2"), model("artFragments/lib4test-base.art2"))
    mergedModel testSave ("artFragments","lib4test-ComponentAddedMerged.art2")

    mergedModel.getTypeDefinitions.toArray.find(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName.equals("ComponentB")
    ) match {
      case None => fail("ComponentB have not been properly added.")
      case Some(component) =>
    }
  }

  @Test def verifyComponentTypeRemoved() {
      val mergedModel = component.merge(model("artFragments/lib4test-base.art2"), model("artFragments/lib4test-ComponentRemoved.art2"))
    mergedModel testSave ("artFragments","lib4test-ComponentRemovedMerged.art2")

    mergedModel.getTypeDefinitions.toArray.find(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName.equals("ComponentB")
    ) match {
      case None =>
      case Some(component) => fail("ComponentB have not been properly removed.")
    }
  }

  @Test def verifyComponentTypeRenamed() {
      val mergedModel = component.merge(model("artFragments/lib4test-base.art2"), model("artFragments/lib4test-ComponentRenamed.art2"))
    mergedModel testSave ("artFragments","lib4test-ComponentRenamedMerged.art2")

    mergedModel.getTypeDefinitions.toArray.find(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName.equals("ComponentB")
    ) match {
      case None =>
      case Some(component) => fail("ComponentB have not been properly renamed.")
    }

    mergedModel.getTypeDefinitions.toArray.find(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName.equals("ComponentC")
    ) match {
      case None => fail("ComponentC is not present. ComponentB have not been correctly renamed.")
      case Some(component) => 
    }

  }

}
