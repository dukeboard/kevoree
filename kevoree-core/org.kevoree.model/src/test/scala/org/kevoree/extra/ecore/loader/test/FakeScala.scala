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
package org.kevoree.extra.ecore.loader.test

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 13/12/12
 * Time: 13:24
 */
class FakeScala {

  val s = List(3, 3)

  @inline def titi() {
    /*
s.foreach{
  sub => println(sub)
}  */

    /*
    for (sub <- s) {
      println(sub)
    }  */

    var size = 0
    while(size < s.size){
      println("YO")
      size+=1
    }

  }


}
