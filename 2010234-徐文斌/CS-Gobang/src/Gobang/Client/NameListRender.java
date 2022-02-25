package Gobang.Client;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.HashMap;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
public class NameListRender extends DefaultListCellRenderer {
 
	private static final long serialVersionUID = 1L;
	private Font font1;
    private Font font2;
    private HashMap<String, String> peopleState;
    public NameListRender() {
        this.font1 = getFont();
        this.font2 = new Font("·ÂËÎ", Font.BOLD, 18);
        setOpaque(true);
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
    }
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    	peopleState = View1.getInstance().getPlayerStatue();
        String name = (String)value;
        if (name.equals(Controller.getInstance().getYourName()))
        	setBackground(Color.gray);
        else if (peopleState.get(name).equals("free")) {
        	if (!isSelected)
        		setBackground(Color.green);
        	else {
        		setBackground(new Color(135, 206, 234));
            	setFont(font2);
        	}
        }else if (peopleState.get(name).equals("busy")) {
        	setBackground(Color.red);
        }
        return this;
    }
}