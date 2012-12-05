/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.core.basechecker.nodechecker

import org.kevoree.ContainerRoot
import org.kevoree.api.service.core.checker.{CheckerService, CheckerViolation}
import java.util
import org.kevoree.NodeType
import org.kevoree.framework.aspects.NodeTypeAspect

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 21/09/12
 * Time: 07:07
 */
class NodeContainerChecker extends CheckerService {
  def check(model: ContainerRoot): java.util.List[CheckerViolation] = {
    val violations: java.util.List[CheckerViolation] = new util.ArrayList[CheckerViolation]()
    model.getNodes.foreach {
      node => //For each Node
        if (node.getHosts.size > 0) {
          val ntype = node.getTypeDefinition.asInstanceOf[NodeType]

          /*
          var hostedCapable = false
          ntype.getManagedPrimitiveTypes.foreach {
            ptype =>
              if (ptype.getName.toLowerCase.equals("addnode")) {
                hostedCapable = true;
              }
              if (ptype.getName.toLowerCase.equals("removenode")) {
                hostedCapable = true;
              }
          } */
          val hostedCapable = NodeTypeAspect(ntype).defineAdaptationPrimitiveType("addnode") && NodeTypeAspect(ntype).defineAdaptationPrimitiveType("removenode")
          if (!hostedCapable) {
            val violation: CheckerViolation = new CheckerViolation
            violation.setMessage(ntype.getName + " has no Node hosting capability " + node.getTypeDefinition.getName)
            violation.setTargetObjects(util.Arrays.asList(node))
            violations.add(violation)
          }
        }
    }
    violations
  }
}
