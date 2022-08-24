OscConnectionView : CompositeView {
	var <widget;
	// GUI elements
	var connectionSelect, ipSelect, restrictToPortCheckBox;
	var deviceSelect, oscMsgSelect, newDeviceBut;
	var inOutMappingSelect;
	// var more, more...

	// FIXME: an OscConnection is a single connection
	// yet, a selection of connections can only belong
	// to the widget
	// -> must be considered when adding the model to the widget's
	// model - maybe the model can't be kept with the view?
	*new { |widget, parent, bounds|
		^super.newCopyArgs(widget).init(parent.asView, bounds.asRect);
	}

	init { |parent, bounds|

	}
}

MidiConnectionView : CompositeView {
	var <widget;
	// GUI elements
	var connectionSelect;

	*new { |widget, parent, bounds|
		^super.newCopyArgs(widget).init(parent.asView, bounds.asRect);
	}

	init {}
}