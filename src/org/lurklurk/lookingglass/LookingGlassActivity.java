package org.lurklurk.lookingglass;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
  }
  public void listJavaPackages(View view) {
    Log.i(TAG, "start JavaPackageListActivity");
    Intent intent = new Intent(LookingGlassActivity.this, JavaPackageListActivity.class);
    startActivity(intent);
  }
}