package rest.bef.befrestdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

/**
 * Created by hojjatimani on 3/2/2016 AD.
 */
public class SignupActivity extends Activity{
    EditText id;
    RadioButton nazdika;
    RadioButton befrest;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        id = (EditText) findViewById(R.id.id);
        nazdika = (RadioButton) findViewById(R.id.nazdika);
        befrest = (RadioButton) findViewById(R.id.befrest);
        submit = (Button) findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!("" + id.getText().toString()).matches("[a-z_]+")) {
                    UIHelper.notifyUser(SignupActivity.this, "[a.js-z_]+     :|");
                } else if (!(nazdika.isChecked() || befrest.isChecked())) {
                    UIHelper.notifyUser(SignupActivity.this, "choose your team!");
                } else {
                    //sign up
                    String team = nazdika.isChecked() ? "nazdika" : "befrest";
                    SignupHelper.signUpAndStart(SignupActivity.this, id.getText().toString(), team);
                    startActivity(new Intent(SignupActivity.this, ActivityMain.class));
                    finish();
                }
            }
        });
    }
}
