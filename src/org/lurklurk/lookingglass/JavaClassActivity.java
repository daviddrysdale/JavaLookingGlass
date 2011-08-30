/* Copyright (c) David Drysdale 2011.  GPLv2 licensed; see LICENSE. */
package org.lurklurk.lookingglass;

import java.lang.reflect.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

public class JavaClassActivity extends Activity {
  public class ClassListAdapter extends BaseExpandableListAdapter {
    // Array index constants
    public static final int INTERFACES = 0;
    public static final int CONSTRUCTORS = 1;
    public static final int METHODS = 2;
    public static final int FIELDS = 3;
    public static final int CLASSES = 4;
    
    private String[] mGroups = { "implements...", "Constructors", "Methods", "Fields", "Classes" };
    public String[] mInterfaceNames;
    public String[] mConstructorNames;
    public String[] mMethodNames;
    public String[] mFieldNames;
    public String[] mClassNames;
    
    @Override public Object getGroup(int groupPosition) { return mGroups[groupPosition]; }
    @Override public int getGroupCount() { return mGroups.length; }
    @Override public long getGroupId(int groupPosition) { return groupPosition; } 
    
    @Override public Object getChild(int groupPosition, int childPosition) {
      switch (groupPosition) {
      case INTERFACES: return mInterfaceNames[childPosition];
      case CONSTRUCTORS: return mConstructorNames[childPosition];
      case METHODS: return mMethodNames[childPosition];
      case FIELDS: return mFieldNames[childPosition]; 
      case CLASSES: return mClassNames[childPosition]; 
      default: Log.e(TAG, "Unknown group position " + groupPosition); return "Error!";
      }
    }
    @Override public long getChildId(int groupPosition, int childPosition) { return childPosition; }

    @Override public int getChildrenCount(int groupPosition) {
      switch (groupPosition) {
      case INTERFACES: return mInterfaceNames.length;
      case CONSTRUCTORS: return mConstructorNames.length;
      case METHODS: return mMethodNames.length;
      case FIELDS: return mFieldNames.length; 
      case CLASSES: return mClassNames.length; 
      default: Log.e(TAG, "Unknown group position " + groupPosition); return 0;
      }
    }
    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView,
                             ViewGroup parent) {
      TextView textView = (TextView)TextView.inflate(JavaClassActivity.this, R.layout.text_list_item, null);  
      textView.setText(getChild(groupPosition, childPosition).toString());
      return textView;
    }
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
      TextView textView = (TextView) TextView.inflate(JavaClassActivity.this, R.layout.text_list_group, null);  
      textView.setText(getGroup(groupPosition).toString());
      return textView;
    }
    @Override public boolean hasStableIds() { return false; }
    @Override public boolean isChildSelectable(int groupPosition, int childPosition) { return true; }
  }
  
  private static final String TAG = "JavaClassActivity";

  private String mClassName;
  private Class<?> mClass;
  private String mSuperClassName;
  private String mContainingClassName;
  private String mComponentClassName;
  private ExpandableListView mResultsList;
  private ClassListAdapter mAdapter = new ClassListAdapter();
  
  private boolean mIncInheritedConstructors = false;
  private boolean mIncInheritedMethods = false;
  private boolean mIncInheritedFields = false;
  private boolean mIncInheritedClasses = false;
  
  /** Called when the activity is first created. */
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
      mClass = null;
    }

    if (mClass == null) {
      Log.i(TAG, "Failed to find class info for " + mClassName);
      setContentView(R.layout.jclass_error);
      TextView nameView = (TextView) findViewById(R.id.jcls_name);
      nameView.setText(mClassName);
      ((TextView)findViewById(R.id.error_message)).setText("Class Not Found");
      mResultsList = null;
    } else if (mClass.isPrimitive()) {
      // Primitive type
      Log.i(TAG, "Primitive type " + mClassName);
      setContentView(R.layout.jclass_error);
      TextView nameView = (TextView) findViewById(R.id.jcls_name);
      nameView.setText(mClassName);
      ((TextView)findViewById(R.id.error_message)).setText("Primitive Type");      
      mResultsList = null;
    } else if (mClass.isArray()) {
      // Array type
      Log.i(TAG, "Array type " + mClassName);
      Class<?> arrayClass = mClass.getComponentType();
      mComponentClassName = arrayClass.getName();
      setContentView(R.layout.jclass_array);
      TextView nameView = (TextView) findViewById(R.id.jcls_name);
      nameView.setText(mComponentClassName);
      mResultsList = null;
    } else {
      Log.i(TAG, "Got class info for " + mClassName);
      setContentView(R.layout.jclass);
      TextView nameView = (TextView) findViewById(R.id.jcls_name);
      nameView.setText(mClassName);
      mResultsList = (ExpandableListView)findViewById(R.id.jcls_results_list);
      
      // First the elements that are set directly
      fillModifiers();
      fillOtherClasses();
      //fillDeclaringClass();
      //fillComponentType();
      
      // Now the elements that form part of the ExpandableListView
      fillInterfaces();
      fillConstructors();
      fillMethods();
      fillFields();
      fillClasses();      
      setUpListView();
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
  
  private void fillOtherClasses() {
    Class<?> superCls = mClass.getSuperclass();
    if (superCls != null) {
      mSuperClassName = superCls.getName();
      Log.i(TAG, "superclass of " + mClassName + " is " + mSuperClassName);
      ((TextView)findViewById(R.id.jcls_superclass)).setText(mSuperClassName);
    } else {
      mSuperClassName = null;
    }
    Class<?> containingClass = mClass.getDeclaringClass();
    if (containingClass != null) {
      mContainingClassName = containingClass.getName();
      Log.i(TAG, "containing class for " + mClassName + " is " + mContainingClassName);
      ((TextView)findViewById(R.id.jcls_containing_class)).setText(mContainingClassName);
      findViewById(R.id.containing_class_layout).setVisibility(View.VISIBLE);     
    } else {
      mContainingClassName = null;
      findViewById(R.id.containing_class_layout).setVisibility(View.GONE);     
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
  
  public void containingClassClicked(View view) {
    Log.i(TAG, "User clicked on containing class name: " + mContainingClassName);
    if (mContainingClassName == null) return;
    Intent intent = new Intent(JavaClassActivity.this, JavaClassActivity.class);
    intent.putExtra("jclass_name", mContainingClassName);
    Log.i(TAG, "start JavaClassActivity for " + mContainingClassName);
    startActivity(intent);
  }
  
  public void componentClassClicked(View view) {
    Log.i(TAG, "User clicked on component class name: " + mComponentClassName);
    if (mComponentClassName == null) return;
    Intent intent = new Intent(JavaClassActivity.this, JavaClassActivity.class);
    intent.putExtra("jclass_name", mComponentClassName);
    Log.i(TAG, "start JavaClassActivity for " + mComponentClassName);
    startActivity(intent);
  }

  private void fillInterfaces() {
    Class<?>[] interfaceList = mClass.getInterfaces();     
    mAdapter.mInterfaceNames = new String[interfaceList.length];
    for (int ii=0; ii<interfaceList.length; ii++) {
      mAdapter.mInterfaceNames[ii] = interfaceList[ii].getName();
      Log.d(TAG, "found interface name: " + mAdapter.mInterfaceNames[ii]);
    }        
  }
  
  private void fillConstructors() {
    Constructor<?>[] constructorList;
    if (mIncInheritedConstructors) {
      constructorList = mClass.getConstructors();     
    } else {
      constructorList = mClass.getDeclaredConstructors();
    }
    mAdapter.mConstructorNames = new String[constructorList.length];
    for (int ii=0; ii<constructorList.length; ii++) {
      mAdapter.mConstructorNames[ii] = constructorList[ii].getName();
      Log.d(TAG, "found constructor name: " + mAdapter.mConstructorNames[ii]);
    }
  }
  
  private void fillMethods() {
    Method[] methodList;
    if (mIncInheritedMethods) {
      methodList = mClass.getMethods();     
    } else {
      methodList = mClass.getDeclaredMethods();           
    }
    mAdapter.mMethodNames = new String[methodList.length];
    for (int ii=0; ii<methodList.length; ii++) {
      mAdapter.mMethodNames[ii] = methodList[ii].getName();
      Log.d(TAG, "found method name: " + mAdapter.mMethodNames[ii]);
    }
  }
  
  private void fillFields() {
    Field[] fieldList;
    if (mIncInheritedFields) {
      fieldList = mClass.getFields();     
    } else {
      fieldList = mClass.getDeclaredFields();
    }
    mAdapter.mFieldNames = new String[fieldList.length];
    for (int ii=0; ii<fieldList.length; ii++) {
      String fieldName = fieldList[ii].getName();
      Class<?> fieldType = fieldList[ii].getType();
      String separator = " ";
      if (fieldType.isArray()) {
        fieldType = fieldType.getComponentType();
        separator = "[] ";
      } 
      mAdapter.mFieldNames[ii] = fieldType.getName() + separator + fieldName;
      Log.d(TAG, "found field name: " + mAdapter.mFieldNames[ii]);
    }        
  }
  
  private void fillClasses() {
    Class<?>[] classList;
    if (mIncInheritedClasses) {
      classList = mClass.getClasses();     
    } else {
      classList = mClass.getDeclaredClasses();   
    }
    mAdapter.mClassNames = new String[classList.length];
    for (int ii=0; ii<classList.length; ii++) {
      mAdapter.mClassNames[ii] = classList[ii].getName();
      Log.d(TAG, "found class name: " + mAdapter.mClassNames[ii]);
    }
  }
  

  private void setUpListView() {
    if (mResultsList == null) {
      Log.e(TAG, "No results list to fill out!");
      return;
    }
    mResultsList.setAdapter(mAdapter);
    mResultsList.setOnChildClickListener(new OnChildClickListener() {
      @Override
      public boolean onChildClick(ExpandableListView parent, View v,
                                  int groupPosition, int childPosition, long id) {
        switch (groupPosition) {
        case ClassListAdapter.INTERFACES: {  
          Intent intent = new Intent(JavaClassActivity.this, JavaClassActivity.class);
          intent.putExtra("jclass_name", mAdapter.mInterfaceNames[childPosition]);
          Log.i(TAG, "start JavaClassActivity for " + mAdapter.mInterfaceNames[childPosition] );
          startActivity(intent);
          return true;
        }
        case ClassListAdapter.CONSTRUCTORS: {
          // TODO fill in launch of constructor display
          return true;
        }
        case ClassListAdapter.METHODS: {
          // TODO: fill in launch of method display
          return true;
        }
        case ClassListAdapter.FIELDS: {
          Intent intent = new Intent(JavaClassActivity.this, JavaClassActivity.class);
          // Extract the class name from the field description
          String[] components = mAdapter.mFieldNames[childPosition].split(" ");
          int arrayOffset = components[0].indexOf("[]");
          if (arrayOffset != -1) {
            // Regenerate the mangled name indicating this is an array type
            components[0] = "[L" +  components[0].substring(0, arrayOffset) + ";";
          }
          intent.putExtra("jclass_name", components[0]);
          Log.i(TAG, "start JavaClassActivity for " + mAdapter.mFieldNames[childPosition]);
          startActivity(intent);
          return true; 
        }
        case ClassListAdapter.CLASSES: {
          Intent intent = new Intent(JavaClassActivity.this, JavaClassActivity.class);
          intent.putExtra("jclass_name", mAdapter.mClassNames[childPosition]);
          Log.i(TAG, "start JavaClassActivity for " + mAdapter.mClassNames[childPosition]);
          startActivity(intent); 
          return true;
        }
        default: {
          Log.e(TAG, "Unknown group position " + groupPosition); 
          return false;
        }
        }
      }
    });    
  }
}
