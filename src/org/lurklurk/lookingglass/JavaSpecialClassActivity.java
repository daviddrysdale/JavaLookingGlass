package org.lurklurk.lookingglass;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/*
 * Activity for displaying things that aren't plain Classes:
 *  - arrays of other types
 *  - primitive types
 *  - non-existent Classes
 */
public class JavaSpecialClassActivity extends Activity {
  private static final String TAG = "JavaSpecialClassActivity";
  private String mClassName;
  private Class<?> mClass;
  private String mComponentClassName;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.i(TAG, "onCreate");
    super.onCreate(savedInstanceState);
    Intent intent = getIntent();
    mClassName = intent.getStringExtra("jclass_name");  

    if (mClassName == null) {
      Log.e(TAG, "No package name");
      mClassName = "unknown";
    }
    try
    {
      mClass = Class.forName(mClassName);
    } catch (ClassNotFoundException e) {
      // Lookup of primitive types by name fails, so watch for them as a special case
      if (mClassName.equals("boolean")) {
        mClass = boolean.class;
      } else if (mClassName.equals("byte")) {
        mClass = byte.class;
      } else if (mClassName.equals("short")) {
        mClass = short.class;
      } else if (mClassName.equals("int")) {
        mClass = int.class;
      } else if (mClassName.equals("long")) {
        mClass = long.class;
      } else if (mClassName.equals("char")) {
        mClass = char.class;
      } else if (mClassName.equals("float")) {
        mClass = float.class;
      } else if (mClassName.equals("double")) {
        mClass = double.class;
      } else {
        Log.e(TAG, "Could not find class " + mClassName + ", error " + e);
        mClass = null;
      }
    }
    if (mClass == null) {
      // Class not found
      Log.i(TAG, "Failed to find class info for " + mClassName);
      setContentView(R.layout.jclass_error);
      TextView nameView = (TextView) findViewById(R.id.jcls_name);
      nameView.setText(mClassName);
      ((TextView)findViewById(R.id.error_message)).setText("Class Not Found");
    } else if (mClass.isPrimitive()) {
      // Primitive type
      Log.i(TAG, "Primitive type " + mClassName);
      setContentView(R.layout.jclass_error);
      TextView nameView = (TextView) findViewById(R.id.jcls_name);
      nameView.setText(mClassName);
      ((TextView)findViewById(R.id.error_message)).setText("Primitive Type");      
    } else if (mClass.isArray()) {
      // Array type
      Log.i(TAG, "Array type " + mClassName);
      Class<?> arrayClass = mClass.getComponentType();
      mComponentClassName = arrayClass.getName();
      setContentView(R.layout.jclass_array);
      TextView nameView = (TextView) findViewById(R.id.jcls_name);
      nameView.setText(mComponentClassName);
    } else {
      Log.e(TAG, "Unexpectedly got a plain Class " + mClassName);
      intent = new Intent(JavaSpecialClassActivity.this, JavaClassActivity.class);
      intent.putExtra("jclass_name", mClassName);
      Log.i(TAG, "start JavaClassActivity for " + mClassName);
      startActivity(intent);
      finish();
    }
  }

  public void componentClassClicked(View view) {
    Log.i(TAG, "User clicked on component class name: " + mComponentClassName);
    if (mComponentClassName == null) return;
    Intent intent = new Intent(JavaSpecialClassActivity.this, JavaClassActivity.class);
    intent.putExtra("jclass_name", mComponentClassName);
    Log.i(TAG, "start JavaClassActivity for " + mComponentClassName);
    startActivity(intent);
  }


}
