/* Copyright (c) David Drysdale 2011.  GPLv2 licensed; see LICENSE. */
package org.lurklurk.lookingglass;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;

public class LookingGlassActivity extends Activity {
  private static final String TAG = "LookingGlassActivity";
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.i(TAG, "onCreate");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    
    // Plumb package name entry through to JavaPackageActivity
    final EditText jpkgEntry = (EditText)findViewById(R.id.lookup_package);
    jpkgEntry.setOnKeyListener(new OnKeyListener() {
      @Override
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
            (keyCode == KeyEvent.KEYCODE_ENTER)) {
          String packageName = jpkgEntry.getText().toString();
          Intent intent = new Intent(LookingGlassActivity.this, JavaPackageActivity.class);
          intent.putExtra("jpackage_name", packageName);
          Log.i(TAG, "start JavaPackageActivity for " + packageName);
          startActivity(intent);
          return true;
        } else {
          return false;
        }
      }
    });
    
    // Plumb class name entry through to JavaClassActivity
    final EditText jclsEntry = (EditText)findViewById(R.id.lookup_class);
    jclsEntry.setOnKeyListener(new OnKeyListener() {
      @Override
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
            (keyCode == KeyEvent.KEYCODE_ENTER)) {
          String className = jclsEntry.getText().toString();
          Intent intent = new Intent(LookingGlassActivity.this, JavaClassActivity.class);
          intent.putExtra("jclass_name", className);
          Log.i(TAG, "start JavaClassActivity for " + className);
          startActivity(intent);
          return true;
        } else {
          return false;
        }
      }
    });
    PackageManager pm = getPackageManager();
    List<ApplicationInfo> apps = pm.getInstalledApplications(0);
    for (ApplicationInfo app: apps) {
      describeApplication("app.", app);
    }
    
    List<PackageInfo> pkgs = pm.getInstalledPackages(0);
    for (PackageInfo pkg: pkgs) {
      Log.d(TAG, "pkg.packageName=" + pkg.packageName);
      if (pkg.activities != null) {
        for (int ii=0; ii<pkg.activities.length; ii++) {
          String prefix = "  pkg.activities[" + ii + "].";
          Log.d(TAG, prefix + "permission=" + pkg.activities[ii].permission);
          describeComponent(prefix, pkg.activities[ii]);
        }
      }
    } 
  }
  
  private void describeApplication(String prefix, ApplicationInfo app) {
    Log.d(TAG, prefix + "className=" + app.className);
    Log.d(TAG, prefix + "publicSourceDir=" + app.publicSourceDir);
    Log.d(TAG, prefix + "sourceDir=" + app.sourceDir);
    if (app.sharedLibraryFiles != null) {
      for (int ii = 0; ii < app.sharedLibraryFiles.length; ii++) {
        Log.d(TAG, "  " + prefix + "sharedLibraryFiles[" + ii +"]=" + app.sharedLibraryFiles[ii]);
      }
    }
    
  }
  private void describeComponent(String prefix, ComponentInfo component) {
    Log.d(TAG, prefix + "processName=" + component.processName);
    Log.d(TAG, prefix + "exported=" + component.exported);
    Log.d(TAG, prefix + "enabled=" + component.enabled);
    describeApplication(prefix+"applicationInfo.", component.applicationInfo);
    describeItem(prefix, component);
  }
  private void describeItem(String prefix, PackageItemInfo item) {
    Log.d(TAG, prefix + "name=" + item.name);
    Log.d(TAG, prefix + "packageName=" + item.packageName);
  }
  
  public void listJavaPackages(View view) {
    Log.i(TAG, "start JavaPackageListActivity");
    Intent intent = new Intent(LookingGlassActivity.this, JavaPackageListActivity.class);
    startActivity(intent);
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu, menu);
    return true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
          case R.id.settings:
            startActivity(new Intent(this, LookingGlassPreferences.class));
            break;
      }
      return true;
  }
  
}