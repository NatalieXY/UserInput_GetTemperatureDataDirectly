package com.example.android.bluetoothlegatt;

/**
 * Created by Xingyu on 11/19/17.
 */


        import android.content.Context;
        import android.content.Intent;
        import android.os.Bundle;
        import android.app.Activity;
        import android.support.annotation.Nullable;
        import android.text.InputFilter;
        import android.text.InputType;
        import android.text.Spanned;
        import android.view.View;
        import android.view.WindowManager;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ListView;
        import android.widget.TextView;

        import java.util.UUID;

public class UserInputAddressActivity extends Activity {

    private TextView targetAddress;
    private Button button1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.macaddress);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        targetAddress = (EditText) findViewById(R.id.macAdd);


        button1 = (Button) findViewById(R.id.Enter);

        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String mac_address = targetAddress.getText().toString();
                Intent intent = new Intent(UserInputAddressActivity.this, DeviceScanActivity.class);
               // Intent intent = new Intent(UserInputAddressActivity.this, AutoConnectActivity.class);
                intent.putExtra("character_id", "F000AA01-0451-4000-B000-000000000000");
                intent.putExtra("service_id", "F000AA00-0451-4000-B000-000000000000");
                intent.putExtra("ctrl2_character_id", "F0001112-0451-4000-B000-000000000000");
                intent.putExtra("ctrl2_service_id", "F0001110-0451-4000-B000-000000000000");
                intent.putExtra("address", mac_address);
                startActivity(intent);

            }
        });


    }}
