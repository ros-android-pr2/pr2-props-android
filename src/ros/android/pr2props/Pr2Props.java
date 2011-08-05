/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ros.android.pr2props;

import org.ros.exception.RemoteException;
import ros.android.activity.AppManager;
import ros.android.activity.RosAppActivity;
import android.os.Bundle;
import org.ros.node.Node;
import android.view.Window;
import android.view.WindowManager;
import android.util.Log;
import org.ros.node.service.ServiceClient;
import org.ros.service.app_manager.StartApp;
import org.ros.node.service.ServiceResponseListener;
import android.widget.Toast;
import android.view.Menu;
import android.view.View;
import android.view.MenuInflater;
import android.view.MenuItem;
import ros.android.util.Dashboard;
import android.widget.LinearLayout;
import org.ros.service.std_srvs.Empty;

//TODO: search for all instances of TODO

/**
 * @author damonkohler@google.com (Damon Kohler)
 * @author pratkanis@willowgarage.com (Tony Pratkanis)
 */
public class Pr2Props extends RosAppActivity {
  
  private String robotAppName;
  private String cameraTopic;
  private Dashboard dashboard;

  //Please only edit the functions above this line.
  //You should not need to edit the ones below. If you find
  //yourself doing so, that could be a feature request.

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //Setup the window.
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setContentView(R.layout.main);

    //Get the name of the application to start on the robot from the application system
    //if it is null, instead start the default application.
    robotAppName = getIntent().getStringExtra(AppManager.PACKAGE + ".robot_app_name");
    if( robotAppName == null ) {
      robotAppName = "pr2_props_app/pr2_props";
    }
    
    //Find the dashboard, the top bar that allows the user to see the robot's battery
    //and reset the robot's motors and breakers.
    dashboard = new Dashboard(this);
    dashboard.setView((LinearLayout)findViewById(R.id.top_bar),
                      new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 
                                                    LinearLayout.LayoutParams.WRAP_CONTENT));
  
    //TODO: add code
    //Called on creation. ROS hasn't started yet, so don't start
    //anything that depends on ROS. Instead, look up things like
    //resources. Initialize your layout here.
  }

  private void runService(String service) {
    Log.i("Pr2Props", "Run: " + service);
    try {
      ServiceClient<Empty.Request, Empty.Response> appServiceClient =
        getNode().newServiceClient(service, "std_srvs/Empty");
      Empty.Request appRequest = new Empty.Request();
      appServiceClient.call(appRequest, new ServiceResponseListener<Empty.Response>() {
          @Override public void onSuccess(Empty.Response message) {
          }
        
          @Override public void onFailure(RemoteException e) {
            //TODO: SHOULD ERROR
            Log.e("Pr2Props", e.toString());
          }
        });
    } catch (Exception e) {
      //TODO: should error
      Log.e("Pr2Props", e.toString());
    }
  }

  //Callbacks
  public void highFiveLeft(View view) {
    runService("/pr2_props/high_five_left");
  }
  public void highFiveRight(View view) {
    runService("/pr2_props/high_five_right");
  }
  public void highFiveDouble(View view) { 
    runService("/pr2_props/high_five_double");
  }
  public void lowFiveLeft(View view) { 
    runService("/pr2_props/low_five_left");
  }
  public void lowFiveRight(View view) { 
    runService("/pr2_props/low_five_right");
  }
  public void poundLeft(View view) { 
    runService("/pr2_props/pound_left");
  }
  public void poundRight(View view) { 
    runService("/pr2_props/low_five_right");
  }
  public void poundDouble(View view) { 
    runService("/pr2_props/pound_double");
  }
  public void hug(View view) { 
    runService("/pr2_props/hug");
  }
  


  @Override
  protected void onNodeCreate(Node node) {
    super.onNodeCreate(node);
    try {
      //Start up the application on the robot and start the dashboard.
      startApp();
      dashboard.start(node);
      //TODO: Put your initialization code here
    } catch (Exception ex) {
      Log.e("Pr2Props", "Init error: " + ex.toString());
      safeToastStatus("Failed: " + ex.getMessage());
    }
  }

  @Override
  protected void onNodeDestroy(Node node) {
    super.onNodeDestroy(node);
    dashboard.stop();
    //TODO: Put your shutdown code here for things the reference the node
  }
  
  
  /** Starts the application on the robot. Calls the service with the name */
  private void startApp() {
    appManager.startApp(robotAppName,
        new ServiceResponseListener<StartApp.Response>() {
          @Override
          public void onSuccess(StartApp.Response message) {
          }
          @Override
          public void onFailure(RemoteException e) {
            safeToastStatus("Failed: " + e.getMessage());
          }
        });
  }

  /** Creates the menu for the options */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.pr2_props_menu, menu);
    return true;
  }

  /** Run when the menu is clicked. */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.kill: //Shutdown if the user clicks kill
      android.os.Process.killProcess(android.os.Process.myPid());
      return true;
    //TODO: add cases for any additional menu items here.
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  /** Displays a status tip at the bottom of the screen from any thread. */
  private void safeToastStatus(final String message) {
    runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Toast.makeText(Pr2Props.this, message, Toast.LENGTH_SHORT).show();
        }
      });
  } 
}
