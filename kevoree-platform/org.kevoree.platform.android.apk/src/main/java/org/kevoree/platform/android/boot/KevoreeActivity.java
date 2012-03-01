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
package org.kevoree.platform.android.boot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.*;
import android.widget.Button;
import android.widget.TabHost.TabSpec;

import java.io.*;


/**
 * Hello world!
 */
public class KevoreeActivity extends Activity {

    class PreExistingViewFactory implements TabHost.TabContentFactory {

        private View v = null;

        public PreExistingViewFactory(View v) {
            this.v = v;
        }

        @Override
        public View createTabContent(String s) {
            return v;
        }
    }


    public static KevoreeActivity singleton = null;
    public static String nodeName = "";
    public static PrintStream STDwriter = null;
    public static PrintStream ERRwriter = null;

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("kevoree", "Kevoree UIActivity Start /" + this.toString());
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("kevoree", "Kevoree UIActivity Stop /" + this.toString());
        // Intent intent = new Intent(".KevoreeService.ACTION");
        //stopService(intent);
    }

    private Boolean alreadyStarted = false;
    public static TabHost tabs = null;


    @Override
    protected synchronized void onCreate(Bundle savedInstanceState) {
        if (singleton == null) {
            super.onCreate(savedInstanceState);

            Display display = getWindowManager().getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();

            final Scroller scroller;
            final TextView logs;
            final CheckBox checkbox_info;
            final CheckBox checkbox_debug;
            final CheckBox checkbox_warn;
            final View.OnClickListener checkbox_list;


            checkbox_info = new CheckBox(this);
            checkbox_info.setText("INFO");
            checkbox_info.setChecked(true);
            checkbox_debug = new CheckBox(this);
            checkbox_debug.setText("DEBUG");
            checkbox_warn = new CheckBox(this);
            checkbox_warn.setChecked(true);
            checkbox_warn.setText("WARN");


            checkbox_list = new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            };

            checkbox_warn.setOnClickListener(checkbox_list);
            checkbox_debug.setOnClickListener(checkbox_list);
            checkbox_info.setOnClickListener(checkbox_list);

            scroller = new Scroller(this);

            logs = new TextView(this);

            logs.setSingleLine(false);
            logs.setScroller(scroller);
            logs.setMovementMethod(new ScrollingMovementMethod());
            logs.setWidth(width);
            logs.setHeight(height / 2);
            logs.setText("");
            logs.setBackgroundColor(Color.WHITE);


            /* Catch  stdout and stderr */
            STDwriter = new PrintStream(new TextOutputStream(logs, Color.BLACK));
            ERRwriter = new PrintStream(new TextOutputStream(logs, Color.RED));
            System.setOut(STDwriter);
            System.setErr(ERRwriter);

            LinearLayout main = new LinearLayout(this);
            main.setOrientation(LinearLayout.VERTICAL);
            setContentView(main);
            tabs = new TabHost(this, null);
            tabs.setId(android.R.id.tabhost);
            main.addView(tabs);
            TabWidget tabWidget = new TabWidget(this);
            tabWidget.setId(android.R.id.tabs);
            tabs.addView(tabWidget);
            FrameLayout tabContent = new FrameLayout(this);
            tabContent.setId(android.R.id.tabcontent);
            tabContent.setPadding(0, 65, 0, 0);
            tabs.addView(tabContent);
            tabs.setup();

            TabSpec tspec1 = tabs.newTabSpec("Admin");
            tspec1.setIndicator("Admin");

            LinearLayout adminLayout = new LinearLayout(this);
            LinearLayout layout = new LinearLayout(this);

            layout.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT, AbsListView.LayoutParams.FILL_PARENT));
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(adminLayout);

            final EditText nodeNameView = new EditText(this);


            nodeNameView.setText("node0");
            nodeNameView.setWidth(width / 4);


            Button btstart = new Button(this);
            btstart.setText("Start");
            Button btstop = new Button(this);
            btstop.setText("Stop");

            adminLayout.addView(nodeNameView);

            adminLayout.addView(btstart);
            adminLayout.addView(btstop);
            adminLayout.addView(checkbox_info);
            adminLayout.addView(checkbox_debug);
            adminLayout.addView(checkbox_warn);
            layout.addView(logs);


            tspec1.setContent(new PreExistingViewFactory(layout));
            tabs.addTab(tspec1);
            /*
           TabSpec tspec2 = tabs.newTabSpec("Tab2");
           tspec2.setIndicator("Two", this.getResources().getDrawable(android.R.drawable.star_on));
           tspec2.setContent(new PreExistingViewFactory(content2));

           tabs.addTab(tspec2);
           TabSpec tspec3 = tabs.newTabSpec("Tab3");
           tspec3.setIndicator("Three", this.getResources().getDrawable(android.R.drawable.star_on));
           tspec3.setContent(new PreExistingViewFactory(content3));
           tabs.addTab(tspec3);


           this.setContentView(R.layout.main);
            */

            final Context app_ctx = this.getApplicationContext();
            final Context ctx = this;

            //   Button btstart = (Button) findViewById(R.id.StartFelix);
            btstart.setOnClickListener(new Button.OnClickListener() {

                public void onClick(View v) {
                    Intent intent_start = new Intent(ctx, KevoreeService.class);
                    Log.i("art2.service", "start bind service");
                    if (!alreadyStarted) {
                        nodeName = nodeNameView.getText().toString();
                        System.setProperty("node.name", nodeName);
                        nodeNameView.setClickable(false);
                        nodeNameView.setCursorVisible(false);
                        nodeNameView.setBackgroundColor(Color.GRAY);
                        nodeNameView.setSelected(false);
                        nodeNameView.setEnabled(false);

                        startService(intent_start);
                        alreadyStarted = true;
                    }
                }
            });

            //  Button btstop = (Button) findViewById(R.id.StopFelix);
            btstop.setOnClickListener(new Button.OnClickListener() {

                public void onClick(View v) {
                    Log.i("kevoree.platform", "try to stop the platform");
                    if (alreadyStarted) {
                        Intent intent_stop = new Intent(ctx, KevoreeService.class);
                        stopService(intent_stop);
                        alreadyStarted = false;
                    }
                }
            });

        }
        if (singleton == null)
            singleton = this;
    }

    private class TextOutputStream extends OutputStream {
        private TextView _textArea = null;
        private int _color = 0;
        StringBuilder currentLine = new StringBuilder();

        public TextOutputStream(TextView textArea, int color) {
            _textArea = textArea;
            _color = color;
        }


        @Override
        public void write(final int b) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (b == (int) '\n') {

                            if (!currentLine.toString().startsWith("Error reading from ")) {
                                _textArea.append("\n" + currentLine.toString());
                                final int scrollAmount = _textArea.getLayout().getLineTop(_textArea.getLineCount()) - _textArea.getHeight();

                                if (scrollAmount > 0)
                                    _textArea.scrollTo(0, scrollAmount);
                                else
                                    _textArea.scrollTo(0, 0);

                                _textArea.setTextColor(_color);
                                Log.i("kevoree.activity.logger", currentLine.toString());
                            }


                            currentLine = new StringBuilder();
                        } else {
                            currentLine.append((char) b);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            });

        }

    }
}

