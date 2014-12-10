package edu.asu.assi;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
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
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mcassi.R;

public class RockPaperSOri extends ActionBarActivity {
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
		setContentView(R.layout.fragment_main);

		Intent i = getIntent();
		username = i.getStringExtra("username");

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
					// if(Math.abs(prevX - actionX) > 2.0 || Math.abs(prevY -
					// actionY) > 2.0){

					canvas.drawLine(prevX, prevY, actionX, actionY, paint);
					imageView.invalidate();
					// }
					// numClick = 0;
					// prevX = 0;
					// prevY = 0;
					// prev2X = 0;
					// prev2Y = 0;
					// actionX = 0;
					// actionY = 0;
				}
				if (event.getActionMasked() == android.view.MotionEvent.ACTION_UP) {
					// Toast.makeText(MainActivity.this, "Not Pressed",
					// Toast.LENGTH_LONG).show();
					// Toast.makeText(MainActivity.this,
					// Integer.toString(drawingX.size()),
					// Toast.LENGTH_LONG).show();

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
			scissors = true;
		} else if (corner == 2) {
			tv.setText("Rock");
			rock = true;
		} else {
			tv.setText("Paper");
			paper = true;
		}
		// d) Design a game engine that selects rock, paper,
		// or scissors according to some logic
		int isWin = 0;
		int randCmp = (int) (Math.random() * 3 + 1);
		StringBuilder sb = new StringBuilder();

		switch (randCmp) {
		case 1:
			sb.append("Computer is Rock, ");
			if (paper == true) {
				sb.append("Congratulation! You Win!");
				isWin = 1;
				break;
			} else if (scissors == true) {
				sb.append("Sorry! You Lose!");
				isWin = 2;
				break;
			} else if (rock == true) {
				sb.append("Draw!");
				break;
			}

		case 2:
			sb.append("Computer is Scissor, ");
			if (rock == true) {
				sb.append("Congratulation! You Win!");
				isWin = 1;
				break;
			} else if (paper == true) {
				sb.append("Sorry! You Lose!");
				isWin = 2;
				break;
			} else if (scissors == true) {
				sb.append("Draw!");
				break;
			}
		case 3:
			sb.append("Computer is Paper, ");
			if (scissors == true) {
				sb.append("Congratulation! You Win!");
				isWin = 1;
				break;
			} else if (rock == true) {
				sb.append("Sorry! You Lose!");
				isWin = 2;
				break;
			} else if (paper == true) {
				sb.append("Draw!");
				break;
			}
		}
		// update the database
		updateWinHistory(isWin);
		int[] history = getWinHistory(username);
		sb.append('\n');
		sb.append(" You won " + history[0] + " times and You lost "
				+ history[1] + " times");
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);
		alertDialogBuilder.setTitle("Want another game?!");
		alertDialogBuilder
				.setMessage(sb.toString())
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// if this button is clicked, close
								// current activity
								Intent intent = getIntent();
							    finish();
							    startActivity(intent);
								//RockPaperS.this.startActivity(getIntent());
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						Intent i = new Intent();
						i.setClass(getApplicationContext(),
								MCAssiActivity.class);
						startActivity(i);
					}
				});
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
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
		if (isWin == 0) {
			return;
		}
		try {
			String databaseName = "gameResult";
			db = SQLiteDatabase.openDatabase("sdcard/MCAssi", null,
					SQLiteDatabase.CREATE_IF_NECESSARY);
			if (isWin == 1) {
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

}
