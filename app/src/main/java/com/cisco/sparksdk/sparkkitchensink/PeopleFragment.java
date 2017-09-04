package com.cisco.sparksdk.sparkkitchensink;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.ciscospark.CompletionHandler;
import com.ciscospark.Spark;
import com.ciscospark.SparkError;
import com.ciscospark.people.Person;
import com.ciscospark.people.PersonClient;

import java.util.ArrayList;
import java.util.List;

public class PeopleFragment extends Fragment {

    private static final String TAG = "PeopleFragment";
    private ListView mListView;
    private SearchView mSearchView;
    private Spark mSpark;
    private List<Person> mPersonList;

    public PeopleFragment() {
        // Required empty public constructor
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: ->start");
        mSpark = ((KitchenSinkApplication)getActivity().getApplication()).mSpark;
        mPersonList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_people, container, false);
        mListView = (ListView)rootView.findViewById(R.id.people_list);
        mSearchView = (SearchView)rootView.findViewById(R.id.search_people);
        PersonAdapter adapter = new PersonAdapter(getActivity(), R.layout.listview_person, mPersonList);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final Person item = (Person) parent.getItemAtPosition(position);
                Log.d(TAG, item.getEmails()[0]);
                ((DialActivity)getActivity()).makeCall(item.getEmails()[0]);
            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, query);
                if (query.isEmpty()) return true;

                mSearchView.setEnabled(false);
                mSearchView.setIconified(true);
                mSearchView.clearFocus();
                mSearchView.setInputType(InputType.TYPE_NULL);

                mPersonList.clear();
                ((BaseAdapter) mListView.getAdapter()).notifyDataSetChanged();

                PersonClient client = mSpark.people();
                String email = null;
                String name = null;
                if (isValidEmail(query)) {
                    email = query;
                } else {
                    name = query;
                }
                client.list(email, name, 100, new CompletionHandler<List<Person>>() {
                    @Override
                    public void onComplete(List<Person> persons) {
                        mSearchView.setEnabled(true);
                        mSearchView.setInputType(InputType.TYPE_CLASS_TEXT);
                        mPersonList.clear();
                        Log.d(TAG, "list person get:" + persons.size());
                        for (Person p : persons) {
                            mPersonList.add(p);
                        }
                        ((BaseAdapter) mListView.getAdapter()).notifyDataSetChanged();
                        Toast.makeText(getActivity(), "find " + persons.size() + " person", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(SparkError sparkError) {
                        mSearchView.setEnabled(true);
                        mSearchView.setInputType(InputType.TYPE_CLASS_TEXT);
                        mPersonList = null;
                        Toast.makeText(getActivity(), "find error", Toast.LENGTH_SHORT).show();
                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return rootView;
    }

    public class PersonAdapter extends ArrayAdapter<Person> {

        private int resourceId;

        public PersonAdapter(Context context, int textViewResourceId,
                            List<Person> objects) {
            super(context, textViewResourceId, objects);
            resourceId = textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Person person = getItem(position);
            View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            TextView name = (TextView) view.findViewById(R.id.person_name);
            TextView email = (TextView) view.findViewById(R.id.person_email);
            name.setText(person.getDisplayName());
            email.setText(person.getEmails()[0]);
            return view;
        }

    }
}
