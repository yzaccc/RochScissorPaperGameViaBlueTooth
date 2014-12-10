package edu.asu.assi;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mcassi.R;

public class MCAssiActivity extends Activity {
	private EditText usernameText;
	private EditText ageText;
	private RadioButton radioMale;
	private RadioButton radioFemale;
	private Button buttonGo;
	private Button buttonReadme;
	private RadioButton radioSexButton;
	private Button buttonBlueTooth;
	private boolean single = true;
	private String username;
	private String age;
	private String sex;
	private SQLiteDatabase db;
	private final Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_activity);
		BluetoothAdapter btAdp = BluetoothAdapter.getDefaultAdapter();
		btAdp.disable();
		usernameText = (EditText) findViewById(R.id.username);
		ageText = (EditText) findViewById(R.id.age);
		radioMale = (RadioButton) findViewById(R.id.radioMale);
		radioFemale = (RadioButton) findViewById(R.id.radioFemale);
		buttonGo = (Button) findViewById(R.id.buttonGo);
		buttonBlueTooth = (Button) findViewById(R.id.buttonBlue);
		buttonReadme = (Button) findViewById(R.id.button1);
		final RadioGroup rg = (RadioGroup) findViewById(R.id.radioSex);
		// Readme button click event
		buttonReadme.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						context);
				alertDialogBuilder.setTitle("Readme");
				alertDialogBuilder
						.setMessage(
								"1. Input all the required information;\n"
										+ "2. Draw a curve for a scissor; \n"
										+ "   Draw a circle or triangle for a rock;\n"
										+ "   Draw a quadrangle for a paper;\n"
										+ "3. The software will generate a scissor, rock or a paper in equal chance;\n"
										+ "   And display the result and save the user information and result in database sdcard/MCAssi;\n"
										+ "   The table name would be 'gameResult.\n"
										+ "4. If you have any question, please contact zyang64@gmail.com'.\n"
										+ "5. Group members: Zongkun Yang, Ersi Zha, Daqi Yin.")
						.setCancelable(false)
						.setPositiveButton("Ok",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										startActivity(getIntent());
									}
								});
				AlertDialog alertDialog = alertDialogBuilder.create();

				// show it
				alertDialog.show();
			}
		});
		// Go button click event
		buttonGo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				int selectedId = rg.getCheckedRadioButtonId();
				RadioButton radioSexButton = (RadioButton) findViewById(selectedId);
				if (validate(selectedId)) {
					sex = radioSexButton.getText().toString();
					username = usernameText.getText().toString();
					age = ageText.getText().toString();
					try {
						// b) Create a database for the user that will store his
						// or her win or loss history. Store the data base in
						// the SD card
						String databaseName = "gameResult";
						db = SQLiteDatabase.openDatabase("sdcard/MCAssi", null,
								SQLiteDatabase.CREATE_IF_NECESSARY);
						db.execSQL("create table if not exists "
								+ databaseName
								+ "(username text PRIMARY KEY, age text, sex text, win INTEGER, lose INTEGER);");
						db.execSQL("insert into " + databaseName
								+ "(username,age,sex,win,lose) values('"
								+ username + "','" + age + "','" + sex
								+ "','0','0')");
						if (null != db) {
							db.close();
						}

						Intent i = new Intent();
						i.setClass(getApplicationContext(), RockPaperS.class);
						i.putExtra("username", username);
						i.putExtra("single", single);
						startActivity(i);
					} catch (SQLiteException e) {
						if (e instanceof android.database.sqlite.SQLiteConstraintException) {
							AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
									context);
							alertDialogBuilder.setTitle("Welcome back "
									+ username);
							alertDialogBuilder
									.setMessage("Enjoy the game!")
									.setCancelable(false)
									.setPositiveButton(
											"Ok",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int id) {
													Intent i = new Intent();
													i.setClass(
															getApplicationContext(),
															RockPaperSOri.class);
													i.putExtra("username",
															username);
													startActivity(i);
												}
											});
							AlertDialog alertDialog = alertDialogBuilder
									.create();

							// show it
							alertDialog.show();
						}

					}

				} else {
					Toast.makeText(getApplicationContext(),
							"Please input all the info required!", 2).show();
				}

			}
		});

		// BlueTooth button click event
		buttonBlueTooth.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				int selectedId = rg.getCheckedRadioButtonId();
				RadioButton radioSexButton = (RadioButton) findViewById(selectedId);
				if (validate(selectedId)) {
					sex = radioSexButton.getText().toString();
					username = usernameText.getText().toString();
					age = ageText.getText().toString();
					try {
						// b) Create a database for the user that will store his
						// or her win or loss history. Store the data base in
						// the SD card
						String databaseName = "gameResult";
						db = SQLiteDatabase.openDatabase("sdcard/MCAssi", null,
								SQLiteDatabase.CREATE_IF_NECESSARY);
						db.execSQL("create table if not exists "
								+ databaseName
								+ "(username text PRIMARY KEY, age text, sex text, win INTEGER, lose INTEGER);");
						db.execSQL("insert into " + databaseName
								+ "(username,age,sex,win,lose) values('"
								+ username + "','" + age + "','" + sex
								+ "','0','0')");
						if (null != db) {
							db.close();
						}

						Intent i = new Intent();
						i.setClass(getApplicationContext(), RockPaperS.class);
						i.putExtra("username", username);

						startActivity(i);
					} catch (SQLiteException e) {
						if (e instanceof android.database.sqlite.SQLiteConstraintException) {
							AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
									context);
							alertDialogBuilder.setTitle("Welcome back "
									+ username);
							alertDialogBuilder
									.setMessage("Enjoy the game!")
									.setCancelable(false)
									.setPositiveButton(
											"Ok",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int id) {
													Intent i = new Intent();
													i.setClass(
															getApplicationContext(),
															RockPaperS.class);
													i.putExtra("username",
															username);
													startActivity(i);
												}
											});
							AlertDialog alertDialog = alertDialogBuilder
									.create();

							// show it
							alertDialog.show();
						}

					}

				} else {
					Toast.makeText(getApplicationContext(),
							"Please input all the info required!", 2).show();
				}

			}
		});

	}

	public boolean validate(int selectedId) {
		if (selectedId == -1 || usernameText.getText() == null
				|| ageText.getText() == null) {
			return false;
		}
		return true;
	}

}
