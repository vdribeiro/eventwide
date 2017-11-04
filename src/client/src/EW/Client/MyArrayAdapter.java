package EW.Client;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyArrayAdapter extends ArrayAdapter<String> {
	private final Activity context;
	private final String[] names;
	private final String[] places;
	private final int[] images;

	public MyArrayAdapter(Activity context, String[] names, String[] places, int[] images) {
		super(context, R.layout.levent, names);
		this.context = context;
		this.names = names;
		this.places = places;
		this.images = images;
	}

	// static to save the reference to the outer class and to avoid access to
	// any members of the containing class
	static class ViewHolder {
		public ImageView imageView;
		public TextView textViewname;
		public TextView textViewplace;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// ViewHolder will buffer the access to the individual fields of the row
		// layout

		ViewHolder holder;
		// Recycle existing view if passed as parameter
		// This will save memory and time on Android
		// This only works if the base layout for all classes are the same
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(R.layout.levent, null, true);
			holder = new ViewHolder();
			holder.textViewname = (TextView) rowView.findViewById(R.id.TextViewNamel);
			holder.textViewplace = (TextView) rowView.findViewById(R.id.textViewPlacel);
			holder.imageView = (ImageView) rowView.findViewById(R.id.imageViewicon);
			rowView.setTag(holder);
		} else {
			holder = (ViewHolder) rowView.getTag();
		}

		holder.textViewname.setText(names[position]);
		holder.textViewplace.setText(places[position]);
		holder.imageView.setImageResource(images[position]);
		// Change the icon for Windows and iPhone
		String s = names[position];
		if (s.startsWith("Windows7") || s.startsWith("iPhone")
				|| s.startsWith("Solaris")) {
			//holder.imageView.setImageResource(R.drawable.no);
		} else {
			//holder.imageView.setImageResource(R.drawable.ok);
		}

		return rowView;
	}
}
