package EW.Client;

import EW.Client.MyArrayAdapter.ViewHolder;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CompaniesAdapter extends ArrayAdapter<String> {
	
	private final Activity context;
	private final String[] names;
	private final String[] cities;

	public CompaniesAdapter(Activity context, String[] names, String[] cities) {
		super(context, R.layout.lcompany, names);
		this.context = context;
		this.names = names;
		this.cities = cities;
	}
	
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
			rowView = inflater.inflate(R.layout.lcompany, null, true);
			holder = new ViewHolder();
			holder.textViewname = (TextView) rowView.findViewById(R.id.TextViewCompanyNamel);
			holder.textViewplace = (TextView) rowView.findViewById(R.id.textViewCityl);
			rowView.setTag(holder);
		} else {
			holder = (ViewHolder) rowView.getTag();
		}

		holder.textViewname.setText(names[position]);
		holder.textViewplace.setText(cities[position]);
		
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
