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
package org.kevoree.tools.ui.editor

import org.kevoree.ContainerRoot
import java.util.{UUID, Date, List}
import org.kevoree.api.service.core.handler._
import java.lang.{Long, String}
import org.kevoree.context.ContextRoot


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 28/11/11
 * Time: 10:48
 * To change this template use File | Settings | File Templates.
 */

class ModelHandlerServiceWrapper(kernel : KevoreeUIKernel) extends KevoreeModelHandlerService {

  def getLastModel: ContainerRoot = {
    kernel.getModelHandler.getActualModel
  }

  def getLastModification: Date = null

  def updateModel(model: ContainerRoot) {}

  def atomicUpdateModel(model: ContainerRoot): Date = null

  def getPreviousModel: List[ContainerRoot] = null

  def getNodeName: String = ""

  def registerModelListener(listener: ModelListener) {}

  def unregisterModelListener(listener: ModelListener) {}

  def getLastUUIDModel: UUIDModel = {
    return new UUIDModel {
      def getUUID: UUID = UUID.randomUUID()

      def getModel: ContainerRoot = kernel.getModelHandler.getActualModel
    }
  }

  def compareAndSwapModel(previousModel: UUIDModel, targetModel: ContainerRoot) {}

  def atomicCompareAndSwapModel(previousModel: UUIDModel, targetModel: ContainerRoot): Date = null

  def getContextModel: ContextRoot = null

  def acquireLock(callBack: ModelHandlerLockCallBack, timeout: Long) {callBack.lockRejected()}

  def releaseLock(uuid: UUID) {}

  def checkModel(targetModel: ContainerRoot): Boolean = true

  def updateModel(model: ContainerRoot, callback: ModelUpdateCallback) {

  }

  def compareAndSwapModel(previousModel: UUIDModel, targetModel: ContainerRoot, callback: ModelUpdateCallback) {

  }
}