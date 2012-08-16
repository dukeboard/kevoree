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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.tools.annotation.generator

import java.io.File
import org.kevoree.framework.aspects.KevoreeAspects._

import org.kevoree.framework.{KevoreeGeneratorHelper, Constants}
import javax.tools.StandardLocation
import org.kevoree.{TypedElement, ContainerRoot, MessagePortType, PortTypeRef, ComponentType => KevoreeComponentType, ServicePortType}

object KevoreeProvidedPortGenerator {

  def generate(root: ContainerRoot, filer: javax.annotation.processing.Filer, ct: KevoreeComponentType, ref: PortTypeRef, targetNodeType: String) {
    val portPackage = KevoreeGeneratorHelper.getTypeDefinitionGeneratedPackage(ct, targetNodeType)
    // var portPackage = ct.getFactoryBean().substring(0, ct.getFactoryBean().lastIndexOf("."));
    val portName = ct.getName + "PORT" + ref.getName
    val wrapper = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", new String(portPackage.replace(".", "/") + "/" + portName + ".scala"))
    val writer = wrapper.openWriter()
    writer.append("package " + portPackage + "\n")
    writer.append("import org.kevoree.framework.port._\n")
    writer.append("import " + KevoreeGeneratorHelper.getTypeDefinitionBasePackage(ct) + "._\n")
    writer.append("import scala.{Unit=>void}\n")
    writer.append("class " + portName + "(component : " + ct.getName + ") extends " + ref.getRef.getName + " with KevoreeProvidedPort {\n")

    writer.append("def getName : String = \"" + ref.getName + "\"\n")
    writer.append("def getComponentName : String = component.getName \n")

    ref.getRef match {
      case mPT: MessagePortType => {
        /* GENERATE METHOD MAPPING */
        writer.append("def process(o : Object) = {this ! o}\n")

        ref.getMappings.find(map => {
          map.getServiceMethodName.equals(Constants.KEVOREE_MESSAGEPORT_DEFAULTMETHOD)
        }) match {
          case Some(mapping) => {
            /* GENERATE LOOP */
            writer.append("override def internal_process(msg : Any)= msg match {\n")
            /* CALL MAPPED METHOD */
            writer.append("case _ @ msg =>try{component.")

            writer.append(mapping.getBeanMethodName + "(")
            if(mapping.getParamTypes != null && mapping.getParamTypes != "" && mapping.getParamTypes.split(",").size >1 ){
              
              val elemsT = mapping.getParamTypes.split(",")
              elemsT.foreach{ t =>
                t match {
                  case "java.lang.String" => writer.append("getName")
                  case "java.lang.Object" => writer.append("msg")
                  case _ => writer.append("null")
                }
                if(elemsT.last != t){
                  writer.append(",")
                }
              }
              
            } else {
              writer.append("msg")
            }
            
            
            writer.append(")}catch{case _ @ e => {e.printStackTrace();println(\"Uncatched exception while processing Kevoree message\")}}\n")
            writer.append("}\n")
          }
          case None => {
            error("KevoreeProvidedPortGenerator::No mapping found for method '" + Constants.KEVOREE_MESSAGEPORT_DEFAULTMETHOD + "' of MessagePort '" + ref.getName + "' in component '" + ct.getName + "'")
            error("No mapping found for method '" + Constants.KEVOREE_MESSAGEPORT_DEFAULTMETHOD + "' of MessagePort '" + ref.getName + "' in component '" + ct.getName + "'")

          }
        }
      }

      case sPT: ServicePortType => {
        /* CREATE INTERFACE ACTOR MOK */
        sPT.getOperations.foreach {
          op =>
          /* GENERATE METHOD SIGNATURE */
            writer.append("def " + op.getName + "(")
            op.getParameters.foreach {
              param =>
                writer.append(param.getName + ":" + param.getType.get.print('[', ']'))
                if (op.getParameters.indexOf(param) != (op.getParameters.size - 1)) {
                  writer.append(",")
                }
            }
            /* GENERATES RETURN TYPE in rt */
            var rt = op.getReturnType.get.getName
            if(op.getReturnType.get.getGenericTypes.size > 0) {
              rt += op.getReturnType.get.getGenericTypes.collect{case s:TypedElement=>s.getName}.mkString("[",",","]")
            }
            writer.append(") : " + rt + " ={\n")


            /* Generate method corpus */
            /* CREATE MSG OP CALL */
            writer.append("val msgcall = new org.kevoree.framework.MethodCallMessage\n")
            writer.append("msgcall.setMethodName(\"" + op.getName + "\")\n")
            op.getParameters.foreach {
              param =>
                writer.append("msgcall.getParams.put(\"" + param.getName + "\"," + param.getName + ".asInstanceOf[AnyRef])\n")
            }
            writer.append("(this !? msgcall).asInstanceOf[" + rt + "]")
            writer.append("}\n")
        }
        /* CREATE ACTOR LOOP */
        writer.append("override def internal_process(msg : Any)= msg match {\n")
        writer.append("case opcall : org.kevoree.framework.MethodCallMessage => reply(opcall.getMethodName match {\n")
        sPT.getOperations.foreach {
          op =>
          /* FOUND METHOD MAPPING */
            ref.getMappings.find(map => {
              map.getServiceMethodName.equals(op.getName)
            }) match {
              case Some(mapping) => {
                writer.append("case \"" + op.getName + "\"=> try { component." + mapping.getBeanMethodName + "(")
                var i = 0
                op.getParameters.foreach {
                  param =>
                    writer.append("if(opcall.getParams.containsKey(\"" + param.getName + "\")){")
                    writer.append("opcall.getParams.get(\"" + param.getName + "\").asInstanceOf[" + param.getType.get.print('[', ']') + "]")
                    writer.append("}else{")
                    writer.append("opcall.getParams.get(\"arg" + i + "\").asInstanceOf[" + param.getType.get.print('[', ']') + "]")
                    writer.append("}")

                    if (op.getParameters.indexOf(param) != (op.getParameters.size - 1)) {
                      writer.append(",")
                    }
                    i = i + 1
                }
                writer.append(")} catch {case _ @ e => e.printStackTrace();println(\"Uncatched exception while processing Kevoree message\");null }\n")

              }
              case None => {
                sys.error("No mapping found for method '" + op.getName + "' of ServicePort '" + ref.getName + "' in component '" + ct.getName + "'")
                //println("No mapping found for method '"+op.getName+"' of ServicePort '" + ref.getName + "' in component '" + ct.getName + "'")

              }
            }
        }
        writer.append("case _ @ o => println(\"uncatch message , method not found in service declaration : \"+o);null \n")
        writer.append("})")

        //writer.append("case _ @ msg => println(\"WTF!\");reply(null)")
        writer.append("}\n")
      }

    }

    writer.append("}\n")
    writer.close()
  }

}
