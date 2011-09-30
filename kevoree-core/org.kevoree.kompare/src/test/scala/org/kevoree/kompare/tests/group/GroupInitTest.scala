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
package org.kevoree.kompare.tests.group

import org.scalatest.junit.AssertionsForJUnit
import org.kevoree.kompare.tests.KompareSuite
import org.junit._
import org.kevoree.kompare.{JavaSePrimitive, KevoreeKompareBean}

class GroupInitTest extends AssertionsForJUnit with KompareSuite {

  var component : KevoreeKompareBean = null

  @Before def initialize() {
    component = new KevoreeKompareBean
  }

  @Test def verifyINIT() {

    var kompareModel = component.kompare(emptyModel, model("test_instance/groupInitAddOneTwoBinding.kev"), "duke")

    //var kompareModel = component.kompare(model("tests_dictionary/dictionary_1.kev"), model("tests_dictionary/dictionary_2.kev"), "node-0")
    //error("NOT IMPLEMENTED YET")

    kompareModel.shouldContain(JavaSePrimitive.AddType,"GossipGroup")
    kompareModel.shouldContain(JavaSePrimitive.AddInstance,"group1426020324")


    //kompareModel.print

  }

}