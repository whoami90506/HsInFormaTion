package com.example.user.volcanoalarm;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class UserData {


    //file
    Context mContext;
    public static final String DataFileName = "UserData.txt";
    public static final String CheckListFileName = "CheckList.txt";

    //data
    boolean[] mOptions = new boolean[5]; //hasChild, hasSenior, hasMedicine, hasPet, isDisabled
    String mContact, mName;


    public UserData(Context c){
        Log.v("MyTag", "UserData/constructor");
        mContext = c;

        try {
            FileInputStream is = c.openFileInput(DataFileName);
            if (is == null)init();
            else{
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String temp;
                boolean success = true;
                for(int i = 0;i < mOptions.length; ++i){
                    temp = br.readLine();
                    if(temp == null)success = false;
                    else if(!temp.equals("0") && !temp.equals("1"))success = false;
                    else mOptions[i] = (temp.equals("1"));

                    if(!success){
                        init();
                        break;
                    }
                }

                if(success){
                    mContact = br.readLine();
                    if(mContact == null){
                        init();
                        success = false;
                    }
                }

                if(success){
                    mName = br.readLine();
                    if(mName == null)init();
                }
                is.close();

                Log.v("MyTag", "UserData/constructor: success in reading file.");

            }
        } catch (FileNotFoundException e) {
            init();
        } catch (IOException e) {
            init();
        }
    }

    public boolean hasChild(){return mOptions[0]; }
    public boolean hasSenior(){return mOptions[1]; }
    public boolean hasMedicine(){return mOptions[2]; }
    public boolean hasPets(){return mOptions[3]; }
    public boolean isDisable(){return mOptions[4]; }
    public String getContact(){ return mContact.equals("0") ? "" : mContact; }
    public String getName(){ return mName; }

    public void setHasChild(boolean b){ mOptions[0] = b; }
    public void setHasSenior(boolean b){ mOptions[1] = b; }
    public void setHasMedicine(boolean b){ mOptions[2] = b; }
    public void setHasPets(boolean b){ mOptions[3] = b; }
    public void setIsDisable(boolean b){ mOptions[4] = b; }

    public void save(String tel, String name){
        Log.v("MyTag", "UserData/save");
        try {
            FileOutputStream os = mContext.openFileOutput(DataFileName, 0);
            for (boolean mOption : mOptions) {
                if (mOption) os.write('1');
                else os.write('0');

                os.write('\n');
            }
            mContact = tel;
            os.write(mContact.getBytes());
            os.write('\n');
            mName = name;
            os.write(mName.getBytes());
            os.write('\n');

            os.close();
        } catch (FileNotFoundException e) {
            Log.v("MyTagError", "UserData/save: " + e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.v("MyTagError", "UserData/save: " + e);
            e.printStackTrace();
        }

        requestChecklist();
    }

    private void init(){
        Log.v("MyTag", "UserData/init");
        for(int i = 0; i < mOptions.length; ++i)mOptions[i] = false;
        save("0", "");
    }

    private void requestChecklist(){
        Log.v("MyTag", "UserData/requestChecklist");
        String[] checklistUrl = new String[6];
        for(int i = 0; i < mOptions.length; ++i)
            if(mOptions[i])checklistUrl[i] = "1";
            else checklistUrl[i] = "0";
        checklistUrl[5] = mContact;

        getCheckListTask t = new getCheckListTask();
        t.execute(checklistUrl);
    }

    private class getCheckListTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... s) {
            Log.v("MyTag", "UserData/getCheckListTask/doInBackGround");
            String url = QueryUtils.MainUrl + "/checklist/opt";
            for(int i = 0; i < 6; ++i)url = url + "/" + s[i];
            String ans = QueryUtils.makeHTTPRequest(url);

            if(ans != null){
                try {
                    FileOutputStream os = mContext.openFileOutput(CheckListFileName, 0);
                    os.write(ans.getBytes());
                    os.write('\n');
                    os.close();
                } catch (FileNotFoundException e) {
                    Log.v("MyTagErr", "UserData/getCheckListTask/doInBackGround: " + e);
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.v("MyTagErr", "UserData/getCheckListTask/doInBackGround: " + e);
                    e.printStackTrace();
                }
                return ans;
            }

            return "";
        }
    }


}
