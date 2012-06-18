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
package org.kevoree.tools.ui.editor.command;

import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import scala.io.Source;

import javax.swing.*;

/**
 *
 * @author ffouquet
 */
public class LoadKevScriptCommandUI implements Command {

    private JFileChooser filechooser = new JFileChooser();
    private KevScriptCommand lcommand = new KevScriptCommand();

    public void setKernel(KevoreeUIKernel kernel) {
        this.kernel = kernel;
        lcommand.setKernel(kernel);
    }
    private KevoreeUIKernel kernel;

    /* Input expected : Model URI */
    @Override
    public void execute(Object p) {
        int returnVal = filechooser.showOpenDialog(kernel.getModelPanel());
        if (filechooser.getSelectedFile() != null  && returnVal == JFileChooser.APPROVE_OPTION) {
            lcommand.execute(Source.fromFile(filechooser.getSelectedFile(),"UTF-8").getLines().mkString("\n"));
        }
    }
}