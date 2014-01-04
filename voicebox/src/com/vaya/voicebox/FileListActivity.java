package com.vaya.voicebox;

import java.io.File;  
import java.util.ArrayList;  
import java.util.List;  

import com.vaya.voicebox.ShakeInterface.OnShakeListener;
  
import android.app.AlertDialog;  
import android.app.ListActivity;  
import android.content.Context;
import android.content.DialogInterface;  
import android.os.Bundle;  
import android.util.Log;
import android.view.View;  
import android.widget.ArrayAdapter;  
import android.widget.ListView;  
import android.widget.TextView;  

public class FileListActivity extends ListActivity {
	
	
	private String TAG = "FileListActivity";
	ShakeInterface shake;
	MySensorEventListener mySensorEventListener = new MySensorEventListener();
	Context context;
	
	
	/** Called when the activity is first created. */  
    private List<String> items = null;//�������  
    private List<String> paths = null;//���·��  
    private String rootPath = "/";  
    private TextView tv;  
    
    
	private class MySensorEventListener implements OnShakeListener{

		@Override
		public void onShake() {
			// TODO Auto-generated method stub
			Log.d(TAG, getfile());
		}
		
	};
  
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_filelist);  
        tv = (TextView) this.findViewById(R.id.TextView);  
        this.getFileDir(rootPath);//��ȡrootPathĿ¼�µ��ļ�.  
        
        shake_phone(this);
    }  
  
    public void getFileDir(String filePath) {  
        try{  
            this.tv.setText("��ǰ·��:"+filePath);// ���õ�ǰ����·��  
            items = new ArrayList<String>();  
            paths = new ArrayList<String>();  
            File f = new File(filePath);  
            File[] files = f.listFiles();// �г������ļ�  
            // ������Ǹ�Ŀ¼,���г����ظ�Ŀ¼����һĿ¼ѡ��  
            if (!filePath.equals(rootPath)) {  
                items.add("���ظ�Ŀ¼");  
                paths.add(rootPath);  
                items.add("������һ��Ŀ¼");  
                paths.add(f.getParent());  
            }  
            // �������ļ�����list��  
            if(files != null){  
                int count = files.length;
                for (int i = 0; i < count; i++) {  
                    File file = files[i];  
                    items.add(file.getName());  
                    paths.add(file.getPath());  
                }  
            }  
  
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,  
                    android.R.layout.simple_list_item_1, items);  
            this.setListAdapter(adapter);  
        }catch(Exception ex){  
            ex.printStackTrace();  
        }  
  
    }  
  
    @Override  
    protected void onListItemClick(ListView l, View v, int position, long id) {  
        super.onListItemClick(l, v, position, id);  
        String path = paths.get(position);  
        File file = new File(path);  
        //������ļ��оͼ����ֽ�  
        if(file.isDirectory()){  
            this.getFileDir(path);  
        }else{  
            new AlertDialog.Builder(this).setTitle("��ʾ").setMessage(file.getName()+" ��һ���ļ���").setPositiveButton("OK", new DialogInterface.OnClickListener(){  
  
                public void onClick(DialogInterface dialog, int which) {  
                                          
                }  
                  
            }).show();  
        }  
    }
    
	public void shake_phone(Context context){
		//System.out.println("haha\n");
		this.context = context;
		shake = new ShakeInterface(context);
		shake.registerOnShakeListener(mySensorEventListener);
		shake.start();
	}
	
	 
	 public String getfile(){
		 File file = new File("/sdcard/VoiceBox/");
		 int len = file.list().length - 1;
		 String lastfile = "";
		 long time = 0;
		 for(;len >= 0;len--){
			 if(time < file.listFiles()[len].lastModified()){
				 time = file.listFiles()[len].lastModified();
				 lastfile = file.list()[len];
			 }
		 }
		 
		 return lastfile;
	 }
}
