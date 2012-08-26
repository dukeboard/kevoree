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
package org.kevoree.platform.standalone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);


    public void start() throws Exception {

    //    System.setProperty("kevoree.log.level", "DEBUG");
      //  System.setProperty("node.bootstrap","/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-platform/org.kevoree.platform.standalone.min/boot.kevs");


        //TO REMOVE
        if(System.getProperty("node.groupType") == null){
            System.setProperty("node.groupType","NanoRestGroup");
        }
        

       // System.setProperty("node.update.timeout","100000");
        //System.setProperty("kevoree.offline","true");
        //System.setProperty("actors.enableForkJoin", "false");

        try {
            ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
            if (System.getProperty("node.log.appender.file") != null) {
                System.out.println("Kevoree log will out in file => " + System.getProperty("node.log.appender.file"));
            } else {
                root.detachAppender("FILE");
            }
        } catch (Exception e){
           //Logback not present
        }


        String node_name = System.getProperty("node.name");
        if (node_name == null || node_name.equals("")) {
            node_name = "node0";
            System.setProperty("node.name", node_name);
        }


        final KevoreeBootStrap kb = new KevoreeBootStrap();

        Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Hook") {

            public void run() {
                try {
                    kb.stop();
                } catch (Exception ex) {
                    logger.warn("Error stopping framework: ", ex);
                }
            }
        });

        kb.start();
    }

    public static void main(String[] args) throws Exception {

        Long startTime = System.currentTimeMillis();

        App app = new App();
        app.start();

        logger.info("Kevoree runtime boot time {} ms",(System.currentTimeMillis()-startTime));

    }
}
