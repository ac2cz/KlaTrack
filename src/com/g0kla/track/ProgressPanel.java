package com.g0kla.track;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * 
 * @author g0kla@arrl.net
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
@SuppressWarnings("serial")
public class ProgressPanel extends JDialog implements ActionListener {

	String title;
	
	public ProgressPanel(JFrame owner, String message, boolean modal) {
		super(owner, modal);
		title = message;
		setTitle(message);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		int x = 100;
		int y = 100;
		if (owner != null) {
			x = owner.getX() + owner.getWidth()/2 - (message.length()*9)/2;
			y = owner.getY() + owner.getHeight()/2;
		} else {
			Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
			x = (int) ((dimension.getWidth() - this.getWidth()) / 2);
			y = (int) ((dimension.getHeight() - this.getHeight()) / 2);
		}
		setBounds(100, 100, message.length()*12, 10);
	
		    this.setLocation(x, y);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void updateProgress(int p) {
		if (p == 100) this.dispose();
		setTitle(title + " (" + p + "%)");
	}

}
