package com.maps.mapsplotter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * Created by vksingh on 03/02/15.
 */
public class MyFriendListActivity extends Activity{

    MyAdapter ma ;
    List<String> name1 = new ArrayList<String>();
    List<String> phno1 = new ArrayList<String>();
    DatabaseHandler db = new DatabaseHandler(this);
    ListView lv;
    int totalFriends;
    boolean showAlert=false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display);
        if (!Utils.isInternetConnected(getApplicationContext())) {
            new AlertDialogManager().showAlertDialog(MyFriendListActivity.this,
                    "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }

        lv= (ListView) findViewById(R.id.lv);
        EditText et= (EditText) findViewById(R.id.inputSearch);
        Button bt=(Button) findViewById(R.id.button1);
        bt.setText("Delete");
        et.setVisibility(View.GONE);
        ma = new MyAdapter();
        lv.setAdapter(ma);

        bt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                List<Contact> restore=new ArrayList<Contact>();
                StringBuilder checkedcontacts= new StringBuilder();
                totalFriends=db.getContactsCount();
                System.out.println(totalFriends);
                for(int i = 0; i < totalFriends; i++)
                {
                    if(ma.mCheckStates.get(i)==false)
                    {
                      Contact cn = new Contact();
                      cn= db.getContact(i+1);
                      restore.add(cn);
                    }
                    else
                    {
                        ma.mCheckStates.put(i,false);
                        checkedcontacts.append(name1.get(i));
                        checkedcontacts.append("\n");
                    }
                }
                db.deleteAllContact();

                for(int i=0; i<restore.size();i++)
                {
                    db.addContact(restore.get(i));
                }
                if(checkedcontacts.length()==0)
                {
                  Toast.makeText(MyFriendListActivity.this, "Please select items from the list !",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MyFriendListActivity.this, checkedcontacts, Toast.LENGTH_SHORT).show();
                    Pair pair=Utils.updateFriendListInServer(getApplicationContext());
                    pair.setFriendNumber("UpdateList");
                    new SendNotificationAsynTask().execute(pair);
                }
                onResume();
                ma.notifyDataSetChanged();
            }
        });

    }

   @Override
    protected void onPause() {
       super.onPause();
       showAlert=false;
       ma.notifyDataSetInvalidated();
   }

    @Override
    protected void onResume() {
        super.onResume();
        name1.clear();
        phno1.clear();
        readDB();
    }

    public int readDB() {
        DatabaseHandler db = new DatabaseHandler(this);
        try {
          int totalFriends = db.getContactsCount();
            System.out.println(totalFriends);
            if (totalFriends != 0) {
                for (int i = 1; i <=totalFriends; i++) {
                    Contact cn = db.getContact(i);
                    String log = "Id: " + cn.getID() + " ,Name: " + cn.getName() + " ,Phone: " + cn.getPhoneNumber();
                    name1.add(cn.getName());
                    phno1.add(cn.getPhoneNumber());
                    Log.d("Name: ", log);
                }
            } else {
                if(!showAlert)
                {
                    AlertDialog.Builder
                            alertDialog = new AlertDialog.Builder(this);

                    alertDialog.setTitle("Friend List Empty !");

                    alertDialog.setMessage("Add maximum 3 friend's with whom you want to share your location ");

                    //On Pressing Setting button
                    alertDialog.setPositiveButton("Add Friend", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                            startActivity(intent);
                        }
                    });
                    //On pressing cancel button
                    alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            finish();
                        }
                    });

                    //alertDialog.show();
                    showAlert=true;
                }

            }
            return totalFriends;
        }
        finally {
            db.close();
        }
    }

//        class ViewHolder {
//        private RoundedLetterView mRoundedLetterView;
//        private TextView mTextView;
//        private TextView mPhoneNumber;
//        private CheckBox mCheckBox;
//
//
//    }

    class MyAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener,View.OnClickListener{
        LayoutInflater mInflater;
        TextView tv1, tv;
        CheckBox cb;
        RoundedLetterView mRoundedLetterView;

        private SparseBooleanArray mCheckStates;

        MyAdapter() {
            int count=readDB();
            mCheckStates = new SparseBooleanArray(count);
            mInflater = (LayoutInflater) MyFriendListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return name1.size();
        }

        @Override
        public Object getItem(int position) {
            return name1.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            if (convertView == null)
                vi = mInflater.inflate(R.layout.friendlist, null);
            mRoundedLetterView=(RoundedLetterView)vi.findViewById(R.id.rlv_name);
            tv = (TextView) vi.findViewById(R.id.friendView1);
            tv1 = (TextView) vi.findViewById(R.id.friendView2);
            cb = (CheckBox) vi.findViewById(R.id.checkBox1);
            tv.setText(name1.get(position));
            cb.setTag(position);
            cb.setChecked(mCheckStates.get(position, false));
            cb.setOnCheckedChangeListener(this);
            tv1.setText(phno1.get(position));
            String item = getItem(position).toString();
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
                    }
                    else
                    {
                        ma.mCheckStates.put(position,true);
                    }
                    notifyDataSetInvalidated();
                }
            });

            return vi;
        }
        public boolean isChecked(int position) {
            return mCheckStates.get(position, false);
        }
        @Override
        public void onClick(View v) {
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mCheckStates.put((Integer) buttonView.getTag(), isChecked);
        }
    }


}
