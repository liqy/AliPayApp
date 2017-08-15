package com.bwie.alipayapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.bwie.alipayapp.model.PayResult;
import com.bwie.alipayapp.util.OrderInfoUtil2_0;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    /**
     * 支付宝支付业务：入参app_id
     */
    public static final String APPID = "2017030606072113";

    /** 商户私钥，pkcs8格式 */
    /** 如下私钥，RSA2_PRIVATE 或者 RSA_PRIVATE 只需要填入一个 */
    /** 如果商户两个都设置了，优先使用 RSA2_PRIVATE */
    /** RSA2_PRIVATE 可以保证商户交易在更加安全的环境下进行，建议使用 RSA2_PRIVATE */
    /** 获取 RSA2_PRIVATE，建议使用支付宝提供的公私钥生成工具生成， */
    /**
     * 工具地址：https://doc.open.alipay.com/docs/doc.htm?treeId=291&articleId=106097&docType=1
     */
    public static final String RSA2_PRIVATE = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKcKWATl3ZKr1U3x+VCIny97xMpiLY1aaxACyBGc8tfMPXrAQqn3crNZwTTR1UHaCrIMM4pJtXkPAtN/01TNMHRY556RTWdHQBBjxsHsGWtiBO82nqKMy7qBFcafe0169KcUmkJ3WVW58NM+286wOYdYU4s0gYzpTk8esZAB+XelAgMBAAECgYA7Z59T5pe9EKUkOjjLHjtWfLLIss+2ZICMyiByuxlWC4mVvQg4QAsno3TlnvYznCKPnW068em66s67fssebnabZM6Qo+WVGy7tDNIKVD+hIhLmIAm24kKFM0BcfxZlo+VqMU8sEUtEbuI0tttxp7EDQKmbgncXod6NRGxmzMQPpQJBAPTY8TMkNl8n+uNEXM5n1vg8aqVlzRokMxONa45L2M+gD3TScSBqkWKJsO9Tal+MwAz+rR1Y2LMffwShfEFhGUMCQQCupiDV7+Nt5g+jsIaA7tfj3j3ddmYjWMUaw2skfDEVIxODg+DudjvEUos0nBSJwvHIhATq4GhH6KK/91QBgAj3AkBtOPVn3eiPTDNj/FP2E2ZW+ASO4bm7xpguSbDIGleOTxV2BIZIcqGGNmwCZtCV7SCi61zoMYEBbrRnqW3XcGwnAkB9fK5hKYE/KKuaEK9EGtkSSFApPPY8dX8CMOFeEMHvjwlpWKuYi2l8MVcGURNMvL6fNYSXAvDKojO2PM6mWUmXAkEAn0MjcTbtLj9ADFu5Dg5SrFkph3QnUsPyXMcGfTCeaFPTeyYyFIXORR/3CtLcYLo4zjBpghQxQQqYsCVDyFhZ6Q==";
    public static final String RSA_PRIVATE = "";


    TextView payAli;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        payAli=(TextView)findViewById(R.id.payAli);
        payAli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payV2();
            }
        });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            PayResult payResult = new PayResult((Map<String, String>) msg.obj);
            /**
             对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
             */
            String resultInfo = payResult.getResult();// 同步返回需要验证的信息
            String resultStatus = payResult.getResultStatus();
            // 判断resultStatus 为9000则代表支付成功
            if (TextUtils.equals(resultStatus, "9000")) {
                // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                Toast.makeText(MainActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
            } else {
                // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                Toast.makeText(MainActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * 支付宝支付业务
     *
     */
    public void payV2() {
        if (TextUtils.isEmpty(APPID) || (TextUtils.isEmpty(RSA2_PRIVATE) && TextUtils.isEmpty(RSA_PRIVATE))) {
            new AlertDialog.Builder(this).setTitle("警告").setMessage("需要配置APPID | RSA_PRIVATE")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {
                            //
                            finish();
                        }
                    }).show();
            return;
        }

        /**
         * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
         * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
         * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险；
         *
         * orderInfo的获取必须来自服务端；
         */
        boolean rsa2 = (RSA2_PRIVATE.length() > 0);
        Map<String, String> params = OrderInfoUtil2_0.buildOrderParamMap(APPID, rsa2);
        String orderParam = OrderInfoUtil2_0.buildOrderParam(params);

        String privateKey = rsa2 ? RSA2_PRIVATE : RSA_PRIVATE;
        String sign = OrderInfoUtil2_0.getSign(params, privateKey, rsa2);

        final String orderInfo = orderParam + "&" + sign;//通过网络请求获取

        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                PayTask alipay = new PayTask(MainActivity.this);
                Map<String, String> result = alipay.payV2(orderInfo, true);
                Log.i("msp", result.toString());
                Message msg = new Message();
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };

        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }
}
