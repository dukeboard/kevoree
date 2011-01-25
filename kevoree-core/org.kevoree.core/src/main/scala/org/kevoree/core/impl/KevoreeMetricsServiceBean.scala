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

package org.kevoree.core.impl

import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.framework.modelService._

case class KevoreeMetricsServiceBean(handler : KevoreeModelHandlerService) extends MetricsService {

  def updatePortMetric(nodeName : String,componentName:String,portName:String,value :String,typeName : String)={
    println("UPDATE PMETRIC "+nodeName+"-"+componentName+"-"+portName)
  }
  def updateChannelMetric(nodeName : String,channelName:String,value :String,typeName : String)={
    println("UPDATE CMETRIC "+nodeName+"-"+channelName)
  }
  
}
