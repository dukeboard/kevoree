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
package org.kevoree.tools.marShell.interpreter.sub

import org.kevoree.tools.marShell.ast.{AddChildStatment}
import org.kevoree.tools.marShell.interpreter.{KevsInterpreterContext, KevsAbstractInterpreter}
import org.slf4j.LoggerFactory

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 21/11/11
 * Time: 16:12
 *
 * @author Erwan Daubert
 * @version 1.0
 */

case class KevsAddChildInterpreter (addChild: AddChildStatment) extends KevsAbstractInterpreter {
  val logger = LoggerFactory.getLogger(this.getClass);

  def interpret (context: KevsInterpreterContext): Boolean = {
    context.model.getNodes.find(node => node.getName == addChild.childNodeName) match {
      case None => logger.error("Unknown child name:" + addChild.childNodeName + "\nThe node must already exist. Please check !"); false
      case Some(child) => {
        context.model.getNodes.find(node => node.getName == addChild.fatherNodeName) match {
          case None => {
            logger.error("Unknown father name:" + addChild.childNodeName + "\nThe node must already exist. Please check !");
            false
          }
          case Some(father) => {
            father.getHosts.find(c => c.getName == child.getName) match {
              case None => {
                context.model.getNodes.find(n => n.getHosts.find(c => c.getName == child.getName).isDefined) match {
                  case None => father.addHosts(child) ; true
                  case Some(f) => logger.error("The child {} has already a parent: {}", child.getName, f.getName); false
                }
              }
              case Some(c) => logger.warn("The node {} is already a child of {}", child.getName, father.getName); true
            }
          }
        }
      }
    }
  }
}