package be.hogent.tarsos.ui.link;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import be.hogent.tarsos.ui.link.coordinatessystems.Units;
import be.hogent.tarsos.ui.link.layers.featurelayers.ConstantQLayer;
import be.hogent.tarsos.ui.link.layers.featurelayers.FFTLayer;
import be.hogent.tarsos.ui.link.layers.featurelayers.FeatureLayer;
import be.hogent.tarsos.ui.link.layers.featurelayers.PitchContourLayer;
import be.hogent.tarsos.ui.link.layers.featurelayers.WaveFormLayer;

public class AddLayerDialog extends JDialog implements ItemListener, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4659122917576332161L;

	private final String LAYER_FEATURE_FFT = "FFT";
	
	private final String LAYER_FEATURE_WAVEFORM = "WaveForm";
//	private final String LAYER_FEATURE_MFCC = "MFCC";
	private final String LAYER_FEATURE_CQT = "CQT";
	private final String LAYER_FEATURE_PITCH = "Pitch contour";
	private final String LAYER_SEGMENTATION = "Segmentation";
	
	private Integer[] frameSizes = {512, 1024, 2048, 4096, 8192, 16384, 32768, 65536};
	private Float[] overlapPercent = {0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f};
	
	private Color color;
	
	private JComboBox featureTypeList;
	private JComboBox frameSizeList;
	private JComboBox overlapList;
	private JComboBox colorList;
	
	private JButton createButton;
	private JButton cancelButton;
	
	private String featureType;
	private int frameSize;
	private float overlap;
	
	private LinkedPanel parent;

	private boolean answer = false;

	public boolean getAnswer() {
		return answer;
	}
	
	public AddLayerDialog(JFrame frame, LinkedPanel parent, boolean setModel, String myMessage) {
		super(frame, myMessage, setModel);
		this.parent = parent;
		initialise();
		
		
		this.setMinimumSize(new Dimension(300,150));
//		this.setResizable(false);
//		this.setVisible(true);
		setLocationRelativeTo(frame);
		setVisible(true);
	}
	
	public void initialise(){
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new FlowLayout());
		
		String[] featureItems = { this.LAYER_FEATURE_WAVEFORM, this.LAYER_FEATURE_CQT, this.LAYER_FEATURE_PITCH, this.LAYER_SEGMENTATION, this.LAYER_FEATURE_FFT };
		featureTypeList = new JComboBox(featureItems);
		featureTypeList.addItemListener(this);
		
		
		String[] kleuren = { "Grijs", "Rood", "Blauw", "Groen", "Geel"};
		colorList = new JComboBox(kleuren);
		
		frameSizeList = new JComboBox(frameSizes);
		frameSizeList.addItemListener(this);
		
		
		overlapList = new JComboBox(overlapPercent);
		overlapList.addItemListener(this);
		
		
		frameSizeList.setSelectedIndex(5);
		overlapList.setSelectedIndex(5);
//		featureTypeList.setSelectedItem(FEATURE_WAVEFORM);
		featureTypeList.setSelectedIndex(0);
		
		
		frameSize = frameSizes[5];
		overlap = overlapPercent[5];
		featureType = LAYER_FEATURE_WAVEFORM;
		
//		JLabel lblTypeLayer = new JLabel("Type layer: ");
		JLabel lblTypeFeature = new JLabel("Type feature: ");
		JLabel lblColor = new JLabel("Kleur: ");
		JLabel lblFrameSize = new JLabel("Framesize: ");
		JLabel lblOverlapping = new JLabel("Overlapping: ");

		createButton = new JButton("Create");
		createButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		
		contentPanel.add(lblTypeFeature);
		contentPanel.add(featureTypeList);
		
		
		contentPanel.add(lblFrameSize);
		contentPanel.add(frameSizeList);
		
		contentPanel.add(lblOverlapping);
		contentPanel.add(overlapList);
		
		contentPanel.add(createButton);
		contentPanel.add(cancelButton);

		this.getContentPane().add(contentPanel);
	}

//	@Override
	public void itemStateChanged(ItemEvent arg0) {
		if (arg0.getSource().equals(featureTypeList)) {
			featureType = arg0.getItemSelectable().getSelectedObjects()[0].toString();
			if (featureType.equals(this.LAYER_FEATURE_WAVEFORM)){
				this.frameSizeList.setEnabled(false);
				this.frameSizeList.setVisible(false);
				this.overlapList.setEnabled(false);
				this.overlapList.setVisible(false);
			} else {
				this.frameSizeList.setEnabled(true);
				this.frameSizeList.setVisible(true);
				this.overlapList.setEnabled(true);
				this.overlapList.setVisible(true);
			}
		} else if (arg0.getSource().equals(frameSizeList)) {
			frameSize = (Integer)arg0.getItemSelectable().getSelectedObjects()[0];
		} else if (arg0.getSource().equals(overlapList)){
			overlap = (Float)arg0.getItemSelectable().getSelectedObjects()[0];
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if (createButton == e.getSource()) {
			answer = true;
			this.setVisible(false);
		} else if (cancelButton == e.getSource()) {
			answer = false;
			this.setVisible(false);
		}
	}
	
	public FeatureLayer getLayer(){
		if (featureType == LAYER_FEATURE_CQT){
			return new ConstantQLayer(parent, frameSize, (int)(overlap*frameSize));
		} else if (featureType == LAYER_FEATURE_FFT){
				return new FFTLayer(parent, frameSize, (int)(overlap*frameSize));
		} else if (featureType == LAYER_FEATURE_PITCH){
			return new PitchContourLayer(parent, frameSize, (int)(overlap*frameSize));
		} else if (featureType == LAYER_FEATURE_WAVEFORM){
			return new WaveFormLayer(parent);
		} else if (featureType == LAYER_SEGMENTATION){
			
		}
		return null;
	}
	
}
