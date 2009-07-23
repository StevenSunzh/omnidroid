/*******************************************************************************
 * Copyright 2009 OmniDroid - http://code.google.com/p/omnidroid 
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 *******************************************************************************/
package edu.nyu.cs.omnidroid.ui.simple;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import edu.nyu.cs.omnidroid.R;
import edu.nyu.cs.omnidroid.ui.Constants;
import edu.nyu.cs.omnidroid.ui.simple.model.ModelAttribute;
import edu.nyu.cs.omnidroid.ui.simple.model.ModelEvent;
import edu.nyu.cs.omnidroid.ui.simple.model.ModelRuleFilter;

/**
 * This dialog shows a list of attributes linked to the selected root event. After the user selects
 * an attribute to filter on, we move them to the <code>ActivityDlgFilters</code> dialog.
 */
public class ActivityDlgAttributes extends Activity {

  private ListView listView;
  private AdapterAttributes adapterAttributes;
  private SharedPreferences state;

  private static final String KEY_STATE = "StateDlgAttributes";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_dlg_attributes);
    // setTitle("Attributes");

    ModelEvent event = RuleBuilder.instance().getChosenEvent();
    adapterAttributes = new AdapterAttributes(this, event);

    listView = (ListView) findViewById(R.id.activity_dlg_attributes_listview);
    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    listView.setAdapter(adapterAttributes);

    TextView mTextViewInfo = (TextView) findViewById(R.id.activity_dlg_attributes_tv_info1);
    mTextViewInfo.setText("Select an attribute of [" + event.getTypeName() + "] to filter on:");

    Button btnOk = (Button) findViewById(R.id.activity_dlg_attributes_btnOk);
    btnOk.setOnClickListener(listenerBtnClickOk);
    Button btnInfo = (Button) findViewById(R.id.activity_dlg_attributes_btnInfo);
    btnInfo.setOnClickListener(listenerBtnClickInfo);
    Button btnCancel = (Button) findViewById(R.id.activity_dlg_attributes_btnCancel);
    btnCancel.setOnClickListener(listenerBtnClickCancel);

    UtilUI.inflateDialog((LinearLayout) findViewById(R.id.activity_dlg_attributes_ll_main));

    // Restore UI state.
    state = getSharedPreferences(ActivityDlgAttributes.KEY_STATE, Context.MODE_WORLD_READABLE
        | Context.MODE_WORLD_WRITEABLE);
    listView.setItemChecked(state.getInt("selectedAttribute", -1), true);
  }

  @Override
  protected void onPause() {

    super.onPause();

    // Save UI state.
    SharedPreferences.Editor prefsEditor = state.edit();
    prefsEditor.putInt("selectedAttribute", listView.getCheckedItemPosition());
    prefsEditor.commit();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    // If the user constructed a valid filter, also kill ourselves.
    ModelRuleFilter filter = RuleBuilder.instance().getChosenRuleFilter();
    if (filter != null) {
      // Be sure to wipe our UI state, otherwise the onStop will save it!
      resetUI();
      finish();
    }
  }

  private View.OnClickListener listenerBtnClickOk = new View.OnClickListener() {
    public void onClick(View v) {
      // The user has chosen an attribute, now get a list of filters associated
      // with that attribute, from the database.
      int position = listView.getCheckedItemPosition();
      if (position < 0) {
        UtilUI.showAlert(v.getContext(), "Sorry!",
            "Please select an attribute from the list above to filter on!");
        return;
      }

      // Show a dialog with only these filters listed.
      showDlgFilters();
    }
  };

  private View.OnClickListener listenerBtnClickInfo = new View.OnClickListener() {
    public void onClick(View v) {
      // TODO: (markww) Add help info about attribute.
      UtilUI.showAlert(v.getContext(), "Sorry!",
          "We'll implement an info dialog about the selected attribute soon!");
    }
  };

  private View.OnClickListener listenerBtnClickCancel = new View.OnClickListener() {
    public void onClick(View v) {
      // Be sure to wipe our UI state, otherwise the onStop will save it!
      resetUI();
      finish();
    }
  };

  /**
   * Start the filters activity, which will show all possible filters for the selected attribute.
   */
  private void showDlgFilters() {
    int position = listView.getCheckedItemPosition();
    if (position < 0) {
      UtilUI.showAlert(this, "Sorry!", "Please select a filter from the list above, then hit OK!");
      return;
    }

    // Store the selected attribute in the RuleBuilder so the next activity can pick it up.
    ModelAttribute attribute = (ModelAttribute) adapterAttributes.getItem(position);
    RuleBuilder.instance().setChosenAttribute(attribute);

    Intent intent = new Intent();
    intent.setClass(getApplicationContext(), ActivityDlgFilters.class);
    startActivityForResult(intent, Constants.ACTIVITY_RESULT_ADD_FILTER);
  }

  private void resetUI() {
    if (listView.getCheckedItemPosition() > -1) {
      listView.setItemChecked(listView.getCheckedItemPosition(), false);
    }
  }

  /**
   * Here we display attributes associated with our parent root event.
   */
  public class AdapterAttributes extends BaseAdapter {
    private Context context;
    private ArrayList<ModelAttribute> attributes;

    public AdapterAttributes(Context context, ModelEvent eventRoot) {
      this.context = context;

      // Fetch all available attributes for the root event from the
      // database.
      attributes = UIDbHelperStore.instance().db().getAttributesForEvent(eventRoot);
    }

    public int getCount() {
      return attributes.size();
    }

    public Object getItem(int position) {
      return attributes.get(position);
    }

    public long getItemId(int position) {
      return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

      LinearLayout ll = new LinearLayout(context);
      ll.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.FILL_PARENT,
          LayoutParams.FILL_PARENT));
      ll.setMinimumHeight(50);
      ll.setOrientation(LinearLayout.HORIZONTAL);
      ll.setGravity(Gravity.CENTER_VERTICAL);

      ImageView iv = new ImageView(context);
      iv.setImageResource(attributes.get(position).getIconResId());
      iv.setAdjustViewBounds(true);
      iv.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT,
          LayoutParams.WRAP_CONTENT));
      if (listView.getCheckedItemPosition() == position) {
        iv.setBackgroundResource(R.drawable.icon_hilight);
      }

      TextView tv = new TextView(context);
      tv.setText(attributes.get(position).getDescriptionShort());
      tv.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.FILL_PARENT,
          LayoutParams.FILL_PARENT));
      tv.setGravity(Gravity.CENTER_VERTICAL);
      tv.setPadding(10, 0, 0, 0);
      tv.setTextSize(14.0f);
      tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
      tv.setTextColor(context.getResources().getColor(R.color.list_element_text));
      tv.setMinHeight(46);

      ll.addView(iv);
      ll.addView(tv);

      return ll;
    }
  }
}