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

package org.kevoree.framework

import org.kevoree.ContainerRoot
import java.util.zip.{Deflater, Inflater}
import org.slf4j.LoggerFactory
import java.io._
import org.kevoree.loader.ContainerRootLoader
import org.kevoree.serializer.ModelSerializer
import xml.PrettyPrinter

object KevoreeXmiHelper {

  val logger = LoggerFactory.getLogger(this.getClass)

  def save(uri: String, root: ContainerRoot) {
    //CHECK DIRECTORY CREATION
    if(logger.isDebugEnabled) logger.debug("XmiHelper::Save::Save asked in " + uri + ". Checking folder with separator:" + File.separator)
    val folderUri = if(uri.contains(File.separator)){
      uri.substring(0,uri.lastIndexOf(File.separator))
    } else {
      uri
    }
  //  val folderUri = uri.substring(0,uri.lastIndexOf(File.separator))
    if(logger.isDebugEnabled) logger.debug("XmiHelper::Save::Checking and/or creating foler:" + folderUri)
    val folder = new File(folderUri)
    if (!folder.exists) folder.mkdirs
    val serializer = new ModelSerializer
    val pp = new PrettyPrinter(3000,1)
    if(logger.isDebugEnabled) logger.debug("XmiHelper::Save::Serializing in :" + uri)
    val outputFile = new File(uri);
    if(!outputFile.exists) {
      outputFile.createNewFile
      if(logger.isDebugEnabled) logger.debug("XmiHelper::Save::Creating new file.")
    }
    val fileWrite = new FileWriter(outputFile)
    fileWrite.append(pp.format(serializer.serialize(root)))
    fileWrite.flush()
    fileWrite.close()
  }

  def saveToString(root:ContainerRoot, prettyPrint : Boolean) : String = {
    val serializer = new ModelSerializer
    val res = serializer.serialize(root)
    if(prettyPrint) {
    val pp = new PrettyPrinter(3000,1)
      pp.format(res)
    } else {
      res.toString()
    }
  }

  def loadString(model : String) : ContainerRoot = {
    if(logger.isDebugEnabled) logger.debug("load model from String")
    val localModel = ContainerRootLoader.loadModel(model);
    localModel match {
      case Some(m) => m
      case None => println("Model not loaded!"); null
    }
  }

  def load(uri: String): ContainerRoot = {
    if(logger.isDebugEnabled) logger.debug("load model from => " + uri)
    val localModel = ContainerRootLoader.loadModel(new File(uri));
    localModel match {
      case Some(m) => m
      case None => println("Model not loaded!"); null
    }

  }

  def loadStream(input: InputStream): ContainerRoot = {
    val localModel = ContainerRootLoader.loadModel(input);
    localModel match {
      case Some(m) => m
      case None => println("Model not loaded!"); null
    }

  }

  def saveStream(output: OutputStream, root: ContainerRoot) : Unit = {
    val serializer = new ModelSerializer
    val result = serializer.serialize(root)
    val pr = new PrintWriter(output)
    pr.print(result)
    pr.flush()
  }

  def saveCompressedStream(output: OutputStream, root: ContainerRoot) : Unit = {
    val modelStream = new ByteArrayOutputStream()
    saveStream(modelStream, root)
    output.write(ZipUtil.compressByteArray(modelStream.toByteArray))
    output.flush()

  }

  def loadCompressedStream(input: InputStream): ContainerRoot = {
    val inputData: Array[Byte] = Stream.continually(input.read).takeWhile(-1 !=).map(_.toByte).toArray
    val inputS = new ByteArrayInputStream(ZipUtil.uncompressByteArray(inputData))
    loadStream(inputS)
  }


}
