package org.lurklurk.lookingglass;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class JavaPackageListActivity extends ListActivity {
  private static final String TAG = "JavaPackageListActivity";

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.i(TAG, "onCreate");
    super.onCreate(savedInstanceState);
    // @@@@ this returns an empty list
    Package[] pkgList = Package.getPackages();
    String[] pkgNames = new String[pkgList.length];
    for (int ii=0; ii<pkgList.length; ii++) {
      pkgNames[ii] = pkgList[ii].getName();
      Log.d(TAG, "found package name: " + pkgNames[ii]);
    }
    
    ArrayAdapter<String> aa = new ArrayAdapter<String>(this, R.layout.text_list_item, pkgNames);
    setListAdapter(aa);
    ListView lv = getListView();
    lv.setTextFilterEnabled(true);
    lv.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String packageName = ((TextView)view).getText().toString();
        Intent intent = new Intent(JavaPackageListActivity.this, JavaPackageActivity.class);
        intent.putExtra("jpackage_name", packageName);
        Log.i(TAG, "start JavaPackageActivity for " + packageName);
        startActivity(intent);
      }
    });
  }
}