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

package org.kevoree.framework.aspects

import org.kevoree._
import KevoreeAspects._

case class DeployUnitAspect(self: DeployUnit) {

  def isModelEquals(other: DeployUnit): Boolean = {

    if (other.getUnitName != self.getUnitName || other.getGroupName != self.getGroupName || other.getVersion != self.getVersion) {
      return false
    }
    if (other.getTargetNodeType.isDefined && self.getTargetNodeType.isEmpty) {
      return false
    }
    if (other.getTargetNodeType.isEmpty && self.getTargetNodeType.isDefined) {
      return false
    }
    if (other.getTargetNodeType.isDefined && self.getTargetNodeType.isDefined) {
      other.getTargetNodeType.get.getName == self.getTargetNodeType.get.getName
    } else {
      true
    }

  }

  def isDeployUnitUsed(targetDU: DeployUnit): Boolean = {
    if (targetDU.isModelEquals(self)) {
      true
    } else {
      self.getRequiredLibs.exists(du => {
        du.isDeployUnitUsed(targetDU)
      })
    }
  }

}
