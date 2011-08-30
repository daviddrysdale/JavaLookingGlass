/* Copyright (c) David Drysdale 2011.  GPLv2 licensed; see LICENSE. */
package org.lurklurk.lookingglass;

import java.lang.reflect.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class JavaClassActivity extends Activity {
  private static final String TAG = "JavaClassActivity";

  private String mClassName;
  private Class<?> mClass;
  private String mSuperClassName;
  private boolean mInterfacesVisible = false;
  private boolean mConstructorsVisible = false;
  private boolean mIncInheritedConstructors = false;
  private boolean mMethodsVisible = false;
  private boolean mIncInheritedMethods = false;
  private boolean mFieldsVisible = false;
  private boolean mIncInheritedFields = false;
  private boolean mClassesVisible = false;
  private boolean mIncInheritedClasses = false;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.i(TAG, "onCreate");
    super.onCreate(savedInstanceState);


    Intent intent = getIntent();
    mClassName = intent.getStringExtra("jclass_name");  
    fillOutResults();
  }
  
  private void fillOutResults() {
    if (mClassName == null) {
      Log.e(TAG, "No package name");
      mClassName = "unknown";
    }
    try
    {
      mClass = Class.forName(mClassName);
    } catch (ClassNotFoundException e) {
      mClass = null;
    }

    if (mClass == null) {
      Log.i(TAG, "Failed to find class info for " + mClassName);
      setContentView(R.layout.jclass_error);
      TextView nameView = (TextView) findViewById(R.id.jcls_name);
      nameView.setText(mClassName);     
    } else {
      Log.i(TAG, "Got class info for " + mClassName);
      setContentView(R.layout.jclass);
      TextView nameView = (TextView) findViewById(R.id.jcls_name);
      nameView.setText(mClassName);
    
      fillModifiers();
      fillSuperClass();
      //fillDeclaringClass();
      //fillComponentType();
      fillInterfaces();
      fillConstructors();
      fillMethods();
      fillFields();
      fillClasses();
    }
  }

  
  private String getModifierString(int modifiers) {
    String modDesc = "";
    if ((modifiers & Modifier.PUBLIC) != 0) modDesc += "public ";
    if ((modifiers & Modifier.PRIVATE) != 0) modDesc += "private ";
    if ((modifiers & Modifier.PROTECTED) != 0) modDesc += "protected ";
    if ((modifiers & Modifier.STATIC) != 0) modDesc += "static ";
    if ((modifiers & Modifier.FINAL) != 0) modDesc += "final ";
    if ((modifiers & Modifier.SYNCHRONIZED) != 0) modDesc += "synchronized ";
    if ((modifiers & Modifier.VOLATILE) != 0) modDesc += "volatile ";
    if ((modifiers & Modifier.TRANSIENT) != 0) modDesc += "transient ";
    if ((modifiers & Modifier.NATIVE) != 0) modDesc += "native ";
    if ((modifiers & Modifier.INTERFACE) != 0) modDesc += "interface ";
    if ((modifiers & Modifier.ABSTRACT) != 0) modDesc += "abstract ";
    if ((modifiers & Modifier.STRICT) != 0) modDesc += "strict ";
    return modDesc;
  }
  
  private void fillModifiers() {
    int modifiers = mClass.getModifiers();
    // Strip out interface bit as we display that separately
    String modDesc = getModifierString(modifiers & ~Modifier.INTERFACE);
    ((TextView)findViewById(R.id.jcls_modifiers)).setText(modDesc);
    ((TextView)findViewById(R.id.jcls_cls_or_if)).setText(mClass.isInterface() ? "interface" : "class");
  }
  
  private void fillSuperClass() {
    Class<?> superCls = mClass.getSuperclass();
    if (superCls != null) {
      mSuperClassName = superCls.getName();
      ((TextView)findViewById(R.id.jcls_superclass)).setText(mSuperClassName);
    } else {
      mSuperClassName = null;
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
  

  private void fillInterfaces() {
    Class<?>[] interfaceList = mClass.getInterfaces();     
    final String[] names = new String[interfaceList.length];
    for (int ii=0; ii<interfaceList.length; ii++) {
      names[ii] = interfaceList[ii].getName();
      Log.d(TAG, "found interface name: " + names[ii]);
    }
    ListView listView = (ListView) findViewById(R.id.jcls_interfaces);
    ArrayAdapter<String> aa = new ArrayAdapter<String>(this, R.layout.text_list_item, names);
    listView.setAdapter(aa);
    listView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(JavaClassActivity.this, JavaClassActivity.class);
        intent.putExtra("jclass_name", names[position]);
        Log.i(TAG, "start JavaClassActivity for " + names[position] + " = interfaceNames[" + position + "]");
        startActivity(intent);
      }
    });         
  }
  public void toggleInterfaces(View view) {
    mInterfacesVisible = !mInterfacesVisible;
    Log.i(TAG, "Toggle display of " + mClassName + " interfaces to be visible=" + mInterfacesVisible);
    syncListLayoutDisplay("interfaces", mInterfacesVisible, R.id.jcls_toggle_interfaces, R.id.jcls_interfaces_layout);
  }
  
  private void fillConstructors() {
    Constructor<?>[] constructorList;
    if (mIncInheritedConstructors) {
      constructorList = mClass.getConstructors();     
    } else {
      constructorList = mClass.getDeclaredConstructors();
    }
    final String[] names = new String[constructorList.length];
    for (int ii=0; ii<names.length; ii++) {
      names[ii] = constructorList[ii].getName();
      Log.d(TAG, "found constructor name: " + names[ii]);
    }
    ListView listView = (ListView) findViewById(R.id.jcls_constructors);
    ArrayAdapter<String> aa = new ArrayAdapter<String>(this, R.layout.text_list_item, names);
    listView.setAdapter(aa);
    listView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        /*
        Intent intent = new Intent(JavaClassActivity.this, JavaMethodActivity.class);
        intent.putExtra("jclass_name", interfaceNames[position]);
        Log.i(TAG, "start JavaClassActivity for " + interfaceNames[position] + " = interfaceNames[" + position + "]");
        startActivity(intent);
        */
      }
    });          
  }
  public void toggleConstructors(View view) {
    mConstructorsVisible = !mConstructorsVisible;
    Log.i(TAG, "Toggle display of " + mClassName + " constructors to be visible=" + mConstructorsVisible);
    syncListLayoutDisplay("constructors", mConstructorsVisible, R.id.jcls_toggle_constructors, R.id.jcls_constructors_layout);
  }
  
  private void fillMethods() {
    Method[] methodList;
    if (mIncInheritedMethods) {
      methodList = mClass.getMethods();     
    } else {
      methodList = mClass.getDeclaredMethods();           
    }
    final String[] names = new String[methodList.length];
    for (int ii=0; ii<names.length; ii++) {
      names[ii] = methodList[ii].getName();
      Log.d(TAG, "found method name: " + names[ii]);
    }
    ListView listView = (ListView) findViewById(R.id.jcls_methods);
    ArrayAdapter<String> aa = new ArrayAdapter<String>(this, R.layout.text_list_item, names);
    listView.setAdapter(aa);
    listView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        /*
        Intent intent = new Intent(JavaClassActivity.this, JavaMethodActivity.class);
        intent.putExtra("jclass_name", interfaceNames[position]);
        Log.i(TAG, "start JavaClassActivity for " + interfaceNames[position] + " = interfaceNames[" + position + "]");
        startActivity(intent);
        */
      }
    });          
  }
  public void toggleMethods(View view) {
    mMethodsVisible = !mMethodsVisible;
    Log.i(TAG, "Toggle display of " + mClassName + " methods to be visible=" + mMethodsVisible);
    syncListLayoutDisplay("methods", mMethodsVisible, R.id.jcls_toggle_methods, R.id.jcls_methods_layout);
  }
  
  private void fillFields() {
    Field[] fieldList;
    if (mIncInheritedFields) {
      fieldList = mClass.getFields();     
    } else {
      fieldList = mClass.getDeclaredFields();
    }
    final String[] names = new String[fieldList.length];
    for (int ii=0; ii<names.length; ii++) {
      String fieldName = fieldList[ii].getName();
      Class<?> fieldType = fieldList[ii].getType();
      names[ii] = fieldType.getName() + " " + fieldName;
      Log.d(TAG, "found field name: " + names[ii]);
    }
    ListView listView = (ListView) findViewById(R.id.jcls_fields);
    ArrayAdapter<String> aa = new ArrayAdapter<String>(this, R.layout.text_list_item, names);
    listView.setAdapter(aa);
    listView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(JavaClassActivity.this, JavaClassActivity.class);
        // Extract the class name from the field description
        String[] components = names[position].split(" ");
        intent.putExtra("jclass_name", components[0]);
        Log.i(TAG, "start JavaClassActivity for " + names[position]);
        startActivity(intent);
      }
    });          
  }
  public void toggleFields(View view) {
    mFieldsVisible = !mFieldsVisible;
    Log.i(TAG, "Toggle display of " + mClassName + " fields to be visible=" + mFieldsVisible);
    syncListLayoutDisplay("fields", mFieldsVisible, R.id.jcls_toggle_fields, R.id.jcls_fields_layout);
  }
  
  
  private void fillClasses() {
    Class<?>[] classList;
    if (mIncInheritedClasses) {
      classList = mClass.getClasses();     
    } else {
      classList = mClass.getDeclaredClasses();   
    }
    final String[] names = new String[classList.length];
    for (int ii=0; ii<names.length; ii++) {
      names[ii] = classList[ii].getName();
      Log.d(TAG, "found class name: " + names[ii]);
    }
    ListView listView = (ListView) findViewById(R.id.jcls_classes);
    ArrayAdapter<String> aa = new ArrayAdapter<String>(this, R.layout.text_list_item, names);
    listView.setAdapter(aa);
    listView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {       
        Intent intent = new Intent(JavaClassActivity.this, JavaClassActivity.class);
        intent.putExtra("jclass_name", names[position]);
        Log.i(TAG, "start JavaClassActivity for " + names[position] + " = names[" + position + "]");
        startActivity(intent);
      }
    });          
  }
  public void toggleClasses(View view) {
    mClassesVisible = !mClassesVisible;
    Log.i(TAG, "Toggle display of " + mClassName + " classes to be visible=" + mClassesVisible);
    syncListLayoutDisplay("classes", mClassesVisible, R.id.jcls_toggle_classes, R.id.jcls_classes_layout);
  }
  
  private void syncListLayoutDisplay(String name, boolean visible, int buttonId, int layoutId) {
    Log.d(TAG, "Make " + name + " display visible=" + visible);
    ImageButton button = (ImageButton) findViewById(buttonId);
    View listLayout =  findViewById(layoutId);
    if (visible) {
      button.setImageResource(R.drawable.triangle_down);
      listLayout.setVisibility(View.VISIBLE);
    } else {
      button.setImageResource(R.drawable.triangle_right);
      listLayout.setVisibility(View.GONE);      
    }    
  }
 
}
