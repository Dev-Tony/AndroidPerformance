package org.jt.performance;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    static {
        System.loadLibrary("android_performance");
    }

    class Entry {
        public String label;
        public Intent intent;

        @Override
        public String toString() {
            return label;
        }
    }

    public static final int LOAD_APPS = 0;

    private static final String ACTION_TEST = "org.jt.performance.ACTION_TEST";
    private EntriesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = (ListView) findViewById(R.id.app_list);
        adapter = new EntriesAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(adapter);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                PackageManager pm = getPackageManager();
                List<ResolveInfo> infos = pm.queryIntentActivities(new Intent(ACTION_TEST), PackageManager.MATCH_DEFAULT_ONLY);
                List<Entry> entries = new ArrayList<Entry>(infos.size());
                for (ResolveInfo info :
                        infos) {
                    CharSequence label = info.loadLabel(pm);
                    Entry entry = new Entry();
                    ActivityInfo activity = info.activityInfo;
                    entry.label = label != null ? label.toString() : activity.name;
                    entry.intent = new Intent();
                    entry.intent.setClassName(activity.applicationInfo.packageName, activity.name);
                    entries.add(entry);
                }
                adapter.setData(entries);
            }
        });
    }

    class EntriesAdapter extends ArrayAdapter<Entry> implements AdapterView.OnItemClickListener{
        public EntriesAdapter() {
            super(MainActivity.this, android.R.layout.simple_list_item_1);
        }

        public void setData(List<Entry> entries) {
            clear();
            addAll(entries);
            notifyDataSetChanged();
        }


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Entry entry = getItem(position);
            startActivity(entry.intent);
        }
    }
}
