package br.gbizotto.saitama;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.gbizotto.saitama.data.SaitamaContract;
import br.gbizotto.saitama.integration.JitenshaApi;
import br.gbizotto.saitama.integration.JitenshaParameters;
import br.gbizotto.saitama.utils.ValidationUtil;

public class PaymentActivity extends AppCompatActivity {

    private static final String LOG_TAG = PaymentActivity.class.getSimpleName();

    Context mContext;

    // UI elements
    private Button mButton;
    private TextView mCreditCardOwnerTextView;
    private TextView mCreditCardNumberTextView;
    private TextView mCreditCardExpirationTextView;
    private TextView mCreditCardSecurityCodeTextView;

    // Values
    private String mCreditCardNumber;
    private String mCreditCardOwner;
    private String mCreditCardExpiration;
    private String mCreditCardCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        mContext = this;

        mCreditCardOwnerTextView = (TextView) findViewById(R.id.credit_card_owner);
        mCreditCardNumberTextView = (TextView) findViewById(R.id.credit_card_number);
        mCreditCardExpirationTextView = (TextView) findViewById(R.id.credit_card_expiration);
        mCreditCardSecurityCodeTextView = (TextView) findViewById(R.id.credit_card_code);

        mButton = (Button) findViewById(R.id.payment_button);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean cancel = false;
                View focusView = null;

                // Checks if credit card is valid
                mCreditCardNumber = mCreditCardNumberTextView.getText().toString();
                if(!ValidationUtil.isValidCreditCard(mCreditCardNumber)){
                    mCreditCardNumberTextView.setError(getString(R.string.error_invalid_credit_card));
                    focusView = mCreditCardNumberTextView;
                    cancel = true;
                }

                mCreditCardExpiration = mCreditCardExpirationTextView.getText().toString();

                if(!ValidationUtil.isValidCreditCardExpiration(mCreditCardExpiration)) {
                    mCreditCardExpirationTextView.setError(getString(R.string.error_expiration_date));
                    focusView = mCreditCardExpirationTextView;
                    cancel = true;
                }

                if (cancel) {
                    // There was an error; don't attempt to send payment data and focus the first
                    // form field with an error.
                    focusView.requestFocus();
                } else {
                    // Sends payment data.
                    mCreditCardCode = mCreditCardSecurityCodeTextView.getText().toString();
                    mCreditCardOwner = mCreditCardOwnerTextView.getText().toString();

                    PaymentTask paymentTask = new PaymentTask();
                    paymentTask.execute((Void) null);
                }

            }
        });
    }

    private void onPayment(String result){
        if(TextUtils.isDigitsOnly(result)){
            // Shows message asking to come back later
            Toast.makeText(mContext, R.string.error_rent, Toast.LENGTH_LONG).show();

        }else{
            // Shows success message and forwards to map
            Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
            Intent intent = new Intent();
            intent.setClass(mContext, MapsActivity.class);
            mContext.startActivity(intent);
        }
    }

    public class PaymentTask extends AsyncTask<Void, Void, String>{

        @Override
        protected String doInBackground(Void... voids) {
            SharedPreferences sharedPreferences = getApplication().getSharedPreferences(SaitamaContract.SHARED_PREFERENCES, Context.MODE_PRIVATE);
            String accessToken = sharedPreferences.getString(SaitamaContract.SHARED_ACCESS_TOKEN,null);

            StringBuffer uri = new StringBuffer()
                    .append(mContext.getString(R.string.jitensha_api_base_url))
                    .append(mContext.getString(R.string.jitensha_api_rent));

            String result = JitenshaApi.connectByPost(Uri.parse(uri.toString()).buildUpon().build(),
                    JitenshaApi.buildJsonPayment(mCreditCardOwner,mCreditCardNumber,mCreditCardExpiration,mCreditCardCode),
                    accessToken);

            String message = result;
            boolean success = true;
            if (TextUtils.isDigitsOnly(result)){
                success = false;
            }

            if(success) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    message = jsonObject.getString(JitenshaParameters.RESPONSE_MESSAGE);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(),e);
                }
            }

            return message;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            onPayment(result);
        }
    }
}
