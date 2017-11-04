package EW.Client;

public class Event {
	
	public String Ide, Idc, Name, Description, Place, Company, Start, End, Contador;;
	public boolean Check;

	public Event() {}
	
	public Event(String ide, String idc, String name, String description, String place, 
			String company, String start, String end, String contador) {
		this.Ide=ide;
		this.Idc=idc;
		this.Name=name;
		this.Description=description;
		this.Place=place;
		this.Company=company;
		this.Start=start;
		this.End=end;
		this.Contador=contador;
		this.Check=false;
	}

}
