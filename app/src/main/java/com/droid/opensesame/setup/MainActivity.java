package com.droid.opensesame.setup;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import android.Manifest;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private List<Row> rowList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    private static final String PREFERENCE = "address";

    private static final int REQUEST_PERMISSION = 1;
    private static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };

    private DownloadManager downloadManager;
    DownloadReceiver downloadReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        adapter = new RowsAdapter(rowList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.addOnItemTouchListener(createRecycleViewListener());
        recyclerView.setAdapter(adapter);

        createRowData();
        initializeDownloadManagement();

        if (!isPermissionGranted()) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS,
                    REQUEST_PERMISSION
            );
        }

        createDirectories();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(downloadReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            return;
        }

    }

    private void initializeDownloadManagement() {
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        downloadReceiver = new DownloadReceiver();
        registerReceiver(downloadReceiver, filter);
    }

    private void createDirectories() {
        File experimentsDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + getString(R.string.exp_dir_path_relative));
        experimentsDir.mkdir();

        File resultsDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + getString(R.string.results_dir_path_relative));
        resultsDir.mkdir();
    }

    private RecyclerTouchListener createRecycleViewListener() {
        return new RecyclerTouchListener(getApplicationContext(),
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                selectAction(position);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        });
    }

    private void createRowData() {
        List<Row> rows = Arrays.asList(
                new Row(getString(R.string.header_download_runtime),
                        getString(R.string.desc_download_runtime)),
                new Row(getString(R.string.header_set_server),
                        getString(R.string.desc_set_server)),
                new Row(getString(R.string.header_check_updates),
                        getString(R.string.desc_check_updates)),
                new Row(getString(R.string.header_choose_experiment),
                        getString(R.string.desc_choose_experiment)),
                new Row(getString(R.string.header_new_experiment),
                        getString(R.string.desc_new_experiment)),
                new Row(getString(R.string.header_share_results),
                        getString(R.string.desc_share_results))
        );

        rowList.addAll(rows);
        adapter.notifyDataSetChanged();
    }

    private void selectAction(int position) {
        switch (position) {
            case 0:
                downloadRuntime();
                break;
            case 1:
                setServerConfiguration();
                break;
            case 2:
                checkForUpdates();
                break;
            case 3:
                selectExperiment();
                break;
            case 4:
                startExperiment();
                break;
            case 5:
                shareResults();
                break;
            default:
                break;
        }
    }

    private void downloadRuntime() {
        Context context = getApplicationContext();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(
                Uri.parse("market://details?id=" + getString(R.string.opensesame_package_name)));
        context.startActivity(intent);
    }

    private void setServerConfiguration() {
        String preference = getServerAddress();

        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.edit_server, null);
        TextInputEditText editText = dialogView.findViewById(R.id.edit_text_input);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.title_server_address))
                .setView(dialogView)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TextInputEditText editText = dialogView.findViewById(R.id.edit_text_input);
                        String address = editText.getText().toString().trim();

                        if (!address.isEmpty()) {
                            getPreferences(Context.MODE_PRIVATE)
                                    .edit()
                                    .putString(PREFERENCE, address)
                                    .apply();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();

        editText.setText(preference);


    }

    @NonNull
    private String getServerAddress() {
        return getPreferences(Context.MODE_PRIVATE)
                .getString(PREFERENCE, getString(R.string.server_address));
    }

    private void checkForUpdates() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = getString(R.string.server_address) + getString(R.string.endpoint_exp);

        StringRequest request =
                new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        Type listType = new TypeToken<List<String>>() {
                        }.getType();

                        List<String> files = gson.fromJson(response, listType);

                        Toast.makeText(getApplicationContext(),
                                getString(R.string.new_files) + files.toString(),
                                Toast.LENGTH_SHORT)
                                .show();

                        downloadExperimentFiles(files);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });

        queue.add(request);
    }

    private void downloadExperimentFiles(List<String> files) {
        for (String file : files) {
            downloadSingleFile(file);
        }
    }

    private long downloadSingleFile(String file) {
        File externalStorageExperiments =
                new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + File.separator + getString(R.string.exp_dir_path_relative));
        Uri destinationUri = Uri.withAppendedPath(Uri.fromFile(externalStorageExperiments), file);
        Uri uri = Uri.parse(getString(R.string.server_address)
                + getString(R.string.endpoint_exp) + file);

        long downloadReference;

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(getString(R.string.data_download));
        request.setDescription(getString(R.string.description) + file);
        Environment.getExternalStorageDirectory();
        request.setDestinationUri(destinationUri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE
                | DownloadManager.Request.NETWORK_WIFI);
        request.setVisibleInDownloadsUi(true);

        downloadReference = downloadManager.enqueue(request);


        return downloadReference;
    }

    //Default android template is broken for OpenSesame Kafkaesque Koffka version
    //Only some of example experiments work (e.g. Gaze cuing)
    private void selectExperiment() {
        if (isPermissionGranted()) {
            File directory = new File(getString(R.string.exp_dir_path));
            final String[] files = directory.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(getString(R.string.extension));
                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.select_experiment));
            builder.setItems(files, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    writeExperimentFile(files[which]);
                }
            });
            builder.show();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.warning_grant_permission),
                    Toast.LENGTH_LONG).show();
        }

    }

    private void writeExperimentFile(String experiment) {
        String filename = getString(R.string.autorun_file);
        StringBuilder builder = new StringBuilder();
        String fileContents = builder
                .append("experiment: ")
                .append(getString(R.string.exp_dir_path))
                .append(experiment)
                .append(System.lineSeparator())
                .append("subject_nr: 1")
                .append(System.lineSeparator())
                .append("logfile: ")
                .append(getString(R.string.result_dir_path))
                .append(getString(R.string.results_filename))
                .toString();

        try {
            FileOutputStream outputStream = new FileOutputStream(new File(filename), false);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        File file = new File(getString(R.string.autorun_file));
        file.setReadable(true, false);
        file.setExecutable(true, false);
    }

    private void startExperiment() {
        Context context = getApplicationContext();
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(getString(R.string.opensesame_package_name));

        if (intent == null) {
            Toast.makeText(context, getString(R.string.warning_download_runtime), Toast.LENGTH_LONG)
                    .show();
            return;
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void shareResults() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = getString(R.string.server_address) + getString(R.string.endpoint_results);

        String resultsFile = getString(R.string.result_dir_path) + getString(R.string.results_filename);
        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(), getString(R.string.results_sent),
                                Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(com.android.volley.error.VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });

        request.addFile("file", resultsFile);
        queue.add(request);
    }

    private boolean isPermissionGranted() {
        int permission = ContextCompat
                .checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

}
