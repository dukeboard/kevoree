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

package org.kevoree.api.configuration

object ConfigConstants extends Enumeration {
  type Value = ConfigConstant
  case class ConfigConstant(value : String,defaultValue : String) extends Val(value){
    def getValue = value
    def getDefaultValue = defaultValue
  }
  val KEVOREE_NODE_NAME = ConfigConstant("node.name","KEVOREEDefaultNodeName")
  val KEVOREE_NODE_BOOTSTRAP = ConfigConstant("node.bootstrap","")
  val KEVOREE_NODE_MODELSYNC_PORT = ConfigConstant("node.modelsync.port","auto")
  val KEVOREE_NODE_DISPATCHER_PORT = ConfigConstant("node.dispatcher.port","auto")
  val KEVOREE_CONFIG = ConfigConstant("KEVOREE.config",null)
}