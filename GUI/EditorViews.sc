ConnectionStackView : CompositeView {
	var <widget;
	var parent;
	var connectionSelect, editorStack;

	*new { |widget, parent|
		^super.new.init(widget, parent);
	}

	init { |wdgt, parent|
		widget = wdgt;
		parent ?? {
			parent = Window("%: OSC connections".format(widget.name)).front
		};
		parent.layout_(
			VLayout([
				connectionSelect = PopUpMenu(),
				editorStack = StackLayout()
			])
		)l
	}

	front {
		this.front;
	}
}

OscConnectionEditorView : CompositeView {
	var <widget;
	// GUI elements
	var ipSelect, restrictToPortCheckBox;
	var deviceSelect, oscMsgSelect, newDeviceBut;
	var oscCmdTextField, oscCmdSlotNumBox;
	var inputConstraintsLoNumBox, inputConstraintsHiNumBox, zeroCrossCorrectStaticText;
	var calibrationButton, resetButton;
	var specConstraintsStaticText, inOutMappingSelect, connectionButton;
	// var more, more...

	// FIXME: an OscConnection is a single connection
	// yet, a selection of connections can only belong
	// to the widget
	// -> must be considered when adding the model to the widget's
	// model - maybe the model can't be kept with the view?
	*new { |widget, parent|
		^super.newCopyArgs(widget).init(parent.asView);
	}

	init { |parent|

	}
}

MidiConnectionEditorView : CompositeView {
	var <widget;
	// GUI elements
	var connectionSelect;

	*new { |widget, parent, bounds|
		^super.newCopyArgs(widget).init(parent.asView, bounds.asRect);
	}

	init {}
}