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
package org.kevoree.kcl

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
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 30/01/12
 * Time: 10:52
 */

object Tester extends App {

  val jcl2 = new KevoreeJarClassLoader
  jcl2.add("/Users/duke/Desktop/org.kevoree.library.javase.webserver.collaborationToolsBasics20987646844231618581117939900.jar")
  println(jcl2.getResourceAsStream("ihmcodemirror/E86E16CE2F26CDDCFD23F909A16BAE6A.gwt.rpc"))

  //jcl2.add("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-platform/org.kevoree.platform.standalone.gui/target/org.kevoree.platform.standalone.gui-1.7.4.jar")
  /*
 println("Begin loop")
for (i <- 0 until 30) {


    val jcl3 = new KevoreeJarClassLoader
   // jcl3.setLazyLoad(false)
    jcl3.add("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-corelibrary/javase/org.kevoree.library.javase.grapher/target/org.kevoree.library.javase.grapher-1.7.5-SNAPSHOT.jar")
    jcl3.addSubClassLoader(jcl2)
    jcl3.loadClass("org.kevoree.library.javase.grapher.Grapher")
    println("iteration i=" + i)
  }

//   System.gc()


Thread.sleep(Long.MaxValue)

  */


  /*
  val bigURL = new URL("jar:/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-library/frascati/org.kevoree.library.frascati.frascatiNode/target/org.kevoree.library.frascati.frascatiNode-1.6.0-SNAPSHOT.jar!/lib/frascati-factory-tools-1.4.jar!/META-INF/MANIFEST.MF")
  bigURL.openConnection()


  URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory{
    def createURLStreamHandler(p1: String): URLStreamHandler = new KevoreeJarClassLoaderHandler(null)
  })

  val key = "java.protocol.handler.pkgs";
  var newValue = "org.kevoree.extra.jcl.classpath";
  //System.setProperty(key, newValue);
  println(System.getProperty(key))

  val u = new URL("classpath:some/package/resource.extension")
  u.openConnection()
  */

  /*
    for (i <- 0 until 10) {

      val jcl2 = new KevoreeJarClassLoader
      jcl2.add("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-platform/org.kevoree.platform.osgi.standalone/target/org.kevoree.platform.osgi.standalone-1.5.1-SNAPSHOT.jar")
      val jcl = new KevoreeJarClassLoader
      jcl.addSubClassLoader(jcl2)
      jcl2.loadClass("org.kevoree.platform.osgi.standalone.App")


      println("iteration i=" + i)
    }


    System.gc()
  */

}