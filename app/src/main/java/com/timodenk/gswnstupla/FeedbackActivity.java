package com.timodenk.gswnstupla;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class FeedbackActivity extends AppCompatActivity {
    private static final int SEND_MAIL_REQUEST_CODE = 1;

    private static final String FEEDBACK_RECIPENT_EMAIL_ADDRESS = "android@timodenk.com";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
    }

    public void submitButtonClicked(View v) {
        String name = ((EditText) findViewById(R.id.etFeedbackName)).getText().toString(),
                message = ((EditText) findViewById(R.id.etFeedbackMessage)).getText().toString();

        // validate form
        if (name.equals("")) {
            Toast.makeText(getApplicationContext(), R.string.enter_your_name, Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.equals("")) {
            Toast.makeText(getApplicationContext(), R.string.enter_a_message, Toast.LENGTH_SHORT).show();
            return;
        }


        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[] { FEEDBACK_RECIPENT_EMAIL_ADDRESS });
        i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.feedback_from) + " " + name);
        i.putExtra(Intent.EXTRA_TEXT, message);

        try {
            startActivityForResult(Intent.createChooser(i, getResources().getString(R.string.send_feedback_using)), SEND_MAIL_REQUEST_CODE);
        }
        catch (android.content.ActivityNotFoundException ex) {
            // could not find any client to send the message
            Toast.makeText(getApplicationContext(), R.string.could_not_send_feedback, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SEND_MAIL_REQUEST_CODE) {
            // wait until the user returns from sending
            finish();
        }
    }
}
