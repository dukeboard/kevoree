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
package org.kevoree.platform.standalone.gui;

import com.explodingpixels.macwidgets.MacUtils;
import com.explodingpixels.macwidgets.MacWidgetFactory;
import com.explodingpixels.macwidgets.UnifiedToolBar;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.api.handler.ModelListener;
import org.kevoree.bootstrap.Bootstrap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

public class KevoreeGUIFrame extends JFrame {


    public static KevoreeGUIFrame singleton = null;

    private static KevoreeLeftModel left = null;

    public KevoreeGUIFrame() {
        singleton = this;
        MacUtils.makeWindowLeopardStyle(this.getRootPane());
        UnifiedToolBar toolBar = new UnifiedToolBar();
        add(toolBar.getComponent(), BorderLayout.NORTH);

        URL urlSmallIcon = getClass().getClassLoader().getResource("kev-logo-full.png");
        final ImageIcon smallIcon = new ImageIcon(urlSmallIcon);
        this.setIconImage(smallIcon.getImage());

        URL urlIcon = getClass().getClassLoader().getResource("kevoree-logo-full.png");
        ImageIcon topIIcon = new ImageIcon(urlIcon);
        JLabel topImage = new JLabel(topIIcon);
        topImage.setOpaque(false);
        toolBar.addComponentToLeft(topImage);

        left = new KevoreeLeftModel();
        new Thread() {
            @Override
            public void run() {
                startNode();
            }
        }.start();
        setVisible(true);

        setPreferredSize(new Dimension(1024, 768));
        setSize(getPreferredSize());

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    private void startNode() {
        final Bootstrap[] bootstrap = new Bootstrap[1];
        Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Hook") {

            public void run() {
                try {
                    bootstrap[0].stop();
                } catch (Throwable ex) {
                    System.out.println("Error stopping framework: " + ex.getMessage());
                }
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            bootstrap[0].stop();
                            dispose();
                            Runtime.getRuntime().exit(0);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
        try {
            ConsoleShell shell = null;
            shell = new ConsoleShell();
            String nodeName = "node0";
            bootstrap[0] = new Bootstrap(nodeName);
            KevoreeGUIFrame.showShell(shell);
            bootstrap[0].getCore().registerModelListener(new ModelListener() {
                @Override
                public boolean preUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
                    return true;
                }

                @Override
                public boolean initUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
                    return true;
                }

                @Override
                public boolean afterLocalUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
                    return true;
                }

                @Override
                public void modelUpdated() {
                    ContainerRoot currentModel = bootstrap[0].getCore().getCurrentModel().getModel();
                    ContainerNode currentNode = currentModel.findNodesByID(bootstrap[0].getCore().getNodeName());
                    if (currentNode != null) {
                        left.reload(currentNode);
                    }
                }

                @Override
                public void preRollback(ContainerRoot currentModel, ContainerRoot proposedModel) {

                }

                @Override
                public void postRollback(ContainerRoot currentModel, ContainerRoot proposedModel) {

                }
            });

            bootstrap[0].bootstrapFromKevScript(App.class.getClassLoader().getResourceAsStream("default.kevs"));

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void showShell(JComponent shell) {
        JSplitPane splitPane = MacWidgetFactory.createSplitPaneForSourceList(left.getSourceList(), shell);
        splitPane.setDividerLocation(200);
        singleton.add(splitPane, BorderLayout.CENTER);
        singleton.pack();
    }


}
