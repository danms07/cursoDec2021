package com.dtse.demoandroid.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapApiException;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseReq;
import com.huawei.hms.iap.entity.InAppPurchaseData;
import com.huawei.hms.iap.entity.IsEnvReadyResult;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.ProductInfo;
import com.huawei.hms.iap.entity.ProductInfoReq;
import com.huawei.hms.iap.entity.ProductInfoResult;
import com.huawei.hms.iap.entity.PurchaseIntentReq;
import com.huawei.hms.iap.entity.PurchaseIntentResult;
import com.huawei.hms.iap.entity.PurchaseResultInfo;
import com.huawei.hms.iap.util.IapClientHelper;
import com.huawei.hms.support.api.client.Status;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private IapClient iapClient;
    private TextView productName, productDesc, productPrice;
    private Button purchaseBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        productName = findViewById(R.id.nombreProducto);
        productDesc = findViewById(R.id.descripcionProducto);
        productPrice = findViewById(R.id.precioProducto);
        purchaseBtn = findViewById(R.id.comprar);
        iapClient = Iap.getIapClient(this);
        initIAP();
    }

    public void initIAP() {
        Task<IsEnvReadyResult> task = iapClient.isEnvReady();
        task.addOnSuccessListener((result) -> onEnvReady());
        task.addOnFailureListener(this::onEnvFail);
    }

    private void onEnvReady() {
        //Descargar las ordenes de compra purchaseInfoRequest
        //Verificar que todas se hayan entregado for
        //Reintentar la entrega de los consumibles que no se hayan entregado consumeOwnedPurchase
        //Descargar la lista de productios

        List<String> productIdList = new ArrayList<>();
        productIdList.add("COIN");
        ProductInfoReq req = new ProductInfoReq();
        // priceType: 0: consumable; 1: non-consumable; 2: subscription
        req.setPriceType(0);
        req.setProductIds(productIdList);
        Task<ProductInfoResult> task = iapClient.obtainProductInfo(req);
        task.addOnSuccessListener(this::onProducts);
        task.addOnFailureListener(this::onProductFail);
    }

    private void onProducts(ProductInfoResult result) {
        List<ProductInfo> productList = result.getProductInfoList();
        //Actualizar la interfaz grafica con el resultado
        ProductInfo coin = productList.get(0);
        productName.setText(coin.getProductName());
        productDesc.setText(coin.getProductDesc());
        productPrice.setText(coin.getPrice());
        purchaseBtn.setOnClickListener((v) -> purchase(coin.getProductId()));

    }

    private void purchase(String productId) {
        // Construct a PurchaseIntentReq object.
        PurchaseIntentReq req = new PurchaseIntentReq();
// Only those products already configured in AppGallery Connect can be purchased through the createPurchaseIntent API.
        req.setProductId(productId);
// priceType: 0: consumable; 1: non-consumable; 2: subscription
        req.setPriceType(0);
// Call the createPurchaseIntent API to create a managed product order.
        Task<PurchaseIntentResult> task = iapClient.createPurchaseIntent(req);
        task.addOnSuccessListener(purchaseIntentResult -> {
            Status status = purchaseIntentResult.getStatus();
            if (status.hasResolution()) {
                try {
                    // 6666 is a constant defined by yourself.
                    // Open the checkout screen returned.
                    status.startResolutionForResult(MainActivity.this, 6666);
                } catch (IntentSender.SendIntentException exp) {
                }
            }
        });
        task.addOnFailureListener(exception->{
            exception.printStackTrace();
        });
    }

    private void onProductFail(Exception e) {
        if (e instanceof IapApiException) {
            IapApiException apiException = (IapApiException) e;
            int returnCode = apiException.getStatusCode();
        } else {
            // Other external errors.
        }
    }

    private void onEnvFail(Exception e) {
        if (e instanceof IapApiException) {
            IapApiException apiException = (IapApiException) e;
            Status status = apiException.getStatus();
            if (status.getStatusCode() == OrderStatusCode.ORDER_HWID_NOT_LOGIN) {
                // HUAWEI ID is not signed in.
                if (status.hasResolution()) {
                    try {
                        // 6666 is a constant defined by yourself.
                        // Open the sign-in screen returned.
                        status.startResolutionForResult(MainActivity.this, 1000);
                    } catch (IntentSender.SendIntentException exp) {

                    }
                }
            } else if (status.getStatusCode() == OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED) {
                // The current country/region does not support HUAWEI IAP.
            }
        } else {
            // Other external errors.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (data != null) {
                // Call the parseRespCodeFromIntent method to obtain the result of the API request.
                int returnCode = IapClientHelper.parseRespCodeFromIntent(data);
                if (returnCode == 0) {
                    onEnvReady();
                }
            }
        }
        else if(requestCode==6666){
            //Procesamos el resultado de la orden de compra
            if (data == null) {
                Log.e("onActivityResult", "data is null");
                return;
            }

            // Call the parsePurchaseResultInfoFromIntent method to parse the payment result.
            PurchaseResultInfo purchaseResultInfo = iapClient.parsePurchaseResultInfoFromIntent(data);
            switch(purchaseResultInfo.getReturnCode()) {
                case OrderStatusCode.ORDER_STATE_CANCEL:
                    // The user cancels the purchase.
                    break;
                case OrderStatusCode.ORDER_STATE_FAILED:
                case OrderStatusCode.ORDER_PRODUCT_OWNED:
                    // Reintentar entrega.
                    break;
                case OrderStatusCode.ORDER_STATE_SUCCESS:
                    // The payment is successful.
                    String inAppPurchaseData = purchaseResultInfo.getInAppPurchaseData();
                    String inAppPurchaseDataSignature = purchaseResultInfo.getInAppDataSignature();
                    // Verify the signature using your app's IAP public key.
                    // Start delivery if the verification is successful.
                    // Call the consumeOwnedPurchase API to consume the product after delivery if the product is a consumable.
                    //entregarProducto(productId)
                    try {
                        // Obtain purchaseToken from InAppPurchaseData.
                        InAppPurchaseData inAppPurchaseDataBean = new InAppPurchaseData(inAppPurchaseData);
                        String purchaseToken = inAppPurchaseDataBean.getPurchaseToken();
                        ConsumeOwnedPurchaseReq req = new ConsumeOwnedPurchaseReq();
                        req.setPurchaseToken(purchaseToken);
                        iapClient.consumeOwnedPurchase(req);
                        //Termina el proceso de compra
                    } catch (JSONException e) {
                    }

                    break;
                default:
                    break;
            }
        }
    }


}