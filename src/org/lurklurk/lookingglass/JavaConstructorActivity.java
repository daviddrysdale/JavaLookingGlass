/* Copyright (c) David Drysdale 2011.  GPLv2 licensed; see LICENSE. */
package org.lurklurk.lookingglass;

import java.lang.reflect.*;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

public class JavaConstructorActivity extends ExpandableListActivity {
  private static final String TAG = "JavaConstructorActivity";

  private String mClassName;
  private Class<?> mClass;
  private Constructor<?> mConstructor;
  private Class<?> mReturnType;
  
  private ParameterListAdapter mAdapter;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.i(TAG, "onCreate");
    super.onCreate(savedInstanceState);
    mAdapter = new ParameterListAdapter(this);
    
    // Retrieve info from the Intent that should allow us to retrieve the Constructor in question
    Intent intent = getIntent();
    mClassName = intent.getStringExtra("jclass_name");
    boolean incInherited = intent.getBooleanExtra("include_inherited", false);
    int index = intent.getIntExtra("index", 0);

    if (mClassName == null) {
      Log.e(TAG, "No package name");
      mClassName = "unknown";
    }
    try {
      Log.d(TAG, "Look in class " + mClassName + " for constructor [" + index + "]");
      mClass = Class.forName(mClassName);
      Constructor<?>[] constructors;
      if (incInherited) {
        constructors = mClass.getConstructors();
      } else {
        constructors = mClass.getDeclaredConstructors();
      }
      mConstructor = constructors[index];
      Log.i(TAG, "display info for constructor " + mConstructor);
    } catch (ClassNotFoundException e) {
      Log.e(TAG, "Could not find class " + mClassName + ", error " + e);
      mClass = null;
    }
    if (mConstructor == null) {
      Toast toast = Toast.makeText(this, "Constructor not found!", Toast.LENGTH_SHORT);
      toast.show();
      finish();
    }
    fillPrologue();
    mAdapter.mParameterTypes = mConstructor.getParameterTypes();
    mAdapter.mExceptions = mConstructor.getExceptionTypes();
    setListAdapter(mAdapter);
  }
  
  private void fillPrologue() {
    LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View prologueView = inflater.inflate(R.layout.jmethod_prologue, null);
    
    // Modifier display
    int modifiers = mConstructor.getModifiers();
    String modDesc = JavaClassActivity.getModifierString(modifiers);
    ((TextView)prologueView.findViewById(R.id.jmethod_modifiers)).setText(modDesc);
 
    // Return type not applicable to cosntructors
    prologueView.findViewById(R.id.jmethod_return_type).setVisibility(View.GONE);
    
    // Method name display
    ((TextView)prologueView.findViewById(R.id.jmethod_name)).setText(mConstructor.getName() + "()");

    // Declaring class display
    ((TextView)prologueView.findViewById(R.id.jmethod_declaring_class)).setText(mClassName);

    ExpandableListView elv = getExpandableListView();
    elv.addHeaderView(prologueView);
  }
  
  public void declaringClassClicked(View view) {
    Log.i(TAG, "User clicked on declaring class name: " + mClassName);
    if (mClassName == null) return;
    Intent intent = new Intent(JavaConstructorActivity.this, JavaClassActivity.class);
    intent.putExtra("jclass_name", mClassName);
    Log.i(TAG, "start JavaClassActivity for " + intent.getStringExtra("jclass_name"));
    startActivity(intent);
  }
  public void returnTypeClicked(View view) {
    Log.i(TAG, "User clicked on return type: " + mReturnType);
    if (mReturnType == null) return;
    Intent intent = new Intent(JavaConstructorActivity.this, JavaClassActivity.class);
    intent.putExtra("jclass_name", mReturnType.getName());
    Log.i(TAG, "start JavaClassActivity for " + intent.getStringExtra("jclass_name"));
    startActivity(intent);
  }
  
  @Override
  public boolean onChildClick(ExpandableListView parent, View v, 
                              int groupPosition, int childPosition, long id) {
    switch (groupPosition) {
    case ParameterListAdapter.PARAMETERS: {  
      Intent intent = new Intent(JavaConstructorActivity.this, JavaClassActivity.class);
      intent.putExtra("jclass_name", mAdapter.mParameterTypes[childPosition].getName());
      Log.i(TAG, "start JavaClassActivity for " + intent.getStringExtra("jclass_name"));
      startActivity(intent);
      return true;
    }
    case ParameterListAdapter.EXCEPTIONS: {  
      Intent intent = new Intent(JavaConstructorActivity.this, JavaClassActivity.class);
      intent.putExtra("jclass_name", mAdapter.mExceptions[childPosition].getName());
      Log.i(TAG, "start JavaClassActivity for " + intent.getStringExtra("jclass_name"));
      startActivity(intent);
      return true;
    }
    default: {
      Log.e(TAG, "Unknown group position " + groupPosition); 
      return false;
    }
    }
  }
  
}
