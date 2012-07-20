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
package org.slf4j.impl;

import org.slf4j.IMarkerFactory;
import org.slf4j.MarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MarkerFactoryBinder;

/**
 * 
 * The binding of {@link org.slf4j.MarkerFactory} class with an actual instance of
 * {@link org.slf4j.IMarkerFactory} is performed using information returned by this class.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class StaticMarkerBinder implements MarkerFactoryBinder {

  /**
   * The unique instance of this class.
   */
  public static final StaticMarkerBinder SINGLETON = new StaticMarkerBinder();
  
  final IMarkerFactory markerFactory = new BasicMarkerFactory();
  
  private StaticMarkerBinder() {
  }
  
  /**
   * Currently this method always returns an instance of 
   * {@link org.slf4j.helpers.BasicMarkerFactory}.
   */
  public IMarkerFactory getMarkerFactory() {
    return markerFactory;
  }
  
  /**
   * Currently, this method returns the class name of
   * {@link org.slf4j.helpers.BasicMarkerFactory}.
   */
  public String getMarkerFactoryClassStr() {
    return BasicMarkerFactory.class.getName();
  }
  
  
}
