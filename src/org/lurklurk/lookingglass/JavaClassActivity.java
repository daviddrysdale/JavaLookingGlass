/* Copyright (c) David Drysdale 2011.  GPLv2 licensed; see LICENSE. */
package org.lurklurk.lookingglass;

import java.lang.reflect.*;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class JavaClassActivity extends ExpandableListActivity {
  public class ClassListAdapter extends BaseExpandableListAdapter { 
    private static final String TAG = "ClassListAdapter";
    
    // Array index constants
    public static final int INTERFACES = 0;
    public static final int CONSTRUCTORS = 1;
    public static final int METHODS = 2;
    public static final int FIELDS = 3;
    public static final int CLASSES = 4;
    
    ForegroundColorSpan mLinkColor;
    ForegroundColorSpan mNameColor;
    
    private String[] mGroups = { "implements...", "Constructors", "Methods", "Fields", "Classes" };
    public Class<?>[] mInterfaces;
    public Constructor<?>[] mConstructors;
    public Method[] mMethods;
    public Field[] mFields;
    public Class<?>[] mClasses;
    public ClassListAdapter(ForegroundColorSpan linkColor, ForegroundColorSpan nameColor) {
      super();
      mLinkColor = linkColor;
      mNameColor = nameColor;
    }
    @Override public Object getGroup(int groupPosition) { return mGroups[groupPosition]; }
    @Override public int getGroupCount() { return mGroups.length; }
    @Override public long getGroupId(int groupPosition) { return groupPosition; } 
    
    @Override public Object getChild(int groupPosition, int childPosition) {
      switch (groupPosition) {
      case INTERFACES: return mInterfaces[childPosition];
      case CONSTRUCTORS: return mConstructors[childPosition];
      case METHODS: return mMethods[childPosition];
      case FIELDS: return mFields[childPosition]; 
      case CLASSES: return mClasses[childPosition]; 
      default: Log.e(TAG, "Unknown group position " + groupPosition); return "Error!";
      }
    }
    @Override public long getChildId(int groupPosition, int childPosition) { return childPosition; }

    @Override public int getChildrenCount(int groupPosition) {
      switch (groupPosition) {
      case INTERFACES: return mInterfaces.length;
      case CONSTRUCTORS: return mConstructors.length;
      case METHODS: return mMethods.length;
      case FIELDS: return mFields.length; 
      case CLASSES: return mClasses.length; 
      default: Log.e(TAG, "Unknown group position " + groupPosition); return 0;
      }
    }
    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView,
                             ViewGroup parent) {
      TextView textView = (TextView)TextView.inflate(JavaClassActivity.this, R.layout.text_list_item, null);
      String modifierInfo = null;
      String linkInfo = null;
      String nameInfo = "";
      switch (groupPosition) {
      case INTERFACES: {
        modifierInfo = getModifierString(mInterfaces[childPosition].getModifiers());
        linkInfo = mInterfaces[childPosition].getName();
        break;
      }
      case CONSTRUCTORS: {
        modifierInfo = getModifierString(mConstructors[childPosition].getModifiers());
        linkInfo = mConstructors[childPosition].getName();
        break;
      }
      case METHODS: {
        modifierInfo = getModifierString(mMethods[childPosition].getModifiers());
        linkInfo = mMethods[childPosition].getName();
        break;
      }
      case FIELDS: {
        modifierInfo = getModifierString(mFields[childPosition].getModifiers());
        Class<?> fieldType = mFields[childPosition].getType();
        if (fieldType.isArray()) {
          fieldType = fieldType.getComponentType();
          linkInfo = fieldType.getName() + "[]";
        } else {
          linkInfo = fieldType.getName();                  
        }
        nameInfo = " " + mFields[childPosition].getName();
        break;
      }
      case CLASSES: {
        modifierInfo = getModifierString(mClasses[childPosition].getModifiers());
        linkInfo = mClasses[childPosition].getName(); 
        break;
      }
      default: {
        Log.e(TAG, "Unknown group position " + groupPosition); 
        modifierInfo = "";
        linkInfo = "";
        nameInfo = "Error!";
        break;
      }
      }
      SpannableString itemInfo = new SpannableString(modifierInfo + linkInfo + nameInfo);
      if (linkInfo.length() > 0) {
        itemInfo.setSpan(mLinkColor, 
                         modifierInfo.length(), 
                         modifierInfo.length() + linkInfo.length(), 
                         0);
      }
      if (nameInfo.length() > 0) {
        itemInfo.setSpan(mNameColor,
                         modifierInfo.length() + linkInfo.length(),
                         modifierInfo.length() + linkInfo.length() + nameInfo.length(),
                         0);
      }
      textView.setText(itemInfo, BufferType.SPANNABLE);
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

  private ClassListAdapter mAdapter;
 
  ForegroundColorSpan mLinkColor;
  ForegroundColorSpan mNameColor;
  
  private boolean mIncInheritedConstructors = false;
  private boolean mIncInheritedMethods = false;
  private boolean mIncInheritedFields = false;
  private boolean mIncInheritedClasses = false;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.i(TAG, "onCreate");
    super.onCreate(savedInstanceState);
    Resources resources = getResources();
    mLinkColor = new ForegroundColorSpan(resources.getColor(R.color.link));
    mNameColor = new ForegroundColorSpan(resources.getColor(R.color.name));  
    mAdapter = new ClassListAdapter(mLinkColor, mNameColor);

    Intent intent = getIntent();
    mClassName = intent.getStringExtra("jclass_name");  

    if (mClassName == null) {
      Log.e(TAG, "No package name");
      mClassName = "unknown";
    }
    try {
      mClass = Class.forName(mClassName);
    } catch (ClassNotFoundException e) {
      Log.e(TAG, "Could not find class " + mClassName + ", error " + e);
      mClass = null;
    }

    if ((mClass == null) || mClass.isPrimitive() || mClass.isArray()) {
      intent = new Intent(JavaClassActivity.this, JavaSpecialClassActivity.class);
      intent.putExtra("jclass_name", mClassName);
      Log.i(TAG, "start JavaSpecialClassActivity for " + mClassName);
      startActivity(intent);
      finish();
    } else {
      Log.i(TAG, "Got class info for " + mClassName);
      
      // First the elements that are set directly in the prologue
      fillPrologue();
      
      // Now the elements that form part of the ExpandableListView
      mAdapter.mInterfaces = mClass.getInterfaces();        
      if (mIncInheritedConstructors) {
        mAdapter.mConstructors = mClass.getConstructors();     
      } else {
        mAdapter.mConstructors = mClass.getDeclaredConstructors();
      }
      if (mIncInheritedMethods) {
        mAdapter.mMethods = mClass.getMethods();     
      } else {
        mAdapter.mMethods = mClass.getDeclaredMethods();           
      }
      if (mIncInheritedFields) {
        mAdapter.mFields = mClass.getFields();     
      } else {
        mAdapter.mFields = mClass.getDeclaredFields();
      }
      if (mIncInheritedClasses) {
        mAdapter.mClasses = mClass.getClasses();     
      } else {
        mAdapter.mClasses = mClass.getDeclaredClasses();   
      }
      setListAdapter(mAdapter);
    }
  }
  
  public static String getModifierString(int modifiers) {
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
    
  private void fillPrologue() {
    LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View prologueView = inflater.inflate(R.layout.jclass_prologue, null);
    // Modifier display
    int modifiers = mClass.getModifiers();
    // Strip out interface bit as we display that separately
    String modDesc = getModifierString(modifiers & ~Modifier.INTERFACE);
    
    ((TextView)prologueView.findViewById(R.id.jcls_modifiers)).setText(modDesc);
    ((TextView)prologueView.findViewById(R.id.jcls_cls_or_if)).setText(mClass.isInterface() ? "interface" : "class");

    // Class name display, including link to package
    SpannableString nameString = new SpannableString(mClassName);
    TextView nameView = (TextView)prologueView.findViewById(R.id.jcls_name);
    Package pkg = mClass.getPackage();
    if (pkg != null) {
      Log.i(TAG, "Class is in package " + pkg);
      final String packageName = pkg.getName();
      int pkgNameOffset = mClassName.indexOf(packageName);
      if (pkgNameOffset != -1) {
        nameString.setSpan(mLinkColor, pkgNameOffset, pkgNameOffset + packageName.length(), 0);
        nameView.setClickable(true);
        nameView.setOnClickListener(new OnClickListener() {
          @Override public void onClick(View v) {
            Intent intent = new Intent(JavaClassActivity.this, JavaPackageActivity.class);
            intent.putExtra("jpackage_name", packageName);
            Log.i(TAG, "start JavaPackageActivity for " + packageName);
            startActivity(intent);
          }          
        });
      }
    }
    nameView.setText(nameString, BufferType.SPANNABLE);
    
    // Superclass display
    Class<?> superCls = mClass.getSuperclass();
    if (superCls != null) {
      mSuperClassName = superCls.getName();
      Log.i(TAG, "superclass of " + mClassName + " is " + mSuperClassName);
      ((TextView)prologueView.findViewById(R.id.jcls_superclass)).setText(mSuperClassName);
    } else {
      Log.i(TAG, "no superclass of " + mClassName);
      mSuperClassName = null;
      prologueView.findViewById(R.id.superclass_layout).setVisibility(View.GONE);
    }
    
    // Containing class display (for inner classes)
    Class<?> containingClass = mClass.getDeclaringClass();
    if (containingClass != null) {
      mContainingClassName = containingClass.getName();
      Log.i(TAG, "containing class for " + mClassName + " is " + mContainingClassName);
      ((TextView)prologueView.findViewById(R.id.jcls_containing_class)).setText(mContainingClassName);
      prologueView.findViewById(R.id.containing_class_layout).setVisibility(View.VISIBLE);     
    } else {
      mContainingClassName = null;
      prologueView.findViewById(R.id.containing_class_layout).setVisibility(View.GONE);     
    }
    ExpandableListView elv = getExpandableListView();
    elv.addHeaderView(prologueView);
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
   
  @Override
  public boolean onChildClick(ExpandableListView parent, View v, 
                              int groupPosition, int childPosition, long id) {
    switch (groupPosition) {
    case ClassListAdapter.INTERFACES: {  
      Intent intent = new Intent(JavaClassActivity.this, JavaClassActivity.class);
      intent.putExtra("jclass_name", mAdapter.mInterfaces[childPosition].getName());
      Log.i(TAG, "start JavaClassActivity for " + intent.getStringExtra("jclass_name"));
      startActivity(intent);
      return true;
    }
    case ClassListAdapter.CONSTRUCTORS: {
      // We can't parcel up the Constructor object, so include enough information to 
      // find it again.
      Intent intent = new Intent(JavaClassActivity.this, JavaConstructorActivity.class);
      Method method = mAdapter.mMethods[childPosition];
      intent.putExtra("jclass_name", mClassName);
      intent.putExtra("include_inherited", mIncInheritedMethods);
      intent.putExtra("index", childPosition);
      Log.i(TAG, "start JavaMethodActivity for " + method);
      startActivity(intent);
      return true;
    }
    case ClassListAdapter.METHODS: {
      // We can't parcel up the Method object, so include enough information to 
      // find it again.
      Intent intent = new Intent(JavaClassActivity.this, JavaMethodActivity.class);
      Method method = mAdapter.mMethods[childPosition];
      intent.putExtra("jclass_name", mClassName);
      intent.putExtra("include_inherited", mIncInheritedMethods);
      intent.putExtra("index", childPosition);
      Log.i(TAG, "start JavaMethodActivity for " + method);
      startActivity(intent);
      return true; 
    }
    case ClassListAdapter.FIELDS: {
      Intent intent = new Intent(JavaClassActivity.this, JavaClassActivity.class);
      intent.putExtra("jclass_name", mAdapter.mFields[childPosition].getType().getName());
      Log.i(TAG, "start JavaClassActivity for " + intent.getStringExtra("jclass_name"));
      startActivity(intent);
      return true; 
    }
    case ClassListAdapter.CLASSES: {
      Intent intent = new Intent(JavaClassActivity.this, JavaClassActivity.class);
      intent.putExtra("jclass_name", mAdapter.mClasses[childPosition].getName());
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
