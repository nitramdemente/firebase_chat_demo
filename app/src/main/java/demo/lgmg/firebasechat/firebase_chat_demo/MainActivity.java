package demo.lgmg.firebasechat.firebase_chat_demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import demo.lgmg.firebasechat.firebase_chat_demo.login.LoginActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnlogin = (Button) findViewById(R.id.btnlogin);
        Intent i = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(i);
        finish();
//        btnlogin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
    }

}
