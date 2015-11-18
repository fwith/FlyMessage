package com.fwith.mobile.android.flymessage;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class SMSHistoryFragment extends Fragment implements AbsListView.OnItemClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static AbsListView mListView;
    //    private static ListAdapter mAdapter;
    private static SQLController dbcon;
    private static SimpleCursorAdapter adapter;
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;

    public SMSHistoryFragment() {
    }

    public static void refresh() {
        Cursor cursor = dbcon.readData();
        adapter.changeCursor(cursor);
        mListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        dbcon = new SQLController(getActivity());
        dbcon.open();

        // Get tracker.
        Tracker t = ((BaseApplication) getActivity().getApplication()).getTracker(BaseApplication.TrackerName.APP_TRACKER);
        // Enable Advertising Features.
        t.enableAdvertisingIdCollection(true);
        t.setScreenName("SMSHistoryFragment");
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    @Override
    public void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(getActivity()).reportActivityStart(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(getActivity()).reportActivityStop(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_smshistory, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.tool_bar);
        toolbar.setTitle("FlyMessageHistory");
        toolbar.setTitleTextColor(Color.WHITE);

        mListView = (AbsListView) view.findViewById(android.R.id.list);
//        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        mListView.setOnItemClickListener(this);


        Button buttonFileSave = (Button) view.findViewById(R.id.button_file_save);
        buttonFileSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tracker t = ((BaseApplication) getActivity().getApplication()).getTracker(BaseApplication.TrackerName.APP_TRACKER);
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("SMSHistoryFragment")
                        .setAction("buttonFileSave")
                        .setLabel("fileSave").build());

                convertCSV();
            }
        });

        ImageButton buttonRefresh = (ImageButton) view.findViewById(R.id.button_refresh);
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        Cursor cursor = dbcon.readData();
        String[] from = new String[]{DBhelper.SMS_DATE, DBhelper.SMS_PHONENUMBER, DBhelper.SMS_CONTENTS};
        int[] to = new int[]{R.id.sms_date, R.id.sms_phonenum, R.id.sms_contents};
        adapter = new SimpleCursorAdapter(getActivity(), R.layout.view_sms_entry, cursor, from, to);
        mListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    private void convertCSV() {
        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.fwith.mobile.android.flymessage";
        File dir = new File(sdPath, "db");

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, "message_history.csv");
        try {
            Cursor cursor = dbcon.readData();
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            csvWrite.writeNext(cursor.getColumnNames());
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                String arrStr[] = {cursor.getString(0), cursor.getString(1), cursor.getString(2)};
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            cursor.close();

            Intent intentShareFile = new Intent(Intent.ACTION_SEND);
            File fileWithinMyDir = new File(file.getAbsolutePath());

            if (fileWithinMyDir.exists()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("(yyMMdd)", Locale.getDefault());
                Date date = new Date();

                intentShareFile.setType("application/csv");
                intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.getAbsolutePath()));
                intentShareFile.putExtra(Intent.EXTRA_SUBJECT, dateFormat.format(date) + " FlyMessage");
                intentShareFile.putExtra(Intent.EXTRA_TEXT, "FlyMessage에서 보낸 파일");
                startActivity(Intent.createChooser(intentShareFile, "Share File"));
            }
        } catch (SQLException | IOException sqlEx) {
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(String id);
    }
}
