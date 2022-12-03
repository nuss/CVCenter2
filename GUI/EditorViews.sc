OscConnectionsEditorView : CompositeView {
	var <widget, <mc, <parent;
	// GUI elements
	var connectionSelect, addButton, removeButton;
	var ipSelect, restrictToPortCheckBox;
	var deviceSelect, oscCmdSelect, newDeviceBut;
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
		^super.new.init(widget, parent.asView);
	}

	init { |wdgt, parentView|
		widget = wdgt;
		if (parentView.isNil) {
			parent = Window("%: OSC connections".format(widget.name), Rect(0, 0, 300, 300))
		} { parent = parentView };
		// Just create a new OscConnection if none exists
		// it will automatically be added to the widget's list
		// of OscConnections within OscConnection:-init - should
		// also update the connectionSelect's items
		if (widget.oscConnections.isEmpty) {
			OscConnection(widget)
		};
		mc = widget.oscConnections;
		parent.layout_(
			VLayout(
				HLayout(
					connectionSelect = PopUpMenu(parent),
					addButton = Button(parent).states_([["+"]]),
					removeButton = Button(parent).states_([["-"]])
				),
				HLayout(
					ipSelect = PopUpMenu(parent),
					StaticText(parent).string_("restrict to port"),
					restrictToPortCheckBox = CheckBox(parent)
				),
				StaticText(parent).string_("OSC command name - either select from list provided by the selected device or set custom one"),
				HLayout(
					deviceSelect = PopUpMenu(parent),
					oscCmdSelect = PopUpMenu(parent),
					newDeviceBut = Button(parent)
				),
				HLayout(
					oscCmdTextField = TextField(parent),
					oscCmdSlotNumBox = NumberBox(parent)
				),
				StaticText(parent).string_("OSC input constraints, zero-crossing correction"),
				HLayout(
					inputConstraintsLoNumBox = NumberBox(parent),
					inputConstraintsHiNumBox = NumberBox(parent),
					zeroCrossCorrectStaticText = StaticText(parent),
					calibrationButton = Button(parent),
					resetButton = Button(parent),
				),
				specConstraintsStaticText = StaticText(parent).string_("current widget spec constraints (lo/hi): 0/0"),
				StaticText(parent).string_("input to output mapping"),
				inOutMappingSelect = PopUpMenu(parent),
				connectionButton = Button(parent)
			)
		);
	}

	front {
		parent.front;
	}
}

MidiConnectionsEditorView : CompositeView {
	var <widget;
	// GUI elements
	var connectionSelect;

	*new { |widget, parent, bounds|
		^super.newCopyArgs(widget).init(parent.asView, bounds.asRect);
	}

	init {}
}