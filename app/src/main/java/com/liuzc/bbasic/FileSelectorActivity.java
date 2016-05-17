package com.liuzc.bbasic;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileSelectorActivity extends ListActivity {

    private TextView pathTextOnTop_TextView ;
    private List<String> listOnCurrentPath_List = new ArrayList<>();
    private ArrayAdapter<String> listAdapter ;

    Handler updateTopPathText_handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0: {
                    pathTextOnTop_TextView.setText((String)msg.obj);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_selector);

        pathTextOnTop_TextView = (EditText)findViewById(R.id.path);
        listAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,listOnCurrentPath_List);
        setListAdapter(listAdapter);

        ImageButton upPath_button = (ImageButton)findViewById(R.id.back);
        ImageButton setting_button = (ImageButton)findViewById(R.id.setting);
        ImageButton resize_button = (ImageButton)findViewById(R.id.resize);

        upPath_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPath = pathTextOnTop_TextView.getText().toString();
                if(currentPath.equals("/")) {
                    System.exit(0);
                }
                if(currentPath.contains("/")){
                    currentPath = currentPath.substring(0,currentPath.lastIndexOf("/"));
                    if(currentPath.equals("")) currentPath = "/";
                    if(currentPath.contains("/")) new refreshFileList_Task().execute(currentPath,".bin");
                }
            }
        });

        setting_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FileSelectorActivity.this, PreferenceActivity.class));
            }
        });

        resize_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FileSelectorActivity.this, LayoutSettingActivity.class));
            }
        });

        try {
            String baseFilePath = Environment.getExternalStorageDirectory().getCanonicalPath() + "/BBasic/";
            File file = new File(baseFilePath);
            if (!file.exists()) {
                if(!file.mkdir()){
                    Toast.makeText(this, "创建BBasic文件夹失败,请检查磁盘写权限", Toast.LENGTH_SHORT).show();
                }
            }
            new refreshFileList_Task().execute(baseFilePath,".bin");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "请选择程序文件(.bin)", Toast.LENGTH_SHORT).show();

    }

    @Override
    public boolean onKeyDown(int KeyCode,KeyEvent keyEvent){
        if(KeyCode == KeyEvent.KEYCODE_BACK && keyEvent.getRepeatCount() == 0){
            String currentPath = pathTextOnTop_TextView.getText().toString();
            if(currentPath.equals("/")) {
                System.exit(0);
            }
            if(currentPath.contains("/")){
                currentPath = currentPath.substring(0,currentPath.lastIndexOf("/"));
                if(currentPath.equals("")) currentPath = "/";
                if(currentPath.contains("/")) new refreshFileList_Task().execute(currentPath,".bin");
            }
            return true;
        }
        return true;
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id){
        super.onListItemClick(listView, view, position, id);
        String itemSelected_String = listOnCurrentPath_List.get(position);
        String absolutePath;
        if(pathTextOnTop_TextView.getText().toString().equals("/")){
            absolutePath = pathTextOnTop_TextView.getText() + itemSelected_String;
        }else {
            absolutePath = pathTextOnTop_TextView.getText() + "/" + itemSelected_String;
        }

        String lastChar = absolutePath.substring(absolutePath.length()-1);
        if(lastChar.equals("/")){
            absolutePath = absolutePath.substring(0,absolutePath.length()-1);
            File selected = new File(absolutePath);
            if(selected.isDirectory()) {
                new refreshFileList_Task().execute(absolutePath,".bin");
            }
        }else{
            SharedPreferences setting = getSharedPreferences("com.liuzc.bbasic_preferences",MODE_PRIVATE);
            boolean isfullscreen = setting.getBoolean("isfullscreen",false);
            String m = setting.getString("msdelay_time", "1");
            int msdelay_time = 1000;
            if (m.equals("0.5")) msdelay_time = 500;
            else if (m.equals("2")) msdelay_time = 2000;
            else if (m.equals("4")) msdelay_time = 4000;
            else if (m.equals("0")) msdelay_time = 0;
            int gettick_which = Integer.valueOf(setting.getString("gettick_which", "0"));
            String f = setting.getString("flippage_pauseTime","0");
            if (!f.matches("\\d+")) {
                f = "0";
                setting.edit().putString("flippage_pauseTime","0").apply();
            }
            int flippage_pauseTime = Integer.valueOf(f);
            int background_color = Integer.parseInt(setting.getString("background_color","ECECED"),16);

            SharedPreferences layout = getSharedPreferences("LayoutSettingActivity",MODE_PRIVATE);
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            float rate = layout.getFloat("rate", 1f);
            float center_x = layout.getFloat("center_x", dm.widthPixels / 2);
            float center_y = layout.getFloat("center_y", dm.heightPixels / 2);
            //Toast.makeText(getBaseContext(), ""+isfullscreen+","+msdelay_time+","+gettick_which+","+flippage_pauseTime+","+background_color+","+rate+","+center_x+","+center_y+","+absolutePath, Toast.LENGTH_SHORT).show();
            //Log.e("",""+isfullscreen+","+msdelay_time+","+gettick_which+","+flippage_pauseTime+","+background_color+","+rate+","+center_x+","+center_y+","+absolutePath);

            Intent intent = new Intent(FileSelectorActivity.this, MainActivity.class);
            intent.putExtra("isfullscreen",isfullscreen);
            intent.putExtra("msdelay_time",msdelay_time);
            intent.putExtra("gettick_which",gettick_which);
            intent.putExtra("flippage_pauseTime",flippage_pauseTime);
            intent.putExtra("background_color",background_color);
            intent.putExtra("rate",rate);
            intent.putExtra("center_x",center_x);
            intent.putExtra("center_y",center_y);
            intent.putExtra("absolutePath",absolutePath);
            startActivity(intent);
            //finish();
        }
    }

    private class refreshFileList_Task extends AsyncTask<String, String, List> {
        protected List doInBackground(final String... params){
            updateTopPathText_handler.obtainMessage(0,params[0]).sendToTarget();
            return getTypeListFromPath(params[0], params[1]);
        }

        protected void onPostExecute(List list){
            listOnCurrentPath_List.clear();
            listOnCurrentPath_List.addAll(list);
            listAdapter.notifyDataSetChanged();
        }
    }

    private List getTypeListFromPath(final String rootPath,String type){
        List<String> list = new ArrayList<>();
        File files = new File(rootPath);
        String path,filename;
        if(files.listFiles() == null) return list;
        for(File single : files.listFiles()){
            if(single.isDirectory() && single.getPath().contains("/")) {
                path = single.getPath();
                path = path.substring(path.lastIndexOf('/')+1) + "/";
                list.add(path);
            }else if(single.isFile()){
                filename = single.getName();
                if (filename.contains(".")) {
                    if(filename.substring(filename.lastIndexOf('.')).equals(type)) {
                        list.add(filename);
                    }
                }
            }
        }
        return list;
    }

}
