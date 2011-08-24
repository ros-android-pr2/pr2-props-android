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
import org.ros.node.topic.Publisher;
import org.ros.service.app_manager.StartApp;
import org.ros.node.service.ServiceResponseListener;
import android.widget.Toast;
import android.view.Menu;
import android.view.View;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import org.ros.service.std_srvs.Empty;
import org.ros.message.trajectory_msgs.JointTrajectory;
import org.ros.message.trajectory_msgs.JointTrajectoryPoint;
import java.util.ArrayList;
import org.ros.message.Duration;

/**
 * @author damonkohler@google.com (Damon Kohler)
 * @author pratkanis@willowgarage.com (Tony Pratkanis)
 */
public class Pr2Props extends RosAppActivity {
  
  private String robotAppName;
  private String cameraTopic;
  private double spineHeight;
  private Thread spineThread;
  private Publisher spinePub;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    setDefaultAppName("pr2_props_app/pr2_props");
    setDashboardResource(R.id.top_bar);
    setMainWindowResource(R.layout.main);
    spineHeight = 0.0;
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void onNodeCreate(Node node) {
    super.onNodeCreate(node);
    spinePub = node.newPublisher("torso_controller/command", "trajectory_msgs/JointTrajectory");
    spineThread = new Thread(new Runnable() {
        @Override
        public void run() {
          JointTrajectory spineMessage = new JointTrajectory();
          spineMessage.points = new ArrayList<JointTrajectoryPoint>();
          spineMessage.joint_names = new ArrayList<String>();
          spineMessage.joint_names.add("torso_lift_joint");
          JointTrajectoryPoint p = new JointTrajectoryPoint();
          p.positions = new double[] { 0.0 };
          p.velocities = new double[] { 0.1 };
          p.time_from_start = new Duration(0.25);
          spineMessage.points.add(p);
          try {
            while (true) {
              spineMessage.points.get(0).positions[0] = spineHeight;
              spinePub.publish(spineMessage);
              Thread.sleep(200L);
            }
          } catch (InterruptedException e) {
          }
        }
      });
    spineThread.start();
  }
  
  @Override
  protected void onNodeDestroy(Node node) {
    super.onNodeDestroy(node);
    final Thread thread = spineThread;
    if (thread != null) {
      spineThread.interrupt();
    }
    spineThread = null;
    final Publisher pub = spinePub;
    if (pub != null) {
      pub.shutdown();
    }
    spinePub = null;
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
  public void raiseSpine(View view) { 
    spineHeight = 0.31;
  }
  public void lowerSpine(View view) { 
    spineHeight = 0.0;
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
    default:
      return super.onOptionsItemSelected(item);
    }
  }
}
