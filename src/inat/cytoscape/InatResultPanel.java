package inat.cytoscape;

import inat.analyser.LevelResult;
import inat.graph.Graph;
import inat.model.Model;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;

/**
 * The Inat result panel.
 * 
 * @author Brend Wanders
 */
public class InatResultPanel extends JPanel implements ChangeListener {

	private static final long serialVersionUID = -163756255393221954L;
	private final Model model; //The model from which the results were obtained
	private final LevelResult result; //Contains the results to be shown in this panel
	private JSlider slider; //The slider to allow the user to choose a moment in the simulation time, which will be reflected on the network window as node colors, indicating the corresponding reactant activity level.

	/**
	 * The panel constructor.
	 * 
	 * @param model the model this panel uses
	 * @param result the results object this panel uses
	 */
	public InatResultPanel(Model model, LevelResult result, double scale) {
		super(new BorderLayout(), true);
		this.model = model;
		this.result = result;

		JPanel sliderPanel = new JPanel(new BorderLayout());
		this.slider = new JSlider();
		this.slider.setOrientation(JSlider.HORIZONTAL);
		this.slider.setMinimum(0);
		this.slider.setMaximum((int)(double)(result.getTimeIndices().get(result.getTimeIndices().size() - 1)));
		this.slider.setValue(0);
		this.slider.getModel().addChangeListener(this);

		sliderPanel.add(this.slider, BorderLayout.CENTER);

		this.add(sliderPanel, BorderLayout.SOUTH);

		Graph g = new Graph();
		//We map reactant IDs to their corresponding aliases (canonical names, i.e., the names displayed to the user in the network window), so that
		//we will be able to use graph series names consistent with what the user has chosen.
		Map<String, String> seriesNameMapping = new HashMap<String, String>();
		for (String r : result.getReactantIds()) {
			String name = null;
			if (model.getReactant(r) != null) { //we can also refer to a name not present in the reactant collection
				name = model.getReactant(r).get("alias").as(String.class); //if an alias is set, we prefer it
				if (name == null) {
					name = model.getReactant(r).get("name").as(String.class);
				}
			} else if (r.contains("_StdDev")) {
				if (model.getReactant(r.substring(0, r.indexOf("_"))).get("alias").as(String.class) != null) {
					name = model.getReactant(r.substring(0, r.indexOf("_"))).get("alias").as(String.class) + "_StdDev";
				} else {
					name = r; //in this case, I simply don't know what we are talking about =)
				}
			}
			seriesNameMapping.put(r, name);
		}
		g.parseLevelResult(result, seriesNameMapping, scale); //Add all series to the graph, using the mapping we built here to "translate" the names into the user-defined ones.
		g.setXSeriesName("Time (min)");

		if (!model.getProperties().get("levels").isNull()) { //if we find a maximum value for activity levels, we declare it to the graph, so that other added graphs (such as experimental data) will be automatically rescaled to match us
			int nLevels = model.getProperties().get("levels").as(Integer.class);
			g.declareMaxYValue(nLevels);
			double maxTime = scale * result.getTimeIndices().get(result.getTimeIndices().size()-1);
			g.setDrawArea(0, (int)maxTime, 0, nLevels); //This is done because the graph automatically computes the area to be shown based on minimum and maximum values for X and Y, including StdDev. So, if the StdDev of a particular series (which represents an average) in a particular point is larger that the value of that series in that point, the minimum y value would be negative. As this is not very nice to see, I decided that we will recenter the graph to more strict bounds instead.
		}
		this.add(g, BorderLayout.CENTER);
	}

	/**
	 * When the user moves the time slider, we update the activity ratio (SHOWN_LEVEL) of
	 * all nodes in the network window, so that, thanks to the continuous Visual Mapping
	 * defined when the interface is augmented (see AugmentAction), different colors will
	 * show different activity levels. 
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		final int t = this.slider.getValue();
		CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();
		final int levels = this.model.getProperties().get("levels").as(Integer.class); //at this point, all levels have already been rescaled to the maximum (= the number of levels of the model), so we use it as a reference for the number of levels to show on the network nodes 
		for (String r : this.result.getReactantIds()) {
			if (this.model.getReactant(r) == null) continue;
			final String id = this.model.getReactant(r).get("name").as(String.class);
			final double level = this.result.getConcentration(r, t);
			nodeAttributes.setAttribute(id, "activityRatio", level / levels);
		}

		Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);

		Cytoscape.getCurrentNetworkView().updateView();
	}
}
