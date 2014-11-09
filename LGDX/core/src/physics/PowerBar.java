package physics;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSlider;

public class PowerBar extends JPanel {


	public PowerBar() {

	    super(true);
	    this.setLayout(new BorderLayout());
	    JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 3, 2);

	    slider.setMinorTickSpacing(2);
	    slider.setMajorTickSpacing(10);
	    slider.setPaintTicks(true);
	    slider.setPaintLabels(true);

	    // We'll just use the standard numeric labels for now...
	    slider.setLabelTable(slider.createStandardLabels(1));

	    add(slider, BorderLayout.CENTER);
	  }
}