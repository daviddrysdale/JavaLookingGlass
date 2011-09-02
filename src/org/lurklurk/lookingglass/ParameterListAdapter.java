package org.lurklurk.lookingglass;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView; 

public class ParameterListAdapter extends BaseExpandableListAdapter {
  private static final String TAG = "ParameterListAdapter";
  
  // Array index constants
  public static final int PARAMETERS = 0;
  public static final int EXCEPTIONS = 1;
  
  private String[] mGroups = { "Parameters", "throws..."};
  private Context mContext;
  
  public Class<?>[] mParameterTypes;
  public Class<?>[] mExceptions;
  
  public ParameterListAdapter(Context context) {
    super();
    mContext = context;
  }
  @Override public Object getGroup(int groupPosition) { return mGroups[groupPosition]; }
  @Override public int getGroupCount() { return mGroups.length; }
  @Override public long getGroupId(int groupPosition) { return groupPosition; } 
  
  @Override public Object getChild(int groupPosition, int childPosition) {
    switch (groupPosition) {
    case PARAMETERS: return mParameterTypes[childPosition];
    case EXCEPTIONS: return mExceptions[childPosition];
    default: Log.e(TAG, "Unknown group position " + groupPosition); return "Error!";
    }
  }
  @Override public long getChildId(int groupPosition, int childPosition) { return childPosition; }

  @Override public int getChildrenCount(int groupPosition) {
    switch (groupPosition) {
    case PARAMETERS: return mParameterTypes.length;
    case EXCEPTIONS: return mExceptions.length;
    default: Log.e(TAG, "Unknown group position " + groupPosition); return 0;
    }
  }
  @Override
  public View getChildView(int groupPosition, int childPosition,
                           boolean isLastChild, View convertView,
                           ViewGroup parent) {
    TextView textView = (TextView)TextView.inflate(mContext, R.layout.text_link_item, null);
    String linkInfo = null;
    switch (groupPosition) {
    case PARAMETERS: {
      linkInfo = mParameterTypes[childPosition].getName();
      break;
    }
    case EXCEPTIONS: {
      linkInfo = mExceptions[childPosition].getName();
      break;
    }
    default: {
      Log.e(TAG, "Unknown group position " + groupPosition); 
      linkInfo = "";
      break;
    }
    }
    textView.setText(linkInfo);
    return textView;
  }
  @Override
  public View getGroupView(int groupPosition, boolean isExpanded,
                           View convertView, ViewGroup parent) {
    TextView textView = (TextView) TextView.inflate(mContext, R.layout.text_list_group, null);  
    textView.setText(getGroup(groupPosition).toString());
    return textView;
  }
  @Override public boolean hasStableIds() { return false; }
  @Override public boolean isChildSelectable(int groupPosition, int childPosition) { return true; }
}
