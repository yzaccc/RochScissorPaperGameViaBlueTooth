package edu.asu.assi;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mcassi.R;

public class RockPaperS extends ActionBarActivity implements
		SensorEventListener {
	int result = -1;
	SensorManager sensorManager = null;
	TextView oriX, oriY, oriZ;
	TextView accX, accY, accZ;
	Float azimut;
	boolean singlePlayer = false;

	private String mConnectedDeviceName = null;
	private static final int handRock = 0;
	private static final int handPaper = 1;
	private static final int handScissors = 2;

	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	public int reMessage;
	public int senMessage;
	// Message types sent from the BluetoothGameService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	BluetoothAdapter btAdp = BluetoothAdapter.getDefaultAdapter();
	private BluetoothGameService mGameService = null;

	public int reNum = 0;
	public int senNum = 0;

	int numClick = 0;
	float prevX = 0;
	float prevY = 0;
	float prev2X = 0;
	float prev2Y = 0;
	float actionX = 0;
	float actionY = 0;
	int corner = 0;
	boolean rock = false;
	boolean paper = false;
	boolean scissors = false;
	ImageView imageView;
	Bitmap bitmap;
	Canvas canvas;
	Paint paint;
	final Context context = this;
	List<Double> drawingX = new ArrayList<Double>();
	List<Double> drawingY = new ArrayList<Double>();
	EditText tv;
	String username;
	SQLiteDatabase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (btAdp == null) {
			Toast.makeText(RockPaperS.this, "No BlueTooth adapter found",
					Toast.LENGTH_LONG).show();
		}
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		setContentView(R.layout.fragment_main);

		Intent i = getIntent();
		username = i.getStringExtra("username");
		boolean single = i.getBooleanExtra("single", false);
		if (single) {
			singlePlayer = true;
		}
		tv = (EditText) findViewById(R.id.editText1);
		imageView = (ImageView) this.findViewById(R.id.imageView1);

		Display currentDisplay = getWindowManager().getDefaultDisplay();
		float dw = currentDisplay.getWidth();
		float dh = currentDisplay.getHeight();

		bitmap = Bitmap.createBitmap((int) dw, (int) dh,
				Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		paint = new Paint();
		paint.setStrokeWidth(50);
		paint.setColor(Color.BLUE);
		imageView.setImageBitmap(bitmap);

		imageView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (numClick == 0) {
					numClick++;
					bitmap.eraseColor(Color.TRANSPARENT);
					canvas.drawBitmap(bitmap, 0, 0, paint);
					imageView.invalidate();
				} else if (numClick == 1) {
					prevX = actionX;
					prevY = actionY;
					actionX = event.getX();
					actionY = event.getY();
					drawingX.add((double) actionX);
					drawingY.add((double) actionY);
					numClick++;

				} else if (numClick > 1) {
					prev2X = prevX;
					prev2Y = prevY;
					prevX = actionX;
					prevY = actionY;
					actionX = event.getX();
					actionY = event.getY();
					drawingX.add((double) actionX);
					drawingY.add((double) actionY);
					// if(Math.abs(prevX - actionX) > 2.0 ||
					// Math.abs(prevY -
					// actionY) > 2.0){

					canvas.drawLine(prevX, prevY, actionX, actionY, paint);
					imageView.invalidate();

				}
				if (event.getActionMasked() == android.view.MotionEvent.ACTION_UP) {

					prevX = 0;
					prevY = 0;
					prev2X = 0;
					prev2Y = 0;
					actionX = 0;
					actionY = 0;
					if (drawingX.size() > 10) {
						callShapeDetector();
					}

					corner = 0;
					numClick = 0;
					drawingX.clear();
					drawingY.clear();

				}

				return true;
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		if (!btAdp.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		} else {
			if (mGameService == null) {

				setupGame();
			}

		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				sensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				sensorManager.SENSOR_DELAY_FASTEST);

		if (mGameService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mGameService.getState() == BluetoothGameService.STATE_NONE) {
				// Start the Bluetooth chat services
				mGameService.start();
			}
		}
	}

	private void setupGame() {

		// Initialize the BluetoothChatService to perform bluetooth connections
		if (mGameService == null) {
			mGameService = new BluetoothGameService(this, mHandler);
		}
		if (result == 0) {
			Context context = getApplicationContext();
			CharSequence t = "";
			if (singlePlayer) {
				switch (genVal()) {
				case handPaper:
					t = "Player2 played the same hand. Replay!";
					Toast.makeText(context, t, Toast.LENGTH_SHORT).show();
					break;
				case handRock:
					t = "Player2 played Paper. Paper Beats Rock!";
					Toast.makeText(context, t, Toast.LENGTH_SHORT).show();
					updateDatabaseAndShowDialog(0,"");
					break;
				case handScissors:
					t = "Player2 played Scissors. Rock beats Scissors!";
					Toast.makeText(context, t, Toast.LENGTH_SHORT).show();
					updateDatabaseAndShowDialog(2,"");
					break;
				}
			} else {
				if (Math.abs(senNum + 1 - reNum) > 1) {
					Toast.makeText(context, "Please wait for the opponent!",
							Toast.LENGTH_SHORT).show();
				} else {
					int message = handRock;
					senMessage = message;
					senNum++;
					sendMessage(message + "");
				}
			}

		} else if (result == 1) {
			Context context = getApplicationContext();
			CharSequence t = "";
			if (singlePlayer) {

				switch (genVal()) {
				case handPaper:
					t = "Player2 played the same hand. Replay!";
					Toast.makeText(context, t, Toast.LENGTH_SHORT).show();
					break;
				case handRock:
					t = "Player2 played Rock. Paper Beats Rock!";
					Toast.makeText(context, t, Toast.LENGTH_SHORT).show();
					updateDatabaseAndShowDialog(2,"");
					break;
				case handScissors:
					t = "Player2 played Scissors. Scissors beats Paper!";
					Toast.makeText(context, t, Toast.LENGTH_SHORT).show();
					updateDatabaseAndShowDialog(0,"");
					break;
				}
			} else {
				if (Math.abs(senNum + 1 - reNum) > 1) {
					Toast.makeText(context, "Please wait for the opponent!",
							Toast.LENGTH_SHORT).show();
				} else {
					int message = handPaper;
					senMessage = message;
					senNum++;
					sendMessage(message + "");
				}
			}

		} else if (result == 2) {
			Context context = getApplicationContext();
			CharSequence t = "";
			if (singlePlayer) {
				switch (genVal()) {
				case handScissors:
					t = "Player2 played the same hand. Replay!";
					Toast.makeText(context, t, Toast.LENGTH_SHORT).show();
					break;
				case handPaper:
					t = "Player2 played Paper. Scissors beat Paper!";
					Toast.makeText(context, t, Toast.LENGTH_SHORT).show();
					updateDatabaseAndShowDialog(2,"");
					break;
				case handRock:
					t = "Player2 played Rock. Rock beats Scissors!";
					Toast.makeText(context, t, Toast.LENGTH_SHORT).show();
					updateDatabaseAndShowDialog(0,"");
					break;
				}
			} else {
				if (Math.abs(senNum + 1 - reNum) > 1) {
					Toast.makeText(context, "Please wait for the opponent!",
							Toast.LENGTH_SHORT).show();
				} else {
					int message = handScissors;
					senMessage = message;
					senNum++;
					sendMessage(message + "");
				}
			}
		}

	}

	public void updateDatabaseAndShowDialog(int win, String t) {
		updateWinHistory(win);
		final StringBuilder sb = new StringBuilder();
		int[] history = getWinHistory(username);
		sb.append('\n');
//		sb.append(" You won " + history[0] + " times and You lost "
//				+ history[1] + " times");
		sb.append(t);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);
		alertDialogBuilder.setTitle("");
		alertDialogBuilder
				.setMessage(t+"\n"+"Do you want another game?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// if this button is clicked, close
								// current activity
//								Toast.makeText(getApplicationContext(),
//										sb.toString(), Toast.LENGTH_SHORT)
//										.show();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, just close
						sendMessage(111+"");
//						Intent i = new Intent();
//						i.setClass(getApplicationContext(), MCAssiActivity.class);
//						startActivity(i);
						//btAdp.disable();
					}
				});
		AlertDialog alertDialog = alertDialogBuilder.create();
		// show it
		alertDialog.show();
		

	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int winOrLose = 1;
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothGameService.STATE_CONNECTED:
					break;
				case BluetoothGameService.STATE_CONNECTING:
					break;
				case BluetoothGameService.STATE_LISTEN:
					break;
				case BluetoothGameService.STATE_NONE:
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				// Toast.makeText(getApplicationContext(), "You played "
				// + writeMessage, Toast.LENGTH_SHORT).show();
				// senNum++;
				
				senMessage = Integer.parseInt(writeMessage);

				if(senMessage == 111){
					btAdp.disable();
					
					Intent i = new Intent();
					i.setClass(getApplicationContext(), MCAssiActivity.class);
					startActivity(i);
				}
				
				String senMessageString = "";
				if (senMessage == 0) {
					senMessageString = "Rock";
				} else if (reMessage == 1) {
					senMessageString = "Paper";
				} else if (reMessage == 2) {
					senMessageString = "Scissors";
				}

				winOrLose = 1; // 0 lose; 1 fair; 2 win
				if (senNum > reNum) {
					Toast.makeText(getApplicationContext(),
							"Please wait for response.", Toast.LENGTH_SHORT)
							.show();
				} else if (reNum == senNum) {
					String t = "";
					if (reMessage == 2 && senMessage == 0) {
						winOrLose = 2;
					} else if (senMessage == 2 && reMessage == 0) {
						winOrLose = 0;
					} else if (reMessage == senMessage) {
						winOrLose = 1;
					} else if (reMessage > senMessage) {
						winOrLose = 0;
					} else {
						winOrLose = 2;
					}
					if (winOrLose == 0) {
//						Toast.makeText(
//								getApplicationContext(),
//								"You Lose. Your Opponent played "
//										+ senMessageString, Toast.LENGTH_SHORT)
//								.show();
						t = "You Lose. Your Opponent played " + senMessageString;
					} else if (winOrLose == 1) {
//						Toast.makeText(
//								getApplicationContext(),
//								"Fair. Your Opponent played "
//										+ senMessageString, Toast.LENGTH_SHORT)
//								.show();
						t = "Fair. Your Opponent played "
								+ senMessageString;
					} else if (winOrLose == 2) {
//						Toast.makeText(
//								getApplicationContext(),
//								"You Win! Your Opponent played "
//										+ senMessageString, Toast.LENGTH_SHORT)
//								.show();
						t = "You Win! Your Opponent played "
								+ senMessageString;
					}
					// if there are results,update DB
					updateDatabaseAndShowDialog(winOrLose,t);
				} else {
					Toast.makeText(getApplicationContext(),
							"Please send again.", Toast.LENGTH_SHORT).show();
				}

				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				reNum++;
				reMessage = Integer.parseInt(readMessage);

				if(reMessage == 111){
					new AlertDialog.Builder(RockPaperS.this).setMessage("Your opponent do not want to continue and has quit. Quit?")
					.setNeutralButton("Ok",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							btAdp.disable();
							
							Intent i = new Intent();
							i.setClass(getApplicationContext(), MCAssiActivity.class);
							startActivity(i);
						}
					})
					.show();
				}
				
				String reMessageString = "";
				if (reMessage == 0) {
					reMessageString = "Rock";
				} else if (reMessage == 1) {
					reMessageString = "Paper";
				} else if (reMessage == 2) {
					reMessageString = "Scissors";
				}

				winOrLose = 1; // 0 lose; 1 fair; 2 win
				if (reNum > senNum) {
					Toast.makeText(getApplicationContext(),
							"Your opponent has post choice.",
							Toast.LENGTH_SHORT).show();
				} else if (reNum == senNum) {
					String t = "";
					if (reMessage == 2 && senMessage == 0) {
						winOrLose = 2;
					} else if (senMessage == 2 && reMessage == 0) {
						winOrLose = 0;
					} else if (reMessage == senMessage) {
						winOrLose = 1;
					} else if (reMessage > senMessage) {
						winOrLose = 0;
					} else {
						winOrLose = 2;
					}
					if (winOrLose == 0) {
//						Toast.makeText(
//								getApplicationContext(),
//								"You Lose. Your Opponent played "
//										+ reMessageString, Toast.LENGTH_SHORT)
//								.show();
						t = "You Lose. Your Opponent played "
								+ reMessageString;
					} else if (winOrLose == 1) {
//						Toast.makeText(
//								getApplicationContext(),
//								"Fair. Your Opponent played " + reMessageString,
//								Toast.LENGTH_SHORT).show();
						t = "Fair. Your Opponent played " + reMessageString;
					} else if (winOrLose == 2) {
//						Toast.makeText(
//								getApplicationContext(),
//								"You Win! Your Opponent played "
//										+ reMessageString, Toast.LENGTH_SHORT)
//								.show();
						t = "You Win! Your Oponent played "
								+ reMessageString;
					}
					updateDatabaseAndShowDialog(winOrLose,t);
				} else {
					Toast.makeText(getApplicationContext(),
							"Please wait for response.", Toast.LENGTH_SHORT)
							.show();
				}
				// Toast.makeText(getApplicationContext(), "Opponent played "
				// + readMessage, Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}

	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			if (resultCode == Activity.RESULT_OK) {
				// When DeviceListActivity returns with a device to connect
				String address = data.getExtras().getString("deviceaddr");
				// Get the BLuetoothDevice object
				BluetoothDevice device = btAdp.getRemoteDevice(address);

				if (mGameService == null)
					setupGame();

				mGameService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			if (btAdp.isEnabled()) {
				Toast.makeText(RockPaperS.this, "Status: Enabled",
						Toast.LENGTH_LONG).show();

				Intent btActStart = new Intent(RockPaperS.this,
						BtMainActivity.class);
				startActivityForResult(btActStart, REQUEST_CONNECT_DEVICE);
			} else {
				Toast.makeText(RockPaperS.this, "Status: Disabled",
						Toast.LENGTH_LONG).show();
			}
			break;
		}
	}

	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mGameService.getState() != BluetoothGameService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			mGameService.write(send);
		}
	}

	private int genVal() {
		Random r = new Random();
		int k = r.nextInt(3);

		return k;
	}

	public void callShapeDetector() {
		// c) Select a method of acquisition of Rock, paper, scissors input
		int index = 0;
		int incr = (int) (Math.floor(drawingX.size() / 4) * 0.7);
		while (index + 10 < drawingX.size()) {
			double prevX = drawingX.get(index);
			double actionX = drawingX.get(index + 10);
			double prevY = drawingY.get(index);
			double actionY = drawingY.get(index + 10);
			double prev2X = drawingX.get(index);
			double action2X = drawingX.get(index + 10);
			double prev2Y = drawingY.get(index);
			double action2Y = drawingY.get(index + 10);
			if (index + incr + 10 < drawingX.size()) {
				prev2X = drawingX.get(index + incr);
				action2X = drawingX.get(index + incr + 10);
				prev2Y = drawingY.get(index + incr);
				action2Y = drawingY.get(index + incr + 10);
			} else if (index + incr + 1 < drawingX.size()) {
				prev2X = drawingX.get(index + incr);
				action2X = drawingX.get(index + incr + 1);
				prev2Y = drawingY.get(index + incr);
				action2Y = drawingY.get(index + incr + 1);
			} else {
				prev2X = 0;
				action2X = 0;
				prev2Y = 0;
				action2Y = 0;
			}

			double ij = (actionX - prevX) * (action2X - prev2X)
					+ (actionY - prevY) * (action2Y - prev2Y);
			double mag1 = Math.sqrt(Math.pow((actionX - prevX), 2)
					+ Math.pow((actionY - prevY), 2));
			double mag2 = Math.sqrt(Math.pow((action2X - prev2X), 2)
					+ Math.pow((action2Y - prev2Y), 2));
			double angle = Math.acos(ij / (mag1 * mag2));
			tv.setText(Double.toString(angle));
			if (180 * angle / 3.14 < 105.1 && 180 * angle / 3.14 > 75.1) {
				corner++;
				// Toast.makeText(MainActivity.this, Integer.toString(corner),
				// Toast.LENGTH_LONG).show();
			}
			index += incr;
		}

		double prevX = drawingX.get(0);
		double actionX = drawingX.get(10);
		double prevY = drawingY.get(0);
		double actionY = drawingY.get(10);
		double prev2X = drawingX.get(drawingX.size() - 10);
		double action2X = drawingX.get(drawingX.size() - 1);
		double prev2Y = drawingY.get(drawingX.size() - 10);
		double action2Y = drawingY.get(drawingX.size() - 1);
		double ij = (actionX - prevX) * (action2X - prev2X) + (actionY - prevY)
				* (action2Y - prev2Y);
		double mag1 = Math.sqrt(Math.pow((actionX - prevX), 2)
				+ Math.pow((actionY - prevY), 2));
		double mag2 = Math.sqrt(Math.pow((action2X - prev2X), 2)
				+ Math.pow((action2Y - prev2Y), 2));
		double angle = Math.acos(ij / (mag1 * mag2));
		tv.setText(Double.toString(180 * angle / 3.14));
		if (180 * angle / 3.14 < 105.1 && 180 * angle / 3.14 > 75.1) {
			corner++;
			// Toast.makeText(MainActivity.this, Integer.toString(corner),
			// Toast.LENGTH_LONG).show();
		}
		if (corner == 1 || corner == 0) {
			tv.setText("Scissors");
			result = 2;
		} else if (corner == 2) {
			tv.setText("Rock");
			result = 0;
		} else {
			tv.setText("Paper");
			result = 1;
		}
		setupGame();

	}

	public int[] getWinHistory(String username) {
		int[] history = new int[2];
		String databaseName = "gameResult";
		int win = 0, lose = 0;
		Cursor cr;
		try {
			db = SQLiteDatabase.openDatabase("sdcard/MCAssi", null,
					SQLiteDatabase.CREATE_IF_NECESSARY);
			cr = db.rawQuery("SELECT win, lose FROM " + databaseName
					+ " WHERE username = '" + username + "'", null);

			if (cr != null && cr.moveToFirst()) {
				win = Integer.parseInt(cr.getString(cr.getColumnIndex("win")));
				lose = Integer
						.parseInt(cr.getString(cr.getColumnIndex("lose")));
				cr.close();
			}
			if (null != db) {
				db.close();
			}
		} catch (SQLiteException e) {
			Toast.makeText(getApplicationContext(), e.getMessage(), 2).show();
		}
		history[0] = win;
		history[1] = lose;
		return history;
	}

	public void updateWinHistory(int isWin) {
		if (isWin == 1) {
			return;
		}
		try {
			String databaseName = "gameResult";
			db = SQLiteDatabase.openDatabase("sdcard/MCAssi", null,
					SQLiteDatabase.CREATE_IF_NECESSARY);
			if (isWin == 2) {
				String sql = "UPDATE " + databaseName
						+ " SET win = win+1 where username=" + "'" + username
						+ "'";
				db.execSQL(sql);

			} else {
				String sql = "UPDATE " + databaseName
						+ " SET lose = lose+1 where username=" + "'" + username
						+ "'";
				db.execSQL(sql);

			}
			if (null != db) {
				db.close();
			}
		} catch (SQLiteException e) {
			Toast.makeText(getApplicationContext(), e.getMessage(), 2).show();
		}
	}

	float[] mGravity;
	float[] mGeomagnetic;

	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			mGravity = event.values;
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			mGeomagnetic = event.values;
			break;
		default:
			break;
		}
		if (mGravity != null && mGeomagnetic != null) {
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
					mGeomagnetic);
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				azimut = orientation[0]; // orientation contains: azimut, pitch
											// and roll
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

}
