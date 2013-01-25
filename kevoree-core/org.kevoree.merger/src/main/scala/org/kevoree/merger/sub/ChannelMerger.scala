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
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.merger.sub

import org.kevoree.merger.Merger
import org.slf4j.LoggerFactory
import org.kevoree._
import scala.Some
import scala.collection.JavaConversions._


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/10/11
 * Time: 14:52
 */

trait ChannelMerger extends Merger with DictionaryMerger {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def mergeAllChannels(actualModel: ContainerRoot, modelToMerge: ContainerRoot) {
    //MERGE CHANNEL
    modelToMerge.getHubs.foreach(hub => {
      val currentHub = actualModel.findByQuery(hub.buildQuery(), classOf[Channel]) match {
        case e: Channel => {
          mergeDictionaryInstance(e, hub)
          e
        }
        case null => {
          actualModel.addHubs(hub)
          hub
        }
      }
    })
    //MERGE NEW BINDING
    modelToMerge.getMBindings.foreach {
      mb => {
        val foundHub: Channel = actualModel.findByQuery(mb.getHub.buildQuery(), classOf[Channel])
        if (foundHub != null) {
          actualModel.findByQuery(mb.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].buildQuery(), classOf[ContainerNode]) match {
            case foundNode: ContainerNode => {
              foundNode.getComponents.find(component => component.getName == mb.getPort.eContainer.asInstanceOf[ComponentInstance].getName) match {
                case Some(foundComponent) => {
                  (foundComponent.getRequired.toList ++ foundComponent.getProvided).find(port => port.getPortTypeRef.getName == mb.getPort.getPortTypeRef.getName) match {
                    case Some(foundPort) => {
                      val newbinding = KevoreeFactory.$instance.createMBinding
                      newbinding.setHub(foundHub)
                      foundHub.removeBindings(mb)
                      newbinding.setPort(foundPort)
                      foundPort.removeBindings(mb)
                      actualModel.getMBindings.find(mb => mb.getHub == foundHub && mb.getPort == foundPort) match {
                        case Some(pMB) => {
                          foundHub.removeBindings(pMB)
                          foundPort.removeBindings(pMB)
                          actualModel.removeMBindings(pMB)
                          actualModel.addMBindings(newbinding)
                        }
                        case None => {
                          actualModel.addMBindings(newbinding)
                        }
                      }
                    }
                    case None => logger.error("Error while merging binding, can't found port for name " + mb.getPort.getPortTypeRef.getName)
                  }
                }
                case None => logger.error("Error while merging binding, can't found component for name " + mb.getPort.eContainer.asInstanceOf[ComponentInstance].getName)
              }
            }
            case null => logger.error("Error while merging binding, can't found node for name " + mb.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName)
          }
        } else {
          logger.error("Error while merging binding, can't found channel for name " + mb.getHub.getName)
        }
      }
    }


  }
}