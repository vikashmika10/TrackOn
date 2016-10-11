package com.maps.mapsplotter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
public class SampleAdapter extends BaseAdapter implements Filterable {
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<String>originalData = null;
    List<String> searchResult=null;
    public List<String> name1 = new ArrayList<>();
    public List<String> phno1 = new ArrayList<>();
    private ItemFilter mFilter = new ItemFilter();
    public int selectedPosition;
    ListView mListView;
    View mLastView;
    private SparseBooleanArray mCheckStates;
    private View currentSelectedView;
    private Boolean firstTimeStartup = false;



    public SampleAdapter(Context context){
        getAllContacts(context.getContentResolver());
        originalData=name1;
        searchResult=phno1;
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        mCheckStates = new SparseBooleanArray(name1.size());


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
    @Override
    public int getCount() {
        return this.name1.size();
    }
    @Override
    public String getItem(int position) {
        return this.name1.get(position);
    }
    @Override
    public long getItemId(int position) {
        return (long) position;
    }
    @Override
    public View getView( final int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;
        if(convertView == null){
            mViewHolder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.list_item,null);
            mViewHolder.mRoundedLetterView = (RoundedLetterView) convertView.findViewById(R.id.rlv_name_view);
            mViewHolder.mTextView = (TextView) convertView.findViewById(R.id.tv_name_holder);
            mViewHolder.mPhoneNumber = (TextView) convertView.findViewById(R.id.phoneNumber);
            convertView.setTag(mViewHolder);
            System.out.println(" I am Here " +position);
            if (firstTimeStartup) {
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }

        }else{
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        String item = getItem(position);
        if (item != null){

            mViewHolder.mTextView.setText(item);
            mViewHolder.mPhoneNumber.setText(phno1.get(position));
            if(item.length() == 0){
                mViewHolder.mRoundedLetterView.setTitleText("?");
            }else{
                mViewHolder.mRoundedLetterView.setTitleText(Utils.multiLetter(item));
            }
            if(position%4 == 0){

                mViewHolder.mRoundedLetterView.setBackgroundColor(mContext.getResources().getColor(R.color.green));
            }else if(position%4 == 1){

                mViewHolder.mRoundedLetterView.setBackgroundColor(mContext.getResources().getColor(R.color.red));
            }
            else if(position%4 == 2)
            {

                mViewHolder.mRoundedLetterView.setBackgroundColor(mContext.getResources().getColor(R.color.blue));
            }
            else
            {

                mViewHolder.mRoundedLetterView.setBackgroundColor(mContext.getResources().getColor(R.color.yellow));
            }
        }

        return convertView;
    }

    private void select(View view) {


        view.setBackgroundColor(mContext.getResources().getColor(R.color.yellow));

    }

    private void deselect(View view) {

        view.setBackgroundColor(mContext.getResources().getColor(R.color.abc_background_cache_hint_selector_material_light));
    }



    private static class ViewHolder {
        private RoundedLetterView mRoundedLetterView;
        private TextView mTextView;
        private TextView mPhoneNumber;


    }
    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {

        FilterResults results2 = new FilterResults();
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();
            System.out.println(filterString);

            FilterResults results = new FilterResults();
            name1=originalData;
            phno1=searchResult;
            final List<String> list = originalData;
            int count = list.size();
            System.out.println(count);
            final ArrayList<String> nlist = new ArrayList<String>(count);
            final ArrayList<String> plist = new ArrayList<String>(count);

            String filterableString ;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i);
                if (filterableString.toLowerCase().contains(filterString)) {
                    System.out.println(filterableString);
                    System.out.println(phno1.get(i));
                    nlist.add(filterableString);
                    plist.add(phno1.get(i));
                }
            }
            results2.values=plist;
            results2.count=plist.size();
            results.values = nlist;
            results.count = nlist.size();
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            name1 = (ArrayList<String>) results.values;
            phno1=(ArrayList<String>)results2.values;
            notifyDataSetInvalidated();
        }


    }


}