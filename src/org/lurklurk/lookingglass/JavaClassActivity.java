package org.lurklurk.lookingglass;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class JavaClassActivity extends Activity {
  private static final String TAG = "JavaClassActivity";
  private static final String CLASS_NAME_KEY = "mClassName";

  private String mClassName;
  private Class<?> mClass;
  private String mSuperClassName;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.i(TAG, "onCreate");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.jclass);

    Intent intent = getIntent();
    if ((intent == null) && (savedInstanceState != null)) {
      try {
        Log.i(TAG, "Retrieve stored instance state");
        mClassName = savedInstanceState.getString(CLASS_NAME_KEY);
      } catch (Exception e) {
        Log.e(TAG, "Failed to retrieve instance state " + e);
      }
    } else {
      mClassName = intent.getStringExtra("jclass_name");     
    }
    fillOutResults();
  }
  
  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    Log.i(TAG, "save instance state");
    savedInstanceState.putString(CLASS_NAME_KEY, mClassName);
    super.onSaveInstanceState(savedInstanceState);
  }
  
  private void fillOutResults() {
    if (mClassName == null) {
      Log.e(TAG, "No package name");
      mClassName = "unknown";
    }
       
    TextView nameView = (TextView) findViewById(R.id.jcls_name);
    nameView.setText(mClassName);
    
    try
    {
      mClass = Class.forName(mClassName);
    } catch (ClassNotFoundException e) {
      mClass = null;
    }
    if (mClass != null) {
      Log.i(TAG, "Got class info for " + mClassName);
      
      int modifiers = mClass.getModifiers();
      String modDesc = "";
      if ((modifiers & 0x0001) != 0) modDesc += "public ";
      if ((modifiers & 0x0010) != 0) modDesc += "final ";
      if ((modifiers & 0x0200) != 0) modDesc += "interface ";
      if ((modifiers & 0x0400) != 0) modDesc += "abstract ";
      ((TextView)findViewById(R.id.jcls_modifiers)).setText(modDesc);

      Class<?> superCls = mClass.getSuperclass();
      if (superCls != null) {
        mSuperClassName = superCls.getName();
        ((TextView)findViewById(R.id.jcls_superclass)).setText(mSuperClassName);
      } else {
        mSuperClassName = null;
      }
      
      // TODO fill out
      
      
      findViewById(R.id.error_message).setVisibility(View.GONE);
      findViewById(R.id.jcls_results).setVisibility(View.VISIBLE);           
    } else {
      Log.w(TAG, "Class "+ mClassName + " not found");
      findViewById(R.id.error_message).setVisibility(View.VISIBLE);
      findViewById(R.id.jcls_results).setVisibility(View.GONE);         
    }
  }
  
  public void superClassClicked(View view) {
    Log.i(TAG, "User clicked on super class name: " + mSuperClassName);
    if (mSuperClassName == null) return;
    Intent intent = new Intent(JavaClassActivity.this, JavaClassActivity.class);
    intent.putExtra("jclass_name", mSuperClassName);
    Log.i(TAG, "start JavaClassActivity for " + mSuperClassName);
    startActivity(intent);
  }
}
