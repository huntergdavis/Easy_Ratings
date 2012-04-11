package com.hunterdavis.easyratings;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class EasyRatings extends Activity {

	// globals
	int SELECT_PICTURE = 22;
	int SELECT_FIRST = 33;
	InventorySQLHelper ratingData = new InventorySQLHelper(this);
	ArrayAdapter<String> m_adapterForCategorySpinner;
	String currentCategory = "";
	private Gallery gallery;
	int currentPosition = 0;
	int mutex = 0;
	String tempName = "";

	public class UriD {
		Uri uri;
		int id;

		UriD(Uri URI, int ID) {
			uri = URI;
			id = ID;
		}
	}

	public ArrayList<UriD> Imgid = new ArrayList<UriD>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		currentPosition = 0;
		
		// gallery listener
		gallery = (Gallery) findViewById(R.id.photoInventory);
		gallery.setAdapter(new AddImgAdp(this));

		gallery.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {

				currentPosition = position;
				selectItemByRowId(Imgid.get(position).id);
			}

		});

		// delete button listener
		OnClickListener DeleteButtonListner = new OnClickListener() {
			public void onClick(View v) {
				yesnoDeleteHandler("Are you sure?",
						"Are you sure you want to delete the category?");
			}
		};
		Button deleteButton = (Button) findViewById(R.id.delete);
		deleteButton.setOnClickListener(DeleteButtonListner);

		// delete button listener
		OnClickListener DeleteItemButtonListner = new OnClickListener() {
			public void onClick(View v) {
				yesnoItemDeleteHandler("Are you sure?",
						"Are you sure you want to delete the Item?");
			}
		};
		Button deleteItemButton = (Button) findViewById(R.id.deleteItembutton);
		deleteItemButton.setOnClickListener(DeleteItemButtonListner);

		// new button listener
		// new button listener
		OnClickListener newButtonListner = new OnClickListener() {
			public void onClick(View v) {
				newCategory(v.getContext());
			}
		};
		Button newPButton = (Button) findViewById(R.id.newrow);
		newPButton.setOnClickListener(newButtonListner);

		// Create an anonymous implementation of OnClickListener
		OnClickListener plusButtonListner = new OnClickListener() {
			public void onClick(View v) {
				// in onCreate or any event where your want the user to
				// select a file
				newItem(v.getContext());
			}
		};

		Button plusButton = (Button) findViewById(R.id.plusbutton);
		plusButton.setOnClickListener(plusButtonListner);

		// Create an anonymous implementation of OnClickListener
		OnClickListener photoButtonListner = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked
				if (Imgid.size() > 0) {
					// in onCreate or any event where your want the user to
					// select a file
					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(
							Intent.createChooser(intent, "Select Photo"),
							SELECT_PICTURE);
				}
			}
		};

		ImageView imageButton = (ImageView) findViewById(R.id.ImageView01);
		imageButton.setOnClickListener(photoButtonListner);
		genColor(imageButton, Color.WHITE);

		// set an adapter for our spinner
		m_adapterForCategorySpinner = new ArrayAdapter<String>(
				getBaseContext(), android.R.layout.simple_spinner_item);
		m_adapterForCategorySpinner
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner spinner = (Spinner) findViewById(R.id.categories);
		spinner.setAdapter(m_adapterForCategorySpinner);

		spinner.setOnItemSelectedListener(new MyUnitsOnItemSelectedListener());

		// fill up our spinner item
		Cursor cursor = getCategoriesCursor();
		if (cursor.getCount() > 0) {
			deleteButton.setEnabled(true);
			while (cursor.moveToNext()) {
				String singlecardName = cursor.getString(1);
				m_adapterForCategorySpinner.add(singlecardName);
			}
			plusButton.setEnabled(true);
		} else {
			spinner.setEnabled(false);
		}

		// set our edittext onchange listner
		EditText contentText = (EditText) findViewById(R.id.extracontent);
		contentText.setText("");
		contentText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {

				if (mutex == 1) {
					mutex = 0;
				} else {
					if(Imgid.size() == 0)
						return;
					// here we call the text changed update sql function
					updateSqlValues(Imgid.get(currentPosition).id, "extrainfo",
							s.toString());
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		// set our ratingbar onchagne listenesr
		RatingBar ourBar = (RatingBar) findViewById(R.id.ratingBar1);
		ourBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating,
					boolean fromUser) {
				if (mutex == 1) {
					mutex = 0;
				} else {
					//
					if(Imgid.size() == 0)
					return;
					updateSqlValues(Imgid.get(currentPosition).id, "rating",
							String.valueOf(rating));
				}
			}

		});

		// set our edittext onchange listner
		EditText itemNameText = (EditText) findViewById(R.id.ItemName);
		itemNameText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if (mutex == 1) {
					mutex = 0;
				} else {

					if(s.toString().trim().equals(tempName)) {
						return;
					}
					// here we call the text changed update sql function
					if(Imgid.size() == 0)
						return;
					updateSqlValues(Imgid.get(currentPosition).id, "name", s
							.toString().trim());
				}

			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				tempName = s.toString().trim();
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		// Look up the AdView as a resource and load a request.
		AdView adView = (AdView) this.findViewById(R.id.adView);
		adView.loadAd(new AdRequest());

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// if (requestCode == MORE_INFO) {
		// RefreshMainGallery();
		// }
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {
				Uri selectedImageUri = data.getData();
				ImageView imageButton = (ImageView) findViewById(R.id.ImageView01);

				imageHelper.scaleURIAndDisplay(getBaseContext(),
						selectedImageUri, imageButton);
				updateSqlValues(Imgid.get(currentPosition).id, "imageuri",
						selectedImageUri.toString());
				UriD resetD = new UriD(selectedImageUri,
						Imgid.get(currentPosition).id);
				Imgid.set(currentPosition, resetD);
				// grab an instance of gallery
				gallery = (Gallery) findViewById(R.id.photoInventory);

				// this refreshes the gallery view
				((BaseAdapter) gallery.getAdapter()).notifyDataSetChanged();
			} else if (requestCode == SELECT_FIRST) {
				Uri selectedImageUri = data.getData();

			}

		}
	}

	// set up the listener class for spinner
	class MyUnitsOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			// Resources res = getResources();
			// updateSqlValues(rowId, "units", unitsarray[pos]);
			// set both global uri settings from the selected item using a sql
			// cursor
			Spinner spinner = (Spinner) findViewById(R.id.categories);
			String spinnerText = spinner.getSelectedItem().toString();
			currentCategory = spinnerText;
			loadCategoryViewByName(spinnerText);
		}

		public void onNothingSelected(AdapterView<?> parent) {

		}
	}

	public void enableBottomItems() {
		Button plusButton = (Button) findViewById(R.id.plusbutton);
		plusButton.setEnabled(true);

		Button deleteItemButton = (Button) findViewById(R.id.deleteItembutton);
		deleteItemButton.setEnabled(true);

		RatingBar ourRating = (RatingBar) findViewById(R.id.ratingBar1);
		ourRating.setEnabled(true);

		EditText ournameEdit = (EditText) findViewById(R.id.ItemName);
		ournameEdit.setEnabled(true);

		EditText ourEdit = (EditText) findViewById(R.id.extracontent);
		ourEdit.setEnabled(true);

	}

	public void disableBottomItems() {
		Button plusButton = (Button) findViewById(R.id.plusbutton);
		plusButton.setEnabled(false);

		Button deleteItemButton = (Button) findViewById(R.id.deleteItembutton);
		deleteItemButton.setEnabled(false);

		RatingBar ourRating = (RatingBar) findViewById(R.id.ratingBar1);
		ourRating.setEnabled(false);

		EditText ourEdit = (EditText) findViewById(R.id.extracontent);
		ourEdit.setEnabled(false);
		mutex = 1;
		ourEdit.setText("");

		EditText ournameEdit = (EditText) findViewById(R.id.ItemName);
		mutex = 1;
		ournameEdit.setText("");
		ournameEdit.setEnabled(false);

		ImageView imageButton = (ImageView) findViewById(R.id.ImageView01);
		genColor(imageButton, Color.WHITE);

	}

	public void selectItemByRowId(int RowId) {

		Cursor rowCursor = getRatingsCursorByRowNumber(RowId);
		if (rowCursor.moveToFirst()) {
			enableBottomItems();
			float rating = rowCursor.getFloat(1);
			RatingBar ourRating = (RatingBar) findViewById(R.id.ratingBar1);
			ourRating.setRating(rating);

			String extraText = rowCursor.getString(4);
			EditText ourEdit = (EditText) findViewById(R.id.extracontent);
			ourEdit.setText(extraText);

			String name = rowCursor.getString(5);
			EditText itemName = (EditText) findViewById(R.id.ItemName);
			mutex = 1;
			itemName.setText(name);

			ImageView imgView = (ImageView) findViewById(R.id.ImageView01);
			String ourUriString = rowCursor.getString(3);
			if (ourUriString.length() > 1) {
				Uri ourUri = Uri.parse(ourUriString);
				imageHelper.scaleURIAndDisplay(getBaseContext(), ourUri,
						imgView);
			} else {
				genColor(imgView, Color.WHITE);
			}

		}
	}

	// newItem is called with a URI selected and already displayed
	public void newItem(Context context) {
		// get a name from the user

		AlertDialog.Builder alert = new AlertDialog.Builder(context);

		alert.setTitle("Item Name?");
		alert.setMessage("Please Enter A Name For The Item");

		// Set an EditText view to get user input
		final EditText input = new EditText(context);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String tempName = input.getText().toString().trim();

				Cursor tempCursor = getRatingsCursorByName(tempName);
				if (tempCursor.getCount() > 0) {
					Toast.makeText(getBaseContext(),
							"That Name is Already In Use!", Toast.LENGTH_LONG)
							.show();
				} else {
					// Do something with value!
					if (tempName.length() > 1) {
						// select a file
						Boolean didweAdd = addItemByName(tempName);
						if (didweAdd) {

							Toast.makeText(getBaseContext(), "Saved",
									Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(getBaseContext(), "Invalid Name!",
								Toast.LENGTH_LONG).show();
					}
				}
			}

		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();

		// enable all the bottom items
		enableBottomItems();

	}

	public Boolean addItemByName(String itemName) {
		// tempUri is someting we'll need to use as well
		// at this point we have a validated name
		// and a tempuri object to work with

		SQLiteDatabase db = ratingData.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(InventorySQLHelper.RATING, 2.5);
		values.put(InventorySQLHelper.CATEGORY, currentCategory);
		values.put(InventorySQLHelper.IMAGEURI, "");
		values.put(InventorySQLHelper.EXTRAINFO, "");
		values.put(InventorySQLHelper.NAME, itemName);
		long latestRowId = db.insert(InventorySQLHelper.TABLE, null, values);
		db.close();

		// now display the stuff
		Uri myuri = Uri.parse("");
		UriD myLocalUrid = new UriD(myuri, (int) latestRowId);
		Imgid.add(myLocalUrid);
		// grab an instance of gallery
		gallery = (Gallery) findViewById(R.id.photoInventory);

		// this refreshes the gallery view
		((BaseAdapter) gallery.getAdapter()).notifyDataSetChanged();
		int imgLength = Imgid.size();
		gallery.setSelection(imgLength);
		selectItemByRowId((int) latestRowId);
		currentPosition = imgLength - 1;

		/*
		 * RatingBar ourRating = (RatingBar) findViewById(R.id.ratingBar1);
		 * mutex = 1; ourRating.setRating((float) 2.5);
		 * 
		 * EditText ourEdit = (EditText) findViewById(R.id.extracontent); mutex
		 * = 1; ourEdit.setText("");
		 * 
		 * EditText itemtextviewname = (EditText) findViewById(R.id.ItemName);
		 * mutex = 1; itemtextviewname.setText(itemName);
		 * 
		 * ImageView imgView = (ImageView) findViewById(R.id.ImageView01);
		 * genColor(imgView, Color.WHITE);
		 * 
		 * 
		 * // at this point they've all been loaded in..
		 * 
		 * 
		 * // now set the selected to be the one just created mutex = 1;
		 */
		return true;

	}

	// this is essentially the oncreate but for the items in a category
	public void loadCategoryViewByName(String catName) {
		// first clear the imgid cache
		Imgid.clear();

		Cursor ratingsCursor = getRatingsCursorByCategoryName(catName);
		if (ratingsCursor.getCount() > 0) {
			while (ratingsCursor.moveToNext()) {

				String URI = ratingsCursor.getString(3);
				Uri tempuri = Uri.parse(URI);
				int ID = ratingsCursor.getInt(0);
				// alertbox("here is the URI parsed",URI);
				UriD localUriD = new UriD(tempuri, ID);
				Imgid.add(localUriD);
			}
		}

		// at this point they've all been loaded in..
		// grab an instance of gallery
		gallery = (Gallery) findViewById(R.id.photoInventory);

		// this refreshes the gallery view
		((BaseAdapter) gallery.getAdapter()).notifyDataSetChanged();
		gallery.setSelection(0);

		int imgSize = Imgid.size();
		ImageView imgView = (ImageView) findViewById(R.id.ImageView01);
		currentPosition = 0;

		if (imgSize > 0) {
			selectItemByRowId(Imgid.get(0).id);
		} else {
			// disable all the bottom
			disableBottomItems();
			// enable the plus button
			Button plusButton = (Button) findViewById(R.id.plusbutton);
			plusButton.setEnabled(true);
		}

	}

	public void newCategory(Context context) {
		AlertDialog.Builder alert = new AlertDialog.Builder(context);

		alert.setTitle("Category Name?");
		alert.setMessage("Please Enter A Name For The Category");

		// Set an EditText view to get user input
		final EditText input = new EditText(context);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String tempName = input.getText().toString().trim();

				Cursor tempCursor = getCategoriesCursorByName(tempName);
				if (tempCursor.getCount() > 0) {
					Toast.makeText(getBaseContext(),
							"That Name is Already In Use!", Toast.LENGTH_LONG)
							.show();
				} else {
					// Do something with value!
					if (tempName.length() > 1) {
						// select a file
						Boolean didweAdd = addCategoryByName(tempName);
						if (didweAdd) {
							Toast.makeText(getBaseContext(), "Saved",
									Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(getBaseContext(), "Invalid Name!",
								Toast.LENGTH_LONG).show();
					}
				}
			}

		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();
	}

	private Boolean addCategoryByName(String categoryName) {
		SQLiteDatabase db = ratingData.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(InventorySQLHelper.CATNAME, categoryName);
		long latestRowId = db.insert(InventorySQLHelper.CATTABLE, null, values);
		db.close();
		m_adapterForCategorySpinner.add(categoryName);
		Button deleteButton = (Button) findViewById(R.id.delete);
		deleteButton.setEnabled(true);
		Spinner spinner = (Spinner) findViewById(R.id.categories);
		spinner.setEnabled(true);
		return true;
	}

	private Cursor getRatingsCursorByCategoryName(String categoryName) {
		SQLiteDatabase db = ratingData.getReadableDatabase();
		Cursor cursor = db.query(InventorySQLHelper.TABLE, null, "category = '"
				+ categoryName + "'", null, null, null, null);
		startManagingCursor(cursor);
		return cursor;
	}

	private Cursor getRatingsCursor() {
		SQLiteDatabase db = ratingData.getReadableDatabase();
		Cursor cursor = db.query(InventorySQLHelper.TABLE, null, null, null,
				null, null, null);
		startManagingCursor(cursor);
		return cursor;
	}

	private Cursor getRatingsCursorByName(String rowId) {
		SQLiteDatabase db = ratingData.getReadableDatabase();
		Cursor cursor = db.query(InventorySQLHelper.TABLE, null, "name = '"
				+ rowId + "'", null, null, null, null);
		startManagingCursor(cursor);
		return cursor;
	}

	private Cursor getRatingsCursorByRowNumber(int rowId) {
		SQLiteDatabase db = ratingData.getReadableDatabase();
		Cursor cursor = db.query(InventorySQLHelper.TABLE, null, "_id = '"
				+ rowId + "'", null, null, null, null);
		startManagingCursor(cursor);
		return cursor;
	}

	private Cursor getCategoriesCursorByName(String rowId) {
		SQLiteDatabase db = ratingData.getReadableDatabase();
		Cursor cursor = db.query(InventorySQLHelper.CATTABLE, null, "name = '"
				+ rowId + "'", null, null, null, null);
		startManagingCursor(cursor);
		return cursor;
	}

	private Cursor getCategoriesCursor() {
		SQLiteDatabase db = ratingData.getReadableDatabase();
		Cursor cursor = db.query(InventorySQLHelper.CATTABLE, null, null, null,
				null, null, null);
		startManagingCursor(cursor);
		return cursor;
	}

	public void DeleteRatingByName(String card) {
		SQLiteDatabase db = ratingData.getWritableDatabase();
		db.delete(InventorySQLHelper.TABLE, "name = '" + card + "'", null);
		db.close();
	}

	public void DeleteRatingByRowID(int rowID) {
		SQLiteDatabase db = ratingData.getWritableDatabase();
		db.delete(InventorySQLHelper.TABLE, "_id = '" + rowID + "'", null);
		db.close();
	}

	private void updateSqlValues(int id, String columnName, String value) {
		SQLiteDatabase db = ratingData.getWritableDatabase();
		ContentValues args = new ContentValues();
		args.put(columnName, value);
		String strFilter = " _id=" + id;
		db.update(InventorySQLHelper.TABLE, args, strFilter, null);
	}

	public void DeleteCategoryByName(String card) {
		SQLiteDatabase db = ratingData.getWritableDatabase();
		db.delete(InventorySQLHelper.CATTABLE, "name = '" + card + "'", null);
		db.close();
		m_adapterForCategorySpinner.remove(card);
		Cursor cursor = getCategoriesCursor();
		Boolean cursorFound = false;
		if (cursor.getCount() < 1) {
			Button delButton = (Button) findViewById(R.id.delete);
			delButton.setEnabled(false);
		}
	}

	protected void yesnoItemDeleteHandler(String title, String mymessage) {
		new AlertDialog.Builder(this)
				.setMessage(mymessage)
				.setTitle(title)
				.setCancelable(true)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// store the row id
								Integer rowId = Imgid.get(currentPosition).id;

								// remove image from main img list
								Imgid.remove(currentPosition);

								// remove sql table entry for item
								DeleteRatingByRowID(rowId);

								// refresh gallery view
								loadCategoryViewByName(currentCategory);
							}
						})
				.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}

	protected void yesnoDeleteHandler(String title, String mymessage) {
		new AlertDialog.Builder(this)
				.setMessage(mymessage)
				.setTitle(title)
				.setCancelable(true)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								Spinner spinner = (Spinner) findViewById(R.id.categories);
								String spinnerText = spinner.getSelectedItem()
										.toString();
								currentCategory = spinnerText;
								DeleteCategoryByName(currentCategory);
							}
						})
				.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}

	public Boolean genColor(ImageView imgview, int Color) {

		// create a width*height long int array and populate it with random 1 or
		// 0
		// final Random myRandom = new Random();
		int rgbSize = 200 * 200;
		int[] rgbValues = new int[rgbSize];
		for (int i = 0; i < rgbSize; i++) {
			rgbValues[i] = Color;
		}

		// create a width*height bitmap
		BitmapFactory.Options staticOptions = new BitmapFactory.Options();
		staticOptions.inSampleSize = 2;
		Bitmap staticBitmap = Bitmap.createBitmap(rgbValues, 200, 200,
				Bitmap.Config.RGB_565);

		// set the imageview to the static
		imgview.setImageBitmap(staticBitmap);

		return true;

	}

	public class AddImgAdp extends BaseAdapter {
		int GalItemBg;
		private Context cont;

		public AddImgAdp(Context c) {
			cont = c;
			TypedArray typArray = obtainStyledAttributes(R.styleable.GalleryTheme);
			GalItemBg = typArray.getResourceId(
					R.styleable.GalleryTheme_android_galleryItemBackground, 0);
			typArray.recycle();
		}

		public int getCount() {
			return Imgid.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imgView = new ImageView(cont);

			imageHelper.scaleURIAndDisplay(getBaseContext(),
					Imgid.get(position).uri, imgView);
			imgView.setLayoutParams(new Gallery.LayoutParams(200, 200));
			imgView.setScaleType(ImageView.ScaleType.FIT_XY);
			imgView.setBackgroundResource(GalItemBg);

			return imgView;
		}
	}

}