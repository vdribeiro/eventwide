package ewadmin;

import javax.swing.JDialog;

/**
 * @author Nuno Marques
 *
 */
public class EventWideAdmin {

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {

		// login
		try {
			Login dialogLogin = new Login();
			dialogLogin.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialogLogin.setLocationRelativeTo(null);
			dialogLogin.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
