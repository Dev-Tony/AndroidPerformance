package org.jt.performance;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by tony on 5/16/16.
 */
public class Download extends Activity implements LoaderManager.LoaderCallbacks<Void> {
    //    static {
//        HostnameVerifier allHostsValid = new HostnameVerifier() {
//            @Override
//            public boolean verify(String hostname, SSLSession session) {
//                return true;
//            }
//        };
//
//        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
//    }
    private static final String TAG = "MainActivity";

    private ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        mProgress = (ProgressBar) findViewById(R.id.progress);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Void> onCreateLoader(int id, Bundle args) {
//        return new HttpClientLoader(getApplicationContext(), new MyProgressCallback(mProgress));
        return new HttpConnectionLoader(getApplicationContext(), new MyProgressCallback(mProgress));
    }

    @Override
    public void onLoadFinished(Loader<Void> loader, Void data) {
    }

    @Override
    public void onLoaderReset(Loader<Void> loader) {
        loader.abandon();
    }

    static class HttpClientLoader extends AsyncTaskLoader<Void> {
        final ProgressCallback pcb;
        public HttpClientLoader(Context context, ProgressCallback pcb) {
            super(context);
            this.pcb = pcb;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        public Void loadInBackground() {
            AndroidHttpClient client = AndroidHttpClient.newInstance("Linux; androidnemoupdate/272");
            HttpGet request = new HttpGet("https://scdn.ainemo.com/as/150130/new/location_app_release-2.6.0-18843.apk?");

            try {
                HttpResponse response = client.execute(request);
                int code = response.getStatusLine().getStatusCode();
                if (HttpStatus.SC_OK == code) {
                    HttpEntity entity = response.getEntity();
                    long total = entity.getContentLength();
                    InputStream input = entity.getContent();
                    byte[] bytes = new byte[2048];
                    int length = 0;
                    long count = 0;
                    int progress = 0;
                    while ((length = input.read(bytes)) > 0) {
                        count += length;
                        int p = (int) ((100 * count) / total);
                        if (progress != p) {
                            progress = p;
                            pcb.setProgressValue(progress);
                        }
                    }
                    Log.d(TAG, "doInBackground: download finish!");
                } else {
                    Log.i(TAG, "status code: " + code);
                }
            } catch (Exception e) {
                Log.e(TAG, "loadInBackground: ", e);
            } finally {
                client.close();
            }

            return null;
        }
    }

    static class HttpConnectionLoader extends AsyncTaskLoader<Void> {
        final ProgressCallback pcb;

        public HttpConnectionLoader(Context context, ProgressCallback pcb) {
            super(context);
            this.pcb = pcb;
        }

        @Override
        public Void loadInBackground() {
            HttpURLConnection conn = null;
            try {
//                URL url = new URL("https", "116.253.191.152", "as/150130/new/location_app_release-2.6.0-18843.apk");
                URL url = new URL("https", "scdn.ainemo.com", "as/150130/new/location_app_release-2.6.0-18843.apk");
                conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                int code = conn.getResponseCode();
                if (code == 200) {
                    Map<String, List<String>> headers = conn.getHeaderFields();
//                        for (Map.Entry<String, List<String>> header:
//                             headers.entrySet()) {
//                            List<String> value = header.getValue();
//                            Log.i(TAG, String.format("header %s=%s", header.getKey(), value.size() > 1 ? value.toString() : value.get(0)));
//                        }
                    String strTotal = headers.get("Content-Length").get(0);
                    long total = Long.parseLong(strTotal);
                    InputStream inputStream = new BufferedInputStream(conn.getInputStream());
                    byte[] bytes = new byte[2048];
                    int length = 0;
                    long count = 0;
                    int progress = 0;
                    while ((length = inputStream.read(bytes)) > 0) {
                        count += length;
                        int p = (int) ((100 * count) / total);
                        if (progress != p) {
                            progress = p;
                            pcb.setProgressValue(progress);
                        }
                    }
                    Log.d(TAG, "doInBackground: download finish!");
                }

            } catch (Exception e) {
                Log.e(TAG, "loadInBackground: ", e);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }
    }


    static class MyProgressCallback implements ProgressCallback {
        private final ProgressBar mProgress;

        MyProgressCallback(ProgressBar mProgress) {
            this.mProgress = mProgress;
        }

        @Override
        public void setProgressValue(final int progress) {
            mProgress.post(new Runnable() {
                @Override
                public void run() {
                    mProgress.setProgress(progress);
                }
            });
        }
    }
}
interface ProgressCallback {
    void setProgressValue(final int progress);
}