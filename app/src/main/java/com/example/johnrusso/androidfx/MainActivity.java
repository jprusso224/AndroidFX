package com.example.johnrusso.androidfx;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.*;
import android.content.Intent;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void startEffect1(View view){
        Intent intent = new Intent(this, Effect_1.class);
        startActivity(intent);
}

    public void startEffect2(View view){
        Intent intent = new Intent(this, Effect_2.class);
        startActivity(intent);
    }

    public void startEffect3(View view){
        Intent intent = new Intent(this, Effect_3.class);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
