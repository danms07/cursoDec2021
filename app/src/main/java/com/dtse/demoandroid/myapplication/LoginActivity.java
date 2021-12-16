package com.dtse.demoandroid.myapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.account.service.AccountAuthService;
import com.huawei.hms.support.api.entity.common.CommonConstant;
import com.huawei.hms.support.hwid.ui.HuaweiIdAuthButton;

public class LoginActivity extends AppCompatActivity {

    ActivityResultLauncher<Context> loginLauncher=registerForActivityResult(new LoginContract(), this::onLogin);

    private void onLogin(AuthAccount authAccount) {
        if(authAccount!=null){
            //Inicio de sesión exitoso
            authAccount.getEmail();//Si está vacía, el usuario no accedió a complartirlo
            Toast.makeText(this,"Bienvenid@ "+authAccount.getDisplayName(),Toast.LENGTH_LONG).show();
            //Navega a la pantalla principal
            Intent intent=new Intent(this,RemoteActivity.class);
            startActivity(intent);
            finish();

        }else {
            //Falló el inicio de sesión
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        HuaweiIdAuthButton loginButton=findViewById(R.id.hwLogin);
        loginButton.setOnClickListener((v)->loginHuawei());
    }

    private void loginHuawei() {
        loginLauncher.launch(this);
        //startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
    }
}