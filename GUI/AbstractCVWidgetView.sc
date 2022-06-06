AbstractCVWidgetView : SCViewHolder {

	classvar <>stringColor, <>backgroundColor;
	classvar <>tfStringColor, <>tfBackgroundColor;
	classvar <>oscStringColor, <>oscBackgroundColor;
	classvar <>midiStringColor, <>midiBackgroundColor;
	classvar <>specsStringColor, <>specsBackgroundColor;
	classvar <>actionsStringColor, <>actionsBackgroundColor;

	*initClass {
		this.stringColor = Color(0.3, 0.3, 0.3);
		this.backgroundColor = Color(0.95, 0.95, 0.95);
		this.tfStringColor = Color(0.05, 0.05, 0.05);
		this.tfBackgroundColor = Color(0.95, 0.95, 0.95);
		this.oscStringColor = Color.white;
		this.oscBackgroundColor = Color(0, 0.4, 0);
		this.midiStringColor = Color.white;
		this.midiBackgroundColor = Color(0.6, 0.0, 0.1);
		this.specsStringColor = Color.white;
		this.specsBackgroundColor = Color(0.8, 0.3);
		this.actionsStringColor = Color.white;
		this.actionsBackgroundColor = Color(0.0, 0.5, 0.5);
	}
}