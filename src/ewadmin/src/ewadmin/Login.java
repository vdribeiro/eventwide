package ewadmin;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * @author Nuno Marques
 *
 */
public class Login extends JDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField txtUsertext;
	private JPasswordField txtPasstext;
	private JTextField txtServertext;

	/**
	 * Create the dialog.
	 */
	public Login() throws Exception {
		
		String[] readLogin= readLastLogin();
		String user=readLogin[0];
		String pass=readLogin[1];
		String ipserver=readLogin[2];
		
		setResizable(false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("login");
		setBounds(100, 100, 330, 200);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		txtUsertext = new JTextField();
		txtUsertext.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
						if (arg0.getKeyCode()==10){
							startLogin();
						}
			}
		});
		txtUsertext.setText(user);
		txtUsertext.setBounds(93, 26, 219, 20);
		contentPanel.add(txtUsertext);
		txtUsertext.setColumns(10);
		
		txtPasstext = new JPasswordField();
		txtPasstext.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				if (arg0.getKeyCode()==10){
					startLogin();
				}
			}
		});
		txtPasstext.setEchoChar('*');
		txtPasstext.setText(pass);
		txtPasstext.setBounds(93, 57, 219, 20);
		contentPanel.add(txtPasstext);
		txtPasstext.setColumns(10);
		
		JLabel lblUser = new JLabel("User:");
		lblUser.setHorizontalAlignment(SwingConstants.RIGHT);
		lblUser.setBounds(15, 29, 68, 14);
		contentPanel.add(lblUser);
		
		JLabel lblPassword = new JLabel("PassWord:");
		lblPassword.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPassword.setBounds(15, 60, 68, 14);
		contentPanel.add(lblPassword);
		
		JLabel lblServer = new JLabel("Server:");
		lblServer.setHorizontalAlignment(SwingConstants.RIGHT);
		lblServer.setBounds(15, 92, 68, 14);
		contentPanel.add(lblServer);
		
		txtServertext = new JTextField();
		txtServertext.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				if (arg0.getKeyCode()==10){
					startLogin();
				}
			}
		});
		txtServertext.setText(ipserver);
		txtServertext.setBounds(93, 89, 219, 20);
		contentPanel.add(txtServertext);
		txtServertext.setColumns(10);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent arg0) {
						int keyReceved = arg0.getKeyCode();
						if (keyReceved==10 || keyReceved==32){
							startLogin();
						}
					}
				});
				okButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent arg0) {
						startLogin();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent arg0) {
						int keyReceved = arg0.getKeyCode();
						if (keyReceved==10 || keyReceved==32){
							setVisible(false);
							dispose();
						}
					}
				});
				cancelButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent arg0) {
						setVisible(false);
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	private void startLogin(){
		String loginReturn;
		try {
			loginReturn = Comunication.Login(txtUsertext.getText(), txtPasstext.getText(), txtServertext.getText());
			if(txtUsertext.getText().length()>0 && txtPasstext.getText().length()>0 && txtServertext.getText().length()>0){
				if(loginReturn.equalsIgnoreCase("200")){
					startAccountManager();
					saveLastLogin(new String[]{txtUsertext.getText(),txtPasstext.getText(),txtServertext.getText()});
					setVisible(false);
					dispose();
				}
			}
			else
				setError("Text Field error.", "");
		} catch (Exception e) {
			loginReturn=e.getMessage();
			if(loginReturn!=null && loginReturn.equalsIgnoreCase("403"))
				setError("Access Forbidden.", loginReturn);
			else if(loginReturn!=null && loginReturn.equalsIgnoreCase("500"))
				setError("Internal Server Error.", loginReturn);
			else 
				setError("Comunication error.", "");
		}
	}
	
	
	private void startAccountManager(){
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				AccountManager accontManagerWindow = new AccountManager(Comunication.getLogintoken());
				accontManagerWindow.getFrame().setLocationRelativeTo(null);
				accontManagerWindow.getFrame().setVisible(true);
			}
		});
	}

	private void setError(String message, String errorCode) {
		JFrame frame = null;
		JOptionPane.showMessageDialog(
			frame, message,
		    "Error " + errorCode,
		    JOptionPane.ERROR_MESSAGE);
	}
	
	private boolean saveLastLogin(String[] login){
		// Stream to write file
		FileOutputStream saveFile;		
		try
		{
		    // Open an output stream
			saveFile = new FileOutputStream ("lastlogin.txt");

		    // Print a lines of text
			PrintStream printStream = new PrintStream(saveFile);
			printStream.println(login[0]);
			printStream.println(login[1]);
			printStream.println(login[2]);

		    // Close our output stream
		    saveFile.close();		
		}
		// Catches any error conditions
		catch (Exception e)
		{
			return false;
		}
		return true;
	}
	
	private String[] readLastLogin( ){
		String[]login = new String[]{"","","http://localhost:8080"};

		// Stream to read file
		FileReader readFile;		
		try
		{
		    // Open an input stream
			readFile = new FileReader("lastlogin.txt");

		    // Read a line of text
		    BufferedReader in = new BufferedReader(readFile);
		    int i=0;
		    while(i<3) {
		    	login[i]=in.readLine();
		    	i++;
		    }

		    // Close our input stream
		    readFile.close();		
		}
		// Catches any error conditions
		catch (IOException e)
		{
			return new String[]{"","","http://localhost:8080"};
		}
		return login;
	}
}
