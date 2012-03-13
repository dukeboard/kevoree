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
package org.kevoree.platform.service

import org.kevoree.KevoreeFactory
import java.io.File
import org.kevoree.tools.aether.framework.AetherUtil

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 12/12/11
 * Time: 14:35
 *
 * @author Erwan Daubert
 * @version 1.0
 */

object KevoreeServiceUpdate extends App{

  val defaultLocation=args(0)

  var version="LATEST"
  if (args.length > 1) {
    version = args(1)
  }

  val jarFile: File = AetherUtil.resolveMavenArtifact("org.kevoree.platform.standalone", "org.kevoree.platform", version, List[String]("http://maven.kevoree.org/snapshots/", "http://maven.kevoree.org/release/"))

  if (jarFile.exists) {
    val p = Runtime.getRuntime.exec(Array[String]("cp", jarFile.getAbsolutePath, defaultLocation))
    p.waitFor()
  }
}