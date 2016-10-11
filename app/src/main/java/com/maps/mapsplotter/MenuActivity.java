package com.maps.mapsplotter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.maps.database.Contact;
import com.maps.database.DatabaseHandler;

import java.util.ArrayList;
import java.util.List;

/**
* Created by vksingh on 02/02/15.
*/
public class MenuActivity extends Activity implements AdapterView.OnItemClickListener {

    List<String> name1 = new ArrayList<String>();
    List<String> phno1 = new ArrayList<String>();
    DatabaseHandler db = new DatabaseHandler(this);
    MyAdapter ma ;
    Button select;
    int globalInc;
    ListView lv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display);

        if (!Utils.isInternetConnected(getApplicationContext())) {
            new AlertDialogManager().showAlertDialog(MenuActivity.this,
                    "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }
        getAllContacts(this.getContentResolver());
        lv= (ListView) findViewById(R.id.lv);
        EditText et= (EditText) findViewById(R.id.inputSearch);
        et.setVisibility(View.GONE);
        ma = new MyAdapter();
        lv.setAdapter(ma);
        lv.setOnItemClickListener(this);
        lv.setItemsCanFocus(false);
        lv.setTextFilterEnabled(true);
        // adding
        select = (Button) findViewById(R.id.button1);
        select.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v) {
        StringBuilder checkedcontacts= new StringBuilder();

                int count=1;
                for(int i = 0; i < name1.size(); i++)
                {
                    if(ma.mCheckStates.get(i)==true)
                    {
                        ma.mCheckStates.put(i,false);
                        checkedcontacts.append(name1.get(i));
                        checkedcontacts.append("\n");
                        try {
                            if (db.getContactsCount() >= 3) {
                                Contact cn = new Contact();
                                cn.setID(count++);
                                cn.setName(name1.get(i));
                                cn.setPhoneNumber(phno1.get(i));
                                db.updateContact(cn);
                            } else {
                                Log.d("Insert: ", "Inserting ..");
                                db.addContact(new Contact(name1.get(i), phno1.get(i)));
                            }
                        }
                        finally {
                            db.close();
                        }
                    }
                }
               readDB();
                if(checkedcontacts.length()>0) {
                   Toast.makeText(MenuActivity.this, checkedcontacts, Toast.LENGTH_SHORT).show();
                    Pair pair=Utils.updateFriendListInServer(getApplicationContext());
                    pair.setFriendNumber("UpdateList");
                    new SendNotificationAsynTask().execute(pair);
                    FriendList.tabHost.setCurrentTab(0);
                }
                else
                {
                    Toast.makeText(MenuActivity.this, "Please select valid contact !", Toast.LENGTH_SHORT).show();
                }
                globalInc=0;
                ma.notifyDataSetInvalidated();
            }
        });
    }
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        ma.toggle(arg2);
    }

    public void readDB()
    {
        DatabaseHandler db = new DatabaseHandler(this);
        try {
            int totalFriends = db.getContactsCount();
            for (int i = 1; i <= totalFriends; i++) {
                Contact cn = db.getContact(i);
                String log = "Id: " + cn.getID() + " ,Name: " + cn.getName() + " ,Phone: " + cn.getPhoneNumber();
                Log.d("Name: ", log);
            }
        }
        finally {
            db.close();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public  void getAllContacts(ContentResolver cr) {

        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
        Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, sortOrder);
        while (phones.moveToNext())
        {
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            name1.add(name);
            phno1.add(phoneNumber);
        }

        phones.close();
    }


    class MyAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener
    {  private SparseBooleanArray mCheckStates;
        LayoutInflater mInflater;
        RoundedLetterView mRoundedLetterView;
        TextView tv1,tv;
        CheckBox cb;
        MyAdapter()
        {
            mCheckStates = new SparseBooleanArray(name1.size());
            mInflater = (LayoutInflater)MenuActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount() {
            return name1.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View vi=convertView;
            if(convertView==null)
            vi = mInflater.inflate(R.layout.friendlist, null);
            mRoundedLetterView=(RoundedLetterView)vi.findViewById(R.id.rlv_name);
            tv= (TextView) vi.findViewById(R.id.friendView1);
            tv1= (TextView) vi.findViewById(R.id.friendView2);
            cb = (CheckBox) vi.findViewById(R.id.checkBox1);
            tv.setText(name1.get(position));
            tv1.setText(phno1.get(position));
            cb.setTag(position);
            cb.setOnCheckedChangeListener(null);
            cb.setChecked(mCheckStates.get(position, false));
            cb.setOnCheckedChangeListener(this);
            String item = name1.get(position);
            if (item != null) {

                tv.setText(item);
                tv1.setText(phno1.get(position));
                if (item.length() == 0) {
                    mRoundedLetterView.setTitleText("?");
                } else {
                    mRoundedLetterView.setTitleText(Utils.multiLetter(item));
                }
                if (position % 4 == 0) {

                    mRoundedLetterView.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.yellow));
                } else if (position % 4 == 1) {

                    mRoundedLetterView.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.red));
                } else if (position % 4 == 2) {

                    mRoundedLetterView.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.blue));
                } else {

                    mRoundedLetterView.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.green));
                }
            }
            vi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(isChecked(position))
                    {
                        ma.mCheckStates.put(position,false);
                        globalInc--;
                    }
                    else
                    {
                        ma.mCheckStates.put(position,true);
                        globalInc++;
                    }

                    if(globalInc >= 4)// it will allow 3 checkboxes only
                    {
                        Toast.makeText(getApplicationContext(), "Maximum 3 friends can be selected !", Toast.LENGTH_SHORT).show();
                        ma.mCheckStates.put(position, false);
                        globalInc--;
                    }
                    lv.smoothScrollToPosition(position);
                    notifyDataSetChanged();
                }

            });

            return vi;
        }
        public boolean isChecked(int position) {
            return mCheckStates.get(position, false);
        }

        public void setChecked(int position, boolean isChecked) {
            mCheckStates.put(position, isChecked);
        }
        public void toggle(int position) {
            setChecked(position, !isChecked(position));
        }
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if(isChecked)
            {
                globalInc++;
            }
            else if(!isChecked)
            {
                globalInc--;
            }
            if(globalInc >= 4)// it will allow 3 checkboxes only
            {
                Toast.makeText(getApplicationContext(), "Maximum 3 friends can be selected !", Toast.LENGTH_SHORT).show();
                buttonView.setChecked(false);
                globalInc--;
            }
            else
            {
                lv.smoothScrollToPosition((Integer)buttonView.getTag());
                mCheckStates.put((Integer) buttonView.getTag(), isChecked);
            }
        }
    }
}