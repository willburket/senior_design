package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilesActivity extends AppCompatActivity {
    ListView filesList;
    List<String> fileNames = new ArrayList<String>();
    String rootPath;
    CustomAdapter customAdapter;
    boolean[] selected;
    File[] files;
    LinearLayout bottom_buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.files);

        // display root path
        TextView file_path = findViewById(R.id.file_path);
        rootPath = getIntent().getStringExtra("ROOT");
        file_path.setText(rootPath.substring(rootPath.lastIndexOf('/')+1));

        // set up list views
        filesList = (ListView) findViewById(R.id.filesListView);
        customAdapter = new CustomAdapter(getApplicationContext(), fileNames);
        filesList.setAdapter(customAdapter);
        updateFilesList();
        customAdapter.notifyDataSetChanged();

        bottom_buttons = (LinearLayout) findViewById(R.id.bottom_buttons);

        // selecting an item
        // clicking on a directory
        filesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // set selected
                selected[position] = !selected[position];
                boolean isOneSelected = false;
                for (boolean b : selected) {
                    if (b) {
                        isOneSelected = true;
                        break;
                    }
                }
                if (isOneSelected) {
                    bottom_buttons.setVisibility(View.VISIBLE);
                }
                else {
                    bottom_buttons.setVisibility(View.GONE);
                }
                customAdapter.notifyDataSetChanged();
            }
        });

        // long clicking on directory
        filesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(FilesActivity.this);
                deleteDialog.setMessage("Select All?");
                deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0; i < customAdapter.getCount(); i++) {
                            selected[i] = true;
                        }
                        bottom_buttons.setVisibility(View.VISIBLE);
                        customAdapter.notifyDataSetChanged();
                    }
                });
                deleteDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                deleteDialog.show();
                return true;
            }
        });

        Button delete_button = findViewById(R.id.delete_file);
        Button preprocess_button = findViewById(R.id.preprocess_file);
        Button export_button = findViewById(R.id.export_file);

        // delete button click
        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(FilesActivity.this);
                deleteDialog.setTitle("Delete");
                deleteDialog.setMessage("Do you really want to delete these files?");
                deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i=0; i<files.length; i++) {
                            if (selected[i]) {
                                deleteFileOrFolder(files[i]);
                            }
                        }
                        Toast.makeText(FilesActivity.this, "File(s) deleted", Toast.LENGTH_SHORT).show();
                        updateFilesList();
                        customAdapter.notifyDataSetChanged();
                        bottom_buttons.setVisibility(View.GONE);
                    }
                });
                deleteDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                deleteDialog.show();
            }
        });

        // Prepreocess button click
        preprocess_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder preDialog = new AlertDialog.Builder(FilesActivity.this);
                preDialog.setTitle("View");
                preDialog.setMessage("Do you really want to view is file?");
                preDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // playback code
                        // updateFilesList();
                       // customAdapter.notifyDataSetChanged();
                        bottom_buttons.setVisibility(View.GONE);

                        Intent playbackIntent = new Intent(getApplicationContext(),
                                playback.class);
                        startActivity(playbackIntent);
                    }
                });
                preDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                preDialog.show();
            }
        });

        // export button click
        export_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder exportDialog = new AlertDialog.Builder(FilesActivity.this);
                exportDialog.setTitle("Preprocess");
                exportDialog.setMessage("Do you really want to Export these files?");
                exportDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // code to export to azure
                        updateFilesList();
                        customAdapter.notifyDataSetChanged();
                        Toast.makeText(FilesActivity.this, "File(s) exported", Toast.LENGTH_SHORT).show();
                        bottom_buttons.setVisibility(View.GONE);
                    }
                });
                exportDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                exportDialog.show();
            }
        });

        // add camera floating action button
        FloatingActionButton camera = (FloatingActionButton) findViewById(R.id.take_picture);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pass root
                String passRootDirectory = rootPath;
                Intent intent = new Intent(FilesActivity.this, VideoActivity.class);
                intent.putExtra("ROOT", passRootDirectory);
                startActivity(intent);
            }
        });
    }

    public void updateFilesList() {
        File dir = new File(rootPath);
        files = dir.listFiles();
        int filesFoundCount;
        if (files != null) {
            filesFoundCount = files.length;
        } else {
            filesFoundCount = 0;
        }
        fileNames.clear();
        for (int i = 0; i < filesFoundCount; i++) {
            if (files[i].isFile()) {
                fileNames.add(files[i].getAbsolutePath());
            }
        }
        selected = new boolean[filesFoundCount];
    }

    public class CustomAdapter extends BaseAdapter {
        Context context;
        List<String> fileNames;
        LayoutInflater inflter;

        public CustomAdapter(Context applicationContext, List<String> fileNames) {
            this.context = context;
            this.fileNames = fileNames;
            inflter = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return fileNames.size();
        }

        @Override
        public Object getItem(int i) {
            return fileNames.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = inflter.inflate(R.layout.files_item, null);
            TextView file_name = (TextView) view.findViewById(R.id.file_name);
            file_name.setText(fileNames.get(i).substring(fileNames.get(i).lastIndexOf('/') + 1));
            if (selected!=null) {
                if (selected[i]) {
                    file_name.setBackgroundColor(Color.LTGRAY);
                } else {
                    file_name.setBackgroundColor(Color.WHITE);
                }
            }
            return view;
        }
    }

    private void deleteFileOrFolder(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles())
                deleteFileOrFolder(child);
        }

        fileOrDirectory.delete();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFilesList();
        customAdapter.notifyDataSetChanged();
    }
}