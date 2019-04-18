package com.example.user.volcanoalarm;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class UserActivity extends AppCompatActivity {

    UserData mUserData;

    //view
    Switch[] mSwitch = new Switch[5];
    EditText mTelEdit, mNameEdit;
    Button mOKButton;
    CompoundButton.OnCheckedChangeListener mCheckListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Switch s = (Switch)buttonView;

            if(s == mSwitch[0])mUserData.setHasChild(isChecked);
            else if(s == mSwitch[1])mUserData.setHasSenior(isChecked);
            else if(s == mSwitch[2])mUserData.setHasMedicine(isChecked);
            else if(s == mSwitch[3])mUserData.setHasPets(isChecked);
            else if(s == mSwitch[4])mUserData.setIsDisable(isChecked);
        }
    };

    View.OnClickListener mOKListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            save();
            UserActivity.this.finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        mUserData = new UserData(UserActivity.this);

        //view
        Typeface microsoft = Typeface.createFromAsset(getResources().getAssets(),"fonts/microsoft.ttf");
        ((TextView)findViewById(R.id.user_tel_textView)).setTypeface(microsoft);
        ((TextView)findViewById(R.id.user_name_textView)).setTypeface(microsoft);
        mTelEdit = findViewById(R.id.user_tel_editText);
        mNameEdit = findViewById(R.id.user_name_editText);

        mOKButton = findViewById(R.id.user_check_button);
        mOKButton.setTypeface(microsoft);
        mOKButton.setOnClickListener(mOKListener);


        mSwitch[0] = findViewById(R.id.user_switch1);
        mSwitch[1] = findViewById(R.id.user_switch2);
        mSwitch[2] = findViewById(R.id.user_switch3);
        mSwitch[3] = findViewById(R.id.user_switch4);
        mSwitch[4] = findViewById(R.id.user_switch5);
        mSwitch[0].setChecked(mUserData.hasChild());
        mSwitch[1].setChecked(mUserData.hasSenior());
        mSwitch[2].setChecked(mUserData.hasMedicine());
        mSwitch[3].setChecked(mUserData.hasPets());
        mSwitch[4].setChecked(mUserData.isDisable());
        mTelEdit.setText(mUserData.getContact() );
        mNameEdit.setText(mUserData.getName());

        for (Switch aMSwitch : mSwitch) {
            aMSwitch.setTypeface(microsoft);
            aMSwitch.setOnCheckedChangeListener(mCheckListener);
        }

        setTitle("基本資料");
    }

    private void save(){
        String tel = mTelEdit.getText().toString();
        if(tel.equals(""))tel = "0";
        mUserData.save(tel, mNameEdit.getText().toString());
    }
}
