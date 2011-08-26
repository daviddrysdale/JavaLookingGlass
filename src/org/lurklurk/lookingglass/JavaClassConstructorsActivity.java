package org.lurklurk.lookingglass;

import java.lang.reflect.Constructor;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class JavaClassConstructorsActivity extends ListActivity {
  private static final String TAG = "JavaClassConstructorsActivity";

  private String mClassName;
  private Class<?> mClass;
  
  /** Called when the activity is first created. */
  public void onCreate(Bundle savedInstanceState) {
    Log.i(TAG, "onCreate");
    super.onCreate(savedInstanceState);
    
    Intent intent = getIntent();
    mClassName = intent.getStringExtra("jclass_name");  
    try
    {
      mClass = Class.forName(mClassName);
    }
    catch (ClassNotFoundException e)
    {
      Log.e(TAG, "Class " + mClassName + " not found!");
      finish();
    }
    Constructor<?>[] ctorList = mClass.getConstructors();
    
    String[] ctorNames = new String[ctorList.length];
    for (int ii=0; ii<ctorList.length; ii++) {
      ctorNames[ii] = ctorList[ii].getName();
      Log.d(TAG, "found constructor name: " + ctorNames[ii]);
    }
    
    ArrayAdapter<String> aa = new ArrayAdapter<String>(this, R.layout.jclass_ctor_list_item, ctorNames);
    setListAdapter(aa);
    ListView lv = getListView();
    lv.setTextFilterEnabled(true);
    lv.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String packageName = ((TextView)view).getText().toString();
        /* @@@
        Intent intent = new Intent(JavaPackageListActivity.this, JavaPackageActivity.class);
        intent.putExtra("jpackage_name", packageName);
        Log.i(TAG, "start JavaPackageActivity for " + packageName);
        startActivity(intent);
        */
      }
    });
  }
   
}
