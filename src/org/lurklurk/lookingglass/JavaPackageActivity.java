package org.lurklurk.lookingglass;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class JavaPackageActivity extends Activity
{
  private static final String TAG = "JavaPackageActivity";
  private static final String PACKAGE_NAME_KEY = "mPackageName";

  private String mPackageName;
  private Package mPackage;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.i(TAG, "onCreate");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.jpackage);

    Intent intent = getIntent();
    if ((intent == null) && (savedInstanceState != null)) {
      try {
        Log.i(TAG, "Retrieve stored instance state");
        mPackageName = savedInstanceState.getString(PACKAGE_NAME_KEY);
      } catch (Exception e) {
        Log.e(TAG, "Failed to retrieve instance state "+e);
      }
    } else {
      mPackageName = intent.getStringExtra("jpackage_name");     
    }
    fillOutResults();
  }
  
  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    Log.i(TAG, "save instance state");
    savedInstanceState.putString(PACKAGE_NAME_KEY, mPackageName);
    super.onSaveInstanceState(savedInstanceState);
  }
  
  private void fillOutResults() {
    if (mPackageName == null) {
      Log.e(TAG, "No package name");
      mPackageName = "unknown";
    }
    TextView nameView = (TextView) findViewById(R.id.jpkg_name);
    nameView.setText(mPackageName);
    
    // @@@ This returns a non-null Package for any input!
    mPackage = Package.getPackage(mPackageName);
    if (mPackage != null) {
      Log.i(TAG, "Got package info for "+ mPackageName + " with name " + mPackage.getName());
      ((TextView)findViewById(R.id.jkpg_impl_title)).setText(mPackage.getImplementationTitle());
      ((TextView)findViewById(R.id.jpkg_impl_vendor)).setText(mPackage.getImplementationVendor());
      ((TextView)findViewById(R.id.jpkg_impl_version)).setText(mPackage.getImplementationVersion());
      ((TextView)findViewById(R.id.jpkg_spec_title)).setText(mPackage.getSpecificationTitle());
      ((TextView)findViewById(R.id.jpkg_spec_vendor)).setText(mPackage.getSpecificationVendor());
      ((TextView)findViewById(R.id.jpkg_spec_version)).setText(mPackage.getSpecificationVersion());

      findViewById(R.id.error_message).setVisibility(View.GONE);
      findViewById(R.id.impl_title_layout).setVisibility(View.VISIBLE);      
      findViewById(R.id.impl_vendor_layout).setVisibility(View.VISIBLE);      
      findViewById(R.id.impl_version_layout).setVisibility(View.VISIBLE);      
      findViewById(R.id.spec_title_layout).setVisibility(View.VISIBLE);      
      findViewById(R.id.spec_vendor_layout).setVisibility(View.VISIBLE);      
      findViewById(R.id.spec_version_layout).setVisibility(View.VISIBLE);      
    } else {
      Log.w(TAG, "Failed to get package info for "+ mPackageName);
      findViewById(R.id.error_message).setVisibility(View.VISIBLE);
      findViewById(R.id.impl_title_layout).setVisibility(View.GONE);      
      findViewById(R.id.impl_vendor_layout).setVisibility(View.GONE);      
      findViewById(R.id.impl_version_layout).setVisibility(View.GONE);      
      findViewById(R.id.spec_title_layout).setVisibility(View.GONE);      
      findViewById(R.id.spec_vendor_layout).setVisibility(View.GONE);      
      findViewById(R.id.spec_version_layout).setVisibility(View.GONE);      
    }
  }
}