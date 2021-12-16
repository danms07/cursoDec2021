package com.dtse.demoandroid.myapplication;

import android.content.Context;
import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huawei.hmf.tasks.Task;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.account.service.AccountAuthService;
import com.huawei.hms.support.api.entity.common.CommonConstant;

public class LoginContract extends ActivityResultContract<Context, AuthAccount> {
    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Context input) {
        AccountAuthParams mAuthParam = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setEmail()
                .setMobileNumber()
                .setIdToken()
                .createParams();

        AccountAuthService mAuthService = AccountAuthManager.getService(context, mAuthParam);
        Intent signInIntent = mAuthService.getSignInIntent();
        signInIntent.putExtra(CommonConstant.RequestParams.IS_FULL_SCREEN, true);
        return signInIntent;
    }

    @Override
    public AuthAccount parseResult(int resultCode, @Nullable Intent intent) {
        //Log.i(TAG, "onActivitResult of sigInInIntent, request code: " + REQUEST_CODE_SIGN_IN);
        Task<AuthAccount> authAccountTask = AccountAuthManager.parseAuthResultFromIntent(intent);
        if (authAccountTask.isSuccessful()) {
            // The sign-in is successful, and the authAccount object that contains the HUAWEI ID information is obtained.
            return authAccountTask.getResult();

            //Log.i(TAG, "onActivitResult of sigInInIntent, request code: " + REQUEST_CODE_SIGN_IN);
        }
        return null;
    }
}
