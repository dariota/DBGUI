package backend;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

//Handles storage and conversion from different JComponents to a usable String for submission
public class ValueHolder {

	private String attName;
	private JComponent jco;
	
	/**
	 * Creates a ValueHolder with the given key and component
	 * @param a
	 * String key
	 * @param b
	 * JComponent containing associated value
	 */
	public ValueHolder(String a, JComponent b) {
		attName = a;
		jco = b;
	}
	
	/**
	 * Gets the string of information associated with JComponent initialised with
	 * @return
	 * String of information
	 */
	@SuppressWarnings("unchecked")
	public AttributeValue attribute() {
		AttributeValue contents = new AttributeValue();
		String value = null;
		if (jco instanceof JTextField) {
			value = ((JTextField)jco).getText();
		} else if (jco instanceof JComboBox){
			value = (String)(((JComboBox<String>)jco).getSelectedItem());
		} else if (jco instanceof JCheckBox) {
			value = Boolean.toString(((JCheckBox)jco).isSelected());
		} else if (jco instanceof JTextArea) {
			value = ((JTextArea)jco).getText();
		}
		contents.setS(value);
		return contents;
	}
	
	/**
	 * Returns the key to be used to submit the information associated to the DB 
	 * @return
	 * String key
	 */
	public String key() {
		return attName;
	}
	
	/**
	 * Sets the value of the component associated to the given value under the best interpretation of it possible
	 * @param s
	 * Value to set the component to
	 */
	@SuppressWarnings("unchecked")
	public void setValue(String s) {
		if (jco instanceof JTextField) {
			((JTextField)jco).setText(s);
		} else if (jco instanceof JComboBox){
			((JComboBox<String>)jco).setSelectedItem(s);
		} else if (jco instanceof JCheckBox) {
			((JCheckBox)jco).setSelected(Boolean.parseBoolean(s));
		} else if (jco instanceof JTextArea) {
			((JTextArea)jco).setText(s);
		}
	}

}
