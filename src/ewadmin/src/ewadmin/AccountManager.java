package ewadmin;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ListSelectionModel;
import javax.swing.JRadioButton;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.MenuKeyEvent;

/**
 * @author Nuno Marques
 *
 */
public class AccountManager {

	private JFrame frmAccoutsManager;
	private JTextField txtSearchtext;
	private JTable table;
	private DefaultTableModel tableModel;
	private JRadioButton rdbtnByName, rdbtnByUser;
	private String athtoken;
	private String lastSearch;

	/**
	 * Create the application.
	 */
	public AccountManager(String token) {
		lastSearch="";
		setAthtoken(token);
		setFrame(new JFrame());
		getFrame().setBounds(100, 100, 500, 345);
		getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frmAccoutsManager.setJMenuBar(menuBar);
		
		JMenu mnAccoutsManager = new JMenu("Account");
		mnAccoutsManager.addMenuKeyListener(new MenuKeyListener() {
			public void menuKeyPressed(MenuKeyEvent arg0) {
				int keyReceved = arg0.getKeyCode();
				//System.out.println(keyReceved);
				if (keyReceved==78){
					newAccount();
				}
				else if(keyReceved==80){
					newPassAccount();
				}
				else if(keyReceved==68){
					deleteAccount();
				}
			}
			public void menuKeyReleased(MenuKeyEvent arg0) {
			}
			public void menuKeyTyped(MenuKeyEvent arg0) {
			}
		});
		mnAccoutsManager.setMnemonic('A');
		menuBar.add(mnAccoutsManager);
		
		final JMenuItem mntmNew = new JMenuItem("New");
		mntmNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0));
		mntmNew.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg) {
				newAccount();
			}
		});
		//mntmNew.setMnemonic('N');
		mnAccoutsManager.add(mntmNew);
		
		JMenuItem mntmNewPassword = new JMenuItem("New Password");
		mntmNewPassword.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0));
		mntmNewPassword.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				newPassAccount();
			}
		});
		//mntmNewPassword.setMnemonic('P');
		mnAccoutsManager.add(mntmNewPassword);
		
		JMenuItem mntmDelete = new JMenuItem("Delete");
		mntmDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0));
		mntmDelete.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				deleteAccount();
			}
		});
		//mntmDelete.setMnemonic('D');
		mnAccoutsManager.add(mntmDelete);
		frmAccoutsManager.getContentPane().setLayout(null);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBounds(0, 242, 492, 48);
		frmAccoutsManager.getContentPane().add(buttonPanel);
		buttonPanel.setLayout(null);
		
		txtSearchtext = new JTextField();
		txtSearchtext.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				if (arg0.getKeyCode()==10){
					searchAccounts(txtSearchtext.getText());
				}
			}
		});
		txtSearchtext.setBounds(10, 15, 280, 20);
		txtSearchtext.setColumns(10);
		buttonPanel.add(txtSearchtext);
		
		JButton button = new JButton("Search");
		button.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				int keyReceved = arg0.getKeyCode();
				if (keyReceved==10 || keyReceved==32 || keyReceved==83){
					searchAccounts(txtSearchtext.getText());
				}
			}
		});
		button.setMnemonic('S');
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				searchAccounts(txtSearchtext.getText());
			}
		});
		button.setBounds(300, 14, 98, 23);
		buttonPanel.add(button);
		
		rdbtnByName = new JRadioButton("by name");
		rdbtnByName.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				int keyReceved = arg0.getKeyCode();
				if (keyReceved==10){
					rdbtnByName.setSelected(true);
					rdbtnByUser.setSelected(false);
				}
				else if(keyReceved==32){
					if(!rdbtnByName.isSelected())
						rdbtnByUser.setSelected(rdbtnByName.isSelected());
					rdbtnByName.setSelected(rdbtnByUser.isSelected());
				}
			}
		});
		rdbtnByName.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				rdbtnByName.setSelected(true);
				rdbtnByUser.setSelected(false);
			}
		});
		rdbtnByName.setFont(new Font("Tahoma", Font.PLAIN, 10));
		rdbtnByName.setBounds(404, 7, 82, 16);
		buttonPanel.add(rdbtnByName);
		
		rdbtnByUser = new JRadioButton("by user");
		rdbtnByUser.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				int keyReceved = arg0.getKeyCode();
				if (keyReceved==10){
					rdbtnByName.setSelected(false);
					rdbtnByUser.setSelected(true);
				}
				else if(keyReceved==32){
					if(!rdbtnByUser.isSelected())
						rdbtnByName.setSelected(rdbtnByUser.isSelected());
					rdbtnByUser.setSelected(rdbtnByName.isSelected());
				}
			}
		});
		rdbtnByUser.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				rdbtnByName.setSelected(false);
				rdbtnByUser.setSelected(true);
			}
		});
		rdbtnByUser.setSelected(true);
		rdbtnByUser.setFont(new Font("Tahoma", Font.PLAIN, 10));
		rdbtnByUser.setBounds(404, 26, 82, 15);
		buttonPanel.add(rdbtnByUser);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 472, 231);
		frmAccoutsManager.getContentPane().add(scrollPane);
		
		table = new JTable();
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				String selectedRow="";
				if(table.getSelectedRow()>=0){
					if(rdbtnByName.isSelected())
						selectedRow = (String) table.getValueAt(table.getSelectedRow(), 1);
					if(rdbtnByUser.isSelected())
						selectedRow = (String) table.getValueAt(table.getSelectedRow(), 0);
					//System.out.println("Posicao da linha" + table.getSelectedRow());
					txtSearchtext.setText(selectedRow);
				}
			}
		});
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		try {
			tableModel = new DefaultTableModel(
					Comunication.listAccounts("","by_username"),
					new String[] {"User", "Name", "Type"});
			table.setModel(tableModel);
		} catch (Exception e) {
			String exceptionReturn=e.getMessage();
			if(exceptionReturn!=null && exceptionReturn.equalsIgnoreCase("204"))
				setError("No Content on list.", exceptionReturn);
			else if(exceptionReturn!=null && exceptionReturn.equalsIgnoreCase("400"))
				setError("Bad Request.", exceptionReturn);
			else 
				setError("Listing accounts error.", "");
		}
		table.setShowVerticalLines(false);
		table.setFillsViewportHeight(true);
		scrollPane.setColumnHeaderView(table);
		scrollPane.setViewportView(table);
	}

	private void newAccount(){
		String newuser=setNewAccount();
		if(newuser!=null){
			try {
				String[] newAccount = Comunication.newAccount(newuser);
				displayNewAccount(newAccount[0],newAccount[1]);
				searchAccounts(lastSearch);
			} catch (Exception e) {
				String exceptionReturn=e.getMessage();
				if(exceptionReturn!=null && exceptionReturn.equalsIgnoreCase("409"))
					setError("Conflict. Username already exists.", exceptionReturn);
				else if(exceptionReturn!=null && exceptionReturn.equalsIgnoreCase("403"))
					setError("Access forbidden.", exceptionReturn);
				else 
					setError("New accounts error.", "");
			}
		}
	}
	
	private void deleteAccount(){
		if(table.getSelectedRow()>=0){
			String deleteUser=(String) table.getValueAt(table.getSelectedRow(), 0);
			System.out.println("Account selected : " + deleteUser);
			
			try {
				String deleteAccount = Comunication.deletAccount(deleteUser);
				if(deleteAccount.equalsIgnoreCase("200")){
					System.out.println("Account deleted : " + deleteUser);
					tableModel.removeRow(table.getSelectedRow());
				}
				else
					setError("Delete account error.", "");
			} catch (Exception e) {
				String exceptionReturn=e.getMessage();
				if(exceptionReturn!=null && exceptionReturn.equalsIgnoreCase("409"))
					setError("Conflict. Username already exists.", exceptionReturn);
				else if(exceptionReturn!=null && exceptionReturn.equalsIgnoreCase("403"))
					setError("Access forbidden.", exceptionReturn);
				else if(exceptionReturn!=null && exceptionReturn.equalsIgnoreCase("400"))
					setError("Bad Request.", exceptionReturn);
				else if(exceptionReturn!=null && exceptionReturn.equalsIgnoreCase("404"))
					setError("Not Found.", exceptionReturn);
				else if(exceptionReturn!=null && exceptionReturn.equalsIgnoreCase("500"))
					setError("Internal Server Error.", exceptionReturn);
				else 
					setError("Delete account error.", "");
			}
		}
		else
			setError("Select one account on the table.", "");
	}
	
	private void newPassAccount(){
		if(table.getSelectedRow()>=0){
			String reseteUser=(String) table.getValueAt(table.getSelectedRow(), 0);
			System.out.println("Account selected : " + reseteUser);
			
			try {
				String[] reseteAccount = Comunication.resetPass(reseteUser);
				displayNewPass(reseteAccount[0],reseteAccount[1]);
			} catch (Exception e) {
				String exceptionReturn=e.getMessage();
				if(exceptionReturn!=null && exceptionReturn.equalsIgnoreCase("409"))
					setError("Conflict. Username already exists.", exceptionReturn);
				else if(exceptionReturn!=null && exceptionReturn.equalsIgnoreCase("403"))
					setError("Access forbidden.", exceptionReturn);
				else if(exceptionReturn!=null && exceptionReturn.equalsIgnoreCase("400"))
					setError("Bad Request.", exceptionReturn);
				else if(exceptionReturn!=null && exceptionReturn.equalsIgnoreCase("404"))
					setError("Not Found.", exceptionReturn);
				else if(exceptionReturn!=null && exceptionReturn.equalsIgnoreCase("500"))
					setError("Internal Server Error.", exceptionReturn);
				else 
					setError("Resete password error.", "");
			}
		}
		else
			setError("Select one account on the table.", "");
	}
	
	private void searchAccounts(String searchText){
		try {
			if(rdbtnByName.isSelected()){
				tableModel= new DefaultTableModel(
						Comunication.listAccounts(searchText,"by_name"),
						new String[] {"User", "Name", "Type"});
				table.setModel(tableModel);
			}
			if(rdbtnByUser.isSelected()){
				tableModel= new DefaultTableModel(
						Comunication.listAccounts(searchText,"by_username"),
						new String[] {"User", "Name", "Type"});
				table.setModel(tableModel);
			}
			lastSearch=txtSearchtext.getText();
		} catch (Exception e) {
			String exceptionReturn=e.getMessage();
			if(exceptionReturn!=null && exceptionReturn.equalsIgnoreCase("204"))
				setError("No Content on list.", exceptionReturn);
			else if(exceptionReturn!=null && exceptionReturn.equalsIgnoreCase("400"))
				setError("Bad Request.", exceptionReturn);
			else 
				setError("Listing accounts error.", "");
		}
	}
	
	/**
	 * @param frame the frame to set
	 */
	public void setFrame(JFrame frame) {
		this.frmAccoutsManager = frame;
		frmAccoutsManager.setResizable(false);
		frmAccoutsManager.setTitle("Account Manager");
	}

	/**
	 * @return the frame
	 */
	public JFrame getFrame() {
		return frmAccoutsManager;
	}
	
	private String setNewAccount() {
		JFrame frame = null;
		String response = (String)JOptionPane.showInputDialog(
                frame, "New User: ", "New Account",
                JOptionPane.PLAIN_MESSAGE);
		return response;
	}
	
	private void displayNewAccount(String user, String password) {
		JFrame frame = null;
		JOptionPane.showMessageDialog(
				frame, "User: " + user + "\nPassword: " + password,
				"New Account", JOptionPane.PLAIN_MESSAGE);
	}

	private void displayNewPass(String user, String password) {
		JFrame frame = null;
		JOptionPane.showMessageDialog(
				frame, "User: " + user + "\nNew password: " + password,
				"New Password", JOptionPane.PLAIN_MESSAGE);
	}

	private void setError(String message, String errorCode) {
		JFrame frame = null;
		JOptionPane.showMessageDialog(
			frame, message,
		    "Error " + errorCode,
		    JOptionPane.ERROR_MESSAGE);
	}

	public void setAthtoken(String athtoken) {
		this.athtoken = athtoken;
	}

	public String getAthtoken() {
		return athtoken;
	}
}
