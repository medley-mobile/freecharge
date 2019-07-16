package cordova.plugin.freecharge;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.content.DialogInterface;
/**
 * This class echoes a string called from JavaScript.
 */

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Random;

import in.freecharge.checkout.android.FreeChargePaymentSdk;
import in.freecharge.checkout.android.commons.FreechargeSdkEnvironment;
import in.freecharge.checkout.android.exception.FreechargeSdkException;
import in.freecharge.checkout.android.handler.FreeChargePaymentCallback;

public class freechargeplugin extends CordovaPlugin {

    private String merchanttxnId;
    private ProgressDialog progressDialog;

    private String checkSumNew = "";
    ///20d9216b-60b5-423a-a808-f33aaf713c01
   // private String merChantKey = "20d9216b-60b5-423a-a808-f33aaf713c01"; //sandbox
  private String merChantKey ="";    //production
    //  private String merchanttxnId = "1286049";
    // private String merchanttxnId = "1666332";
    //   private String merchanttxnId= String.valueOf(1000000 + random_float() * 9000000);
    private String merchantId = "";
    private String amount = "";
    private String customreName = "Nasara";
    // email@medley.com
    private String emailId = "";
    private String customerMobileNum = "";
    private String paymentType = "Cash On Delievery";
    private JSONObject inputObj;
    private String mode="";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
          inputObj = args.getJSONObject(0);
          amount=(inputObj.get("amount")).toString();
          emailId=(inputObj.get("email")).toString();
          customerMobileNum=(inputObj.get("mobile")).toString();
          merchantId=(inputObj.get("merchantId")).toString();
          merChantKey=(inputObj.get("merChantKey")).toString();
          mode=(inputObj.get("mode")).toString();
          Toast.makeText(cordova.getActivity(),inputObj.toString(), Toast.LENGTH_SHORT).show();
          //  this.coolMethod(message, callbackContext);
            this.callFreeChargePaymentService(callbackContext);
            return true;
        }
        return false;


    }

    private void callFreeChargePaymentService(CallbackContext callbackContext) {
        Random ran = new Random();
        merchanttxnId = String.valueOf((100000 + ran.nextInt(900000)));

        if(mode.toString().equals("sandbox")) {
          /* sandbox mode (for testing)*/
          FreeChargePaymentSdk.init(cordova.getActivity(), FreechargeSdkEnvironment.SANDBOX);
        }else if(mode.toString().equals("prod")){
          /* production mode*/
          FreeChargePaymentSdk.init(cordova.getActivity(), FreechargeSdkEnvironment.PRODUCTION);
        }
        String chkSumm = generateChecksum(merChantKey.toString());

        HashMap<String,String> checkoutRequestMap = new HashMap<>();
        checkoutRequestMap.put("amount", amount.toString());
        checkoutRequestMap.put("channel", "ANDROID");
        checkoutRequestMap.put("checksum", chkSumm.toString());
        checkoutRequestMap.put("currency", "INR");
//        checkoutRequestMap.put("customerName", customreName);
        checkoutRequestMap.put("email", emailId.toString());
        checkoutRequestMap.put("furl", "https://www.google.com");
        checkoutRequestMap.put("merchantId", merchantId.toString());
        checkoutRequestMap.put("merchantTxnId", merchanttxnId);
        checkoutRequestMap.put("mobile", customerMobileNum.toString());
        checkoutRequestMap.put("productInfo", "auth");
        checkoutRequestMap.put("surl", "https://www.google.com");
        Log.d("hash map ", "@@@@@@@@@@@@@@@    " + checkoutRequestMap);

        final FreeChargePaymentCallback freeChargePaymentCallback = new FreeChargePaymentCallback() {

            @Override
            public void onTransactionFailed(HashMap<String, String> txnFailResponse) {
                Toast.makeText(cordova.getActivity(), txnFailResponse.toString(), Toast.LENGTH_SHORT).show();
              JSONObject resultObj = new JSONObject();
              try {
                resultObj.put("message",  txnFailResponse.toString());
                resultObj.put("status", "failed");
              } catch (JSONException e) {
                e.printStackTrace();
              }
              callbackContext.success(resultObj);
            }

            @Override
            public void onTransactionCancelled() {
                Toast.makeText(cordova.getActivity(), "user cancelled the transaction", Toast.LENGTH_SHORT).show();
              JSONObject resultObj = new JSONObject();
              try {
                resultObj.put("message", "You Cancelled the transaction");
                resultObj.put("status", "cancelled");
              } catch (JSONException e) {
                e.printStackTrace();
              }
              callbackContext.success(resultObj);
            }

            @Override
            public void onTransactionSucceeded(HashMap<String, String> txnSuccessResponse) {
                Toast.makeText(cordova.getActivity(),txnSuccessResponse.toString(), Toast.LENGTH_SHORT).show();
                String transactionId = txnSuccessResponse.get("txnId");
                String  resultAmount = txnSuccessResponse.get("amount");
                String transactionStatus = txnSuccessResponse.get("status");
              JSONObject resultObj = new JSONObject();
              try {
                resultObj.put("amount", resultAmount);
                resultObj.put("transactionId", transactionId);
                resultObj.put("trstatus", transactionStatus);
                resultObj.put("status", "success");
              } catch (JSONException e) {
                e.printStackTrace();
              }
                callbackContext.success(resultObj);
            }

            @Override
            public void onErrorOccurred(FreechargeSdkException sdkError) {
                Toast.makeText(cordova.getActivity(), sdkError.toString(), Toast.LENGTH_SHORT).show();
              JSONObject resultObj = new JSONObject();
              try {
                resultObj.put("message", sdkError.toString());
                resultObj.put("status", "error");
              } catch (JSONException e) {
                e.printStackTrace();
              }
              callbackContext.success(resultObj);
            }
        };


        FreeChargePaymentSdk.startSafePayment(cordova.getActivity(), checkoutRequestMap, freeChargePaymentCallback);


    }


    private String generateChecksum(String merchantKey) {


        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("amount", amount.toString());
            jsonObject.put("channel", "ANDROID");
            jsonObject.put("currency", "INR");
//            jsonObject.put("customerName", customreName);
//            jsonObject.put("customNote", "please make it fast...; !");
            jsonObject.put("email", emailId.toString());
            jsonObject.put("furl", "https://www.google.com");
            jsonObject.put("merchantId", merchantId.toString());
            jsonObject.put("merchantTxnId", merchanttxnId);
            jsonObject.put("mobile", customerMobileNum.toString());
            jsonObject.put("productInfo", "auth");
            jsonObject.put("surl", "https://www.google.com");


        } catch (JSONException e) {
            e.printStackTrace();
        }

        String plainText = jsonObject.toString().concat(merchantKey.toString()).replace("\\/", "/");;

        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(plainText.getBytes(Charset.defaultCharset()));
        byte[] mdbytes = md.digest();

        // convert the byte to hex format method 1

        StringBuffer checksum = new StringBuffer();
        for (int i = 0; i < mdbytes.length; i++) {
            checksum.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        checkSumNew = checksum.toString();


        return checkSumNew;

    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(cordova.getActivity());
builder.setMessage("Look at this dialog!")
       .setCancelable(false)
       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
                //do things
                 callbackContext.success(message);
           }
       });
              AlertDialog alert = builder.create();
              alert.show();

        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}
