package com.zyj010.huaba.ui;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.runzii.lib.utils.Logs;
import com.zyj010.huaba.R;
import com.zyj010.huaba.databinding.ActivityRegisterBinding;
import com.zyj010.huaba.http.NetworkWrapper;
import com.zyj010.huaba.model.User;
import com.zyj010.huaba.model.http.RegistBody;
import com.zyj010.huaba.utils.Toasts;

import java.util.concurrent.TimeUnit;

import io.vov.vitamio.utils.Log;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;


/**
 * Created by zyj010 on 2016/4/11 0011.
 */
public class RegisterAcitivity extends AppCompatActivity {
    private User user;

    private ActivityRegisterBinding binding;
    private Long time;

    public static final int DELAY_SECONDS = 10;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register);
        RxTextView.textChanges(binding.registerUser)
                .map(CharSequence::toString)
                .map(s -> !TextUtils.isEmpty(s) && s.length() == 11)
                .subscribe(aBoolean -> binding.btnSendCode.setEnabled(aBoolean));
        RxView.clicks(binding.btnSendCode)
                .subscribeOn(AndroidSchedulers.mainThread())
                .flatMap(aVoid1 -> {
                    sendCode();
                    Logs.d("fuck");
                    return Observable.interval(0, 1, TimeUnit.SECONDS)
                            .observeOn(AndroidSchedulers.mainThread()).take(DELAY_SECONDS + 1);
                })
                .map(aLong1 -> DELAY_SECONDS - aLong1.intValue())
                .map(integer -> String.valueOf(integer))
                .map(s -> {
                    binding.btnSendCode.setText("剩下" + s + "s");
                    return s.equals("0");
                })
                .subscribe(aBoolean -> {
                    binding.btnSendCode.setEnabled(aBoolean);
                    if (aBoolean) {
                        binding.btnSendCode.setText(R.string.text_send_verifycode);
                    }
                });
    }

    public void sendCode() {
        String phone_number = binding.registerUser.getText().toString();
        if (TextUtils.isEmpty(phone_number) || phone_number.length() != 11) {
            Toast.makeText(getApplicationContext(), "请输入正确的手机号", Toast.LENGTH_SHORT).show();
            return;
        } else {

            NetworkWrapper.get().sendVerifyCode(phone_number, 1)
                    .subscribe(responseBody -> Toasts.show("验证码发送成功"), throwable -> {
                        Toasts.show(throwable.getMessage());
                        Logs.d(throwable.getMessage());
                    });
        }
    }

    public void regist(View view) {

        String userId = binding.registerUser.getText().toString();
        String password = binding.registerPassword.getText().toString();
        String code = binding.registerVerify.getText().toString();

        if (TextUtils.isEmpty(userId) || userId.length() != 11) {
            Toast.makeText(getApplicationContext(), "请输入正确的手机号", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6 || password.length() > 12) {
            Toast.makeText(getApplicationContext(), "请输入6-12位密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(getApplicationContext(), "请输入验证码", Toast.LENGTH_SHORT).show();
            return;
        } else {

            RegistBody registBody = new RegistBody();
            registBody.setPasswrod(password);
            registBody.setPhone(userId);
            registBody.setUsername(userId);
            NetworkWrapper.get().regist(code, registBody)
                    .subscribe(responseBody -> Toasts.show("成功"), throwable -> {
                        Toasts.show(throwable.getMessage());
                        Logs.d(throwable.getMessage());
                    });

        }
    }
}
